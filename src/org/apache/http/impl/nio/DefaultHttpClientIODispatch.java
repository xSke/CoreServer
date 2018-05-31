/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.impl.nio;

import java.io.IOException;
import javax.net.ssl.SSLContext;
import org.apache.http.annotation.Immutable;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.impl.nio.DefaultNHttpClientConnection;
import org.apache.http.impl.nio.DefaultNHttpClientConnectionFactory;
import org.apache.http.impl.nio.SSLNHttpClientConnectionFactory;
import org.apache.http.impl.nio.reactor.AbstractIODispatch;
import org.apache.http.nio.NHttpClientConnection;
import org.apache.http.nio.NHttpClientEventHandler;
import org.apache.http.nio.NHttpConnectionFactory;
import org.apache.http.nio.reactor.IOSession;
import org.apache.http.nio.reactor.ssl.SSLSetupHandler;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Args;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
@Immutable
public class DefaultHttpClientIODispatch
extends AbstractIODispatch<DefaultNHttpClientConnection> {
    private final NHttpClientEventHandler handler;
    private final NHttpConnectionFactory<DefaultNHttpClientConnection> connFactory;

    public DefaultHttpClientIODispatch(NHttpClientEventHandler handler, NHttpConnectionFactory<DefaultNHttpClientConnection> connFactory) {
        this.handler = Args.notNull(handler, "HTTP client handler");
        this.connFactory = Args.notNull(connFactory, "HTTP client connection factory");
    }

    @Deprecated
    public DefaultHttpClientIODispatch(NHttpClientEventHandler handler, HttpParams params) {
        this(handler, new DefaultNHttpClientConnectionFactory(params));
    }

    @Deprecated
    public DefaultHttpClientIODispatch(NHttpClientEventHandler handler, SSLContext sslcontext, SSLSetupHandler sslHandler, HttpParams params) {
        this(handler, new SSLNHttpClientConnectionFactory(sslcontext, sslHandler, params));
    }

    @Deprecated
    public DefaultHttpClientIODispatch(NHttpClientEventHandler handler, SSLContext sslcontext, HttpParams params) {
        this(handler, sslcontext, null, params);
    }

    public DefaultHttpClientIODispatch(NHttpClientEventHandler handler, ConnectionConfig config) {
        this(handler, new DefaultNHttpClientConnectionFactory(config));
    }

    public DefaultHttpClientIODispatch(NHttpClientEventHandler handler, SSLContext sslcontext, SSLSetupHandler sslHandler, ConnectionConfig config) {
        this(handler, new SSLNHttpClientConnectionFactory(sslcontext, sslHandler, config));
    }

    public DefaultHttpClientIODispatch(NHttpClientEventHandler handler, SSLContext sslcontext, ConnectionConfig config) {
        this(handler, new SSLNHttpClientConnectionFactory(sslcontext, null, config));
    }

    @Override
    protected DefaultNHttpClientConnection createConnection(IOSession session) {
        return this.connFactory.createConnection(session);
    }

    @Override
    protected void onConnected(DefaultNHttpClientConnection conn) {
        Object attachment = conn.getContext().getAttribute("http.session.attachment");
        try {
            this.handler.connected(conn, attachment);
        }
        catch (Exception ex) {
            this.handler.exception(conn, ex);
        }
    }

    @Override
    protected void onClosed(DefaultNHttpClientConnection conn) {
        this.handler.closed(conn);
    }

    @Override
    protected void onException(DefaultNHttpClientConnection conn, IOException ex) {
        this.handler.exception(conn, ex);
    }

    @Override
    protected void onInputReady(DefaultNHttpClientConnection conn) {
        conn.consumeInput(this.handler);
    }

    @Override
    protected void onOutputReady(DefaultNHttpClientConnection conn) {
        conn.produceOutput(this.handler);
    }

    @Override
    protected void onTimeout(DefaultNHttpClientConnection conn) {
        try {
            this.handler.timeout(conn);
        }
        catch (Exception ex) {
            this.handler.exception(conn, ex);
        }
    }
}

