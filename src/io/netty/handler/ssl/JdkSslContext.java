/*
 * Decompiled with CFR 0_129.
 */
package io.netty.handler.ssl;

import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.ApplicationProtocolNegotiator;
import io.netty.handler.ssl.CipherSuiteFilter;
import io.netty.handler.ssl.JdkAlpnApplicationProtocolNegotiator;
import io.netty.handler.ssl.JdkApplicationProtocolNegotiator;
import io.netty.handler.ssl.JdkDefaultApplicationProtocolNegotiator;
import io.netty.handler.ssl.JdkNpnApplicationProtocolNegotiator;
import io.netty.handler.ssl.SslContext;
import io.netty.util.internal.EmptyArrays;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import java.io.File;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.crypto.NoSuchPaddingException;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.TrustManager;

public abstract class JdkSslContext
extends SslContext {
    private static final InternalLogger logger;
    static final String PROTOCOL = "TLS";
    static final String[] PROTOCOLS;
    static final List<String> DEFAULT_CIPHERS;
    static final Set<String> SUPPORTED_CIPHERS;
    private final String[] cipherSuites;
    private final List<String> unmodifiableCipherSuites;
    private final JdkApplicationProtocolNegotiator apn;

    private static /* varargs */ void addIfSupported(Set<String> supported, List<String> enabled, String ... names) {
        for (String n : names) {
            if (!supported.contains(n)) continue;
            enabled.add(n);
        }
    }

    JdkSslContext(Iterable<String> ciphers, CipherSuiteFilter cipherFilter, ApplicationProtocolConfig config, boolean isServer) {
        this(ciphers, cipherFilter, JdkSslContext.toNegotiator(config, isServer));
    }

    JdkSslContext(Iterable<String> ciphers, CipherSuiteFilter cipherFilter, JdkApplicationProtocolNegotiator apn) {
        this.apn = ObjectUtil.checkNotNull(apn, "apn");
        this.cipherSuites = ObjectUtil.checkNotNull(cipherFilter, "cipherFilter").filterCipherSuites(ciphers, DEFAULT_CIPHERS, SUPPORTED_CIPHERS);
        this.unmodifiableCipherSuites = Collections.unmodifiableList(Arrays.asList(this.cipherSuites));
    }

    public abstract SSLContext context();

    @Override
    public final SSLSessionContext sessionContext() {
        if (this.isServer()) {
            return this.context().getServerSessionContext();
        }
        return this.context().getClientSessionContext();
    }

    @Override
    public final List<String> cipherSuites() {
        return this.unmodifiableCipherSuites;
    }

    @Override
    public final long sessionCacheSize() {
        return this.sessionContext().getSessionCacheSize();
    }

    @Override
    public final long sessionTimeout() {
        return this.sessionContext().getSessionTimeout();
    }

    @Override
    public final SSLEngine newEngine(ByteBufAllocator alloc) {
        SSLEngine engine = this.context().createSSLEngine();
        engine.setEnabledCipherSuites(this.cipherSuites);
        engine.setEnabledProtocols(PROTOCOLS);
        engine.setUseClientMode(this.isClient());
        return this.wrapEngine(engine);
    }

    @Override
    public final SSLEngine newEngine(ByteBufAllocator alloc, String peerHost, int peerPort) {
        SSLEngine engine = this.context().createSSLEngine(peerHost, peerPort);
        engine.setEnabledCipherSuites(this.cipherSuites);
        engine.setEnabledProtocols(PROTOCOLS);
        engine.setUseClientMode(this.isClient());
        return this.wrapEngine(engine);
    }

    private SSLEngine wrapEngine(SSLEngine engine) {
        return this.apn.wrapperFactory().wrapSslEngine(engine, this.apn, this.isServer());
    }

    @Override
    public JdkApplicationProtocolNegotiator applicationProtocolNegotiator() {
        return this.apn;
    }

    static JdkApplicationProtocolNegotiator toNegotiator(ApplicationProtocolConfig config, boolean isServer) {
        if (config == null) {
            return JdkDefaultApplicationProtocolNegotiator.INSTANCE;
        }
        switch (config.protocol()) {
            case NONE: {
                return JdkDefaultApplicationProtocolNegotiator.INSTANCE;
            }
            case ALPN: {
                if (isServer) {
                    switch (config.selectorFailureBehavior()) {
                        case FATAL_ALERT: {
                            return new JdkAlpnApplicationProtocolNegotiator(true, config.supportedProtocols());
                        }
                        case NO_ADVERTISE: {
                            return new JdkAlpnApplicationProtocolNegotiator(false, config.supportedProtocols());
                        }
                    }
                    throw new UnsupportedOperationException("JDK provider does not support " + (Object)((Object)config.selectorFailureBehavior()) + " failure behavior");
                }
                switch (config.selectedListenerFailureBehavior()) {
                    case ACCEPT: {
                        return new JdkAlpnApplicationProtocolNegotiator(false, config.supportedProtocols());
                    }
                    case FATAL_ALERT: {
                        return new JdkAlpnApplicationProtocolNegotiator(true, config.supportedProtocols());
                    }
                }
                throw new UnsupportedOperationException("JDK provider does not support " + (Object)((Object)config.selectedListenerFailureBehavior()) + " failure behavior");
            }
            case NPN: {
                if (isServer) {
                    switch (config.selectedListenerFailureBehavior()) {
                        case ACCEPT: {
                            return new JdkNpnApplicationProtocolNegotiator(false, config.supportedProtocols());
                        }
                        case FATAL_ALERT: {
                            return new JdkNpnApplicationProtocolNegotiator(true, config.supportedProtocols());
                        }
                    }
                    throw new UnsupportedOperationException("JDK provider does not support " + (Object)((Object)config.selectedListenerFailureBehavior()) + " failure behavior");
                }
                switch (config.selectorFailureBehavior()) {
                    case FATAL_ALERT: {
                        return new JdkNpnApplicationProtocolNegotiator(true, config.supportedProtocols());
                    }
                    case NO_ADVERTISE: {
                        return new JdkNpnApplicationProtocolNegotiator(false, config.supportedProtocols());
                    }
                }
                throw new UnsupportedOperationException("JDK provider does not support " + (Object)((Object)config.selectorFailureBehavior()) + " failure behavior");
            }
        }
        throw new UnsupportedOperationException("JDK provider does not support " + (Object)((Object)config.protocol()) + " protocol");
    }

    protected static KeyManagerFactory buildKeyManagerFactory(File certChainFile, File keyFile, String keyPassword, KeyManagerFactory kmf) throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, InvalidAlgorithmParameterException, CertificateException, KeyException, IOException {
        String algorithm = Security.getProperty("ssl.KeyManagerFactory.algorithm");
        if (algorithm == null) {
            algorithm = "SunX509";
        }
        return JdkSslContext.buildKeyManagerFactory(certChainFile, algorithm, keyFile, keyPassword, kmf);
    }

    protected static KeyManagerFactory buildKeyManagerFactory(File certChainFile, String keyAlgorithm, File keyFile, String keyPassword, KeyManagerFactory kmf) throws KeyStoreException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, InvalidAlgorithmParameterException, IOException, CertificateException, KeyException, UnrecoverableKeyException {
        char[] keyPasswordChars = keyPassword == null ? EmptyArrays.EMPTY_CHARS : keyPassword.toCharArray();
        KeyStore ks = JdkSslContext.buildKeyStore(certChainFile, keyFile, keyPasswordChars);
        if (kmf == null) {
            kmf = KeyManagerFactory.getInstance(keyAlgorithm);
        }
        kmf.init(ks, keyPasswordChars);
        return kmf;
    }

    static {
        int i;
        SSLContext context;
        logger = InternalLoggerFactory.getInstance(JdkSslContext.class);
        try {
            context = SSLContext.getInstance("TLS");
            context.init(null, null, null);
        }
        catch (Exception e) {
            throw new Error("failed to initialize the default SSL context", e);
        }
        SSLEngine engine = context.createSSLEngine();
        String[] supportedProtocols = engine.getSupportedProtocols();
        HashSet<String> supportedProtocolsSet = new HashSet<String>(supportedProtocols.length);
        for (i = 0; i < supportedProtocols.length; ++i) {
            supportedProtocolsSet.add(supportedProtocols[i]);
        }
        ArrayList<String> protocols = new ArrayList<String>();
        JdkSslContext.addIfSupported(supportedProtocolsSet, protocols, "TLSv1.2", "TLSv1.1", "TLSv1");
        PROTOCOLS = !protocols.isEmpty() ? protocols.toArray(new String[protocols.size()]) : engine.getEnabledProtocols();
        String[] supportedCiphers = engine.getSupportedCipherSuites();
        SUPPORTED_CIPHERS = new HashSet<String>(supportedCiphers.length);
        for (i = 0; i < supportedCiphers.length; ++i) {
            SUPPORTED_CIPHERS.add(supportedCiphers[i]);
        }
        ArrayList<String> ciphers = new ArrayList<String>();
        JdkSslContext.addIfSupported(SUPPORTED_CIPHERS, ciphers, "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256", "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA", "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA", "TLS_RSA_WITH_AES_128_GCM_SHA256", "TLS_RSA_WITH_AES_128_CBC_SHA", "TLS_RSA_WITH_AES_256_CBC_SHA", "SSL_RSA_WITH_3DES_EDE_CBC_SHA");
        if (ciphers.isEmpty()) {
            for (String cipher : engine.getEnabledCipherSuites()) {
                if (cipher.contains("_RC4_")) continue;
                ciphers.add(cipher);
            }
        }
        DEFAULT_CIPHERS = Collections.unmodifiableList(ciphers);
        if (logger.isDebugEnabled()) {
            logger.debug("Default protocols (JDK): {} ", (Object)Arrays.asList(PROTOCOLS));
            logger.debug("Default cipher suites (JDK): {}", (Object)DEFAULT_CIPHERS);
        }
    }

}

