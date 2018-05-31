/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.impl.nio.reactor;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.Socket;
import java.nio.channels.Channel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadFactory;
import org.apache.http.annotation.ThreadSafe;
import org.apache.http.impl.nio.reactor.BaseIOReactor;
import org.apache.http.impl.nio.reactor.ChannelEntry;
import org.apache.http.impl.nio.reactor.ExceptionEvent;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.reactor.IOEventDispatch;
import org.apache.http.nio.reactor.IOReactor;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.nio.reactor.IOReactorExceptionHandler;
import org.apache.http.nio.reactor.IOReactorStatus;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.Args;
import org.apache.http.util.Asserts;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
@ThreadSafe
public abstract class AbstractMultiworkerIOReactor
implements IOReactor {
    protected volatile IOReactorStatus status;
    @Deprecated
    protected final HttpParams params;
    protected final IOReactorConfig config;
    protected final Selector selector;
    protected final long selectTimeout;
    protected final boolean interestOpsQueueing;
    private final int workerCount;
    private final ThreadFactory threadFactory;
    private final BaseIOReactor[] dispatchers;
    private final Worker[] workers;
    private final Thread[] threads;
    private final Object statusLock;
    protected IOReactorExceptionHandler exceptionHandler;
    protected List<ExceptionEvent> auditLog;
    private int currentWorker = 0;

    public AbstractMultiworkerIOReactor(IOReactorConfig config, ThreadFactory threadFactory) throws IOReactorException {
        this.config = config != null ? config : IOReactorConfig.DEFAULT;
        this.params = new BasicHttpParams();
        try {
            this.selector = Selector.open();
        }
        catch (IOException ex) {
            throw new IOReactorException("Failure opening selector", ex);
        }
        this.selectTimeout = this.config.getSelectInterval();
        this.interestOpsQueueing = this.config.isInterestOpQueued();
        this.statusLock = new Object();
        this.threadFactory = threadFactory != null ? threadFactory : new DefaultThreadFactory();
        this.workerCount = this.config.getIoThreadCount();
        this.dispatchers = new BaseIOReactor[this.workerCount];
        this.workers = new Worker[this.workerCount];
        this.threads = new Thread[this.workerCount];
        this.status = IOReactorStatus.INACTIVE;
    }

    public AbstractMultiworkerIOReactor() throws IOReactorException {
        this(null, null);
    }

    @Deprecated
    static IOReactorConfig convert(int workerCount, HttpParams params) {
        Args.notNull(params, "HTTP parameters");
        return IOReactorConfig.custom().setSelectInterval(params.getLongParameter("http.nio.select-interval", 1000L)).setShutdownGracePeriod(params.getLongParameter("http.nio.grace-period", 500L)).setInterestOpQueued(params.getBooleanParameter("http.nio.select-interval", false)).setIoThreadCount(workerCount).setSoTimeout(params.getIntParameter("http.socket.timeout", 0)).setConnectTimeout(params.getIntParameter("http.connection.timeout", 0)).setSoTimeout(params.getIntParameter("http.socket.timeout", 0)).setSoReuseAddress(params.getBooleanParameter("http.socket.reuseaddr", false)).setSoKeepAlive(params.getBooleanParameter("http.socket.keepalive", false)).setSoLinger(params.getIntParameter("http.socket.linger", -1)).setTcpNoDelay(params.getBooleanParameter("http.tcp.nodelay", true)).build();
    }

    @Deprecated
    public AbstractMultiworkerIOReactor(int workerCount, ThreadFactory threadFactory, HttpParams params) throws IOReactorException {
        this(AbstractMultiworkerIOReactor.convert(workerCount, params), threadFactory);
    }

    @Override
    public IOReactorStatus getStatus() {
        return this.status;
    }

    public synchronized List<ExceptionEvent> getAuditLog() {
        if (this.auditLog != null) {
            return new ArrayList<ExceptionEvent>(this.auditLog);
        }
        return null;
    }

    protected synchronized void addExceptionEvent(Throwable ex, Date timestamp) {
        if (ex == null) {
            return;
        }
        if (this.auditLog == null) {
            this.auditLog = new ArrayList<ExceptionEvent>();
        }
        this.auditLog.add(new ExceptionEvent(ex, timestamp != null ? timestamp : new Date()));
    }

    protected void addExceptionEvent(Throwable ex) {
        this.addExceptionEvent(ex, null);
    }

    public void setExceptionHandler(IOReactorExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    protected abstract void processEvents(int var1) throws IOReactorException;

    protected abstract void cancelRequests() throws IOReactorException;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void execute(IOEventDispatch eventDispatch) throws InterruptedIOException, IOReactorException {
        int i;
        Args.notNull(eventDispatch, "Event dispatcher");
        Object object = this.statusLock;
        synchronized (object) {
            BaseIOReactor dispatcher;
            if (this.status.compareTo(IOReactorStatus.SHUTDOWN_REQUEST) >= 0) {
                this.status = IOReactorStatus.SHUT_DOWN;
                this.statusLock.notifyAll();
                return;
            }
            Asserts.check(this.status.compareTo(IOReactorStatus.INACTIVE) == 0, "Illegal state %s", new Object[]{this.status});
            this.status = IOReactorStatus.ACTIVE;
            for (i = 0; i < this.dispatchers.length; ++i) {
                dispatcher = new BaseIOReactor(this.selectTimeout, this.interestOpsQueueing);
                dispatcher.setExceptionHandler(this.exceptionHandler);
                this.dispatchers[i] = dispatcher;
            }
            for (i = 0; i < this.workerCount; ++i) {
                dispatcher = this.dispatchers[i];
                this.workers[i] = new Worker(dispatcher, eventDispatch);
                this.threads[i] = this.threadFactory.newThread(this.workers[i]);
            }
        }
        try {
            for (int i2 = 0; i2 < this.workerCount; ++i2) {
                if (this.status != IOReactorStatus.ACTIVE) {
                    return;
                }
                this.threads[i2].start();
            }
            do {
                int readyCount;
                try {
                    readyCount = this.selector.select(this.selectTimeout);
                }
                catch (InterruptedIOException ex) {
                    throw ex;
                }
                catch (IOException ex) {
                    throw new IOReactorException("Unexpected selector failure", ex);
                }
                if (this.status.compareTo(IOReactorStatus.ACTIVE) == 0) {
                    this.processEvents(readyCount);
                }
                for (i = 0; i < this.workerCount; ++i) {
                    Worker worker = this.workers[i];
                    Exception ex = worker.getException();
                    if (ex == null) continue;
                    throw new IOReactorException("I/O dispatch worker terminated abnormally", ex);
                }
            } while (this.status.compareTo(IOReactorStatus.ACTIVE) <= 0);
        }
        catch (ClosedSelectorException ex) {
            this.addExceptionEvent(ex);
        }
        catch (IOReactorException ex) {
            if (ex.getCause() != null) {
                this.addExceptionEvent(ex.getCause());
            }
            throw ex;
        }
        finally {
            this.doShutdown();
            Object ex = this.statusLock;
            synchronized (ex) {
                this.status = IOReactorStatus.SHUT_DOWN;
                this.statusLock.notifyAll();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void doShutdown() throws InterruptedIOException {
        block20 : {
            Object object = this.statusLock;
            synchronized (object) {
                if (this.status.compareTo(IOReactorStatus.SHUTTING_DOWN) >= 0) {
                    return;
                }
                this.status = IOReactorStatus.SHUTTING_DOWN;
            }
            try {
                this.cancelRequests();
            }
            catch (IOReactorException ex) {
                if (ex.getCause() == null) break block20;
                this.addExceptionEvent(ex.getCause());
            }
        }
        this.selector.wakeup();
        if (this.selector.isOpen()) {
            for (SelectionKey key : this.selector.keys()) {
                try {
                    SelectableChannel channel = key.channel();
                    if (channel == null) continue;
                    channel.close();
                }
                catch (IOException ex) {
                    this.addExceptionEvent(ex);
                }
            }
            try {
                this.selector.close();
            }
            catch (IOException ex) {
                this.addExceptionEvent(ex);
            }
        }
        for (int i = 0; i < this.workerCount; ++i) {
            BaseIOReactor dispatcher = this.dispatchers[i];
            dispatcher.gracefulShutdown();
        }
        long gracePeriod = this.config.getShutdownGracePeriod();
        try {
            int i;
            for (i = 0; i < this.workerCount; ++i) {
                BaseIOReactor dispatcher = this.dispatchers[i];
                if (dispatcher.getStatus() != IOReactorStatus.INACTIVE) {
                    dispatcher.awaitShutdown(gracePeriod);
                }
                if (dispatcher.getStatus() == IOReactorStatus.SHUT_DOWN) continue;
                try {
                    dispatcher.hardShutdown();
                    continue;
                }
                catch (IOReactorException ex) {
                    if (ex.getCause() == null) continue;
                    this.addExceptionEvent(ex.getCause());
                }
            }
            for (i = 0; i < this.workerCount; ++i) {
                Thread t = this.threads[i];
                if (t == null) continue;
                t.join(gracePeriod);
            }
        }
        catch (InterruptedException ex) {
            throw new InterruptedIOException(ex.getMessage());
        }
    }

    protected void addChannel(ChannelEntry entry) {
        int i = Math.abs(this.currentWorker++ % this.workerCount);
        this.dispatchers[i].addChannel(entry);
    }

    protected SelectionKey registerChannel(SelectableChannel channel, int ops) throws ClosedChannelException {
        return channel.register(this.selector, ops);
    }

    protected void prepareSocket(Socket socket) throws IOException {
        int linger;
        socket.setTcpNoDelay(this.config.isTcpNoDelay());
        socket.setKeepAlive(this.config.isSoKeepalive());
        if (this.config.getSoTimeout() > 0) {
            socket.setSoTimeout(this.config.getSoTimeout());
        }
        if (this.config.getSndBufSize() > 0) {
            socket.setSendBufferSize(this.config.getSndBufSize());
        }
        if (this.config.getRcvBufSize() > 0) {
            socket.setReceiveBufferSize(this.config.getRcvBufSize());
        }
        if ((linger = this.config.getSoLinger()) >= 0) {
            socket.setSoLinger(linger > 0, linger);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void awaitShutdown(long timeout) throws InterruptedException {
        Object object = this.statusLock;
        synchronized (object) {
            long deadline = System.currentTimeMillis() + timeout;
            long remaining = timeout;
            while (this.status != IOReactorStatus.SHUT_DOWN) {
                this.statusLock.wait(remaining);
                if (timeout <= 0L || (remaining = deadline - System.currentTimeMillis()) > 0L) continue;
            }
        }
    }

    @Override
    public void shutdown() throws IOException {
        this.shutdown(2000L);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void shutdown(long waitMs) throws IOException {
        Object object = this.statusLock;
        synchronized (object) {
            if (this.status.compareTo(IOReactorStatus.ACTIVE) > 0) {
                return;
            }
            if (this.status.compareTo(IOReactorStatus.INACTIVE) == 0) {
                this.status = IOReactorStatus.SHUT_DOWN;
                this.cancelRequests();
                return;
            }
            this.status = IOReactorStatus.SHUTDOWN_REQUEST;
        }
        this.selector.wakeup();
        try {
            this.awaitShutdown(waitMs);
        }
        catch (InterruptedException ignore) {
            // empty catch block
        }
    }

    static void closeChannel(Channel channel) {
        try {
            channel.close();
        }
        catch (IOException ignore) {
            // empty catch block
        }
    }

    static class DefaultThreadFactory
    implements ThreadFactory {
        private static volatile int COUNT = 0;

        DefaultThreadFactory() {
        }

        public Thread newThread(Runnable r) {
            return new Thread(r, "I/O dispatcher " + ++COUNT);
        }
    }

    static class Worker
    implements Runnable {
        final BaseIOReactor dispatcher;
        final IOEventDispatch eventDispatch;
        private volatile Exception exception;

        public Worker(BaseIOReactor dispatcher, IOEventDispatch eventDispatch) {
            this.dispatcher = dispatcher;
            this.eventDispatch = eventDispatch;
        }

        public void run() {
            try {
                this.dispatcher.execute(this.eventDispatch);
            }
            catch (Exception ex) {
                this.exception = ex;
            }
        }

        public Exception getException() {
            return this.exception;
        }
    }

}

