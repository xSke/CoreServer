/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.impl.nio.reactor;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import org.apache.http.annotation.ThreadSafe;
import org.apache.http.nio.reactor.IOSession;
import org.apache.http.nio.reactor.SessionRequest;
import org.apache.http.nio.reactor.SessionRequestCallback;
import org.apache.http.util.Args;

@ThreadSafe
public class SessionRequestImpl
implements SessionRequest {
    private volatile boolean completed;
    private volatile SelectionKey key;
    private final SocketAddress remoteAddress;
    private final SocketAddress localAddress;
    private final Object attachment;
    private final SessionRequestCallback callback;
    private volatile int connectTimeout;
    private volatile IOSession session = null;
    private volatile IOException exception = null;

    public SessionRequestImpl(SocketAddress remoteAddress, SocketAddress localAddress, Object attachment, SessionRequestCallback callback) {
        Args.notNull(remoteAddress, "Remote address");
        this.remoteAddress = remoteAddress;
        this.localAddress = localAddress;
        this.attachment = attachment;
        this.callback = callback;
        this.connectTimeout = 0;
    }

    public SocketAddress getRemoteAddress() {
        return this.remoteAddress;
    }

    public SocketAddress getLocalAddress() {
        return this.localAddress;
    }

    public Object getAttachment() {
        return this.attachment;
    }

    public boolean isCompleted() {
        return this.completed;
    }

    protected void setKey(SelectionKey key) {
        this.key = key;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void waitFor() throws InterruptedException {
        if (this.completed) {
            return;
        }
        SessionRequestImpl sessionRequestImpl = this;
        synchronized (sessionRequestImpl) {
            while (!this.completed) {
                this.wait();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public IOSession getSession() {
        SessionRequestImpl sessionRequestImpl = this;
        synchronized (sessionRequestImpl) {
            return this.session;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public IOException getException() {
        SessionRequestImpl sessionRequestImpl = this;
        synchronized (sessionRequestImpl) {
            return this.exception;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void completed(IOSession session) {
        Args.notNull(session, "Session");
        if (this.completed) {
            return;
        }
        this.completed = true;
        SessionRequestImpl sessionRequestImpl = this;
        synchronized (sessionRequestImpl) {
            this.session = session;
            if (this.callback != null) {
                this.callback.completed(this);
            }
            this.notifyAll();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void failed(IOException exception) {
        Object channel;
        if (exception == null) {
            return;
        }
        if (this.completed) {
            return;
        }
        this.completed = true;
        SelectionKey key = this.key;
        if (key != null) {
            key.cancel();
            channel = key.channel();
            if (channel.isOpen()) {
                try {
                    channel.close();
                }
                catch (IOException ignore) {
                    // empty catch block
                }
            }
        }
        channel = this;
        synchronized (channel) {
            this.exception = exception;
            if (this.callback != null) {
                this.callback.failed(this);
            }
            this.notifyAll();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void timeout() {
        Object channel;
        if (this.completed) {
            return;
        }
        this.completed = true;
        SelectionKey key = this.key;
        if (key != null) {
            key.cancel();
            channel = key.channel();
            if (channel.isOpen()) {
                try {
                    channel.close();
                }
                catch (IOException ignore) {
                    // empty catch block
                }
            }
        }
        channel = this;
        synchronized (channel) {
            if (this.callback != null) {
                this.callback.timeout(this);
            }
        }
    }

    public int getConnectTimeout() {
        return this.connectTimeout;
    }

    public void setConnectTimeout(int timeout) {
        if (this.connectTimeout != timeout) {
            this.connectTimeout = timeout;
            SelectionKey key = this.key;
            if (key != null) {
                key.selector().wakeup();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void cancel() {
        Object channel;
        if (this.completed) {
            return;
        }
        this.completed = true;
        SelectionKey key = this.key;
        if (key != null) {
            key.cancel();
            channel = key.channel();
            if (channel.isOpen()) {
                try {
                    channel.close();
                }
                catch (IOException ignore) {
                    // empty catch block
                }
            }
        }
        channel = this;
        synchronized (channel) {
            if (this.callback != null) {
                this.callback.cancelled(this);
            }
            this.notifyAll();
        }
    }
}

