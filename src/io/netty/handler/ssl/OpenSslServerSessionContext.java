/*
 * Decompiled with CFR 0_129.
 * 
 * Could not load the following classes:
 *  org.apache.tomcat.jni.SSLContext
 */
package io.netty.handler.ssl;

import io.netty.handler.ssl.OpenSslSessionContext;
import org.apache.tomcat.jni.SSLContext;

public final class OpenSslServerSessionContext
extends OpenSslSessionContext {
    OpenSslServerSessionContext(long context) {
        super(context);
    }

    @Override
    public void setSessionTimeout(int seconds) {
        if (seconds < 0) {
            throw new IllegalArgumentException();
        }
        SSLContext.setSessionCacheTimeout((long)this.context, (long)seconds);
    }

    @Override
    public int getSessionTimeout() {
        return (int)SSLContext.getSessionCacheTimeout((long)this.context);
    }

    @Override
    public void setSessionCacheSize(int size) {
        if (size < 0) {
            throw new IllegalArgumentException();
        }
        SSLContext.setSessionCacheSize((long)this.context, (long)size);
    }

    @Override
    public int getSessionCacheSize() {
        return (int)SSLContext.getSessionCacheSize((long)this.context);
    }

    @Override
    public void setSessionCacheEnabled(boolean enabled) {
        long mode = enabled ? 2L : 0L;
        SSLContext.setSessionCacheMode((long)this.context, (long)mode);
    }

    @Override
    public boolean isSessionCacheEnabled() {
        return SSLContext.getSessionCacheMode((long)this.context) == 2L;
    }

    public boolean setSessionIdContext(byte[] sidCtx) {
        return SSLContext.setSessionIdContext((long)this.context, (byte[])sidCtx);
    }
}

