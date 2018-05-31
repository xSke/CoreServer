/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.reactor.ssl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import org.apache.http.annotation.ThreadSafe;
import org.apache.http.nio.reactor.IOSession;
import org.apache.http.nio.reactor.SessionBufferStatus;
import org.apache.http.nio.reactor.SocketAccessor;
import org.apache.http.nio.reactor.ssl.SSLMode;
import org.apache.http.nio.reactor.ssl.SSLSetupHandler;
import org.apache.http.util.Args;
import org.apache.http.util.Asserts;

@ThreadSafe
public class SSLIOSession
implements IOSession,
SessionBufferStatus,
SocketAccessor {
    public static final String SESSION_KEY = "http.session.ssl";
    private final IOSession session;
    private final SSLEngine sslEngine;
    private final ByteBuffer inEncrypted;
    private final ByteBuffer outEncrypted;
    private final ByteBuffer inPlain;
    private final ByteBuffer outPlain;
    private final InternalByteChannel channel;
    private final SSLSetupHandler handler;
    private int appEventMask;
    private SessionBufferStatus appBufferStatus;
    private boolean endOfStream;
    private volatile SSLMode sslMode;
    private volatile int status;
    private volatile boolean initialized;

    public SSLIOSession(IOSession session, SSLMode sslMode, SSLContext sslContext, SSLSetupHandler handler) {
        Args.notNull(session, "IO session");
        Args.notNull(sslContext, "SSL context");
        this.session = session;
        this.sslMode = sslMode;
        this.appEventMask = session.getEventMask();
        this.channel = new InternalByteChannel();
        this.handler = handler;
        this.session.setBufferStatus(this);
        if (this.sslMode == SSLMode.CLIENT) {
            SocketAddress address = session.getRemoteAddress();
            if (address instanceof InetSocketAddress) {
                String hostname = ((InetSocketAddress)address).getHostName();
                int port = ((InetSocketAddress)address).getPort();
                this.sslEngine = sslContext.createSSLEngine(hostname, port);
            } else {
                this.sslEngine = sslContext.createSSLEngine();
            }
        } else {
            this.sslEngine = sslContext.createSSLEngine();
        }
        int netBuffersize = this.sslEngine.getSession().getPacketBufferSize();
        this.inEncrypted = ByteBuffer.allocate(netBuffersize);
        this.outEncrypted = ByteBuffer.allocate(netBuffersize);
        int appBuffersize = this.sslEngine.getSession().getApplicationBufferSize();
        this.inPlain = ByteBuffer.allocate(appBuffersize);
        this.outPlain = ByteBuffer.allocate(appBuffersize);
    }

    protected SSLSetupHandler getSSLSetupHandler() {
        return this.handler;
    }

    public boolean isInitialized() {
        return this.initialized;
    }

    @Deprecated
    public synchronized void initialize(SSLMode sslMode) throws SSLException {
        this.sslMode = sslMode;
        this.initialize();
    }

    public synchronized void initialize() throws SSLException {
        Asserts.check(!this.initialized, "SSL I/O session already initialized");
        if (this.status >= 1) {
            return;
        }
        switch (this.sslMode) {
            case CLIENT: {
                this.sslEngine.setUseClientMode(true);
                break;
            }
            case SERVER: {
                this.sslEngine.setUseClientMode(false);
            }
        }
        if (this.handler != null) {
            this.handler.initalize(this.sslEngine);
        }
        this.initialized = true;
        this.sslEngine.beginHandshake();
        this.doHandshake();
    }

    public synchronized SSLSession getSSLSession() {
        return this.sslEngine.getSession();
    }

    private SSLException convert(RuntimeException ex) {
        Throwable cause = ex.getCause();
        if (cause == null) {
            cause = ex;
        }
        return new SSLException(cause);
    }

    private SSLEngineResult doWrap(ByteBuffer src, ByteBuffer dst) throws SSLException {
        try {
            return this.sslEngine.wrap(src, dst);
        }
        catch (RuntimeException ex) {
            throw this.convert(ex);
        }
    }

    private SSLEngineResult doUnwrap(ByteBuffer src, ByteBuffer dst) throws SSLException {
        try {
            return this.sslEngine.unwrap(src, dst);
        }
        catch (RuntimeException ex) {
            throw this.convert(ex);
        }
    }

    private void doRunTask() throws SSLException {
        try {
            Runnable r = this.sslEngine.getDelegatedTask();
            if (r != null) {
                r.run();
            }
        }
        catch (RuntimeException ex) {
            throw this.convert(ex);
        }
    }

    private void doHandshake() throws SSLException {
        boolean handshaking = true;
        SSLEngineResult result = null;
        while (handshaking) {
            switch (this.sslEngine.getHandshakeStatus()) {
                case NEED_WRAP: {
                    this.outPlain.flip();
                    result = this.doWrap(this.outPlain, this.outEncrypted);
                    this.outPlain.compact();
                    if (result.getStatus() == SSLEngineResult.Status.OK) break;
                    handshaking = false;
                    break;
                }
                case NEED_UNWRAP: {
                    this.inEncrypted.flip();
                    result = this.doUnwrap(this.inEncrypted, this.inPlain);
                    this.inEncrypted.compact();
                    if (!this.inEncrypted.hasRemaining() && result.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.NEED_UNWRAP) {
                        throw new SSLException("Input buffer is full");
                    }
                    if (this.status >= 1) {
                        this.inPlain.clear();
                    }
                    if (result.getStatus() == SSLEngineResult.Status.OK) break;
                    handshaking = false;
                    break;
                }
                case NEED_TASK: {
                    this.doRunTask();
                    break;
                }
                case NOT_HANDSHAKING: {
                    handshaking = false;
                    break;
                }
            }
        }
        if (result != null && result.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.FINISHED && this.handler != null) {
            this.handler.verify(this.session, this.sslEngine.getSession());
        }
    }

    private void updateEventMask() {
        int oldMask;
        if (this.status == 1 && this.sslEngine.isOutboundDone() && (this.endOfStream || this.sslEngine.isInboundDone())) {
            this.status = Integer.MAX_VALUE;
        }
        if (this.status == 0 && this.endOfStream && this.sslEngine.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.NEED_UNWRAP) {
            this.status = Integer.MAX_VALUE;
        }
        if (this.status == Integer.MAX_VALUE) {
            this.session.close();
            return;
        }
        int newMask = oldMask = this.session.getEventMask();
        switch (this.sslEngine.getHandshakeStatus()) {
            case NEED_WRAP: {
                newMask = 5;
                break;
            }
            case NEED_UNWRAP: {
                newMask = 1;
                break;
            }
            case NOT_HANDSHAKING: {
                newMask = this.appEventMask;
                break;
            }
            case NEED_TASK: {
                break;
            }
        }
        if (this.outEncrypted.position() > 0) {
            newMask |= 4;
        }
        if (oldMask != newMask) {
            this.session.setEventMask(newMask);
        }
    }

    private int sendEncryptedData() throws IOException {
        this.outEncrypted.flip();
        int bytesWritten = this.session.channel().write(this.outEncrypted);
        this.outEncrypted.compact();
        return bytesWritten;
    }

    private int receiveEncryptedData() throws IOException {
        if (this.endOfStream) {
            return -1;
        }
        return this.session.channel().read(this.inEncrypted);
    }

    private boolean decryptData() throws SSLException {
        boolean decrypted = false;
        while (this.inEncrypted.position() > 0) {
            this.inEncrypted.flip();
            SSLEngineResult result = this.doUnwrap(this.inEncrypted, this.inPlain);
            this.inEncrypted.compact();
            if (!this.inEncrypted.hasRemaining() && result.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.NEED_UNWRAP) {
                throw new SSLException("Input buffer is full");
            }
            if (result.getStatus() != SSLEngineResult.Status.OK) break;
            decrypted = true;
            if (result.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING && !this.endOfStream) continue;
            break;
        }
        return decrypted;
    }

    public synchronized boolean isAppInputReady() throws IOException {
        do {
            int bytesRead;
            if ((bytesRead = this.receiveEncryptedData()) == -1) {
                this.endOfStream = true;
            }
            this.doHandshake();
            SSLEngineResult.HandshakeStatus status = this.sslEngine.getHandshakeStatus();
            if (status != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING && status != SSLEngineResult.HandshakeStatus.FINISHED) continue;
            this.decryptData();
        } while (this.sslEngine.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.NEED_TASK);
        return (this.appEventMask & 1) > 0 && (this.inPlain.position() > 0 || this.appBufferStatus != null && this.appBufferStatus.hasBufferedInput() || this.endOfStream && this.status == 0);
    }

    public synchronized boolean isAppOutputReady() throws IOException {
        return (this.appEventMask & 4) > 0 && this.status == 0 && this.sslEngine.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING;
    }

    public synchronized void inboundTransport() throws IOException {
        this.updateEventMask();
    }

    public synchronized void outboundTransport() throws IOException {
        this.sendEncryptedData();
        this.doHandshake();
        this.updateEventMask();
    }

    public synchronized boolean isInboundDone() {
        return this.sslEngine.isInboundDone();
    }

    public synchronized boolean isOutboundDone() {
        return this.sslEngine.isOutboundDone();
    }

    private synchronized int writePlain(ByteBuffer src) throws SSLException {
        Args.notNull(src, "Byte buffer");
        if (this.status != 0) {
            return -1;
        }
        if (this.outPlain.position() > 0) {
            this.outPlain.flip();
            this.doWrap(this.outPlain, this.outEncrypted);
            this.outPlain.compact();
        }
        if (this.outPlain.position() == 0) {
            SSLEngineResult result = this.doWrap(src, this.outEncrypted);
            if (result.getStatus() == SSLEngineResult.Status.CLOSED) {
                this.status = Integer.MAX_VALUE;
            }
            return result.bytesConsumed();
        }
        return 0;
    }

    private synchronized int readPlain(ByteBuffer dst) {
        Args.notNull(dst, "Byte buffer");
        if (this.inPlain.position() > 0) {
            this.inPlain.flip();
            int n = Math.min(this.inPlain.remaining(), dst.remaining());
            for (int i = 0; i < n; ++i) {
                dst.put(this.inPlain.get());
            }
            this.inPlain.compact();
            return n;
        }
        if (this.endOfStream) {
            return -1;
        }
        return 0;
    }

    public synchronized void close() {
        if (this.status >= 1) {
            return;
        }
        this.status = 1;
        this.sslEngine.closeOutbound();
        this.updateEventMask();
    }

    public synchronized void shutdown() {
        if (this.status == Integer.MAX_VALUE) {
            return;
        }
        this.status = Integer.MAX_VALUE;
        this.session.shutdown();
    }

    public int getStatus() {
        return this.status;
    }

    public boolean isClosed() {
        return this.status >= 1 || this.session.isClosed();
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

    public synchronized int getEventMask() {
        return this.appEventMask;
    }

    public synchronized void setEventMask(int ops) {
        this.appEventMask = ops;
        this.updateEventMask();
    }

    public synchronized void setEvent(int op) {
        this.appEventMask |= op;
        this.updateEventMask();
    }

    public synchronized void clearEvent(int op) {
        this.appEventMask &= ~ op;
        this.updateEventMask();
    }

    public int getSocketTimeout() {
        return this.session.getSocketTimeout();
    }

    public void setSocketTimeout(int timeout) {
        this.session.setSocketTimeout(timeout);
    }

    public synchronized boolean hasBufferedInput() {
        return this.appBufferStatus != null && this.appBufferStatus.hasBufferedInput() || this.inEncrypted.position() > 0 || this.inPlain.position() > 0;
    }

    public synchronized boolean hasBufferedOutput() {
        return this.appBufferStatus != null && this.appBufferStatus.hasBufferedOutput() || this.outEncrypted.position() > 0 || this.outPlain.position() > 0;
    }

    public synchronized void setBufferStatus(SessionBufferStatus status) {
        this.appBufferStatus = status;
    }

    public Object getAttribute(String name) {
        return this.session.getAttribute(name);
    }

    public Object removeAttribute(String name) {
        return this.session.removeAttribute(name);
    }

    public void setAttribute(String name, Object obj) {
        this.session.setAttribute(name, obj);
    }

    private static void formatOps(StringBuilder buffer, int ops) {
        if ((ops & 1) > 0) {
            buffer.append('r');
        }
        if ((ops & 4) > 0) {
            buffer.append('w');
        }
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(this.session);
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
        SSLIOSession.formatOps(buffer, this.appEventMask);
        buffer.append("][");
        buffer.append((Object)this.sslEngine.getHandshakeStatus());
        if (this.sslEngine.isInboundDone()) {
            buffer.append("][inbound done][");
        }
        if (this.sslEngine.isOutboundDone()) {
            buffer.append("][outbound done][");
        }
        if (this.endOfStream) {
            buffer.append("][EOF][");
        }
        buffer.append("][");
        buffer.append(this.inEncrypted.position());
        buffer.append("][");
        buffer.append(this.inPlain.position());
        buffer.append("][");
        buffer.append(this.outEncrypted.position());
        buffer.append("][");
        buffer.append(this.outPlain.position());
        buffer.append("]");
        return buffer.toString();
    }

    public Socket getSocket() {
        if (this.session instanceof SocketAccessor) {
            return ((SocketAccessor)((Object)this.session)).getSocket();
        }
        return null;
    }

    private class InternalByteChannel
    implements ByteChannel {
        private InternalByteChannel() {
        }

        public int write(ByteBuffer src) throws IOException {
            return SSLIOSession.this.writePlain(src);
        }

        public int read(ByteBuffer dst) throws IOException {
            return SSLIOSession.this.readPlain(dst);
        }

        public void close() throws IOException {
            SSLIOSession.this.close();
        }

        public boolean isOpen() {
            return !SSLIOSession.this.isClosed();
        }
    }

}

