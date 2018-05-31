/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.impl.nio.reactor;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import org.apache.http.impl.nio.reactor.SSLIOSessionHandler;
import org.apache.http.impl.nio.reactor.SSLIOSessionHandlerAdaptor;
import org.apache.http.impl.nio.reactor.SSLMode;
import org.apache.http.impl.nio.reactor.SSLSetupHandler;
import org.apache.http.impl.nio.reactor.SSLSetupHandlerAdaptor;
import org.apache.http.nio.reactor.IOSession;
import org.apache.http.params.HttpParams;

@Deprecated
public class SSLIOSession
extends org.apache.http.nio.reactor.ssl.SSLIOSession {
    public SSLIOSession(IOSession session, SSLContext sslContext, SSLSetupHandler handler) {
        super(session, org.apache.http.nio.reactor.ssl.SSLMode.CLIENT, sslContext, handler != null ? new SSLSetupHandlerAdaptor(handler) : null);
    }

    public SSLIOSession(IOSession session, SSLContext sslContext, SSLIOSessionHandler handler) {
        super(session, org.apache.http.nio.reactor.ssl.SSLMode.CLIENT, sslContext, handler != null ? new SSLIOSessionHandlerAdaptor(handler) : null);
    }

    public synchronized void bind(SSLMode mode, HttpParams params) throws SSLException {
        org.apache.http.nio.reactor.ssl.SSLSetupHandler handler = this.getSSLSetupHandler();
        if (handler instanceof SSLIOSessionHandlerAdaptor) {
            ((SSLIOSessionHandlerAdaptor)handler).setParams(params);
        } else if (handler instanceof SSLSetupHandlerAdaptor) {
            ((SSLSetupHandlerAdaptor)handler).setParams(params);
        }
        this.initialize(this.convert(mode));
    }

    private org.apache.http.nio.reactor.ssl.SSLMode convert(SSLMode mode) {
        switch (mode) {
            case CLIENT: {
                return org.apache.http.nio.reactor.ssl.SSLMode.CLIENT;
            }
            case SERVER: {
                return org.apache.http.nio.reactor.ssl.SSLMode.SERVER;
            }
        }
        return null;
    }

}

