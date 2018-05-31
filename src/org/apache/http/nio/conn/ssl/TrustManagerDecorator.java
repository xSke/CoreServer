/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.conn.ssl;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.X509TrustManager;
import org.apache.http.conn.ssl.TrustStrategy;

@Deprecated
class TrustManagerDecorator
implements X509TrustManager {
    private final X509TrustManager trustManager;
    private final TrustStrategy trustStrategy;

    TrustManagerDecorator(X509TrustManager trustManager, TrustStrategy trustStrategy) {
        this.trustManager = trustManager;
        this.trustStrategy = trustStrategy;
    }

    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        this.trustManager.checkClientTrusted(chain, authType);
    }

    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        if (!this.trustStrategy.isTrusted(chain, authType)) {
            this.trustManager.checkServerTrusted(chain, authType);
        }
    }

    public X509Certificate[] getAcceptedIssuers() {
        return this.trustManager.getAcceptedIssuers();
    }
}

