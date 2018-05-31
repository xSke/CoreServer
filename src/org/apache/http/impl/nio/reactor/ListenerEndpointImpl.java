/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.impl.nio.reactor;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import org.apache.http.annotation.ThreadSafe;
import org.apache.http.impl.nio.reactor.ListenerEndpointClosedCallback;
import org.apache.http.nio.reactor.ListenerEndpoint;
import org.apache.http.util.Args;

@ThreadSafe
public class ListenerEndpointImpl
implements ListenerEndpoint {
    private volatile boolean completed;
    private volatile boolean closed;
    private volatile SelectionKey key;
    private volatile SocketAddress address;
    private volatile IOException exception;
    private final ListenerEndpointClosedCallback callback;

    public ListenerEndpointImpl(SocketAddress address, ListenerEndpointClosedCallback callback) {
        Args.notNull(address, "Address");
        this.address = address;
        this.callback = callback;
    }

    public SocketAddress getAddress() {
        return this.address;
    }

    public boolean isCompleted() {
        return this.completed;
    }

    public IOException getException() {
        return this.exception;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void waitFor() throws InterruptedException {
        if (this.completed) {
            return;
        }
        ListenerEndpointImpl listenerEndpointImpl = this;
        synchronized (listenerEndpointImpl) {
            while (!this.completed) {
                this.wait();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void completed(SocketAddress address) {
        Args.notNull(address, "Address");
        if (this.completed) {
            return;
        }
        this.completed = true;
        ListenerEndpointImpl listenerEndpointImpl = this;
        synchronized (listenerEndpointImpl) {
            this.address = address;
            this.notifyAll();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void failed(IOException exception) {
        if (exception == null) {
            return;
        }
        if (this.completed) {
            return;
        }
        this.completed = true;
        ListenerEndpointImpl listenerEndpointImpl = this;
        synchronized (listenerEndpointImpl) {
            this.exception = exception;
            this.notifyAll();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void cancel() {
        if (this.completed) {
            return;
        }
        this.completed = true;
        this.closed = true;
        ListenerEndpointImpl listenerEndpointImpl = this;
        synchronized (listenerEndpointImpl) {
            this.notifyAll();
        }
    }

    protected void setKey(SelectionKey key) {
        this.key = key;
    }

    public boolean isClosed() {
        return this.closed || this.key != null && !this.key.isValid();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void close() {
        Object channel;
        if (this.closed) {
            return;
        }
        this.completed = true;
        this.closed = true;
        if (this.key != null) {
            this.key.cancel();
            channel = this.key.channel();
            if (channel.isOpen()) {
                try {
                    channel.close();
                }
                catch (IOException ignore) {
                    // empty catch block
                }
            }
        }
        if (this.callback != null) {
            this.callback.endpointClosed(this);
        }
        channel = this;
        synchronized (channel) {
            this.notifyAll();
        }
    }
}

