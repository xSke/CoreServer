/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.impl.nio.reactor;

import java.io.InterruptedIOException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.apache.http.annotation.ThreadSafe;
import org.apache.http.impl.nio.reactor.AbstractIOReactor;
import org.apache.http.nio.reactor.IOEventDispatch;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.nio.reactor.IOReactorExceptionHandler;
import org.apache.http.nio.reactor.IOSession;
import org.apache.http.util.Args;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
@ThreadSafe
public class BaseIOReactor
extends AbstractIOReactor {
    private final long timeoutCheckInterval;
    private final Set<IOSession> bufferingSessions = new HashSet<IOSession>();
    private long lastTimeoutCheck;
    private IOReactorExceptionHandler exceptionHandler = null;
    private IOEventDispatch eventDispatch = null;

    public BaseIOReactor(long selectTimeout) throws IOReactorException {
        this(selectTimeout, false);
    }

    public BaseIOReactor(long selectTimeout, boolean interestOpsQueueing) throws IOReactorException {
        super(selectTimeout, interestOpsQueueing);
        this.timeoutCheckInterval = selectTimeout;
        this.lastTimeoutCheck = System.currentTimeMillis();
    }

    @Override
    public void execute(IOEventDispatch eventDispatch) throws InterruptedIOException, IOReactorException {
        Args.notNull(eventDispatch, "Event dispatcher");
        this.eventDispatch = eventDispatch;
        this.execute();
    }

    public void setExceptionHandler(IOReactorExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    protected void handleRuntimeException(RuntimeException ex) {
        if (this.exceptionHandler == null || !this.exceptionHandler.handle(ex)) {
            throw ex;
        }
    }

    @Override
    protected void acceptable(SelectionKey key) {
    }

    @Override
    protected void connectable(SelectionKey key) {
    }

    @Override
    protected void readable(SelectionKey key) {
        IOSession session = this.getSession(key);
        try {
            this.eventDispatch.inputReady(session);
            if (session.hasBufferedInput()) {
                this.bufferingSessions.add(session);
            }
        }
        catch (CancelledKeyException ex) {
            this.queueClosedSession(session);
            key.attach(null);
        }
        catch (RuntimeException ex) {
            this.handleRuntimeException(ex);
        }
    }

    @Override
    protected void writable(SelectionKey key) {
        IOSession session = this.getSession(key);
        try {
            this.eventDispatch.outputReady(session);
        }
        catch (CancelledKeyException ex) {
            this.queueClosedSession(session);
            key.attach(null);
        }
        catch (RuntimeException ex) {
            this.handleRuntimeException(ex);
        }
    }

    @Override
    protected void validate(Set<SelectionKey> keys) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - this.lastTimeoutCheck >= this.timeoutCheckInterval) {
            this.lastTimeoutCheck = currentTime;
            if (keys != null) {
                for (SelectionKey key : keys) {
                    this.timeoutCheck(key, currentTime);
                }
            }
        }
        if (!this.bufferingSessions.isEmpty()) {
            Iterator<IOSession> it = this.bufferingSessions.iterator();
            while (it.hasNext()) {
                IOSession session = it.next();
                if (!session.hasBufferedInput()) {
                    it.remove();
                    continue;
                }
                try {
                    if ((session.getEventMask() & 1) <= 0) continue;
                    this.eventDispatch.inputReady(session);
                    if (session.hasBufferedInput()) continue;
                    it.remove();
                }
                catch (CancelledKeyException ex) {
                    it.remove();
                    this.queueClosedSession(session);
                }
                catch (RuntimeException ex) {
                    this.handleRuntimeException(ex);
                }
            }
        }
    }

    @Override
    protected void sessionCreated(SelectionKey key, IOSession session) {
        try {
            this.eventDispatch.connected(session);
        }
        catch (CancelledKeyException ex) {
            this.queueClosedSession(session);
        }
        catch (RuntimeException ex) {
            this.handleRuntimeException(ex);
        }
    }

    @Override
    protected void sessionTimedOut(IOSession session) {
        try {
            this.eventDispatch.timeout(session);
        }
        catch (CancelledKeyException ex) {
            this.queueClosedSession(session);
        }
        catch (RuntimeException ex) {
            this.handleRuntimeException(ex);
        }
    }

    @Override
    protected void sessionClosed(IOSession session) {
        try {
            this.eventDispatch.disconnected(session);
        }
        catch (CancelledKeyException ex) {
        }
        catch (RuntimeException ex) {
            this.handleRuntimeException(ex);
        }
    }
}

