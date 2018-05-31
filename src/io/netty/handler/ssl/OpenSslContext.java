/*
 * Decompiled with CFR 0_129.
 * 
 * Could not load the following classes:
 *  org.apache.tomcat.jni.CertificateVerifier
 *  org.apache.tomcat.jni.Pool
 *  org.apache.tomcat.jni.SSLContext
 */
package io.netty.handler.ssl;

import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.ApplicationProtocolNegotiator;
import io.netty.handler.ssl.CipherSuiteConverter;
import io.netty.handler.ssl.CipherSuiteFilter;
import io.netty.handler.ssl.OpenSsl;
import io.netty.handler.ssl.OpenSslApplicationProtocolNegotiator;
import io.netty.handler.ssl.OpenSslDefaultApplicationProtocolNegotiator;
import io.netty.handler.ssl.OpenSslEngine;
import io.netty.handler.ssl.OpenSslEngineMap;
import io.netty.handler.ssl.OpenSslSessionContext;
import io.netty.handler.ssl.OpenSslSessionStats;
import io.netty.handler.ssl.OpenSslX509Certificate;
import io.netty.handler.ssl.SslContext;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.SystemPropertyUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.tomcat.jni.CertificateVerifier;
import org.apache.tomcat.jni.Pool;
import org.apache.tomcat.jni.SSLContext;

public abstract class OpenSslContext
extends SslContext {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(OpenSslContext.class);
    private static final boolean JDK_REJECT_CLIENT_INITIATED_RENEGOTIATION = SystemPropertyUtil.getBoolean("jdk.tls.rejectClientInitiatedRenegotiation", false);
    private static final List<String> DEFAULT_CIPHERS;
    protected static final int VERIFY_DEPTH = 10;
    protected volatile long ctx;
    private long aprPool;
    private volatile int aprPoolDestroyed;
    private volatile boolean rejectRemoteInitiatedRenegotiation;
    private final List<String> unmodifiableCiphers;
    private final long sessionCacheSize;
    private final long sessionTimeout;
    private final OpenSslEngineMap engineMap = new DefaultOpenSslEngineMap();
    private final OpenSslApplicationProtocolNegotiator apn;
    private final int mode;
    static final OpenSslApplicationProtocolNegotiator NONE_PROTOCOL_NEGOTIATOR;

    OpenSslContext(Iterable<String> ciphers, CipherSuiteFilter cipherFilter, ApplicationProtocolConfig apnCfg, long sessionCacheSize, long sessionTimeout, int mode) throws SSLException {
        this(ciphers, cipherFilter, OpenSslContext.toNegotiator(apnCfg), sessionCacheSize, sessionTimeout, mode);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    OpenSslContext(Iterable<String> ciphers, CipherSuiteFilter cipherFilter, OpenSslApplicationProtocolNegotiator apn, long sessionCacheSize, long sessionTimeout, int mode) throws SSLException {
        ArrayList<String> convertedCiphers;
        Object c;
        OpenSsl.ensureAvailability();
        if (mode != 1 && mode != 0) {
            throw new IllegalArgumentException("mode most be either SSL.SSL_MODE_SERVER or SSL.SSL_MODE_CLIENT");
        }
        this.mode = mode;
        if (mode == 1) {
            this.rejectRemoteInitiatedRenegotiation = JDK_REJECT_CLIENT_INITIATED_RENEGOTIATION;
        }
        if (ciphers == null) {
            convertedCiphers = null;
        } else {
            convertedCiphers = new ArrayList<String>();
            Iterator<String> i$ = ciphers.iterator();
            while (i$.hasNext() && (c = i$.next()) != null) {
                String converted = CipherSuiteConverter.toOpenSsl((String)c);
                if (converted != null) {
                    c = converted;
                }
                convertedCiphers.add((String)c);
            }
        }
        this.unmodifiableCiphers = Arrays.asList(ObjectUtil.checkNotNull(cipherFilter, "cipherFilter").filterCipherSuites(convertedCiphers, DEFAULT_CIPHERS, OpenSsl.availableCipherSuites()));
        this.apn = ObjectUtil.checkNotNull(apn, "apn");
        this.aprPool = Pool.create((long)0L);
        boolean success = false;
        try {
            c = OpenSslContext.class;
            synchronized (OpenSslContext.class) {
                try {
                    this.ctx = SSLContext.make((long)this.aprPool, (int)28, (int)mode);
                }
                catch (Exception e) {
                    throw new SSLException("failed to create an SSL_CTX", e);
                }
                SSLContext.setOptions((long)this.ctx, (int)4095);
                SSLContext.setOptions((long)this.ctx, (int)16777216);
                SSLContext.setOptions((long)this.ctx, (int)33554432);
                SSLContext.setOptions((long)this.ctx, (int)4194304);
                SSLContext.setOptions((long)this.ctx, (int)524288);
                SSLContext.setOptions((long)this.ctx, (int)1048576);
                SSLContext.setOptions((long)this.ctx, (int)65536);
                try {
                    SSLContext.setCipherSuite((long)this.ctx, (String)CipherSuiteConverter.toOpenSsl(this.unmodifiableCiphers));
                }
                catch (SSLException e) {
                    throw e;
                }
                catch (Exception e) {
                    throw new SSLException("failed to set cipher suite: " + this.unmodifiableCiphers, e);
                }
                List<String> nextProtoList = apn.protocols();
                if (!nextProtoList.isEmpty()) {
                    String[] protocols = nextProtoList.toArray(new String[nextProtoList.size()]);
                    int selectorBehavior = OpenSslContext.opensslSelectorFailureBehavior(apn.selectorFailureBehavior());
                    switch (apn.protocol()) {
                        case NPN: {
                            SSLContext.setNpnProtos((long)this.ctx, (String[])protocols, (int)selectorBehavior);
                            break;
                        }
                        case ALPN: {
                            SSLContext.setAlpnProtos((long)this.ctx, (String[])protocols, (int)selectorBehavior);
                            break;
                        }
                        case NPN_AND_ALPN: {
                            SSLContext.setNpnProtos((long)this.ctx, (String[])protocols, (int)selectorBehavior);
                            SSLContext.setAlpnProtos((long)this.ctx, (String[])protocols, (int)selectorBehavior);
                            break;
                        }
                        default: {
                            throw new Error();
                        }
                    }
                }
                if (sessionCacheSize > 0L) {
                    this.sessionCacheSize = sessionCacheSize;
                    SSLContext.setSessionCacheSize((long)this.ctx, (long)sessionCacheSize);
                } else {
                    this.sessionCacheSize = sessionCacheSize = SSLContext.setSessionCacheSize((long)this.ctx, (long)20480L);
                    SSLContext.setSessionCacheSize((long)this.ctx, (long)sessionCacheSize);
                }
                if (sessionTimeout > 0L) {
                    this.sessionTimeout = sessionTimeout;
                    SSLContext.setSessionCacheTimeout((long)this.ctx, (long)sessionTimeout);
                } else {
                    this.sessionTimeout = sessionTimeout = SSLContext.setSessionCacheTimeout((long)this.ctx, (long)300L);
                    SSLContext.setSessionCacheTimeout((long)this.ctx, (long)sessionTimeout);
                }
                // ** MonitorExit[c] (shouldn't be in output)
                success = true;
            }
        }
        finally {
            if (!success) {
                this.destroy();
            }
        }
        {
            
            return;
        }
    }

    private static int opensslSelectorFailureBehavior(ApplicationProtocolConfig.SelectorFailureBehavior behavior) {
        switch (behavior) {
            case NO_ADVERTISE: {
                return 0;
            }
            case CHOOSE_MY_LAST_PROTOCOL: {
                return 1;
            }
        }
        throw new Error();
    }

    @Override
    public final List<String> cipherSuites() {
        return this.unmodifiableCiphers;
    }

    @Override
    public final long sessionCacheSize() {
        return this.sessionCacheSize;
    }

    @Override
    public final long sessionTimeout() {
        return this.sessionTimeout;
    }

    @Override
    public ApplicationProtocolNegotiator applicationProtocolNegotiator() {
        return this.apn;
    }

    @Override
    public final boolean isClient() {
        return this.mode == 0;
    }

    @Override
    public final SSLEngine newEngine(ByteBufAllocator alloc, String peerHost, int peerPort) {
        OpenSslEngine engine = new OpenSslEngine(this.ctx, alloc, this.isClient(), this.sessionContext(), this.apn, this.engineMap, this.rejectRemoteInitiatedRenegotiation, peerHost, peerPort);
        this.engineMap.add(engine);
        return engine;
    }

    @Override
    public final SSLEngine newEngine(ByteBufAllocator alloc) {
        return this.newEngine(alloc, null, -1);
    }

    @Deprecated
    public final long context() {
        return this.ctx;
    }

    @Deprecated
    public final OpenSslSessionStats stats() {
        return this.sessionContext().stats();
    }

    public void setRejectRemoteInitiatedRenegotiation(boolean rejectRemoteInitiatedRenegotiation) {
        this.rejectRemoteInitiatedRenegotiation = rejectRemoteInitiatedRenegotiation;
    }

    protected final void finalize() throws Throwable {
        Object.super.finalize();
        this.destroy();
    }

    @Deprecated
    public final void setTicketKeys(byte[] keys) {
        this.sessionContext().setTicketKeys(keys);
    }

    @Override
    public abstract OpenSslSessionContext sessionContext();

    public final long sslCtxPointer() {
        return this.ctx;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected final void destroy() {
        Class<OpenSslContext> class_ = OpenSslContext.class;
        synchronized (OpenSslContext.class) {
            if (this.ctx != 0L) {
                SSLContext.free((long)this.ctx);
                this.ctx = 0L;
            }
            if (this.aprPool != 0L) {
                Pool.destroy((long)this.aprPool);
                this.aprPool = 0L;
            }
            // ** MonitorExit[var1_1] (shouldn't be in output)
            return;
        }
    }

    protected static X509Certificate[] certificates(byte[][] chain) {
        X509Certificate[] peerCerts = new X509Certificate[chain.length];
        for (int i = 0; i < peerCerts.length; ++i) {
            peerCerts[i] = new OpenSslX509Certificate(chain[i]);
        }
        return peerCerts;
    }

    protected static X509TrustManager chooseTrustManager(TrustManager[] managers) {
        for (TrustManager m : managers) {
            if (!(m instanceof X509TrustManager)) continue;
            return (X509TrustManager)m;
        }
        throw new IllegalStateException("no X509TrustManager found");
    }

    static OpenSslApplicationProtocolNegotiator toNegotiator(ApplicationProtocolConfig config) {
        if (config == null) {
            return NONE_PROTOCOL_NEGOTIATOR;
        }
        switch (config.protocol()) {
            case NONE: {
                return NONE_PROTOCOL_NEGOTIATOR;
            }
            case NPN: 
            case ALPN: 
            case NPN_AND_ALPN: {
                switch (config.selectedListenerFailureBehavior()) {
                    case CHOOSE_MY_LAST_PROTOCOL: 
                    case ACCEPT: {
                        switch (config.selectorFailureBehavior()) {
                            case NO_ADVERTISE: 
                            case CHOOSE_MY_LAST_PROTOCOL: {
                                return new OpenSslDefaultApplicationProtocolNegotiator(config);
                            }
                        }
                        throw new UnsupportedOperationException("OpenSSL provider does not support " + (Object)((Object)config.selectorFailureBehavior()) + " behavior");
                    }
                }
                throw new UnsupportedOperationException("OpenSSL provider does not support " + (Object)((Object)config.selectedListenerFailureBehavior()) + " behavior");
            }
        }
        throw new Error();
    }

    static boolean useExtendedTrustManager(X509TrustManager trustManager) {
        return PlatformDependent.javaVersion() >= 7 && trustManager instanceof X509ExtendedTrustManager;
    }

    static {
        NONE_PROTOCOL_NEGOTIATOR = new OpenSslApplicationProtocolNegotiator(){

            @Override
            public ApplicationProtocolConfig.Protocol protocol() {
                return ApplicationProtocolConfig.Protocol.NONE;
            }

            @Override
            public List<String> protocols() {
                return Collections.emptyList();
            }

            @Override
            public ApplicationProtocolConfig.SelectorFailureBehavior selectorFailureBehavior() {
                return ApplicationProtocolConfig.SelectorFailureBehavior.CHOOSE_MY_LAST_PROTOCOL;
            }

            @Override
            public ApplicationProtocolConfig.SelectedListenerFailureBehavior selectedListenerFailureBehavior() {
                return ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT;
            }
        };
        ArrayList<E> ciphers = new ArrayList<E>();
        Collections.addAll(ciphers, "ECDHE-RSA-AES128-GCM-SHA256", "ECDHE-RSA-AES128-SHA", "ECDHE-RSA-AES256-SHA", "AES128-GCM-SHA256", "AES128-SHA", "AES256-SHA", "DES-CBC3-SHA");
        DEFAULT_CIPHERS = Collections.unmodifiableList(ciphers);
        if (logger.isDebugEnabled()) {
            logger.debug("Default cipher suite (OpenSSL): " + ciphers);
        }
    }

    private static final class DefaultOpenSslEngineMap
    implements OpenSslEngineMap {
        private final Map<Long, OpenSslEngine> engines = PlatformDependent.newConcurrentHashMap();

        private DefaultOpenSslEngineMap() {
        }

        @Override
        public OpenSslEngine remove(long ssl) {
            return this.engines.remove(ssl);
        }

        @Override
        public void add(OpenSslEngine engine) {
            this.engines.put(engine.sslPointer(), engine);
        }
    }

    abstract class AbstractCertificateVerifier
    implements CertificateVerifier {
        AbstractCertificateVerifier() {
        }

        public final boolean verify(long ssl, byte[][] chain, String auth) {
            X509Certificate[] peerCerts = OpenSslContext.certificates(chain);
            OpenSslEngine engine = OpenSslContext.this.engineMap.remove(ssl);
            try {
                this.verify(engine, peerCerts, auth);
                return true;
            }
            catch (Throwable cause) {
                logger.debug("verification of certificate failed", cause);
                SSLHandshakeException e = new SSLHandshakeException("General OpenSslEngine problem");
                e.initCause(cause);
                engine.handshakeException = e;
                return false;
            }
        }

        abstract void verify(OpenSslEngine var1, X509Certificate[] var2, String var3) throws Exception;
    }

}

