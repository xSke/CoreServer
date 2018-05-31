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
import io.netty.handler.ssl.OpenSslContext;
import io.netty.handler.ssl.OpenSslEngine;
import io.netty.handler.ssl.OpenSslSessionContext;
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

public final class OpenSslClientContext
extends OpenSslContext {
    private final OpenSslSessionContext sessionContext;

    public OpenSslClientContext() throws SSLException {
        this(null, null, null, null, null, null, null, IdentityCipherSuiteFilter.INSTANCE, null, 0L, 0L);
    }

    public OpenSslClientContext(File certChainFile) throws SSLException {
        this(certChainFile, null);
    }

    public OpenSslClientContext(TrustManagerFactory trustManagerFactory) throws SSLException {
        this(null, trustManagerFactory);
    }

    public OpenSslClientContext(File certChainFile, TrustManagerFactory trustManagerFactory) throws SSLException {
        this(certChainFile, trustManagerFactory, null, null, null, null, null, IdentityCipherSuiteFilter.INSTANCE, null, 0L, 0L);
    }

    @Deprecated
    public OpenSslClientContext(File certChainFile, TrustManagerFactory trustManagerFactory, Iterable<String> ciphers, ApplicationProtocolConfig apn, long sessionCacheSize, long sessionTimeout) throws SSLException {
        this(certChainFile, trustManagerFactory, null, null, null, null, ciphers, IdentityCipherSuiteFilter.INSTANCE, apn, sessionCacheSize, sessionTimeout);
    }

    @Deprecated
    public OpenSslClientContext(File certChainFile, TrustManagerFactory trustManagerFactory, Iterable<String> ciphers, CipherSuiteFilter cipherFilter, ApplicationProtocolConfig apn, long sessionCacheSize, long sessionTimeout) throws SSLException {
        this(certChainFile, trustManagerFactory, null, null, null, null, ciphers, cipherFilter, apn, sessionCacheSize, sessionTimeout);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public OpenSslClientContext(File trustCertChainFile, TrustManagerFactory trustManagerFactory, File keyCertChainFile, File keyFile, String keyPassword, KeyManagerFactory keyManagerFactory, Iterable<String> ciphers, CipherSuiteFilter cipherFilter, ApplicationProtocolConfig apn, long sessionCacheSize, long sessionTimeout) throws SSLException {
        super(ciphers, cipherFilter, apn, sessionCacheSize, sessionTimeout, 0);
        boolean success = false;
        try {
            if (trustCertChainFile != null && !trustCertChainFile.isFile()) {
                throw new IllegalArgumentException("trustCertChainFile is not a file: " + trustCertChainFile);
            }
            if (keyCertChainFile != null && !keyCertChainFile.isFile()) {
                throw new IllegalArgumentException("keyCertChainFile is not a file: " + keyCertChainFile);
            }
            if (keyFile != null && !keyFile.isFile()) {
                throw new IllegalArgumentException("keyFile is not a file: " + keyFile);
            }
            if (keyFile == null && keyCertChainFile != null || keyFile != null && keyCertChainFile == null) {
                throw new IllegalArgumentException("Either both keyCertChainFile and keyFile needs to be null or none of them");
            }
            Class<OpenSslContext> class_ = OpenSslContext.class;
            synchronized (OpenSslContext.class) {
                long error;
                if (trustCertChainFile != null && !SSLContext.setCertificateChainFile((long)this.ctx, (String)trustCertChainFile.getPath(), (boolean)false) && OpenSsl.isError(error = (long)SSL.getLastErrorNumber())) {
                    throw new SSLException("failed to set certificate chain: " + trustCertChainFile + " (" + SSL.getErrorString((long)error) + ')');
                }
                if (keyCertChainFile != null && keyFile != null) {
                    try {
                        if (!SSLContext.setCertificate((long)this.ctx, (String)keyCertChainFile.getPath(), (String)keyFile.getPath(), (String)keyPassword, (int)0) && OpenSsl.isError(error = (long)SSL.getLastErrorNumber())) {
                            throw new SSLException("failed to set certificate: " + keyCertChainFile + " and " + keyFile + " (" + SSL.getErrorString((long)error) + ')');
                        }
                    }
                    catch (SSLException e) {
                        throw e;
                    }
                    catch (Exception e) {
                        throw new SSLException("failed to set certificate: " + keyCertChainFile + " and " + keyFile, e);
                    }
                }
                SSLContext.setVerify((long)this.ctx, (int)0, (int)10);
                try {
                    if (trustCertChainFile != null) {
                        trustManagerFactory = OpenSslClientContext.buildTrustManagerFactory(trustCertChainFile, trustManagerFactory);
                    } else if (trustManagerFactory == null) {
                        trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                        trustManagerFactory.init((KeyStore)null);
                    }
                    final X509TrustManager manager = OpenSslClientContext.chooseTrustManager(trustManagerFactory.getTrustManagers());
                    if (OpenSslClientContext.useExtendedTrustManager(manager)) {
                        final X509ExtendedTrustManager extendedManager = (X509ExtendedTrustManager)manager;
                        SSLContext.setCertVerifyCallback((long)this.ctx, (CertificateVerifier)new OpenSslContext.AbstractCertificateVerifier(){

                            @Override
                            void verify(OpenSslEngine engine, X509Certificate[] peerCerts, String auth) throws Exception {
                                extendedManager.checkServerTrusted(peerCerts, auth, engine);
                            }
                        });
                    } else {
                        SSLContext.setCertVerifyCallback((long)this.ctx, (CertificateVerifier)new OpenSslContext.AbstractCertificateVerifier(){

                            @Override
                            void verify(OpenSslEngine engine, X509Certificate[] peerCerts, String auth) throws Exception {
                                manager.checkServerTrusted(peerCerts, auth);
                            }
                        });
                    }
                }
                catch (Exception e) {
                    throw new SSLException("unable to setup trustmanager", e);
                }
                this.sessionContext = new OpenSslClientSessionContext(this.ctx);
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
    public OpenSslSessionContext sessionContext() {
        return this.sessionContext;
    }

    private static final class OpenSslClientSessionContext
    extends OpenSslSessionContext {
        private OpenSslClientSessionContext(long context) {
            super(context);
        }

        @Override
        public void setSessionTimeout(int seconds) {
            if (seconds < 0) {
                throw new IllegalArgumentException();
            }
        }

        @Override
        public int getSessionTimeout() {
            return 0;
        }

        @Override
        public void setSessionCacheSize(int size) {
            if (size < 0) {
                throw new IllegalArgumentException();
            }
        }

        @Override
        public int getSessionCacheSize() {
            return 0;
        }

        @Override
        public void setSessionCacheEnabled(boolean enabled) {
        }

        @Override
        public boolean isSessionCacheEnabled() {
            return false;
        }
    }

}

