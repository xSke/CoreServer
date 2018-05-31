/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.impl.nio.reactor;

import java.net.SocketAddress;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import org.apache.http.impl.nio.reactor.SSLIOSessionHandler;
import org.apache.http.nio.reactor.IOSession;
import org.apache.http.nio.reactor.ssl.SSLSetupHandler;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

@Deprecated
class SSLIOSessionHandlerAdaptor
implements SSLSetupHandler {
    private final SSLIOSessionHandler handler;
    private HttpParams params;

    public SSLIOSessionHandlerAdaptor(SSLIOSessionHandler handler) {
        this.handler = handler;
    }

    public void initalize(SSLEngine sslengine) throws SSLException {
        this.handler.initalize(sslengine, this.params != null ? this.params : new BasicHttpParams());
    }

    public void verify(IOSession iosession, SSLSession sslsession) throws SSLException {
        this.handler.verify(iosession.getRemoteAddress(), sslsession);
    }

    public void setParams(HttpParams params) {
        this.params = params;
    }
}

