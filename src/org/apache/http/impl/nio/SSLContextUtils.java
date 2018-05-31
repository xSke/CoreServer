/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.impl.nio;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

class SSLContextUtils {
    SSLContextUtils() {
    }

    static SSLContext getDefault() {
        SSLContext sslcontext;
        try {
            try {
                sslcontext = SSLContext.getInstance("Default");
            }
            catch (NoSuchAlgorithmException ex) {
                sslcontext = SSLContext.getInstance("TLS");
            }
            sslcontext.init(null, null, null);
        }
        catch (Exception ex) {
            throw new IllegalStateException("Failure initializing default SSL context", ex);
        }
        return sslcontext;
    }
}

