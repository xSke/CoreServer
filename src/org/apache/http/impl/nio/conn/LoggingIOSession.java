/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.impl.nio.conn;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import org.apache.commons.logging.Log;
import org.apache.http.impl.nio.conn.Wire;
import org.apache.http.nio.reactor.IOSession;
import org.apache.http.nio.reactor.SessionBufferStatus;

class LoggingIOSession
implements IOSession {
    private final IOSession session;
    private final ByteChannel channel;
    private final String id;
    private final Log log;
    private final Wire wirelog;

    public LoggingIOSession(IOSession session, String id, Log log, Log wirelog) {
        this.session = session;
        this.channel = new LoggingByteChannel();
        this.id = id;
        this.log = log;
        this.wirelog = new Wire(wirelog, this.id);
    }

    public ByteChannel channel() {
        return this.channel;
    }

    public SocketAddress getLocalAddress() {
        return this.session.getLocalAddress();
    }

    public SocketAddress getRemoteAddress() {
        return this.session.getRemoteAddress();
    }

    public int getEventMask() {
        return this.session.getEventMask();
    }

    private static String formatOps(int ops) {
        StringBuilder buffer = new StringBuilder(6);
        buffer.append('[');
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
        buffer.append(']');
        return buffer.toString();
    }

    public void setEventMask(int ops) {
        this.session.setEventMask(ops);
        if (this.log.isDebugEnabled()) {
            this.log.debug(this.id + " " + this.session + ": Event mask set " + LoggingIOSession.formatOps(ops));
        }
    }

    public void setEvent(int op) {
        this.session.setEvent(op);
        if (this.log.isDebugEnabled()) {
            this.log.debug(this.id + " " + this.session + ": Event set " + LoggingIOSession.formatOps(op));
        }
    }

    public void clearEvent(int op) {
        this.session.clearEvent(op);
        if (this.log.isDebugEnabled()) {
            this.log.debug(this.id + " " + this.session + ": Event cleared " + LoggingIOSession.formatOps(op));
        }
    }

    public void close() {
        if (this.log.isDebugEnabled()) {
            this.log.debug(this.id + " " + this.session + ": Close");
        }
        this.session.close();
    }

    public int getStatus() {
        return this.session.getStatus();
    }

    public boolean isClosed() {
        return this.session.isClosed();
    }

    public void shutdown() {
        if (this.log.isDebugEnabled()) {
            this.log.debug(this.id + " " + this.session + ": Shutdown");
        }
        this.session.shutdown();
    }

    public int getSocketTimeout() {
        return this.session.getSocketTimeout();
    }

    public void setSocketTimeout(int timeout) {
        if (this.log.isDebugEnabled()) {
            this.log.debug(this.id + " " + this.session + ": Set timeout " + timeout);
        }
        this.session.setSocketTimeout(timeout);
    }

    public void setBufferStatus(SessionBufferStatus status) {
        this.session.setBufferStatus(status);
    }

    public boolean hasBufferedInput() {
        return this.session.hasBufferedInput();
    }

    public boolean hasBufferedOutput() {
        return this.session.hasBufferedOutput();
    }

    public Object getAttribute(String name) {
        return this.session.getAttribute(name);
    }

    public void setAttribute(String name, Object obj) {
        if (this.log.isDebugEnabled()) {
            this.log.debug(this.id + " " + this.session + ": Set attribute " + name);
        }
        this.session.setAttribute(name, obj);
    }

    public Object removeAttribute(String name) {
        if (this.log.isDebugEnabled()) {
            this.log.debug(this.id + " " + this.session + ": Remove attribute " + name);
        }
        return this.session.removeAttribute(name);
    }

    public String toString() {
        return this.id + " " + this.session.toString();
    }

    class LoggingByteChannel
    implements ByteChannel {
        LoggingByteChannel() {
        }

        public int read(ByteBuffer dst) throws IOException {
            int bytesRead = LoggingIOSession.this.session.channel().read(dst);
            if (LoggingIOSession.this.log.isDebugEnabled()) {
                LoggingIOSession.this.log.debug(LoggingIOSession.this.id + " " + LoggingIOSession.this.session + ": " + bytesRead + " bytes read");
            }
            if (bytesRead > 0 && LoggingIOSession.this.wirelog.isEnabled()) {
                ByteBuffer b = dst.duplicate();
                int p = b.position();
                b.limit(p);
                b.position(p - bytesRead);
                LoggingIOSession.this.wirelog.input(b);
            }
            return bytesRead;
        }

        public int write(ByteBuffer src) throws IOException {
            int byteWritten = LoggingIOSession.this.session.channel().write(src);
            if (LoggingIOSession.this.log.isDebugEnabled()) {
                LoggingIOSession.this.log.debug(LoggingIOSession.this.id + " " + LoggingIOSession.this.session + ": " + byteWritten + " bytes written");
            }
            if (byteWritten > 0 && LoggingIOSession.this.wirelog.isEnabled()) {
                ByteBuffer b = src.duplicate();
                int p = b.position();
                b.limit(p);
                b.position(p - byteWritten);
                LoggingIOSession.this.wirelog.output(b);
            }
            return byteWritten;
        }

        public void close() throws IOException {
            if (LoggingIOSession.this.log.isDebugEnabled()) {
                LoggingIOSession.this.log.debug(LoggingIOSession.this.id + " " + LoggingIOSession.this.session + ": Channel close");
            }
            LoggingIOSession.this.session.channel().close();
        }

        public boolean isOpen() {
            return LoggingIOSession.this.session.channel().isOpen();
        }
    }

}

