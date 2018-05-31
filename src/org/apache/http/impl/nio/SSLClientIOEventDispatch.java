/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.impl.nio;

import java.io.IOException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import org.apache.http.HttpResponseFactory;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.nio.DefaultNHttpClientConnection;
import org.apache.http.impl.nio.reactor.SSLIOSession;
import org.apache.http.impl.nio.reactor.SSLIOSessionHandler;
import org.apache.http.impl.nio.reactor.SSLMode;
import org.apache.http.nio.NHttpClientConnection;
import org.apache.http.nio.NHttpClientHandler;
import org.apache.http.nio.NHttpClientIOTarget;
import org.apache.http.nio.reactor.IOEventDispatch;
import org.apache.http.nio.reactor.IOSession;
import org.apache.http.nio.util.ByteBufferAllocator;
import org.apache.http.nio.util.HeapByteBufferAllocator;
import org.apache.http.params.HttpParams;
import org.apache.http.util.Args;

@Deprecated
public class SSLClientIOEventDispatch
implements IOEventDispatch {
    private static final String SSL_SESSION = "SSL_SESSION";
    protected final NHttpClientHandler handler;
    protected final SSLContext sslcontext;
    protected final SSLIOSessionHandler sslHandler;
    protected final HttpParams params;

    public SSLClientIOEventDispatch(NHttpClientHandler handler, SSLContext sslcontext, SSLIOSessionHandler sslHandler, HttpParams params) {
        Args.notNull(handler, "HTTP client handler");
        Args.notNull(sslcontext, "SSL context");
        Args.notNull(params, "HTTP parameters");
        this.handler = handler;
        this.params = params;
        this.sslcontext = sslcontext;
        this.sslHandler = sslHandler;
    }

    public SSLClientIOEventDispatch(NHttpClientHandler handler, SSLContext sslcontext, HttpParams params) {
        this(handler, sslcontext, null, params);
    }

    protected ByteBufferAllocator createByteBufferAllocator() {
        return HeapByteBufferAllocator.INSTANCE;
    }

    protected HttpResponseFactory createHttpResponseFactory() {
        return DefaultHttpResponseFactory.INSTANCE;
    }

    protected NHttpClientIOTarget createConnection(IOSession session) {
        return new DefaultNHttpClientConnection(session, this.createHttpResponseFactory(), this.createByteBufferAllocator(), this.params);
    }

    protected SSLIOSession createSSLIOSession(IOSession session, SSLContext sslcontext, SSLIOSessionHandler sslHandler) {
        return new SSLIOSession(session, sslcontext, sslHandler);
    }

    public void connected(IOSession session) {
        SSLIOSession sslSession = this.createSSLIOSession(session, this.sslcontext, this.sslHandler);
        NHttpClientIOTarget conn = this.createConnection(sslSession);
        session.setAttribute("http.connection", conn);
        session.setAttribute(SSL_SESSION, sslSession);
        Object attachment = session.getAttribute("http.session.attachment");
        this.handler.connected(conn, attachment);
        try {
            sslSession.bind(SSLMode.CLIENT, this.params);
        }
        catch (SSLException ex) {
            this.handler.exception((NHttpClientConnection)conn, ex);
            sslSession.shutdown();
        }
    }

    public void disconnected(IOSession session) {
        NHttpClientIOTarget conn = (NHttpClientIOTarget)session.getAttribute("http.connection");
        if (conn != null) {
            this.handler.closed(conn);
        }
    }

    public void inputReady(IOSession session) {
        NHttpClientIOTarget conn = (NHttpClientIOTarget)session.getAttribute("http.connection");
        SSLIOSession sslSession = (SSLIOSession)session.getAttribute(SSL_SESSION);
        try {
            if (sslSession.isAppInputReady()) {
                conn.consumeInput(this.handler);
            }
            sslSession.inboundTransport();
        }
        catch (IOException ex) {
            this.handler.exception((NHttpClientConnection)conn, ex);
            sslSession.shutdown();
        }
    }

    public void outputReady(IOSession session) {
        NHttpClientIOTarget conn = (NHttpClientIOTarget)session.getAttribute("http.connection");
        SSLIOSession sslSession = (SSLIOSession)session.getAttribute(SSL_SESSION);
        try {
            if (sslSession.isAppOutputReady()) {
                conn.produceOutput(this.handler);
            }
            sslSession.outboundTransport();
        }
        catch (IOException ex) {
            this.handler.exception((NHttpClientConnection)conn, ex);
            sslSession.shutdown();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void timeout(IOSession session) {
        NHttpClientIOTarget conn = (NHttpClientIOTarget)session.getAttribute("http.connection");
        SSLIOSession sslSession = (SSLIOSession)session.getAttribute(SSL_SESSION);
        this.handler.timeout(conn);
        SSLIOSession sSLIOSession = sslSession;
        synchronized (sSLIOSession) {
            if (sslSession.isOutboundDone() && !sslSession.isInboundDone()) {
                sslSession.shutdown();
            }
        }
    }
}

