/*
 * Decompiled with CFR 0_129.
 */
package io.netty.channel.epoll;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SingleThreadEventLoop;
import io.netty.channel.epoll.AbstractEpollChannel;
import io.netty.channel.epoll.EpollEventArray;
import io.netty.channel.epoll.Native;
import io.netty.channel.unix.FileDescriptor;
import io.netty.util.collection.IntObjectHashMap;
import io.netty.util.collection.IntObjectMap;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

final class EpollEventLoop
extends SingleThreadEventLoop {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(EpollEventLoop.class);
    private static final AtomicIntegerFieldUpdater<EpollEventLoop> WAKEN_UP_UPDATER;
    private final int epollFd;
    private final int eventFd;
    private final IntObjectMap<AbstractEpollChannel> channels;
    private final boolean allowGrowing;
    private final EpollEventArray events;
    private volatile int wakenUp;
    private volatile int ioRatio;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    EpollEventLoop(EventLoopGroup parent, ThreadFactory threadFactory, int maxEvents) {
        super(parent, threadFactory, false);
        this.channels = new IntObjectHashMap<AbstractEpollChannel>(4096);
        this.ioRatio = 50;
        if (maxEvents == 0) {
            this.allowGrowing = true;
            this.events = new EpollEventArray(4096);
        } else {
            this.allowGrowing = false;
            this.events = new EpollEventArray(maxEvents);
        }
        boolean success = false;
        int epollFd = -1;
        int eventFd = -1;
        try {
            this.epollFd = epollFd = Native.epollCreate();
            this.eventFd = eventFd = Native.eventFd();
            try {
                Native.epollCtlAdd(epollFd, eventFd, Native.EPOLLIN);
            }
            catch (IOException e) {
                throw new IllegalStateException("Unable to add eventFd filedescriptor to epoll", e);
            }
            success = true;
        }
        finally {
            if (!success) {
                if (epollFd != -1) {
                    try {
                        Native.close(epollFd);
                    }
                    catch (Exception e) {}
                }
                if (eventFd != -1) {
                    try {
                        Native.close(eventFd);
                    }
                    catch (Exception e) {}
                }
            }
        }
    }

    @Override
    protected void wakeup(boolean inEventLoop) {
        if (!inEventLoop && WAKEN_UP_UPDATER.compareAndSet(this, 0, 1)) {
            Native.eventFdWrite(this.eventFd, 1L);
        }
    }

    void add(AbstractEpollChannel ch) throws IOException {
        assert (this.inEventLoop());
        int fd = ch.fd().intValue();
        Native.epollCtlAdd(this.epollFd, fd, ch.flags);
        this.channels.put(fd, ch);
    }

    void modify(AbstractEpollChannel ch) throws IOException {
        assert (this.inEventLoop());
        Native.epollCtlMod(this.epollFd, ch.fd().intValue(), ch.flags);
    }

    void remove(AbstractEpollChannel ch) throws IOException {
        int fd;
        assert (this.inEventLoop());
        if (ch.isOpen() && this.channels.remove(fd = ch.fd().intValue()) != null) {
            Native.epollCtlDel(this.epollFd, ch.fd().intValue());
        }
    }

    @Override
    protected Queue<Runnable> newTaskQueue() {
        return PlatformDependent.newMpscQueue();
    }

    public int getIoRatio() {
        return this.ioRatio;
    }

    public void setIoRatio(int ioRatio) {
        if (ioRatio <= 0 || ioRatio > 100) {
            throw new IllegalArgumentException("ioRatio: " + ioRatio + " (expected: 0 < ioRatio <= 100)");
        }
        this.ioRatio = ioRatio;
    }

    private int epollWait(boolean oldWakenUp) throws IOException {
        int selectCnt = 0;
        long currentTimeNanos = System.nanoTime();
        long selectDeadLineNanos = currentTimeNanos + this.delayNanos(currentTimeNanos);
        do {
            long timeoutMillis;
            if ((timeoutMillis = (selectDeadLineNanos - currentTimeNanos + 500000L) / 1000000L) <= 0L) {
                int ready;
                if (selectCnt != 0 || (ready = Native.epollWait(this.epollFd, this.events, 0)) <= 0) break;
                return ready;
            }
            int selectedKeys = Native.epollWait(this.epollFd, this.events, (int)timeoutMillis);
            ++selectCnt;
            if (selectedKeys != 0 || oldWakenUp || this.wakenUp == 1 || this.hasTasks() || this.hasScheduledTasks()) {
                return selectedKeys;
            }
            currentTimeNanos = System.nanoTime();
        } while (true);
        return 0;
    }

    @Override
    protected void run() {
        do {
            boolean oldWakenUp = WAKEN_UP_UPDATER.getAndSet(this, 0) == 1;
            try {
                int ready;
                if (this.hasTasks()) {
                    ready = Native.epollWait(this.epollFd, this.events, 0);
                } else {
                    ready = this.epollWait(oldWakenUp);
                    if (this.wakenUp == 1) {
                        Native.eventFdWrite(this.eventFd, 1L);
                    }
                }
                int ioRatio = this.ioRatio;
                if (ioRatio == 100) {
                    if (ready > 0) {
                        this.processReady(this.events, ready);
                    }
                    this.runAllTasks();
                } else {
                    long ioStartTime = System.nanoTime();
                    if (ready > 0) {
                        this.processReady(this.events, ready);
                    }
                    long ioTime = System.nanoTime() - ioStartTime;
                    this.runAllTasks(ioTime * (long)(100 - ioRatio) / (long)ioRatio);
                }
                if (this.allowGrowing && ready == this.events.length()) {
                    this.events.increase();
                }
                if (!this.isShuttingDown()) continue;
                this.closeAll();
                if (!this.confirmShutdown()) continue;
            }
            catch (Throwable t) {
                logger.warn("Unexpected exception in the selector loop.", t);
                try {
                    Thread.sleep(1000L);
                }
                catch (InterruptedException ioRatio) {}
                continue;
            }
            break;
        } while (true);
    }

    private void closeAll() {
        try {
            Native.epollWait(this.epollFd, this.events, 0);
        }
        catch (IOException iOException) {
            // empty catch block
        }
        ArrayList<AbstractEpollChannel> array = new ArrayList<AbstractEpollChannel>(this.channels.size());
        for (IntObjectMap.Entry<AbstractEpollChannel> entry : this.channels.entries()) {
            array.add(entry.value());
        }
        for (AbstractEpollChannel ch : array) {
            ch.unsafe().close(ch.unsafe().voidPromise());
        }
    }

    private void processReady(EpollEventArray events, int ready) {
        for (int i = 0; i < ready; ++i) {
            int fd = events.fd(i);
            if (fd == this.eventFd) {
                Native.eventFdRead(this.eventFd);
                continue;
            }
            long ev = events.events(i);
            AbstractEpollChannel ch = this.channels.get(fd);
            if (ch != null) {
                boolean err;
                AbstractEpollChannel.AbstractEpollUnsafe unsafe = (AbstractEpollChannel.AbstractEpollUnsafe)ch.unsafe();
                boolean bl = err = (ev & (long)Native.EPOLLERR) != 0L;
                if (err || (ev & (long)Native.EPOLLOUT) != 0L && ch.isOpen()) {
                    unsafe.epollOutReady();
                }
                if ((ev & (long)Native.EPOLLRDHUP) != 0L) {
                    unsafe.epollRdHupReady();
                }
                if (!err && (ev & (long)Native.EPOLLIN) == 0L || !ch.isOpen()) continue;
                unsafe.epollInReady();
                continue;
            }
            try {
                Native.epollCtlDel(this.epollFd, fd);
                continue;
            }
            catch (IOException unsafe) {
                // empty catch block
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void cleanup() {
        try {
            try {
                Native.close(this.epollFd);
            }
            catch (IOException e) {
                logger.warn("Failed to close the epoll fd.", e);
            }
            try {
                Native.close(this.eventFd);
            }
            catch (IOException e) {
                logger.warn("Failed to close the event fd.", e);
            }
        }
        finally {
            this.events.free();
        }
    }

    static {
        AtomicIntegerFieldUpdater<Object> updater = PlatformDependent.newAtomicIntegerFieldUpdater(EpollEventLoop.class, "wakenUp");
        if (updater == null) {
            updater = AtomicIntegerFieldUpdater.newUpdater(EpollEventLoop.class, "wakenUp");
        }
        WAKEN_UP_UPDATER = updater;
    }
}

