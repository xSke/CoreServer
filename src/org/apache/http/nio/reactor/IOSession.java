/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.reactor;

import java.net.SocketAddress;
import java.nio.channels.ByteChannel;
import org.apache.http.nio.reactor.SessionBufferStatus;

public interface IOSession {
    public static final String ATTACHMENT_KEY = "http.session.attachment";
    public static final int ACTIVE = 0;
    public static final int CLOSING = 1;
    public static final int CLOSED = Integer.MAX_VALUE;

    public ByteChannel channel();

    public SocketAddress getRemoteAddress();

    public SocketAddress getLocalAddress();

    public int getEventMask();

    public void setEventMask(int var1);

    public void setEvent(int var1);

    public void clearEvent(int var1);

    public void close();

    public void shutdown();

    public int getStatus();

    public boolean isClosed();

    public int getSocketTimeout();

    public void setSocketTimeout(int var1);

    public void setBufferStatus(SessionBufferStatus var1);

    public boolean hasBufferedInput();

    public boolean hasBufferedOutput();

    public void setAttribute(String var1, Object var2);

    public Object getAttribute(String var1);

    public Object removeAttribute(String var1);
}

