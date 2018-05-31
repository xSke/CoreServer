/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.impl.nio.reactor;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import org.apache.http.impl.nio.reactor.SSLSetupHandler;
import org.apache.http.nio.reactor.IOSession;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

@Deprecated
class SSLSetupHandlerAdaptor
implements org.apache.http.nio.reactor.ssl.SSLSetupHandler {
    private final SSLSetupHandler handler;
    private HttpParams params;

    public SSLSetupHandlerAdaptor(SSLSetupHandler handler) {
        this.handler = handler;
    }

    public void initalize(SSLEngine sslengine) throws SSLException {
        this.handler.initalize(sslengine, this.params != null ? this.params : new BasicHttpParams());
    }

    public void verify(IOSession iosession, SSLSession sslsession) throws SSLException {
        this.handler.verify(iosession, sslsession);
    }

    public void setParams(HttpParams params) {
        this.params = params;
    }
}

