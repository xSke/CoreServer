/*
 * Decompiled with CFR 0_129.
 * 
 * Could not load the following classes:
 *  org.apache.tomcat.jni.Buffer
 *  org.apache.tomcat.jni.SSL
 */
package io.netty.handler.ssl;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.handler.ssl.ApplicationProtocolAccessor;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.CipherSuiteConverter;
import io.netty.handler.ssl.OpenSsl;
import io.netty.handler.ssl.OpenSslApplicationProtocolNegotiator;
import io.netty.handler.ssl.OpenSslContext;
import io.netty.handler.ssl.OpenSslEngineMap;
import io.netty.handler.ssl.OpenSslJavaxX509Certificate;
import io.netty.handler.ssl.OpenSslSessionContext;
import io.netty.handler.ssl.OpenSslX509Certificate;
import io.netty.handler.ssl.SslParametersUtils;
import io.netty.util.internal.EmptyArrays;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;
import java.security.AlgorithmConstraints;
import java.security.Principal;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSessionBindingEvent;
import javax.net.ssl.SSLSessionBindingListener;
import javax.net.ssl.SSLSessionContext;
import javax.security.auth.x500.X500Principal;
import javax.security.cert.X509Certificate;
import org.apache.tomcat.jni.Buffer;
import org.apache.tomcat.jni.SSL;

public final class OpenSslEngine
extends SSLEngine {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(OpenSslEngine.class);
    private static final Certificate[] EMPTY_CERTIFICATES = EmptyArrays.EMPTY_CERTIFICATES;
    private static final X509Certificate[] EMPTY_X509_CERTIFICATES = EmptyArrays.EMPTY_JAVAX_X509_CERTIFICATES;
    private static final SSLException ENGINE_CLOSED = new SSLException("engine closed");
    private static final SSLException RENEGOTIATION_UNSUPPORTED = new SSLException("renegotiation unsupported");
    private static final SSLException ENCRYPTED_PACKET_OVERSIZED = new SSLException("encrypted packet oversized");
    private static final int MAX_PLAINTEXT_LENGTH = 16384;
    private static final int MAX_COMPRESSED_LENGTH = 17408;
    private static final int MAX_CIPHERTEXT_LENGTH = 18432;
    private static final String PROTOCOL_SSL_V2_HELLO = "SSLv2Hello";
    private static final String PROTOCOL_SSL_V2 = "SSLv2";
    private static final String PROTOCOL_SSL_V3 = "SSLv3";
    private static final String PROTOCOL_TLS_V1 = "TLSv1";
    private static final String PROTOCOL_TLS_V1_1 = "TLSv1.1";
    private static final String PROTOCOL_TLS_V1_2 = "TLSv1.2";
    private static final String[] SUPPORTED_PROTOCOLS;
    private static final Set<String> SUPPORTED_PROTOCOLS_SET;
    static final int MAX_ENCRYPTED_PACKET_LENGTH = 18713;
    static final int MAX_ENCRYPTION_OVERHEAD_LENGTH = 2329;
    private static final AtomicIntegerFieldUpdater<OpenSslEngine> DESTROYED_UPDATER;
    private static final String INVALID_CIPHER = "SSL_NULL_WITH_NULL_NULL";
    private static final long EMPTY_ADDR;
    private static final SSLEngineResult NEED_UNWRAP_OK;
    private static final SSLEngineResult NEED_UNWRAP_CLOSED;
    private static final SSLEngineResult NEED_WRAP_OK;
    private static final SSLEngineResult NEED_WRAP_CLOSED;
    private static final SSLEngineResult CLOSED_NOT_HANDSHAKING;
    private long ssl;
    private long networkBIO;
    private HandshakeState handshakeState = HandshakeState.NOT_STARTED;
    private boolean receivedShutdown;
    private volatile int destroyed;
    private volatile ClientAuthMode clientAuth = ClientAuthMode.NONE;
    private volatile String endPointIdentificationAlgorithm;
    private volatile Object algorithmConstraints;
    private boolean isInboundDone;
    private boolean isOutboundDone;
    private boolean engineClosed;
    private final boolean clientMode;
    private final ByteBufAllocator alloc;
    private final OpenSslEngineMap engineMap;
    private final OpenSslApplicationProtocolNegotiator apn;
    private final boolean rejectRemoteInitiatedRenegation;
    private final OpenSslSession session;
    SSLHandshakeException handshakeException;

    @Deprecated
    public OpenSslEngine(long sslCtx, ByteBufAllocator alloc, String fallbackApplicationProtocol) {
        this(sslCtx, alloc, false, null, OpenSslContext.NONE_PROTOCOL_NEGOTIATOR, OpenSslEngineMap.EMPTY, false);
    }

    OpenSslEngine(long sslCtx, ByteBufAllocator alloc, boolean clientMode, OpenSslSessionContext sessionContext, OpenSslApplicationProtocolNegotiator apn, OpenSslEngineMap engineMap, boolean rejectRemoteInitiatedRenegation) {
        this(sslCtx, alloc, clientMode, sessionContext, apn, engineMap, rejectRemoteInitiatedRenegation, null, -1);
    }

    OpenSslEngine(long sslCtx, ByteBufAllocator alloc, boolean clientMode, OpenSslSessionContext sessionContext, OpenSslApplicationProtocolNegotiator apn, OpenSslEngineMap engineMap, boolean rejectRemoteInitiatedRenegation, String peerHost, int peerPort) {
        super(peerHost, peerPort);
        OpenSsl.ensureAvailability();
        if (sslCtx == 0L) {
            throw new NullPointerException("sslCtx");
        }
        this.alloc = ObjectUtil.checkNotNull(alloc, "alloc");
        this.apn = ObjectUtil.checkNotNull(apn, "apn");
        this.ssl = SSL.newSSL((long)sslCtx, (boolean)(!clientMode));
        this.session = new OpenSslSession(this.ssl, sessionContext);
        this.networkBIO = SSL.makeNetworkBIO((long)this.ssl);
        this.clientMode = clientMode;
        this.engineMap = engineMap;
        this.rejectRemoteInitiatedRenegation = rejectRemoteInitiatedRenegation;
    }

    @Override
    public SSLSession getHandshakeSession() {
        if (this.handshakeState != HandshakeState.NOT_STARTED) {
            return this.session;
        }
        return null;
    }

    public synchronized long sslPointer() {
        return this.ssl;
    }

    public synchronized void shutdown() {
        if (DESTROYED_UPDATER.compareAndSet(this, 0, 1)) {
            this.engineMap.remove(this.ssl);
            SSL.freeSSL((long)this.ssl);
            SSL.freeBIO((long)this.networkBIO);
            this.networkBIO = 0L;
            this.ssl = 0L;
            this.engineClosed = true;
            this.isOutboundDone = true;
            this.isInboundDone = true;
        }
        SSL.clearError();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private int writePlaintextData(ByteBuffer src) {
        int sslWrote;
        int pos = src.position();
        int limit = src.limit();
        int len = Math.min(limit - pos, 16384);
        if (src.isDirect()) {
            long addr = Buffer.address((ByteBuffer)src) + (long)pos;
            sslWrote = SSL.writeToSSL((long)this.ssl, (long)addr, (int)len);
            if (sslWrote > 0) {
                src.position(pos + sslWrote);
            }
        } else {
            ByteBuf buf = this.alloc.directBuffer(len);
            try {
                long addr = OpenSslEngine.memoryAddress(buf);
                src.limit(pos + len);
                buf.setBytes(0, src);
                src.limit(limit);
                sslWrote = SSL.writeToSSL((long)this.ssl, (long)addr, (int)len);
                if (sslWrote > 0) {
                    src.position(pos + sslWrote);
                } else {
                    src.position(pos);
                }
            }
            finally {
                buf.release();
            }
        }
        return sslWrote;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private int writeEncryptedData(ByteBuffer src) {
        int netWrote;
        int pos = src.position();
        int len = src.remaining();
        if (src.isDirect()) {
            long addr = Buffer.address((ByteBuffer)src) + (long)pos;
            netWrote = SSL.writeToBIO((long)this.networkBIO, (long)addr, (int)len);
            if (netWrote >= 0) {
                src.position(pos + netWrote);
            }
        } else {
            ByteBuf buf = this.alloc.directBuffer(len);
            try {
                long addr = OpenSslEngine.memoryAddress(buf);
                buf.setBytes(0, src);
                netWrote = SSL.writeToBIO((long)this.networkBIO, (long)addr, (int)len);
                if (netWrote >= 0) {
                    src.position(pos + netWrote);
                } else {
                    src.position(pos);
                }
            }
            finally {
                buf.release();
            }
        }
        return netWrote;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private int readPlaintextData(ByteBuffer dst) {
        int sslRead;
        if (dst.isDirect()) {
            int len;
            int pos = dst.position();
            long addr = Buffer.address((ByteBuffer)dst) + (long)pos;
            sslRead = SSL.readFromSSL((long)this.ssl, (long)addr, (int)(len = dst.limit() - pos));
            if (sslRead > 0) {
                dst.position(pos + sslRead);
            }
        } else {
            int pos = dst.position();
            int limit = dst.limit();
            int len = Math.min(18713, limit - pos);
            ByteBuf buf = this.alloc.directBuffer(len);
            try {
                long addr = OpenSslEngine.memoryAddress(buf);
                sslRead = SSL.readFromSSL((long)this.ssl, (long)addr, (int)len);
                if (sslRead > 0) {
                    dst.limit(pos + sslRead);
                    buf.getBytes(0, dst);
                    dst.limit(limit);
                }
            }
            finally {
                buf.release();
            }
        }
        return sslRead;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private int readEncryptedData(ByteBuffer dst, int pending) {
        int bioRead;
        if (dst.isDirect() && dst.remaining() >= pending) {
            int pos = dst.position();
            long addr = Buffer.address((ByteBuffer)dst) + (long)pos;
            bioRead = SSL.readFromBIO((long)this.networkBIO, (long)addr, (int)pending);
            if (bioRead > 0) {
                dst.position(pos + bioRead);
                return bioRead;
            }
        } else {
            ByteBuf buf = this.alloc.directBuffer(pending);
            try {
                long addr = OpenSslEngine.memoryAddress(buf);
                bioRead = SSL.readFromBIO((long)this.networkBIO, (long)addr, (int)pending);
                if (bioRead > 0) {
                    int oldLimit = dst.limit();
                    dst.limit(dst.position() + bioRead);
                    buf.getBytes(0, dst);
                    dst.limit(oldLimit);
                    int n = bioRead;
                    return n;
                }
            }
            finally {
                buf.release();
            }
        }
        return bioRead;
    }

    private SSLEngineResult readPendingBytesFromBIO(ByteBuffer dst, int bytesConsumed, int bytesProduced) throws SSLException {
        int pendingNet = SSL.pendingWrittenBytesInBIO((long)this.networkBIO);
        if (pendingNet > 0) {
            int capacity = dst.remaining();
            if (capacity < pendingNet) {
                return new SSLEngineResult(SSLEngineResult.Status.BUFFER_OVERFLOW, this.mayFinishHandshake(this.getHandshakeStatus(pendingNet)), bytesConsumed, bytesProduced);
            }
            int produced = this.readEncryptedData(dst, pendingNet);
            if (produced <= 0) {
                SSL.clearError();
            } else {
                bytesProduced += produced;
                pendingNet -= produced;
            }
            if (this.isOutboundDone) {
                this.shutdown();
            }
            return new SSLEngineResult(this.getEngineStatus(), this.mayFinishHandshake(this.getHandshakeStatus(pendingNet)), bytesConsumed, bytesProduced);
        }
        return null;
    }

    @Override
    public synchronized SSLEngineResult wrap(ByteBuffer[] srcs, int offset, int length, ByteBuffer dst) throws SSLException {
        SSLEngineResult pendingNetResult;
        if (this.isDestroyed()) {
            return CLOSED_NOT_HANDSHAKING;
        }
        if (srcs == null) {
            throw new IllegalArgumentException("srcs is null");
        }
        if (dst == null) {
            throw new IllegalArgumentException("dst is null");
        }
        if (offset >= srcs.length || offset + length > srcs.length) {
            throw new IndexOutOfBoundsException("offset: " + offset + ", length: " + length + " (expected: offset <= offset + length <= srcs.length (" + srcs.length + "))");
        }
        if (dst.isReadOnly()) {
            throw new ReadOnlyBufferException();
        }
        if (this.handshakeState != HandshakeState.FINISHED) {
            SSLEngineResult.HandshakeStatus status;
            if (this.handshakeState != HandshakeState.STARTED_EXPLICITLY) {
                this.handshakeState = HandshakeState.STARTED_IMPLICITLY;
            }
            if ((status = this.handshake()) == SSLEngineResult.HandshakeStatus.NEED_UNWRAP) {
                return NEED_UNWRAP_OK;
            }
            if (this.engineClosed) {
                return NEED_UNWRAP_CLOSED;
            }
        }
        int bytesProduced = 0;
        int bytesConsumed = 0;
        int endOffset = offset + length;
        for (int i = offset; i < endOffset; ++i) {
            ByteBuffer src = srcs[i];
            if (src == null) {
                throw new IllegalArgumentException("srcs[" + i + "] is null");
            }
            while (src.hasRemaining()) {
                SSLEngineResult pendingNetResult2;
                int result = this.writePlaintextData(src);
                if (result > 0) {
                    bytesConsumed += result;
                } else {
                    int sslError = SSL.getError((long)this.ssl, (int)result);
                    switch (sslError) {
                        case 6: {
                            if (!this.receivedShutdown) {
                                this.closeAll();
                            }
                        }
                        case 2: 
                        case 3: {
                            break;
                        }
                        default: {
                            this.shutdownWithError("SSL_write");
                        }
                    }
                }
                if ((pendingNetResult2 = this.readPendingBytesFromBIO(dst, bytesConsumed, bytesProduced)) == null) continue;
                return pendingNetResult2;
            }
        }
        if (bytesConsumed == 0 && (pendingNetResult = this.readPendingBytesFromBIO(dst, 0, bytesProduced)) != null) {
            return pendingNetResult;
        }
        return this.newResult(bytesConsumed, bytesProduced);
    }

    private void checkPendingHandshakeException() throws SSLHandshakeException {
        if (this.handshakeException != null) {
            SSLHandshakeException exception = this.handshakeException;
            this.handshakeException = null;
            this.shutdown();
            throw exception;
        }
    }

    private void shutdownWithError(String operations) throws SSLException {
        String err = SSL.getLastError();
        this.shutdownWithError(operations, err);
    }

    private void shutdownWithError(String operation, String err) throws SSLException {
        if (logger.isDebugEnabled()) {
            logger.debug("{} failed: OpenSSL error: {}", (Object)operation, (Object)err);
        }
        this.shutdown();
        if (this.handshakeState == HandshakeState.FINISHED) {
            throw new SSLException(err);
        }
        throw new SSLHandshakeException(err);
    }

    public synchronized SSLEngineResult unwrap(ByteBuffer[] srcs, int srcsOffset, int srcsLength, ByteBuffer[] dsts, int dstsOffset, int dstsLength) throws SSLException {
        int err;
        ByteBuffer src;
        if (this.isDestroyed()) {
            return CLOSED_NOT_HANDSHAKING;
        }
        if (srcs == null) {
            throw new NullPointerException("srcs");
        }
        if (srcsOffset >= srcs.length || srcsOffset + srcsLength > srcs.length) {
            throw new IndexOutOfBoundsException("offset: " + srcsOffset + ", length: " + srcsLength + " (expected: offset <= offset + length <= srcs.length (" + srcs.length + "))");
        }
        if (dsts == null) {
            throw new IllegalArgumentException("dsts is null");
        }
        if (dstsOffset >= dsts.length || dstsOffset + dstsLength > dsts.length) {
            throw new IndexOutOfBoundsException("offset: " + dstsOffset + ", length: " + dstsLength + " (expected: offset <= offset + length <= dsts.length (" + dsts.length + "))");
        }
        long capacity = 0L;
        int endOffset = dstsOffset + dstsLength;
        for (int i = dstsOffset; i < endOffset; ++i) {
            ByteBuffer dst = dsts[i];
            if (dst == null) {
                throw new IllegalArgumentException("dsts[" + i + "] is null");
            }
            if (dst.isReadOnly()) {
                throw new ReadOnlyBufferException();
            }
            capacity += (long)dst.remaining();
        }
        if (this.handshakeState != HandshakeState.FINISHED) {
            SSLEngineResult.HandshakeStatus status;
            if (this.handshakeState != HandshakeState.STARTED_EXPLICITLY) {
                this.handshakeState = HandshakeState.STARTED_IMPLICITLY;
            }
            if ((status = this.handshake()) == SSLEngineResult.HandshakeStatus.NEED_WRAP) {
                return NEED_WRAP_OK;
            }
            if (this.engineClosed) {
                return NEED_WRAP_CLOSED;
            }
        }
        int srcsEndOffset = srcsOffset + srcsLength;
        long len = 0L;
        for (int i = srcsOffset; i < srcsEndOffset; ++i) {
            src = srcs[i];
            if (src == null) {
                throw new IllegalArgumentException("srcs[" + i + "] is null");
            }
            len += (long)src.remaining();
        }
        if (len > 18713L) {
            this.isInboundDone = true;
            this.isOutboundDone = true;
            this.engineClosed = true;
            this.shutdown();
            throw ENCRYPTED_PACKET_OVERSIZED;
        }
        int bytesConsumed = 0;
        if (srcsOffset < srcsEndOffset) {
            do {
                int remaining;
                if ((remaining = (src = srcs[srcsOffset]).remaining()) == 0) {
                    ++srcsOffset;
                    continue;
                }
                int written = this.writeEncryptedData(src);
                if (written > 0) {
                    bytesConsumed += written;
                    if (written != remaining) break;
                    ++srcsOffset;
                    continue;
                }
                SSL.clearError();
                break;
            } while (srcsOffset < srcsEndOffset);
        }
        this.rejectRemoteInitiatedRenegation();
        int bytesProduced = 0;
        if (capacity > 0L) {
            int idx = dstsOffset;
            while (idx < endOffset) {
                ByteBuffer dst = dsts[idx];
                if (!dst.hasRemaining()) {
                    ++idx;
                    continue;
                }
                int bytesRead = this.readPlaintextData(dst);
                this.rejectRemoteInitiatedRenegation();
                if (bytesRead > 0) {
                    bytesProduced += bytesRead;
                    if (!dst.hasRemaining()) {
                        ++idx;
                        continue;
                    }
                    return this.newResult(bytesConsumed, bytesProduced);
                }
                int sslError = SSL.getError((long)this.ssl, (int)bytesRead);
                switch (sslError) {
                    case 6: {
                        if (!this.receivedShutdown) {
                            this.closeAll();
                        }
                    }
                    case 2: 
                    case 3: {
                        return this.newResult(bytesConsumed, bytesProduced);
                    }
                }
                this.shutdownWithError("SSL_read");
            }
        } else if (SSL.readFromSSL((long)this.ssl, (long)EMPTY_ADDR, (int)0) <= 0 && OpenSsl.isError(err = SSL.getLastErrorNumber())) {
            this.shutdownWithError("SSL_read", SSL.getErrorString((long)err));
        }
        if (this.pendingAppData() > 0) {
            return new SSLEngineResult(SSLEngineResult.Status.BUFFER_OVERFLOW, this.mayFinishHandshake(this.getHandshakeStatus()), bytesConsumed, bytesProduced);
        }
        if (!this.receivedShutdown && (SSL.getShutdown((long)this.ssl) & 2) == 2) {
            this.closeAll();
        }
        return this.newResult(bytesConsumed, bytesProduced);
    }

    private int pendingAppData() {
        return this.handshakeState == HandshakeState.FINISHED ? SSL.pendingReadableBytesInSSL((long)this.ssl) : 0;
    }

    private SSLEngineResult newResult(int bytesConsumed, int bytesProduced) throws SSLException {
        return new SSLEngineResult(this.getEngineStatus(), this.mayFinishHandshake(this.getHandshakeStatus()), bytesConsumed, bytesProduced);
    }

    private void closeAll() throws SSLException {
        this.receivedShutdown = true;
        this.closeOutbound();
        this.closeInbound();
    }

    private void rejectRemoteInitiatedRenegation() throws SSLHandshakeException {
        if (this.rejectRemoteInitiatedRenegation && SSL.getHandshakeCount((long)this.ssl) > 1) {
            this.shutdown();
            throw new SSLHandshakeException("remote-initiated renegotation not allowed");
        }
    }

    public SSLEngineResult unwrap(ByteBuffer[] srcs, ByteBuffer[] dsts) throws SSLException {
        return this.unwrap(srcs, 0, srcs.length, dsts, 0, dsts.length);
    }

    @Override
    public SSLEngineResult unwrap(ByteBuffer src, ByteBuffer[] dsts, int offset, int length) throws SSLException {
        return this.unwrap(new ByteBuffer[]{src}, 0, 1, dsts, offset, length);
    }

    @Override
    public Runnable getDelegatedTask() {
        return null;
    }

    @Override
    public synchronized void closeInbound() throws SSLException {
        if (this.isInboundDone) {
            return;
        }
        this.isInboundDone = true;
        this.engineClosed = true;
        this.shutdown();
        if (this.handshakeState != HandshakeState.NOT_STARTED && !this.receivedShutdown) {
            throw new SSLException("Inbound closed before receiving peer's close_notify: possible truncation attack?");
        }
    }

    @Override
    public synchronized boolean isInboundDone() {
        return this.isInboundDone || this.engineClosed;
    }

    @Override
    public synchronized void closeOutbound() {
        if (this.isOutboundDone) {
            return;
        }
        this.isOutboundDone = true;
        this.engineClosed = true;
        if (this.handshakeState != HandshakeState.NOT_STARTED && !this.isDestroyed()) {
            int err;
            int mode = SSL.getShutdown((long)this.ssl);
            if ((mode & 1) != 1 && (err = SSL.shutdownSSL((long)this.ssl)) < 0) {
                int sslErr = SSL.getError((long)this.ssl, (int)err);
                switch (sslErr) {
                    case 0: 
                    case 2: 
                    case 3: 
                    case 4: 
                    case 6: 
                    case 7: 
                    case 8: {
                        break;
                    }
                    case 1: 
                    case 5: {
                        if (logger.isDebugEnabled()) {
                            logger.debug("SSL_shutdown failed: OpenSSL error: {}", (Object)SSL.getLastError());
                        }
                        this.shutdown();
                        break;
                    }
                    default: {
                        SSL.clearError();
                        break;
                    }
                }
            }
        } else {
            this.shutdown();
        }
    }

    @Override
    public synchronized boolean isOutboundDone() {
        return this.isOutboundDone;
    }

    @Override
    public String[] getSupportedCipherSuites() {
        Set<String> availableCipherSuites = OpenSsl.availableCipherSuites();
        return availableCipherSuites.toArray(new String[availableCipherSuites.size()]);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public String[] getEnabledCipherSuites() {
        String[] enabled;
        OpenSslEngine openSslEngine = this;
        synchronized (openSslEngine) {
            if (this.isDestroyed()) {
                return EmptyArrays.EMPTY_STRINGS;
            }
            enabled = SSL.getCiphers((long)this.ssl);
        }
        if (enabled == null) {
            return EmptyArrays.EMPTY_STRINGS;
        }
        for (int i = 0; i < enabled.length; ++i) {
            String mapped = this.toJavaCipherSuite(enabled[i]);
            if (mapped == null) continue;
            enabled[i] = mapped;
        }
        return enabled;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setEnabledCipherSuites(String[] cipherSuites) {
        ObjectUtil.checkNotNull(cipherSuites, "cipherSuites");
        StringBuilder buf = new StringBuilder();
        for (String c : cipherSuites) {
            if (c == null) break;
            String converted = CipherSuiteConverter.toOpenSsl(c);
            if (converted == null) {
                converted = c;
            }
            if (!OpenSsl.isCipherSuiteAvailable(converted)) {
                throw new IllegalArgumentException("unsupported cipher suite: " + c + '(' + converted + ')');
            }
            buf.append(converted);
            buf.append(':');
        }
        if (buf.length() == 0) {
            throw new IllegalArgumentException("empty cipher suites");
        }
        buf.setLength(buf.length() - 1);
        String cipherSuiteSpec = buf.toString();
        OpenSslEngine len$ = this;
        synchronized (len$) {
            if (!this.isDestroyed()) {
                try {
                    SSL.setCipherSuites((long)this.ssl, (String)cipherSuiteSpec);
                }
                catch (Exception e) {
                    throw new IllegalStateException("failed to enable cipher suites: " + cipherSuiteSpec, e);
                }
            } else {
                throw new IllegalStateException("failed to enable cipher suites: " + cipherSuiteSpec);
            }
        }
    }

    @Override
    public String[] getSupportedProtocols() {
        return (String[])SUPPORTED_PROTOCOLS.clone();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public String[] getEnabledProtocols() {
        int opts;
        ArrayList<String> enabled = new ArrayList<String>();
        enabled.add(PROTOCOL_SSL_V2_HELLO);
        OpenSslEngine openSslEngine = this;
        synchronized (openSslEngine) {
            if (this.isDestroyed()) {
                return enabled.toArray(new String[1]);
            }
            opts = SSL.getOptions((long)this.ssl);
        }
        if ((opts & 67108864) == 0) {
            enabled.add(PROTOCOL_TLS_V1);
        }
        if ((opts & 268435456) == 0) {
            enabled.add(PROTOCOL_TLS_V1_1);
        }
        if ((opts & 134217728) == 0) {
            enabled.add(PROTOCOL_TLS_V1_2);
        }
        if ((opts & 16777216) == 0) {
            enabled.add(PROTOCOL_SSL_V2);
        }
        if ((opts & 33554432) == 0) {
            enabled.add(PROTOCOL_SSL_V3);
        }
        return enabled.toArray(new String[enabled.size()]);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setEnabledProtocols(String[] protocols) {
        if (protocols == null) {
            throw new IllegalArgumentException();
        }
        boolean sslv2 = false;
        boolean sslv3 = false;
        boolean tlsv1 = false;
        boolean tlsv1_1 = false;
        boolean tlsv1_2 = false;
        for (String p : protocols) {
            if (!SUPPORTED_PROTOCOLS_SET.contains(p)) {
                throw new IllegalArgumentException("Protocol " + p + " is not supported.");
            }
            if (p.equals(PROTOCOL_SSL_V2)) {
                sslv2 = true;
                continue;
            }
            if (p.equals(PROTOCOL_SSL_V3)) {
                sslv3 = true;
                continue;
            }
            if (p.equals(PROTOCOL_TLS_V1)) {
                tlsv1 = true;
                continue;
            }
            if (p.equals(PROTOCOL_TLS_V1_1)) {
                tlsv1_1 = true;
                continue;
            }
            if (!p.equals(PROTOCOL_TLS_V1_2)) continue;
            tlsv1_2 = true;
        }
        Object arr$ = this;
        synchronized (arr$) {
            if (!this.isDestroyed()) {
                SSL.setOptions((long)this.ssl, (int)4095);
                if (!sslv2) {
                    SSL.setOptions((long)this.ssl, (int)16777216);
                }
                if (!sslv3) {
                    SSL.setOptions((long)this.ssl, (int)33554432);
                }
                if (!tlsv1) {
                    SSL.setOptions((long)this.ssl, (int)67108864);
                }
                if (!tlsv1_1) {
                    SSL.setOptions((long)this.ssl, (int)268435456);
                }
                if (!tlsv1_2) {
                    SSL.setOptions((long)this.ssl, (int)134217728);
                }
            } else {
                throw new IllegalStateException("failed to enable protocols: " + Arrays.asList(protocols));
            }
        }
    }

    @Override
    public SSLSession getSession() {
        return this.session;
    }

    @Override
    public synchronized void beginHandshake() throws SSLException {
        switch (this.handshakeState) {
            case NOT_STARTED: {
                this.handshake();
                this.handshakeState = HandshakeState.STARTED_EXPLICITLY;
                break;
            }
            case STARTED_IMPLICITLY: {
                this.checkEngineClosed();
                this.handshakeState = HandshakeState.STARTED_EXPLICITLY;
                break;
            }
            case STARTED_EXPLICITLY: {
                throw RENEGOTIATION_UNSUPPORTED;
            }
            default: {
                throw new Error();
            }
        }
    }

    private void checkEngineClosed() throws SSLException {
        if (this.engineClosed || this.isDestroyed()) {
            throw ENGINE_CLOSED;
        }
    }

    private static SSLEngineResult.HandshakeStatus pendingStatus(int pendingStatus) {
        return pendingStatus > 0 ? SSLEngineResult.HandshakeStatus.NEED_WRAP : SSLEngineResult.HandshakeStatus.NEED_UNWRAP;
    }

    private SSLEngineResult.HandshakeStatus handshake() throws SSLException {
        this.checkEngineClosed();
        int code = SSL.doHandshake((long)this.ssl);
        if (code <= 0) {
            this.checkPendingHandshakeException();
            int sslError = SSL.getError((long)this.ssl, (int)code);
            switch (sslError) {
                case 2: 
                case 3: {
                    return OpenSslEngine.pendingStatus(SSL.pendingWrittenBytesInBIO((long)this.networkBIO));
                }
            }
            this.shutdownWithError("SSL_do_handshake");
        }
        this.session.handshakeFinished();
        return SSLEngineResult.HandshakeStatus.FINISHED;
    }

    private static long memoryAddress(ByteBuf buf) {
        if (buf.hasMemoryAddress()) {
            return buf.memoryAddress();
        }
        return Buffer.address((ByteBuffer)buf.nioBuffer());
    }

    private SSLEngineResult.Status getEngineStatus() {
        return this.engineClosed ? SSLEngineResult.Status.CLOSED : SSLEngineResult.Status.OK;
    }

    private SSLEngineResult.HandshakeStatus mayFinishHandshake(SSLEngineResult.HandshakeStatus status) throws SSLException {
        if (status == SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING && this.handshakeState != HandshakeState.FINISHED) {
            return this.handshake();
        }
        return status;
    }

    @Override
    public synchronized SSLEngineResult.HandshakeStatus getHandshakeStatus() {
        if (this.needPendingStatus()) {
            return OpenSslEngine.pendingStatus(SSL.pendingWrittenBytesInBIO((long)this.networkBIO));
        }
        return SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING;
    }

    private SSLEngineResult.HandshakeStatus getHandshakeStatus(int pending) {
        if (this.needPendingStatus()) {
            return OpenSslEngine.pendingStatus(pending);
        }
        return SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING;
    }

    private boolean needPendingStatus() {
        return this.handshakeState != HandshakeState.NOT_STARTED && !this.isDestroyed() && (this.handshakeState != HandshakeState.FINISHED || this.engineClosed);
    }

    private String toJavaCipherSuite(String openSslCipherSuite) {
        if (openSslCipherSuite == null) {
            return null;
        }
        String prefix = OpenSslEngine.toJavaCipherSuitePrefix(SSL.getVersion((long)this.ssl));
        return CipherSuiteConverter.toJava(openSslCipherSuite, prefix);
    }

    private static String toJavaCipherSuitePrefix(String protocolVersion) {
        int c = protocolVersion == null || protocolVersion.length() == 0 ? 0 : (int)protocolVersion.charAt(0);
        switch (c) {
            case 84: {
                return "TLS";
            }
            case 83: {
                return "SSL";
            }
        }
        return "UNKNOWN";
    }

    @Override
    public void setUseClientMode(boolean clientMode) {
        if (clientMode != this.clientMode) {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public boolean getUseClientMode() {
        return this.clientMode;
    }

    @Override
    public void setNeedClientAuth(boolean b) {
        this.setClientAuth(b ? ClientAuthMode.REQUIRE : ClientAuthMode.NONE);
    }

    @Override
    public boolean getNeedClientAuth() {
        return this.clientAuth == ClientAuthMode.REQUIRE;
    }

    @Override
    public void setWantClientAuth(boolean b) {
        this.setClientAuth(b ? ClientAuthMode.OPTIONAL : ClientAuthMode.NONE);
    }

    @Override
    public boolean getWantClientAuth() {
        return this.clientAuth == ClientAuthMode.OPTIONAL;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void setClientAuth(ClientAuthMode mode) {
        if (this.clientMode) {
            return;
        }
        OpenSslEngine openSslEngine = this;
        synchronized (openSslEngine) {
            if (this.clientAuth == mode) {
                return;
            }
            switch (mode) {
                case NONE: {
                    SSL.setVerify((long)this.ssl, (int)0, (int)10);
                    break;
                }
                case REQUIRE: {
                    SSL.setVerify((long)this.ssl, (int)2, (int)10);
                    break;
                }
                case OPTIONAL: {
                    SSL.setVerify((long)this.ssl, (int)1, (int)10);
                }
            }
            this.clientAuth = mode;
        }
    }

    @Override
    public void setEnableSessionCreation(boolean b) {
        if (b) {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public boolean getEnableSessionCreation() {
        return false;
    }

    @Override
    public SSLParameters getSSLParameters() {
        SSLParameters sslParameters = super.getSSLParameters();
        if (PlatformDependent.javaVersion() >= 7) {
            sslParameters.setEndpointIdentificationAlgorithm(this.endPointIdentificationAlgorithm);
            SslParametersUtils.setAlgorithmConstraints(sslParameters, this.algorithmConstraints);
        }
        return sslParameters;
    }

    @Override
    public void setSSLParameters(SSLParameters sslParameters) {
        super.setSSLParameters(sslParameters);
        if (PlatformDependent.javaVersion() >= 7) {
            this.endPointIdentificationAlgorithm = sslParameters.getEndpointIdentificationAlgorithm();
            this.algorithmConstraints = sslParameters.getAlgorithmConstraints();
        }
    }

    protected void finalize() throws Throwable {
        Object.super.finalize();
        this.shutdown();
    }

    private boolean isDestroyed() {
        return this.destroyed != 0;
    }

    static {
        ENGINE_CLOSED.setStackTrace(EmptyArrays.EMPTY_STACK_TRACE);
        RENEGOTIATION_UNSUPPORTED.setStackTrace(EmptyArrays.EMPTY_STACK_TRACE);
        ENCRYPTED_PACKET_OVERSIZED.setStackTrace(EmptyArrays.EMPTY_STACK_TRACE);
        AtomicIntegerFieldUpdater<Object> destroyedUpdater = PlatformDependent.newAtomicIntegerFieldUpdater(OpenSslEngine.class, "destroyed");
        if (destroyedUpdater == null) {
            destroyedUpdater = AtomicIntegerFieldUpdater.newUpdater(OpenSslEngine.class, "destroyed");
        }
        DESTROYED_UPDATER = destroyedUpdater;
        SUPPORTED_PROTOCOLS = new String[]{PROTOCOL_SSL_V2_HELLO, PROTOCOL_SSL_V2, PROTOCOL_SSL_V3, PROTOCOL_TLS_V1, PROTOCOL_TLS_V1_1, PROTOCOL_TLS_V1_2};
        SUPPORTED_PROTOCOLS_SET = new HashSet<String>(Arrays.asList(SUPPORTED_PROTOCOLS));
        EMPTY_ADDR = Buffer.address((ByteBuffer)Unpooled.EMPTY_BUFFER.nioBuffer());
        NEED_UNWRAP_OK = new SSLEngineResult(SSLEngineResult.Status.OK, SSLEngineResult.HandshakeStatus.NEED_UNWRAP, 0, 0);
        NEED_UNWRAP_CLOSED = new SSLEngineResult(SSLEngineResult.Status.CLOSED, SSLEngineResult.HandshakeStatus.NEED_UNWRAP, 0, 0);
        NEED_WRAP_OK = new SSLEngineResult(SSLEngineResult.Status.OK, SSLEngineResult.HandshakeStatus.NEED_WRAP, 0, 0);
        NEED_WRAP_CLOSED = new SSLEngineResult(SSLEngineResult.Status.CLOSED, SSLEngineResult.HandshakeStatus.NEED_WRAP, 0, 0);
        CLOSED_NOT_HANDSHAKING = new SSLEngineResult(SSLEngineResult.Status.CLOSED, SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING, 0, 0);
    }

    private final class OpenSslSession
    implements SSLSession,
    ApplicationProtocolAccessor {
        private final OpenSslSessionContext sessionContext;
        private final long creationTime;
        private X509Certificate[] x509PeerCerts;
        private String protocol;
        private String applicationProtocol;
        private Certificate[] peerCerts;
        private String cipher;
        private byte[] id;
        private Map<String, Object> values;

        OpenSslSession(long ssl, OpenSslSessionContext sessionContext) {
            this.creationTime = SSL.getTime((long)ssl) * 1000L;
            this.sessionContext = sessionContext;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public byte[] getId() {
            OpenSslEngine openSslEngine = OpenSslEngine.this;
            synchronized (openSslEngine) {
                if (this.id == null) {
                    return EmptyArrays.EMPTY_BYTES;
                }
                return (byte[])this.id.clone();
            }
        }

        @Override
        public SSLSessionContext getSessionContext() {
            return this.sessionContext;
        }

        @Override
        public long getCreationTime() {
            return this.creationTime;
        }

        @Override
        public long getLastAccessedTime() {
            return this.getCreationTime();
        }

        @Override
        public void invalidate() {
        }

        @Override
        public boolean isValid() {
            return false;
        }

        @Override
        public void putValue(String name, Object value) {
            if (name == null) {
                throw new NullPointerException("name");
            }
            if (value == null) {
                throw new NullPointerException("value");
            }
            Map<String, Object> values = this.values;
            if (values == null) {
                values = this.values = new HashMap<String, Object>(2);
            }
            Object old = values.put(name, value);
            if (value instanceof SSLSessionBindingListener) {
                ((SSLSessionBindingListener)value).valueBound(new SSLSessionBindingEvent(this, name));
            }
            this.notifyUnbound(old, name);
        }

        @Override
        public Object getValue(String name) {
            if (name == null) {
                throw new NullPointerException("name");
            }
            if (this.values == null) {
                return null;
            }
            return this.values.get(name);
        }

        @Override
        public void removeValue(String name) {
            if (name == null) {
                throw new NullPointerException("name");
            }
            Map<String, Object> values = this.values;
            if (values == null) {
                return;
            }
            Object old = values.remove(name);
            this.notifyUnbound(old, name);
        }

        @Override
        public String[] getValueNames() {
            Map<String, Object> values = this.values;
            if (values == null || values.isEmpty()) {
                return EmptyArrays.EMPTY_STRINGS;
            }
            return values.keySet().toArray(new String[values.size()]);
        }

        private void notifyUnbound(Object value, String name) {
            if (value instanceof SSLSessionBindingListener) {
                ((SSLSessionBindingListener)value).valueUnbound(new SSLSessionBindingEvent(this, name));
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        void handshakeFinished() throws SSLException {
            OpenSslEngine openSslEngine = OpenSslEngine.this;
            synchronized (openSslEngine) {
                if (OpenSslEngine.this.isDestroyed()) {
                    throw new SSLException("Already closed");
                }
                this.id = SSL.getSessionId((long)OpenSslEngine.this.ssl);
                this.cipher = OpenSslEngine.this.toJavaCipherSuite(SSL.getCipherForSSL((long)OpenSslEngine.this.ssl));
                this.protocol = SSL.getVersion((long)OpenSslEngine.this.ssl);
                this.initPeerCerts();
                this.selectApplicationProtocol();
                OpenSslEngine.this.handshakeState = HandshakeState.FINISHED;
            }
        }

        private void initPeerCerts() {
            byte[][] chain = SSL.getPeerCertChain((long)OpenSslEngine.this.ssl);
            byte[] clientCert = !OpenSslEngine.this.clientMode ? SSL.getPeerCertificate((long)OpenSslEngine.this.ssl) : null;
            if (chain == null && clientCert == null) {
                this.peerCerts = EMPTY_CERTIFICATES;
                this.x509PeerCerts = EMPTY_X509_CERTIFICATES;
            } else {
                Certificate[] peerCerts;
                int len = chain != null ? chain.length : 0;
                int i = 0;
                if (clientCert != null) {
                    peerCerts = new Certificate[++len];
                    peerCerts[i++] = new OpenSslX509Certificate(clientCert);
                } else {
                    peerCerts = new Certificate[len];
                }
                if (chain != null) {
                    X509Certificate[] pCerts = new X509Certificate[chain.length];
                    for (int a = 0; a < pCerts.length; ++a) {
                        byte[] bytes = chain[a];
                        pCerts[a] = new OpenSslJavaxX509Certificate(bytes);
                        peerCerts[i] = new OpenSslX509Certificate(bytes);
                        ++i;
                    }
                    this.x509PeerCerts = pCerts;
                } else {
                    this.x509PeerCerts = EMPTY_X509_CERTIFICATES;
                }
                this.peerCerts = peerCerts;
            }
        }

        private void selectApplicationProtocol() throws SSLException {
            ApplicationProtocolConfig.SelectedListenerFailureBehavior behavior = OpenSslEngine.this.apn.selectedListenerFailureBehavior();
            List<String> protocols = OpenSslEngine.this.apn.protocols();
            switch (OpenSslEngine.this.apn.protocol()) {
                case NONE: {
                    break;
                }
                case ALPN: {
                    String applicationProtocol = SSL.getAlpnSelected((long)OpenSslEngine.this.ssl);
                    if (applicationProtocol == null) break;
                    this.applicationProtocol = this.selectApplicationProtocol(protocols, behavior, applicationProtocol);
                    break;
                }
                case NPN: {
                    String applicationProtocol = SSL.getNextProtoNegotiated((long)OpenSslEngine.this.ssl);
                    if (applicationProtocol == null) break;
                    this.applicationProtocol = this.selectApplicationProtocol(protocols, behavior, applicationProtocol);
                    break;
                }
                case NPN_AND_ALPN: {
                    String applicationProtocol = SSL.getAlpnSelected((long)OpenSslEngine.this.ssl);
                    if (applicationProtocol == null) {
                        applicationProtocol = SSL.getNextProtoNegotiated((long)OpenSslEngine.this.ssl);
                    }
                    if (applicationProtocol == null) break;
                    this.applicationProtocol = this.selectApplicationProtocol(protocols, behavior, applicationProtocol);
                    break;
                }
                default: {
                    throw new Error();
                }
            }
        }

        private String selectApplicationProtocol(List<String> protocols, ApplicationProtocolConfig.SelectedListenerFailureBehavior behavior, String applicationProtocol) throws SSLException {
            if (behavior == ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT) {
                return applicationProtocol;
            }
            int size = protocols.size();
            assert (size > 0);
            if (protocols.contains(applicationProtocol)) {
                return applicationProtocol;
            }
            if (behavior == ApplicationProtocolConfig.SelectedListenerFailureBehavior.CHOOSE_MY_LAST_PROTOCOL) {
                return protocols.get(size - 1);
            }
            throw new SSLException("unknown protocol " + applicationProtocol);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public Certificate[] getPeerCertificates() throws SSLPeerUnverifiedException {
            OpenSslEngine openSslEngine = OpenSslEngine.this;
            synchronized (openSslEngine) {
                if (this.peerCerts == null) {
                    throw new SSLPeerUnverifiedException("peer not verified");
                }
                return this.peerCerts;
            }
        }

        @Override
        public Certificate[] getLocalCertificates() {
            return EMPTY_CERTIFICATES;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public X509Certificate[] getPeerCertificateChain() throws SSLPeerUnverifiedException {
            OpenSslEngine openSslEngine = OpenSslEngine.this;
            synchronized (openSslEngine) {
                if (this.x509PeerCerts == null) {
                    throw new SSLPeerUnverifiedException("peer not verified");
                }
                return this.x509PeerCerts;
            }
        }

        @Override
        public Principal getPeerPrincipal() throws SSLPeerUnverifiedException {
            Certificate[] peer = this.getPeerCertificates();
            if (peer == null || peer.length == 0) {
                return null;
            }
            return ((java.security.cert.X509Certificate)peer[0]).getSubjectX500Principal();
        }

        @Override
        public Principal getLocalPrincipal() {
            Certificate[] local = this.getLocalCertificates();
            if (local == null || local.length == 0) {
                return null;
            }
            return ((java.security.cert.X509Certificate)local[0]).getIssuerX500Principal();
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public String getCipherSuite() {
            OpenSslEngine openSslEngine = OpenSslEngine.this;
            synchronized (openSslEngine) {
                if (this.cipher == null) {
                    return OpenSslEngine.INVALID_CIPHER;
                }
                return this.cipher;
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public String getProtocol() {
            String protocol = this.protocol;
            if (protocol == null) {
                OpenSslEngine openSslEngine = OpenSslEngine.this;
                synchronized (openSslEngine) {
                    protocol = !OpenSslEngine.this.isDestroyed() ? SSL.getVersion((long)OpenSslEngine.this.ssl) : "";
                }
            }
            return protocol;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public String getApplicationProtocol() {
            OpenSslEngine openSslEngine = OpenSslEngine.this;
            synchronized (openSslEngine) {
                return this.applicationProtocol;
            }
        }

        @Override
        public String getPeerHost() {
            return OpenSslEngine.this.getPeerHost();
        }

        @Override
        public int getPeerPort() {
            return OpenSslEngine.this.getPeerPort();
        }

        @Override
        public int getPacketBufferSize() {
            return 18713;
        }

        @Override
        public int getApplicationBufferSize() {
            return 16384;
        }
    }

    private static enum HandshakeState {
        NOT_STARTED,
        STARTED_IMPLICITLY,
        STARTED_EXPLICITLY,
        FINISHED;
        

        private HandshakeState() {
        }
    }

    static enum ClientAuthMode {
        NONE,
        OPTIONAL,
        REQUIRE;
        

        private ClientAuthMode() {
        }
    }

}

