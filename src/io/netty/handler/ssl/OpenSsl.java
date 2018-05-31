/*
 * Decompiled with CFR 0_129.
 * 
 * Could not load the following classes:
 *  org.apache.tomcat.jni.Library
 *  org.apache.tomcat.jni.Pool
 *  org.apache.tomcat.jni.SSL
 *  org.apache.tomcat.jni.SSLContext
 */
package io.netty.handler.ssl;

import io.netty.handler.ssl.CipherSuiteConverter;
import io.netty.handler.ssl.OpenSslEngine;
import io.netty.util.internal.NativeLibraryLoader;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import org.apache.tomcat.jni.Library;
import org.apache.tomcat.jni.Pool;
import org.apache.tomcat.jni.SSL;
import org.apache.tomcat.jni.SSLContext;

public final class OpenSsl {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(OpenSsl.class);
    private static final Throwable UNAVAILABILITY_CAUSE;
    private static final Set<String> AVAILABLE_CIPHER_SUITES;

    public static boolean isAvailable() {
        return UNAVAILABILITY_CAUSE == null;
    }

    public static boolean isAlpnSupported() {
        return (long)OpenSsl.version() >= 268443648L;
    }

    public static int version() {
        if (OpenSsl.isAvailable()) {
            return SSL.version();
        }
        return -1;
    }

    public static String versionString() {
        if (OpenSsl.isAvailable()) {
            return SSL.versionString();
        }
        return null;
    }

    public static void ensureAvailability() {
        if (UNAVAILABILITY_CAUSE != null) {
            throw (Error)new UnsatisfiedLinkError("failed to load the required native library").initCause(UNAVAILABILITY_CAUSE);
        }
    }

    public static Throwable unavailabilityCause() {
        return UNAVAILABILITY_CAUSE;
    }

    public static Set<String> availableCipherSuites() {
        return AVAILABLE_CIPHER_SUITES;
    }

    public static boolean isCipherSuiteAvailable(String cipherSuite) {
        String converted = CipherSuiteConverter.toOpenSsl(cipherSuite);
        if (converted != null) {
            cipherSuite = converted;
        }
        return AVAILABLE_CIPHER_SUITES.contains(cipherSuite);
    }

    static boolean isError(long errorCode) {
        return errorCode != 0L;
    }

    private OpenSsl() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static {
        Throwable cause = null;
        try {
            Class.forName("org.apache.tomcat.jni.SSL", false, OpenSsl.class.getClassLoader());
        }
        catch (ClassNotFoundException t) {
            cause = t;
            logger.debug("netty-tcnative not in the classpath; " + OpenSslEngine.class.getSimpleName() + " will be unavailable.");
        }
        if (cause == null) {
            try {
                NativeLibraryLoader.load("netty-tcnative", SSL.class.getClassLoader());
                Library.initialize((String)"provided");
                SSL.initialize((String)null);
            }
            catch (Throwable t) {
                cause = t;
                logger.debug("Failed to load netty-tcnative; " + OpenSslEngine.class.getSimpleName() + " will be unavailable. " + "See http://netty.io/wiki/forked-tomcat-native.html for more information.", t);
            }
        }
        UNAVAILABILITY_CAUSE = cause;
        if (cause == null) {
            LinkedHashSet<String> availableCipherSuites;
            availableCipherSuites = new LinkedHashSet<String>(128);
            long aprPool = Pool.create((long)0L);
            try {
                long sslCtx = SSLContext.make((long)aprPool, (int)28, (int)1);
                try {
                    SSLContext.setOptions((long)sslCtx, (int)4095);
                    SSLContext.setCipherSuite((long)sslCtx, (String)"ALL");
                    long ssl = SSL.newSSL((long)sslCtx, (boolean)true);
                    try {
                        for (String c : SSL.getCiphers((long)ssl)) {
                            if (c == null || c.length() == 0 || availableCipherSuites.contains(c)) continue;
                            availableCipherSuites.add(c);
                        }
                    }
                    finally {
                        SSL.freeSSL((long)ssl);
                    }
                }
                finally {
                    SSLContext.free((long)sslCtx);
                }
            }
            catch (Exception e) {
                logger.warn("Failed to get the list of available OpenSSL cipher suites.", e);
            }
            finally {
                Pool.destroy((long)aprPool);
            }
            AVAILABLE_CIPHER_SUITES = Collections.unmodifiableSet(availableCipherSuites);
        } else {
            AVAILABLE_CIPHER_SUITES = Collections.emptySet();
        }
    }
}

