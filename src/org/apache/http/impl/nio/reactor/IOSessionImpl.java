/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.impl.nio.reactor;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.channels.ByteChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.annotation.ThreadSafe;
import org.apache.http.impl.nio.reactor.InterestOpEntry;
import org.apache.http.impl.nio.reactor.InterestOpsCallback;
import org.apache.http.impl.nio.reactor.SessionClosedCallback;
import org.apache.http.nio.reactor.IOSession;
import org.apache.http.nio.reactor.SessionBufferStatus;
import org.apache.http.nio.reactor.SocketAccessor;
import org.apache.http.util.Args;

@ThreadSafe
public class IOSessionImpl
implements IOSession,
SocketAccessor {
    private final SelectionKey key;
    private final ByteChannel channel;
    private final Map<String, Object> attributes;
    private final InterestOpsCallback interestOpsCallback;
    private final SessionClosedCallback sessionClosedCallback;
    private volatile int status;
    private volatile int currentEventMask;
    private volatile SessionBufferStatus bufferStatus;
    private volatile int socketTimeout;
    private final long startedTime;
    private long lastReadTime;
    private long lastWriteTime;
    private long lastAccessTime;

    public IOSessionImpl(SelectionKey key, InterestOpsCallback interestOpsCallback, SessionClosedCallback sessionClosedCallback) {
        long now;
        Args.notNull(key, "Selection key");
        this.key = key;
        this.channel = (ByteChannel)((Object)this.key.channel());
        this.interestOpsCallback = interestOpsCallback;
        this.sessionClosedCallback = sessionClosedCallback;
        this.attributes = Collections.synchronizedMap(new HashMap());
        this.currentEventMask = key.interestOps();
        this.socketTimeout = 0;
        this.status = 0;
        this.startedTime = now = System.currentTimeMillis();
        this.lastReadTime = now;
        this.lastWriteTime = now;
        this.lastAccessTime = now;
    }

    public IOSessionImpl(SelectionKey key, SessionClosedCallback sessionClosedCallback) {
        this(key, null, sessionClosedCallback);
    }

    public ByteChannel channel() {
        return this.channel;
    }

    public SocketAddress getLocalAddress() {
        if (this.channel instanceof SocketChannel) {
            return ((SocketChannel)this.channel).socket().getLocalSocketAddress();
        }
        return null;
    }

    public SocketAddress getRemoteAddress() {
        if (this.channel instanceof SocketChannel) {
            return ((SocketChannel)this.channel).socket().getRemoteSocketAddress();
        }
        return null;
    }

    public synchronized int getEventMask() {
        return this.interestOpsCallback != null ? this.currentEventMask : this.key.interestOps();
    }

    public synchronized void setEventMask(int ops) {
        if (this.status == Integer.MAX_VALUE) {
            return;
        }
        if (this.interestOpsCallback != null) {
            this.currentEventMask = ops;
            InterestOpEntry entry = new InterestOpEntry(this.key, this.currentEventMask);
            this.interestOpsCallback.addInterestOps(entry);
        } else {
            this.key.interestOps(ops);
        }
        this.key.selector().wakeup();
    }

    public synchronized void setEvent(int op) {
        if (this.status == Integer.MAX_VALUE) {
            return;
        }
        if (this.interestOpsCallback != null) {
            this.currentEventMask |= op;
            InterestOpEntry entry = new InterestOpEntry(this.key, this.currentEventMask);
            this.interestOpsCallback.addInterestOps(entry);
        } else {
            int ops = this.key.interestOps();
            this.key.interestOps(ops | op);
        }
        this.key.selector().wakeup();
    }

    public synchronized void clearEvent(int op) {
        if (this.status == Integer.MAX_VALUE) {
            return;
        }
        if (this.interestOpsCallback != null) {
            this.currentEventMask &= ~ op;
            InterestOpEntry entry = new InterestOpEntry(this.key, this.currentEventMask);
            this.interestOpsCallback.addInterestOps(entry);
        } else {
            int ops = this.key.interestOps();
            this.key.interestOps(ops & ~ op);
        }
        this.key.selector().wakeup();
    }

    public int getSocketTimeout() {
        return this.socketTimeout;
    }

    public synchronized void setSocketTimeout(int timeout) {
        this.socketTimeout = timeout;
        this.lastAccessTime = System.currentTimeMillis();
    }

    public synchronized void close() {
        if (this.status == Integer.MAX_VALUE) {
            return;
        }
        this.status = Integer.MAX_VALUE;
        this.key.cancel();
        try {
            this.key.channel().close();
        }
        catch (IOException ex) {
            // empty catch block
        }
        if (this.sessionClosedCallback != null) {
            this.sessionClosedCallback.sessionClosed(this);
        }
        if (this.key.selector().isOpen()) {
            this.key.selector().wakeup();
        }
    }

    public int getStatus() {
        return this.status;
    }

    public boolean isClosed() {
        return this.status == Integer.MAX_VALUE;
    }

    public void shutdown() {
        this.close();
    }

    public boolean hasBufferedInput() {
        SessionBufferStatus buffStatus = this.bufferStatus;
        return buffStatus != null && buffStatus.hasBufferedInput();
    }

    public boolean hasBufferedOutput() {
        SessionBufferStatus buffStatus = this.bufferStatus;
        return buffStatus != null && buffStatus.hasBufferedOutput();
    }

    public void setBufferStatus(SessionBufferStatus bufferStatus) {
        this.bufferStatus = bufferStatus;
    }

    public Object getAttribute(String name) {
        return this.attributes.get(name);
    }

    public Object removeAttribute(String name) {
        return this.attributes.remove(name);
    }

    public void setAttribute(String name, Object obj) {
        this.attributes.put(name, obj);
    }

    public synchronized long getStartedTime() {
        return this.startedTime;
    }

    public synchronized long getLastReadTime() {
        return this.lastReadTime;
    }

    public synchronized long getLastWriteTime() {
        return this.lastWriteTime;
    }

    public synchronized long getLastAccessTime() {
        return this.lastAccessTime;
    }

    synchronized void resetLastRead() {
        long now;
        this.lastReadTime = now = System.currentTimeMillis();
        this.lastAccessTime = now;
    }

    synchronized void resetLastWrite() {
        long now;
        this.lastWriteTime = now = System.currentTimeMillis();
        this.lastAccessTime = now;
    }

    private static void formatOps(StringBuilder buffer, int ops) {
        if ((ops & 1) > 0) {
            buffer.append('r');
        }
        if ((ops & 4) > 0) {
            buffer.append('w');
        }
        if ((ops & 16) > 0) {
            buffer.append('a');
        }
        if ((ops & 8) > 0) {
            buffer.append('c');
        }
    }

    private static void formatAddress(StringBuilder buffer, SocketAddress socketAddress) {
        if (socketAddress instanceof InetSocketAddress) {
            InetSocketAddress addr = (InetSocketAddress)socketAddress;
            buffer.append(addr.getAddress() != null ? addr.getAddress().getHostAddress() : addr.getAddress()).append(':').append(addr.getPort());
        } else {
            buffer.append(socketAddress);
        }
    }

    public synchronized String toString() {
        StringBuilder buffer = new StringBuilder();
        SocketAddress remoteAddress = this.getRemoteAddress();
        SocketAddress localAddress = this.getLocalAddress();
        if (remoteAddress != null && localAddress != null) {
            IOSessionImpl.formatAddress(buffer, localAddress);
            buffer.append("<->");
            IOSessionImpl.formatAddress(buffer, remoteAddress);
        }
        buffer.append("[");
        switch (this.status) {
            case 0: {
                buffer.append("ACTIVE");
                break;
            }
            case 1: {
                buffer.append("CLOSING");
                break;
            }
            case Integer.MAX_VALUE: {
                buffer.append("CLOSED");
            }
        }
        buffer.append("][");
        if (this.key.isValid()) {
            IOSessionImpl.formatOps(buffer, this.interestOpsCallback != null ? this.currentEventMask : this.key.interestOps());
            buffer.append(":");
            IOSessionImpl.formatOps(buffer, this.key.readyOps());
        }
        buffer.append("]");
        return buffer.toString();
    }

    public Socket getSocket() {
        if (this.channel instanceof SocketChannel) {
            return ((SocketChannel)this.channel).socket();
        }
        return null;
    }
}

