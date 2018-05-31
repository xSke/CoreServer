/*
 * Decompiled with CFR 0_129.
 * 
 * Could not load the following classes:
 *  org.apache.tomcat.jni.CertificateVerifier
 *  org.apache.tomcat.jni.SSL
 *  org.apache.tomcat.jni.SSLContext
 */
package io.netty.handler.ssl;

import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.CipherSuiteFilter;
import io.netty.handler.ssl.IdentityCipherSuiteFilter;
import io.netty.handler.ssl.OpenSsl;
import io.netty.handler.ssl.OpenSslApplicationProtocolNegotiator;
import io.netty.handler.ssl.OpenSslContext;
import io.netty.handler.ssl.OpenSslEngine;
import io.netty.handler.ssl.OpenSslServerSessionContext;
import io.netty.handler.ssl.OpenSslSessionContext;
import io.netty.util.internal.EmptyArrays;
import io.netty.util.internal.ObjectUtil;
import java.io.File;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.tomcat.jni.CertificateVerifier;
import org.apache.tomcat.jni.SSL;
import org.apache.tomcat.jni.SSLContext;

public final class OpenSslServerContext
extends OpenSslContext {
    private final OpenSslServerSessionContext sessionContext;

    public OpenSslServerContext(File certChainFile, File keyFile) throws SSLException {
        this(certChainFile, keyFile, null);
    }

    public OpenSslServerContext(File certChainFile, File keyFile, String keyPassword) throws SSLException {
        this(certChainFile, keyFile, keyPassword, null, IdentityCipherSuiteFilter.INSTANCE, ApplicationProtocolConfig.DISABLED, 0L, 0L);
    }

    @Deprecated
    public OpenSslServerContext(File certChainFile, File keyFile, String keyPassword, Iterable<String> ciphers, ApplicationProtocolConfig apn, long sessionCacheSize, long sessionTimeout) throws SSLException {
        this(certChainFile, keyFile, keyPassword, ciphers, IdentityCipherSuiteFilter.INSTANCE, apn, sessionCacheSize, sessionTimeout);
    }

    @Deprecated
    public OpenSslServerContext(File certChainFile, File keyFile, String keyPassword, Iterable<String> ciphers, Iterable<String> nextProtocols, long sessionCacheSize, long sessionTimeout) throws SSLException {
        this(certChainFile, keyFile, keyPassword, ciphers, OpenSslServerContext.toApplicationProtocolConfig(nextProtocols), sessionCacheSize, sessionTimeout);
    }

    @Deprecated
    public OpenSslServerContext(File certChainFile, File keyFile, String keyPassword, TrustManagerFactory trustManagerFactory, Iterable<String> ciphers, ApplicationProtocolConfig config, long sessionCacheSize, long sessionTimeout) throws SSLException {
        this(certChainFile, keyFile, keyPassword, trustManagerFactory, ciphers, OpenSslServerContext.toNegotiator(config), sessionCacheSize, sessionTimeout);
    }

    @Deprecated
    public OpenSslServerContext(File certChainFile, File keyFile, String keyPassword, TrustManagerFactory trustManagerFactory, Iterable<String> ciphers, OpenSslApplicationProtocolNegotiator apn, long sessionCacheSize, long sessionTimeout) throws SSLException {
        this(null, trustManagerFactory, certChainFile, keyFile, keyPassword, null, ciphers, null, apn, sessionCacheSize, sessionTimeout);
    }

    public OpenSslServerContext(File certChainFile, File keyFile, String keyPassword, Iterable<String> ciphers, CipherSuiteFilter cipherFilter, ApplicationProtocolConfig apn, long sessionCacheSize, long sessionTimeout) throws SSLException {
        this(null, null, certChainFile, keyFile, keyPassword, null, ciphers, cipherFilter, apn, sessionCacheSize, sessionTimeout);
    }

    public OpenSslServerContext(File trustCertChainFile, TrustManagerFactory trustManagerFactory, File keyCertChainFile, File keyFile, String keyPassword, KeyManagerFactory keyManagerFactory, Iterable<String> ciphers, CipherSuiteFilter cipherFilter, ApplicationProtocolConfig config, long sessionCacheSize, long sessionTimeout) throws SSLException {
        this(trustCertChainFile, trustManagerFactory, keyCertChainFile, keyFile, keyPassword, keyManagerFactory, ciphers, cipherFilter, OpenSslServerContext.toNegotiator(config), sessionCacheSize, sessionTimeout);
    }

    @Deprecated
    public OpenSslServerContext(File certChainFile, File keyFile, String keyPassword, TrustManagerFactory trustManagerFactory, Iterable<String> ciphers, CipherSuiteFilter cipherFilter, ApplicationProtocolConfig config, long sessionCacheSize, long sessionTimeout) throws SSLException {
        this(null, trustManagerFactory, certChainFile, keyFile, keyPassword, null, ciphers, cipherFilter, OpenSslServerContext.toNegotiator(config), sessionCacheSize, sessionTimeout);
    }

    @Deprecated
    public OpenSslServerContext(File certChainFile, File keyFile, String keyPassword, TrustManagerFactory trustManagerFactory, Iterable<String> ciphers, CipherSuiteFilter cipherFilter, OpenSslApplicationProtocolNegotiator apn, long sessionCacheSize, long sessionTimeout) throws SSLException {
        this(null, trustManagerFactory, certChainFile, keyFile, keyPassword, null, ciphers, cipherFilter, apn, sessionCacheSize, sessionTimeout);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public OpenSslServerContext(File trustCertChainFile, TrustManagerFactory trustManagerFactory, File keyCertChainFile, File keyFile, String keyPassword, KeyManagerFactory keyManagerFactory, Iterable<String> ciphers, CipherSuiteFilter cipherFilter, OpenSslApplicationProtocolNegotiator apn, long sessionCacheSize, long sessionTimeout) throws SSLException {
        super(ciphers, cipherFilter, apn, sessionCacheSize, sessionTimeout, 1);
        OpenSsl.ensureAvailability();
        ObjectUtil.checkNotNull(keyCertChainFile, "keyCertChainFile");
        if (!keyCertChainFile.isFile()) {
            throw new IllegalArgumentException("keyCertChainFile is not a file: " + keyCertChainFile);
        }
        ObjectUtil.checkNotNull(keyFile, "keyFile");
        if (!keyFile.isFile()) {
            throw new IllegalArgumentException("keyFile is not a file: " + keyFile);
        }
        if (keyPassword == null) {
            keyPassword = "";
        }
        boolean success = false;
        try {
            Class<OpenSslContext> class_ = OpenSslContext.class;
            synchronized (OpenSslContext.class) {
                long error;
                SSLContext.setVerify((long)this.ctx, (int)0, (int)10);
                if (!SSLContext.setCertificateChainFile((long)this.ctx, (String)keyCertChainFile.getPath(), (boolean)true) && OpenSsl.isError(error = (long)SSL.getLastErrorNumber())) {
                    String err = SSL.getErrorString((long)error);
                    throw new SSLException("failed to set certificate chain: " + keyCertChainFile + " (" + err + ')');
                }
                try {
                    if (!SSLContext.setCertificate((long)this.ctx, (String)keyCertChainFile.getPath(), (String)keyFile.getPath(), (String)keyPassword, (int)0) && OpenSsl.isError(error = (long)SSL.getLastErrorNumber())) {
                        String err = SSL.getErrorString((long)error);
                        throw new SSLException("failed to set certificate: " + keyCertChainFile + " and " + keyFile + " (" + err + ')');
                    }
                }
                catch (SSLException e) {
                    throw e;
                }
                catch (Exception e) {
                    throw new SSLException("failed to set certificate: " + keyCertChainFile + " and " + keyFile, e);
                }
                try {
                    if (trustManagerFactory == null) {
                        trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                    }
                    if (trustCertChainFile != null) {
                        trustManagerFactory = OpenSslServerContext.buildTrustManagerFactory(trustCertChainFile, trustManagerFactory);
                    } else {
                        char[] keyPasswordChars = keyPassword == null ? EmptyArrays.EMPTY_CHARS : keyPassword.toCharArray();
                        KeyStore ks = OpenSslServerContext.buildKeyStore(keyCertChainFile, keyFile, keyPasswordChars);
                        trustManagerFactory.init(ks);
                    }
                    final X509TrustManager manager = OpenSslServerContext.chooseTrustManager(trustManagerFactory.getTrustManagers());
                    if (OpenSslServerContext.useExtendedTrustManager(manager)) {
                        final X509ExtendedTrustManager extendedManager = (X509ExtendedTrustManager)manager;
                        SSLContext.setCertVerifyCallback((long)this.ctx, (CertificateVerifier)new OpenSslContext.AbstractCertificateVerifier(){

                            @Override
                            void verify(OpenSslEngine engine, X509Certificate[] peerCerts, String auth) throws Exception {
                                extendedManager.checkClientTrusted(peerCerts, auth, engine);
                            }
                        });
                    } else {
                        SSLContext.setCertVerifyCallback((long)this.ctx, (CertificateVerifier)new OpenSslContext.AbstractCertificateVerifier(){

                            @Override
                            void verify(OpenSslEngine engine, X509Certificate[] peerCerts, String auth) throws Exception {
                                manager.checkClientTrusted(peerCerts, auth);
                            }
                        });
                    }
                }
                catch (Exception e) {
                    throw new SSLException("unable to setup trustmanager", e);
                }
                this.sessionContext = new OpenSslServerSessionContext(this.ctx);
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

    @Override
    public OpenSslServerSessionContext sessionContext() {
        return this.sessionContext;
    }

}

