/*
 * Decompiled with CFR 0_129.
 */
package io.netty.channel.epoll;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;
import io.netty.channel.AbstractChannel;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelMetadata;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelOutboundBuffer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.channel.ConnectTimeoutException;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.EventLoop;
import io.netty.channel.RecvByteBufAllocator;
import io.netty.channel.epoll.AbstractEpollChannel;
import io.netty.channel.epoll.EpollChannelConfig;
import io.netty.channel.epoll.EpollMode;
import io.netty.channel.epoll.IovArray;
import io.netty.channel.epoll.IovArrayThreadLocal;
import io.netty.channel.epoll.Native;
import io.netty.channel.socket.ChannelInputShutdownEvent;
import io.netty.channel.unix.FileDescriptor;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.internal.EmptyArrays;
import io.netty.util.internal.MpscLinkedQueueNode;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.OneTimeTask;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.StringUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.util.Queue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public abstract class AbstractEpollStreamChannel
extends AbstractEpollChannel {
    private static final String EXPECTED_TYPES = " (expected: " + StringUtil.simpleClassName(ByteBuf.class) + ", " + StringUtil.simpleClassName(DefaultFileRegion.class) + ')';
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(AbstractEpollStreamChannel.class);
    static final ClosedChannelException CLOSED_CHANNEL_EXCEPTION = new ClosedChannelException();
    private ChannelPromise connectPromise;
    private ScheduledFuture<?> connectTimeoutFuture;
    private SocketAddress requestedRemoteAddress;
    private final Queue<SpliceInTask> spliceQueue = PlatformDependent.newMpscQueue();
    private volatile boolean inputShutdown;
    private volatile boolean outputShutdown;
    private int pipeIn = -1;
    private int pipeOut = -1;

    protected AbstractEpollStreamChannel(Channel parent, int fd) {
        super(parent, fd, Native.EPOLLIN, true);
        this.flags |= Native.EPOLLRDHUP;
    }

    protected AbstractEpollStreamChannel(int fd) {
        super(fd, Native.EPOLLIN);
        this.flags |= Native.EPOLLRDHUP;
    }

    protected AbstractEpollStreamChannel(FileDescriptor fd) {
        super(null, fd, Native.EPOLLIN, Native.getSoError(fd.intValue()) == 0);
        this.flags |= Native.EPOLLRDHUP;
    }

    @Override
    protected AbstractEpollChannel.AbstractEpollUnsafe newUnsafe() {
        return new EpollStreamUnsafe();
    }

    public final ChannelFuture spliceTo(AbstractEpollStreamChannel ch, int len) {
        return this.spliceTo(ch, len, this.newPromise());
    }

    public final ChannelFuture spliceTo(AbstractEpollStreamChannel ch, int len, ChannelPromise promise) {
        if (ch.eventLoop() != this.eventLoop()) {
            throw new IllegalArgumentException("EventLoops are not the same.");
        }
        if (len < 0) {
            throw new IllegalArgumentException("len: " + len + " (expected: >= 0)");
        }
        if (ch.config().getEpollMode() != EpollMode.LEVEL_TRIGGERED || this.config().getEpollMode() != EpollMode.LEVEL_TRIGGERED) {
            throw new IllegalStateException("spliceTo() supported only when using " + (Object)((Object)EpollMode.LEVEL_TRIGGERED));
        }
        ObjectUtil.checkNotNull(promise, "promise");
        if (!this.isOpen()) {
            promise.tryFailure(CLOSED_CHANNEL_EXCEPTION);
        } else {
            SpliceInChannelTask task = new SpliceInChannelTask(ch, len, ObjectUtil.checkNotNull(promise, "promise"));
            this.spliceQueue.add(task);
            this.failSpliceIfClosed(promise);
        }
        return promise;
    }

    public final ChannelFuture spliceTo(FileDescriptor ch, int offset, int len) {
        return this.spliceTo(ch, offset, len, this.newPromise());
    }

    public final ChannelFuture spliceTo(FileDescriptor ch, int offset, int len, ChannelPromise promise) {
        if (len < 0) {
            throw new IllegalArgumentException("len: " + len + " (expected: >= 0)");
        }
        if (offset < 0) {
            throw new IllegalArgumentException("offset must be >= 0 but was " + offset);
        }
        if (this.config().getEpollMode() != EpollMode.LEVEL_TRIGGERED) {
            throw new IllegalStateException("spliceTo() supported only when using " + (Object)((Object)EpollMode.LEVEL_TRIGGERED));
        }
        ObjectUtil.checkNotNull(promise, "promise");
        if (!this.isOpen()) {
            promise.tryFailure(CLOSED_CHANNEL_EXCEPTION);
        } else {
            SpliceFdTask task = new SpliceFdTask(ch, offset, len, ObjectUtil.checkNotNull(promise, "promise"));
            this.spliceQueue.add(task);
            this.failSpliceIfClosed(promise);
        }
        return promise;
    }

    private void failSpliceIfClosed(ChannelPromise promise) {
        if (!this.isOpen() && promise.tryFailure(CLOSED_CHANNEL_EXCEPTION)) {
            this.eventLoop().execute(new OneTimeTask(){

                @Override
                public void run() {
                    AbstractEpollStreamChannel.this.clearSpliceQueue();
                }
            });
        }
    }

    private boolean writeBytes(ChannelOutboundBuffer in, ByteBuf buf, int writeSpinCount) throws Exception {
        int readableBytes = buf.readableBytes();
        if (readableBytes == 0) {
            in.remove();
            return true;
        }
        if (buf.hasMemoryAddress() || buf.nioBufferCount() == 1) {
            int writtenBytes = this.doWriteBytes(buf, writeSpinCount);
            in.removeBytes(writtenBytes);
            return writtenBytes == readableBytes;
        }
        ByteBuffer[] nioBuffers = buf.nioBuffers();
        return this.writeBytesMultiple(in, nioBuffers, nioBuffers.length, readableBytes, writeSpinCount);
    }

    private boolean writeBytesMultiple(ChannelOutboundBuffer in, IovArray array, int writeSpinCount) throws IOException {
        long expectedWrittenBytes;
        long localWrittenBytes;
        long initialExpectedWrittenBytes = expectedWrittenBytes = array.size();
        int cnt = array.count();
        assert (expectedWrittenBytes != 0L);
        assert (cnt != 0);
        boolean done = false;
        int offset = 0;
        int end = offset + cnt;
        for (int i = writeSpinCount - 1; i >= 0 && (localWrittenBytes = Native.writevAddresses(this.fd().intValue(), array.memoryAddress(offset), cnt)) != 0L; --i) {
            long bytes;
            if ((expectedWrittenBytes -= localWrittenBytes) == 0L) {
                done = true;
                break;
            }
            while ((bytes = array.processWritten(offset, localWrittenBytes)) != -1L) {
                --cnt;
                if (++offset < end && (localWrittenBytes -= bytes) > 0L) continue;
            }
        }
        if (!done) {
            this.setFlag(Native.EPOLLOUT);
        }
        in.removeBytes(initialExpectedWrittenBytes - expectedWrittenBytes);
        return done;
    }

    private boolean writeBytesMultiple(ChannelOutboundBuffer in, ByteBuffer[] nioBuffers, int nioBufferCnt, long expectedWrittenBytes, int writeSpinCount) throws IOException {
        long localWrittenBytes;
        assert (expectedWrittenBytes != 0L);
        long initialExpectedWrittenBytes = expectedWrittenBytes;
        boolean done = false;
        int offset = 0;
        int end = offset + nioBufferCnt;
        block0 : for (int i = writeSpinCount - 1; i >= 0 && (localWrittenBytes = Native.writev(this.fd().intValue(), nioBuffers, offset, nioBufferCnt)) != 0L; --i) {
            int bytes;
            if ((expectedWrittenBytes -= localWrittenBytes) == 0L) {
                done = true;
                break;
            }
            do {
                ByteBuffer buffer = nioBuffers[offset];
                int pos = buffer.position();
                bytes = buffer.limit() - pos;
                if ((long)bytes > localWrittenBytes) {
                    buffer.position(pos + (int)localWrittenBytes);
                    continue block0;
                }
                --nioBufferCnt;
            } while (++offset < end && (localWrittenBytes -= (long)bytes) > 0L);
        }
        in.removeBytes(initialExpectedWrittenBytes - expectedWrittenBytes);
        if (!done) {
            this.setFlag(Native.EPOLLOUT);
        }
        return done;
    }

    private boolean writeFileRegion(ChannelOutboundBuffer in, DefaultFileRegion region, int writeSpinCount) throws Exception {
        long regionCount = region.count();
        if (region.transfered() >= regionCount) {
            in.remove();
            return true;
        }
        long baseOffset = region.position();
        boolean done = false;
        long flushedAmount = 0L;
        for (int i = writeSpinCount - 1; i >= 0; --i) {
            long offset = region.transfered();
            long localFlushedAmount = Native.sendfile(this.fd().intValue(), region, baseOffset, offset, regionCount - offset);
            if (localFlushedAmount == 0L) break;
            flushedAmount += localFlushedAmount;
            if (region.transfered() < regionCount) continue;
            done = true;
            break;
        }
        if (flushedAmount > 0L) {
            in.progress(flushedAmount);
        }
        if (done) {
            in.remove();
        } else {
            this.setFlag(Native.EPOLLOUT);
        }
        return done;
    }

    @Override
    protected void doWrite(ChannelOutboundBuffer in) throws Exception {
        int msgCount;
        int writeSpinCount = this.config().getWriteSpinCount();
        do {
            if ((msgCount = in.size()) != 0) continue;
            this.clearFlag(Native.EPOLLOUT);
            break;
        } while (!(msgCount > 1 && in.current() instanceof ByteBuf ? !this.doWriteMultiple(in, writeSpinCount) : !this.doWriteSingle(in, writeSpinCount)));
    }

    protected boolean doWriteSingle(ChannelOutboundBuffer in, int writeSpinCount) throws Exception {
        Object msg = in.current();
        if (msg instanceof ByteBuf) {
            ByteBuf buf = (ByteBuf)msg;
            if (!this.writeBytes(in, buf, writeSpinCount)) {
                return false;
            }
        } else if (msg instanceof DefaultFileRegion) {
            DefaultFileRegion region = (DefaultFileRegion)msg;
            if (!this.writeFileRegion(in, region, writeSpinCount)) {
                return false;
            }
        } else if (msg instanceof SpliceOutTask) {
            if (!((SpliceOutTask)msg).spliceOut()) {
                return false;
            }
            in.remove();
        } else {
            throw new Error();
        }
        return true;
    }

    private boolean doWriteMultiple(ChannelOutboundBuffer in, int writeSpinCount) throws Exception {
        if (PlatformDependent.hasUnsafe()) {
            IovArray array = IovArrayThreadLocal.get(in);
            int cnt = array.count();
            if (cnt >= 1) {
                if (!this.writeBytesMultiple(in, array, writeSpinCount)) {
                    return false;
                }
            } else {
                in.removeBytes(0L);
            }
        } else {
            ByteBuffer[] buffers = in.nioBuffers();
            int cnt = in.nioBufferCount();
            if (cnt >= 1) {
                if (!this.writeBytesMultiple(in, buffers, cnt, in.nioBufferSize(), writeSpinCount)) {
                    return false;
                }
            } else {
                in.removeBytes(0L);
            }
        }
        return true;
    }

    @Override
    protected Object filterOutboundMessage(Object msg) {
        if (msg instanceof ByteBuf) {
            ByteBuf buf = (ByteBuf)msg;
            if (!(buf.hasMemoryAddress() || !PlatformDependent.hasUnsafe() && buf.isDirect())) {
                if (buf instanceof CompositeByteBuf) {
                    CompositeByteBuf comp = (CompositeByteBuf)buf;
                    if (!comp.isDirect() || comp.nioBufferCount() > Native.IOV_MAX) {
                        buf = this.newDirectBuffer(buf);
                        assert (buf.hasMemoryAddress());
                    }
                } else {
                    buf = this.newDirectBuffer(buf);
                    assert (buf.hasMemoryAddress());
                }
            }
            return buf;
        }
        if (msg instanceof DefaultFileRegion || msg instanceof SpliceOutTask) {
            return msg;
        }
        throw new UnsupportedOperationException("unsupported message type: " + StringUtil.simpleClassName(msg) + EXPECTED_TYPES);
    }

    protected boolean isInputShutdown0() {
        return this.inputShutdown;
    }

    protected boolean isOutputShutdown0() {
        return this.outputShutdown || !this.isActive();
    }

    protected void shutdownOutput0(ChannelPromise promise) {
        try {
            Native.shutdown(this.fd().intValue(), false, true);
            this.outputShutdown = true;
            promise.setSuccess();
        }
        catch (Throwable cause) {
            promise.setFailure(cause);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void doClose() throws Exception {
        try {
            ScheduledFuture future;
            ChannelPromise promise = this.connectPromise;
            if (promise != null) {
                promise.tryFailure(CLOSED_CHANNEL_EXCEPTION);
                this.connectPromise = null;
            }
            if ((future = this.connectTimeoutFuture) != null) {
                future.cancel(false);
                this.connectTimeoutFuture = null;
            }
            super.doClose();
        }
        finally {
            this.safeClosePipe(this.pipeIn);
            this.safeClosePipe(this.pipeOut);
            this.clearSpliceQueue();
        }
    }

    private void clearSpliceQueue() {
        SpliceInTask task;
        while ((task = this.spliceQueue.poll()) != null) {
            task.promise.tryFailure(CLOSED_CHANNEL_EXCEPTION);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected boolean doConnect(SocketAddress remoteAddress, SocketAddress localAddress) throws Exception {
        if (localAddress != null) {
            Native.bind(this.fd().intValue(), localAddress);
        }
        boolean success = false;
        try {
            boolean connected = Native.connect(this.fd().intValue(), remoteAddress);
            if (!connected) {
                this.setFlag(Native.EPOLLOUT);
            }
            success = true;
            boolean bl = connected;
            return bl;
        }
        finally {
            if (!success) {
                this.doClose();
            }
        }
    }

    private void safeClosePipe(int pipe) {
        block3 : {
            if (pipe != -1) {
                try {
                    Native.close(pipe);
                }
                catch (IOException e) {
                    if (!logger.isWarnEnabled()) break block3;
                    logger.warn("Error while closing a pipe", e);
                }
            }
        }
    }

    static {
        CLOSED_CHANNEL_EXCEPTION.setStackTrace(EmptyArrays.EMPTY_STACK_TRACE);
    }

    private final class SpliceFdTask
    extends SpliceInTask {
        private final FileDescriptor fd;
        private final ChannelPromise promise;
        private int offset;

        SpliceFdTask(FileDescriptor fd, int offset, int len, ChannelPromise promise) {
            super(len, promise);
            this.fd = fd;
            this.promise = promise;
            this.offset = offset;
        }

        @Override
        public SpliceFdTask value() {
            return this;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public boolean spliceIn(RecvByteBufAllocator.Handle handle) throws IOException {
            assert (AbstractEpollStreamChannel.this.eventLoop().inEventLoop());
            if (this.len == 0) {
                this.promise.setSuccess();
                return true;
            }
            int pipeIn = -1;
            int pipeOut = -1;
            try {
                boolean splicedOut;
                long fds = Native.pipe();
                pipeIn = (int)(fds >> 32);
                pipeOut = (int)fds;
                int splicedIn = this.spliceIn(pipeOut, handle);
                if (splicedIn > 0) {
                    if (this.len != Integer.MAX_VALUE) {
                        this.len -= splicedIn;
                    }
                    while ((splicedIn -= (splicedOut = Native.splice(pipeIn, -1, this.fd.intValue(), this.offset, splicedIn))) > 0) {
                    }
                    if (this.len == 0) {
                        this.promise.setSuccess();
                        splicedOut = true;
                        return splicedOut;
                    }
                }
                splicedOut = false;
                return splicedOut;
            }
            catch (Throwable cause) {
                this.promise.setFailure(cause);
                boolean bl = true;
                return bl;
            }
            finally {
                AbstractEpollStreamChannel.this.safeClosePipe(pipeIn);
                AbstractEpollStreamChannel.this.safeClosePipe(pipeOut);
            }
        }
    }

    private final class SpliceOutTask {
        private final AbstractEpollStreamChannel ch;
        private final boolean autoRead;
        private int len;

        SpliceOutTask(AbstractEpollStreamChannel ch, int len, boolean autoRead) {
            this.ch = ch;
            this.len = len;
            this.autoRead = autoRead;
        }

        public boolean spliceOut() throws Exception {
            assert (this.ch.eventLoop().inEventLoop());
            try {
                int splicedOut = Native.splice(this.ch.pipeIn, -1, this.ch.fd().intValue(), -1, this.len);
                this.len -= splicedOut;
                if (this.len == 0) {
                    if (this.autoRead) {
                        AbstractEpollStreamChannel.this.config().setAutoRead(true);
                    }
                    return true;
                }
                return false;
            }
            catch (IOException e) {
                if (this.autoRead) {
                    AbstractEpollStreamChannel.this.config().setAutoRead(true);
                }
                throw e;
            }
        }
    }

    private final class SpliceInChannelTask
    extends SpliceInTask
    implements ChannelFutureListener {
        private final AbstractEpollStreamChannel ch;

        SpliceInChannelTask(AbstractEpollStreamChannel ch, int len, ChannelPromise promise) {
            super(len, promise);
            this.ch = ch;
        }

        @Override
        public void operationComplete(ChannelFuture future) throws Exception {
            if (!future.isSuccess()) {
                this.promise.setFailure(future.cause());
            }
        }

        @Override
        public boolean spliceIn(RecvByteBufAllocator.Handle handle) throws IOException {
            assert (this.ch.eventLoop().inEventLoop());
            if (this.len == 0) {
                this.promise.setSuccess();
                return true;
            }
            try {
                int splicedIn;
                int pipeOut = this.ch.pipeOut;
                if (pipeOut == -1) {
                    long fds = Native.pipe();
                    this.ch.pipeIn = (int)(fds >> 32);
                    pipeOut = this.ch.pipeOut = (int)fds;
                }
                if ((splicedIn = this.spliceIn(pipeOut, handle)) > 0) {
                    if (this.len != Integer.MAX_VALUE) {
                        this.len -= splicedIn;
                    }
                    ChannelPromise splicePromise = this.len == 0 ? this.promise : this.ch.newPromise().addListener(this);
                    boolean autoRead = AbstractEpollStreamChannel.this.config().isAutoRead();
                    this.ch.unsafe().write(new SpliceOutTask(this.ch, splicedIn, autoRead), splicePromise);
                    this.ch.unsafe().flush();
                    if (autoRead && !splicePromise.isDone()) {
                        AbstractEpollStreamChannel.this.config().setAutoRead(false);
                    }
                }
                return this.len == 0;
            }
            catch (Throwable cause) {
                this.promise.setFailure(cause);
                return true;
            }
        }
    }

    protected abstract class SpliceInTask
    extends MpscLinkedQueueNode<SpliceInTask> {
        final ChannelPromise promise;
        int len;

        protected SpliceInTask(int len, ChannelPromise promise) {
            this.promise = promise;
            this.len = len;
        }

        @Override
        public SpliceInTask value() {
            return this;
        }

        abstract boolean spliceIn(RecvByteBufAllocator.Handle var1) throws IOException;

        protected final int spliceIn(int pipeOut, RecvByteBufAllocator.Handle handle) throws IOException {
            int localSplicedIn;
            int length = Math.min(handle.guess(), this.len);
            int splicedIn = 0;
            while ((localSplicedIn = Native.splice(AbstractEpollStreamChannel.this.fd().intValue(), -1, pipeOut, -1, length)) != 0) {
                splicedIn += localSplicedIn;
                length -= localSplicedIn;
            }
            handle.record(splicedIn);
            return splicedIn;
        }
    }

    class EpollStreamUnsafe
    extends AbstractEpollChannel.AbstractEpollUnsafe {
        private RecvByteBufAllocator.Handle allocHandle;

        EpollStreamUnsafe() {
        }

        private void closeOnRead(ChannelPipeline pipeline) {
            AbstractEpollStreamChannel.this.inputShutdown = true;
            if (AbstractEpollStreamChannel.this.isOpen()) {
                if (Boolean.TRUE.equals(AbstractEpollStreamChannel.this.config().getOption(ChannelOption.ALLOW_HALF_CLOSURE))) {
                    this.clearEpollIn0();
                    pipeline.fireUserEventTriggered(ChannelInputShutdownEvent.INSTANCE);
                } else {
                    this.close(this.voidPromise());
                }
            }
        }

        private boolean handleReadException(ChannelPipeline pipeline, ByteBuf byteBuf, Throwable cause, boolean close) {
            if (byteBuf != null) {
                if (byteBuf.isReadable()) {
                    this.readPending = false;
                    pipeline.fireChannelRead(byteBuf);
                } else {
                    byteBuf.release();
                }
            }
            pipeline.fireChannelReadComplete();
            pipeline.fireExceptionCaught(cause);
            if (close || cause instanceof IOException) {
                this.closeOnRead(pipeline);
                return true;
            }
            return false;
        }

        @Override
        public void connect(final SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
            if (!promise.setUncancellable() || !this.ensureOpen(promise)) {
                return;
            }
            try {
                if (AbstractEpollStreamChannel.this.connectPromise != null) {
                    throw new IllegalStateException("connection attempt already made");
                }
                boolean wasActive = AbstractEpollStreamChannel.this.isActive();
                if (AbstractEpollStreamChannel.this.doConnect(remoteAddress, localAddress)) {
                    this.fulfillConnectPromise(promise, wasActive);
                } else {
                    AbstractEpollStreamChannel.this.connectPromise = promise;
                    AbstractEpollStreamChannel.this.requestedRemoteAddress = remoteAddress;
                    int connectTimeoutMillis = AbstractEpollStreamChannel.this.config().getConnectTimeoutMillis();
                    if (connectTimeoutMillis > 0) {
                        AbstractEpollStreamChannel.this.connectTimeoutFuture = AbstractEpollStreamChannel.this.eventLoop().schedule(new Runnable(){

                            @Override
                            public void run() {
                                ChannelPromise connectPromise = AbstractEpollStreamChannel.this.connectPromise;
                                ConnectTimeoutException cause = new ConnectTimeoutException("connection timed out: " + remoteAddress);
                                if (connectPromise != null && connectPromise.tryFailure(cause)) {
                                    EpollStreamUnsafe.this.close(EpollStreamUnsafe.this.voidPromise());
                                }
                            }
                        }, (long)connectTimeoutMillis, TimeUnit.MILLISECONDS);
                    }
                    promise.addListener(new ChannelFutureListener(){

                        @Override
                        public void operationComplete(ChannelFuture future) throws Exception {
                            if (future.isCancelled()) {
                                if (AbstractEpollStreamChannel.this.connectTimeoutFuture != null) {
                                    AbstractEpollStreamChannel.this.connectTimeoutFuture.cancel(false);
                                }
                                AbstractEpollStreamChannel.this.connectPromise = null;
                                EpollStreamUnsafe.this.close(EpollStreamUnsafe.this.voidPromise());
                            }
                        }
                    });
                }
            }
            catch (Throwable t) {
                this.closeIfClosed();
                promise.tryFailure(this.annotateConnectException(t, remoteAddress));
            }
        }

        private void fulfillConnectPromise(ChannelPromise promise, boolean wasActive) {
            if (promise == null) {
                return;
            }
            AbstractEpollStreamChannel.this.active = true;
            boolean promiseSet = promise.trySuccess();
            if (!wasActive && AbstractEpollStreamChannel.this.isActive()) {
                AbstractEpollStreamChannel.this.pipeline().fireChannelActive();
            }
            if (!promiseSet) {
                this.close(this.voidPromise());
            }
        }

        private void fulfillConnectPromise(ChannelPromise promise, Throwable cause) {
            if (promise == null) {
                return;
            }
            promise.tryFailure(cause);
            this.closeIfClosed();
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        private void finishConnect() {
            assert (AbstractEpollStreamChannel.this.eventLoop().inEventLoop());
            boolean connectStillInProgress = false;
            try {
                boolean wasActive = AbstractEpollStreamChannel.this.isActive();
                if (!this.doFinishConnect()) {
                    connectStillInProgress = true;
                    return;
                }
                this.fulfillConnectPromise(AbstractEpollStreamChannel.this.connectPromise, wasActive);
            }
            catch (Throwable t) {
                this.fulfillConnectPromise(AbstractEpollStreamChannel.this.connectPromise, this.annotateConnectException(t, AbstractEpollStreamChannel.this.requestedRemoteAddress));
            }
            finally {
                if (!connectStillInProgress) {
                    if (AbstractEpollStreamChannel.this.connectTimeoutFuture != null) {
                        AbstractEpollStreamChannel.this.connectTimeoutFuture.cancel(false);
                    }
                    AbstractEpollStreamChannel.this.connectPromise = null;
                }
            }
        }

        @Override
        void epollOutReady() {
            if (AbstractEpollStreamChannel.this.connectPromise != null) {
                this.finishConnect();
            } else {
                super.epollOutReady();
            }
        }

        private boolean doFinishConnect() throws Exception {
            if (Native.finishConnect(AbstractEpollStreamChannel.this.fd().intValue())) {
                AbstractEpollStreamChannel.this.clearFlag(Native.EPOLLOUT);
                return true;
            }
            AbstractEpollStreamChannel.this.setFlag(Native.EPOLLOUT);
            return false;
        }

        @Override
        void epollRdHupReady() {
            if (AbstractEpollStreamChannel.this.isActive()) {
                this.epollInReady();
            } else {
                this.closeOnRead(AbstractEpollStreamChannel.this.pipeline());
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        void epollInReady() {
            EpollChannelConfig config = AbstractEpollStreamChannel.this.config();
            boolean edgeTriggered = AbstractEpollStreamChannel.this.isFlagSet(Native.EPOLLET);
            if (!(this.readPending || edgeTriggered || config.isAutoRead())) {
                this.clearEpollIn0();
                return;
            }
            ChannelPipeline pipeline = AbstractEpollStreamChannel.this.pipeline();
            ByteBufAllocator allocator = config.getAllocator();
            RecvByteBufAllocator.Handle allocHandle = this.allocHandle;
            if (allocHandle == null) {
                this.allocHandle = allocHandle = config.getRecvByteBufAllocator().newHandle();
            }
            ByteBuf byteBuf = null;
            boolean close = false;
            try {
                int maxMessagesPerRead = edgeTriggered ? Integer.MAX_VALUE : config.getMaxMessagesPerRead();
                int messages = 0;
                int totalReadAmount = 0;
                do {
                    SpliceInTask spliceTask;
                    if ((spliceTask = (SpliceInTask)AbstractEpollStreamChannel.this.spliceQueue.peek()) != null) {
                        if (!spliceTask.spliceIn(allocHandle)) break;
                        if (!AbstractEpollStreamChannel.this.isActive()) continue;
                        AbstractEpollStreamChannel.this.spliceQueue.remove();
                        continue;
                    }
                    byteBuf = allocHandle.allocate(allocator);
                    int writable = byteBuf.writableBytes();
                    int localReadAmount = AbstractEpollStreamChannel.this.doReadBytes(byteBuf);
                    if (localReadAmount <= 0) {
                        byteBuf.release();
                        close = localReadAmount < 0;
                        break;
                    }
                    this.readPending = false;
                    pipeline.fireChannelRead(byteBuf);
                    byteBuf = null;
                    if (totalReadAmount >= Integer.MAX_VALUE - localReadAmount) {
                        allocHandle.record(totalReadAmount);
                        totalReadAmount = localReadAmount;
                    } else {
                        totalReadAmount += localReadAmount;
                    }
                    if (localReadAmount < writable || !edgeTriggered && !config.isAutoRead()) break;
                } while (++messages < maxMessagesPerRead);
                pipeline.fireChannelReadComplete();
                allocHandle.record(totalReadAmount);
                if (close) {
                    this.closeOnRead(pipeline);
                    close = false;
                }
            }
            catch (Throwable t) {
                boolean closed = this.handleReadException(pipeline, byteBuf, t, close);
                if (!closed) {
                    AbstractEpollStreamChannel.this.eventLoop().execute(new Runnable(){

                        @Override
                        public void run() {
                            EpollStreamUnsafe.this.epollInReady();
                        }
                    });
                }
            }
            finally {
                if (!this.readPending && !config.isAutoRead()) {
                    this.clearEpollIn0();
                }
            }
        }

    }

}

