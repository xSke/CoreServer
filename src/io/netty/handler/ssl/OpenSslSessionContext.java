/*
 * Decompiled with CFR 0_129.
 * 
 * Could not load the following classes:
 *  org.apache.tomcat.jni.SSLContext
 */
package io.netty.handler.ssl;

import io.netty.handler.ssl.OpenSslSessionStats;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSessionContext;
import org.apache.tomcat.jni.SSLContext;

public abstract class OpenSslSessionContext
implements SSLSessionContext {
    private static final Enumeration<byte[]> EMPTY = new EmptyEnumeration();
    private final OpenSslSessionStats stats;
    final long context;

    OpenSslSessionContext(long context) {
        this.context = context;
        this.stats = new OpenSslSessionStats(context);
    }

    @Override
    public SSLSession getSession(byte[] bytes) {
        if (bytes == null) {
            throw new NullPointerException("bytes");
        }
        return null;
    }

    @Override
    public Enumeration<byte[]> getIds() {
        return EMPTY;
    }

    public void setTicketKeys(byte[] keys) {
        if (keys == null) {
            throw new NullPointerException("keys");
        }
        SSLContext.setSessionTicketKeys((long)this.context, (byte[])keys);
    }

    public abstract void setSessionCacheEnabled(boolean var1);

    public abstract boolean isSessionCacheEnabled();

    public OpenSslSessionStats stats() {
        return this.stats;
    }

    private static final class EmptyEnumeration
    implements Enumeration<byte[]> {
        private EmptyEnumeration() {
        }

        @Override
        public boolean hasMoreElements() {
            return false;
        }

        @Override
        public byte[] nextElement() {
            throw new NoSuchElementException();
        }
    }

}

