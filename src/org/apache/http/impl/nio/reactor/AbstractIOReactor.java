/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.impl.nio.reactor;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.Socket;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.http.annotation.ThreadSafe;
import org.apache.http.impl.nio.reactor.ChannelEntry;
import org.apache.http.impl.nio.reactor.IOSessionImpl;
import org.apache.http.impl.nio.reactor.InterestOpEntry;
import org.apache.http.impl.nio.reactor.InterestOpsCallback;
import org.apache.http.impl.nio.reactor.SessionClosedCallback;
import org.apache.http.impl.nio.reactor.SessionRequestImpl;
import org.apache.http.nio.reactor.IOReactor;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.nio.reactor.IOReactorStatus;
import org.apache.http.nio.reactor.IOSession;
import org.apache.http.util.Args;
import org.apache.http.util.Asserts;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
@ThreadSafe
public abstract class AbstractIOReactor
implements IOReactor {
    private volatile IOReactorStatus status;
    private final Object statusMutex;
    private final long selectTimeout;
    private final boolean interestOpsQueueing;
    private final Selector selector;
    private final Set<IOSession> sessions;
    private final Queue<InterestOpEntry> interestOpsQueue;
    private final Queue<IOSession> closedSessions;
    private final Queue<ChannelEntry> newChannels;

    public AbstractIOReactor(long selectTimeout) throws IOReactorException {
        this(selectTimeout, false);
    }

    public AbstractIOReactor(long selectTimeout, boolean interestOpsQueueing) throws IOReactorException {
        Args.positive(selectTimeout, "Select timeout");
        this.selectTimeout = selectTimeout;
        this.interestOpsQueueing = interestOpsQueueing;
        this.sessions = Collections.synchronizedSet(new HashSet());
        this.interestOpsQueue = new ConcurrentLinkedQueue<InterestOpEntry>();
        this.closedSessions = new ConcurrentLinkedQueue<IOSession>();
        this.newChannels = new ConcurrentLinkedQueue<ChannelEntry>();
        try {
            this.selector = Selector.open();
        }
        catch (IOException ex) {
            throw new IOReactorException("Failure opening selector", ex);
        }
        this.statusMutex = new Object();
        this.status = IOReactorStatus.INACTIVE;
    }

    protected abstract void acceptable(SelectionKey var1);

    protected abstract void connectable(SelectionKey var1);

    protected abstract void readable(SelectionKey var1);

    protected abstract void writable(SelectionKey var1);

    protected abstract void validate(Set<SelectionKey> var1);

    protected void sessionCreated(SelectionKey key, IOSession session) {
    }

    protected void sessionClosed(IOSession session) {
    }

    protected void sessionTimedOut(IOSession session) {
    }

    protected IOSession getSession(SelectionKey key) {
        return (IOSession)key.attachment();
    }

    @Override
    public IOReactorStatus getStatus() {
        return this.status;
    }

    public boolean getInterestOpsQueueing() {
        return this.interestOpsQueueing;
    }

    public void addChannel(ChannelEntry channelEntry) {
        Args.notNull(channelEntry, "Channel entry");
        this.newChannels.add(channelEntry);
        this.selector.wakeup();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void execute() throws InterruptedIOException, IOReactorException {
        this.status = IOReactorStatus.ACTIVE;
        try {
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
                if (this.status == IOReactorStatus.SHUT_DOWN) {
                    break;
                }
                if (this.status == IOReactorStatus.SHUTTING_DOWN) {
                    this.closeSessions();
                    this.closeNewChannels();
                }
                if (readyCount > 0) {
                    this.processEvents(this.selector.selectedKeys());
                }
                this.validate(this.selector.keys());
                this.processClosedSessions();
                if (this.status == IOReactorStatus.ACTIVE) {
                    this.processNewChannels();
                }
                if (this.status.compareTo(IOReactorStatus.ACTIVE) > 0 && this.sessions.isEmpty()) {
                    break;
                }
                if (!this.interestOpsQueueing) continue;
                this.processPendingInterestOps();
            } while (true);
        }
        catch (ClosedSelectorException ignore2) {
            this.hardShutdown();
            Object ignore2 = this.statusMutex;
            synchronized (ignore2) {
                this.statusMutex.notifyAll();
            }
        }
        finally {
            this.hardShutdown();
            Object readyCount = this.statusMutex;
            synchronized (readyCount) {
                this.statusMutex.notifyAll();
            }
        }
    }

    private void processEvents(Set<SelectionKey> selectedKeys) {
        for (SelectionKey key : selectedKeys) {
            this.processEvent(key);
        }
        selectedKeys.clear();
    }

    protected void processEvent(SelectionKey key) {
        IOSessionImpl session = (IOSessionImpl)key.attachment();
        try {
            if (key.isAcceptable()) {
                this.acceptable(key);
            }
            if (key.isConnectable()) {
                this.connectable(key);
            }
            if (key.isReadable()) {
                session.resetLastRead();
                this.readable(key);
            }
            if (key.isWritable()) {
                session.resetLastWrite();
                this.writable(key);
            }
        }
        catch (CancelledKeyException ex) {
            this.queueClosedSession(session);
            key.attach(null);
        }
    }

    protected void queueClosedSession(IOSession session) {
        if (session != null) {
            this.closedSessions.add(session);
        }
    }

    private void processNewChannels() throws IOReactorException {
        ChannelEntry entry;
        while ((entry = this.newChannels.poll()) != null) {
            SocketChannel channel;
            SelectionKey key;
            IOSessionImpl session;
            try {
                channel = entry.getChannel();
                channel.configureBlocking(false);
                key = channel.register(this.selector, 1);
            }
            catch (ClosedChannelException ex) {
                SessionRequestImpl sessionRequest = entry.getSessionRequest();
                if (sessionRequest != null) {
                    sessionRequest.failed(ex);
                }
                return;
            }
            catch (IOException ex) {
                throw new IOReactorException("Failure registering channel with the selector", ex);
            }
            SessionClosedCallback sessionClosedCallback = new SessionClosedCallback(){

                public void sessionClosed(IOSession session) {
                    AbstractIOReactor.this.queueClosedSession(session);
                }
            };
            InterestOpsCallback interestOpsCallback = null;
            if (this.interestOpsQueueing) {
                interestOpsCallback = new InterestOpsCallback(){

                    public void addInterestOps(InterestOpEntry entry) {
                        AbstractIOReactor.this.queueInterestOps(entry);
                    }
                };
            }
            try {
                session = new IOSessionImpl(key, interestOpsCallback, sessionClosedCallback);
                int timeout = 0;
                try {
                    timeout = channel.socket().getSoTimeout();
                }
                catch (IOException ex) {
                    // empty catch block
                }
                session.setAttribute("http.session.attachment", entry.getAttachment());
                session.setSocketTimeout(timeout);
            }
            catch (CancelledKeyException ex) {
                continue;
            }
            try {
                this.sessions.add(session);
                SessionRequestImpl sessionRequest = entry.getSessionRequest();
                if (sessionRequest != null) {
                    sessionRequest.completed(session);
                }
                key.attach(session);
                this.sessionCreated(key, session);
            }
            catch (CancelledKeyException ex) {
                this.queueClosedSession(session);
                key.attach(null);
            }
        }
    }

    private void processClosedSessions() {
        IOSession session;
        while ((session = this.closedSessions.poll()) != null) {
            if (!this.sessions.remove(session)) continue;
            try {
                this.sessionClosed(session);
            }
            catch (CancelledKeyException ex) {}
        }
    }

    private void processPendingInterestOps() {
        InterestOpEntry entry;
        if (!this.interestOpsQueueing) {
            return;
        }
        while ((entry = this.interestOpsQueue.poll()) != null) {
            SelectionKey key = entry.getSelectionKey();
            int eventMask = entry.getEventMask();
            if (!key.isValid()) continue;
            key.interestOps(eventMask);
        }
    }

    private boolean queueInterestOps(InterestOpEntry entry) {
        Asserts.check(this.interestOpsQueueing, "Interest ops queueing not enabled");
        if (entry == null) {
            return false;
        }
        this.interestOpsQueue.add(entry);
        return true;
    }

    protected void timeoutCheck(SelectionKey key, long now) {
        int timeout;
        IOSessionImpl session = (IOSessionImpl)key.attachment();
        if (session != null && (timeout = session.getSocketTimeout()) > 0 && session.getLastAccessTime() + (long)timeout < now) {
            this.sessionTimedOut(session);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void closeSessions() {
        Set<IOSession> set = this.sessions;
        synchronized (set) {
            for (IOSession session : this.sessions) {
                session.close();
            }
        }
    }

    protected void closeNewChannels() throws IOReactorException {
        ChannelEntry entry;
        while ((entry = this.newChannels.poll()) != null) {
            SessionRequestImpl sessionRequest = entry.getSessionRequest();
            if (sessionRequest != null) {
                sessionRequest.cancel();
            }
            SocketChannel channel = entry.getChannel();
            try {
                channel.close();
            }
            catch (IOException ignore) {}
        }
    }

    protected void closeActiveChannels() throws IOReactorException {
        try {
            Set<SelectionKey> keys = this.selector.keys();
            for (SelectionKey key : keys) {
                IOSession session = this.getSession(key);
                if (session == null) continue;
                session.close();
            }
            this.selector.close();
        }
        catch (IOException ignore) {
            // empty catch block
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void gracefulShutdown() {
        Object object = this.statusMutex;
        synchronized (object) {
            if (this.status != IOReactorStatus.ACTIVE) {
                return;
            }
            this.status = IOReactorStatus.SHUTTING_DOWN;
        }
        this.selector.wakeup();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void hardShutdown() throws IOReactorException {
        Object object = this.statusMutex;
        synchronized (object) {
            if (this.status == IOReactorStatus.SHUT_DOWN) {
                return;
            }
            this.status = IOReactorStatus.SHUT_DOWN;
        }
        this.closeNewChannels();
        this.closeActiveChannels();
        this.processClosedSessions();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void awaitShutdown(long timeout) throws InterruptedException {
        Object object = this.statusMutex;
        synchronized (object) {
            long deadline = System.currentTimeMillis() + timeout;
            long remaining = timeout;
            while (this.status != IOReactorStatus.SHUT_DOWN) {
                this.statusMutex.wait(remaining);
                if (timeout <= 0L || (remaining = deadline - System.currentTimeMillis()) > 0L) continue;
            }
        }
    }

    @Override
    public void shutdown(long gracePeriod) throws IOReactorException {
        if (this.status != IOReactorStatus.INACTIVE) {
            this.gracefulShutdown();
            try {
                this.awaitShutdown(gracePeriod);
            }
            catch (InterruptedException ignore) {
                // empty catch block
            }
        }
        if (this.status != IOReactorStatus.SHUT_DOWN) {
            this.hardShutdown();
        }
    }

    @Override
    public void shutdown() throws IOReactorException {
        this.shutdown(1000L);
    }

}

