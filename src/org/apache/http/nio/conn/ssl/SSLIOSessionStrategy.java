/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.conn.ssl;

import java.io.IOException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import org.apache.http.HttpHost;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.BrowserCompatHostnameVerifier;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.StrictHostnameVerifier;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.nio.conn.SchemeIOSessionStrategy;
import org.apache.http.nio.reactor.IOSession;
import org.apache.http.nio.reactor.ssl.SSLIOSession;
import org.apache.http.nio.reactor.ssl.SSLMode;
import org.apache.http.nio.reactor.ssl.SSLSetupHandler;
import org.apache.http.util.Args;
import org.apache.http.util.Asserts;
import org.apache.http.util.TextUtils;

public class SSLIOSessionStrategy
implements SchemeIOSessionStrategy {
    public static final X509HostnameVerifier ALLOW_ALL_HOSTNAME_VERIFIER = new AllowAllHostnameVerifier();
    public static final X509HostnameVerifier BROWSER_COMPATIBLE_HOSTNAME_VERIFIER = new BrowserCompatHostnameVerifier();
    public static final X509HostnameVerifier STRICT_HOSTNAME_VERIFIER = new StrictHostnameVerifier();
    private final SSLContext sslContext;
    private final String[] supportedProtocols;
    private final String[] supportedCipherSuites;
    private final X509HostnameVerifier hostnameVerifier;

    private static String[] split(String s) {
        if (TextUtils.isBlank(s)) {
            return null;
        }
        return s.split(" *, *");
    }

    public static SSLIOSessionStrategy getDefaultStrategy() {
        return new SSLIOSessionStrategy(SSLContexts.createDefault(), BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
    }

    public static SSLIOSessionStrategy getSystemDefaultStrategy() {
        return new SSLIOSessionStrategy(SSLContexts.createSystemDefault(), SSLIOSessionStrategy.split(System.getProperty("https.protocols")), SSLIOSessionStrategy.split(System.getProperty("https.cipherSuites")), BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
    }

    public SSLIOSessionStrategy(SSLContext sslContext, String[] supportedProtocols, String[] supportedCipherSuites, X509HostnameVerifier hostnameVerifier) {
        this.sslContext = Args.notNull(sslContext, "SSL context");
        this.supportedProtocols = supportedProtocols;
        this.supportedCipherSuites = supportedCipherSuites;
        this.hostnameVerifier = hostnameVerifier != null ? hostnameVerifier : BROWSER_COMPATIBLE_HOSTNAME_VERIFIER;
    }

    public SSLIOSessionStrategy(SSLContext sslcontext, X509HostnameVerifier hostnameVerifier) {
        this(sslcontext, null, null, hostnameVerifier);
    }

    public SSLIOSessionStrategy(SSLContext sslcontext) {
        this(sslcontext, null, null, BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
    }

    public SSLIOSession upgrade(final HttpHost host, IOSession iosession) throws IOException {
        Asserts.check(!(iosession instanceof SSLIOSession), "I/O session is already upgraded to TLS/SSL");
        SSLIOSession ssliosession = new SSLIOSession(iosession, SSLMode.CLIENT, this.sslContext, new SSLSetupHandler(){

            public void initalize(SSLEngine sslengine) throws SSLException {
                if (SSLIOSessionStrategy.this.supportedProtocols != null) {
                    sslengine.setEnabledProtocols(SSLIOSessionStrategy.this.supportedProtocols);
                }
                if (SSLIOSessionStrategy.this.supportedCipherSuites != null) {
                    sslengine.setEnabledCipherSuites(SSLIOSessionStrategy.this.supportedCipherSuites);
                }
                SSLIOSessionStrategy.this.initializeEngine(sslengine);
            }

            public void verify(IOSession iosession, SSLSession sslsession) throws SSLException {
                SSLIOSessionStrategy.this.verifySession(host, iosession, sslsession);
            }
        });
        iosession.setAttribute("http.session.ssl", ssliosession);
        ssliosession.initialize();
        return ssliosession;
    }

    protected void initializeEngine(SSLEngine engine) {
    }

    protected void verifySession(HttpHost host, IOSession iosession, SSLSession sslsession) throws SSLException {
        Certificate[] certs = sslsession.getPeerCertificates();
        X509Certificate x509 = (X509Certificate)certs[0];
        this.hostnameVerifier.verify(host.getHostName(), x509);
    }

    public boolean isLayeringRequired() {
        return true;
    }

}

