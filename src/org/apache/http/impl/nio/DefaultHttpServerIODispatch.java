/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.impl.nio;

import java.io.IOException;
import javax.net.ssl.SSLContext;
import org.apache.http.annotation.Immutable;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.impl.nio.DefaultNHttpServerConnection;
import org.apache.http.impl.nio.DefaultNHttpServerConnectionFactory;
import org.apache.http.impl.nio.SSLNHttpServerConnectionFactory;
import org.apache.http.impl.nio.reactor.AbstractIODispatch;
import org.apache.http.nio.NHttpConnectionFactory;
import org.apache.http.nio.NHttpServerConnection;
import org.apache.http.nio.NHttpServerEventHandler;
import org.apache.http.nio.reactor.IOSession;
import org.apache.http.nio.reactor.ssl.SSLSetupHandler;
import org.apache.http.params.HttpParams;
import org.apache.http.util.Args;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
@Immutable
public class DefaultHttpServerIODispatch
extends AbstractIODispatch<DefaultNHttpServerConnection> {
    private final NHttpServerEventHandler handler;
    private final NHttpConnectionFactory<DefaultNHttpServerConnection> connFactory;

    public DefaultHttpServerIODispatch(NHttpServerEventHandler handler, NHttpConnectionFactory<DefaultNHttpServerConnection> connFactory) {
        this.handler = Args.notNull(handler, "HTTP client handler");
        this.connFactory = Args.notNull(connFactory, "HTTP server connection factory");
    }

    @Deprecated
    public DefaultHttpServerIODispatch(NHttpServerEventHandler handler, HttpParams params) {
        this(handler, new DefaultNHttpServerConnectionFactory(params));
    }

    @Deprecated
    public DefaultHttpServerIODispatch(NHttpServerEventHandler handler, SSLContext sslcontext, SSLSetupHandler sslHandler, HttpParams params) {
        this(handler, new SSLNHttpServerConnectionFactory(sslcontext, sslHandler, params));
    }

    @Deprecated
    public DefaultHttpServerIODispatch(NHttpServerEventHandler handler, SSLContext sslcontext, HttpParams params) {
        this(handler, sslcontext, null, params);
    }

    public DefaultHttpServerIODispatch(NHttpServerEventHandler handler, ConnectionConfig config) {
        this(handler, new DefaultNHttpServerConnectionFactory(config));
    }

    public DefaultHttpServerIODispatch(NHttpServerEventHandler handler, SSLContext sslcontext, SSLSetupHandler sslHandler, ConnectionConfig config) {
        this(handler, new SSLNHttpServerConnectionFactory(sslcontext, sslHandler, config));
    }

    public DefaultHttpServerIODispatch(NHttpServerEventHandler handler, SSLContext sslcontext, ConnectionConfig config) {
        this(handler, new SSLNHttpServerConnectionFactory(sslcontext, null, config));
    }

    @Override
    protected DefaultNHttpServerConnection createConnection(IOSession session) {
        return this.connFactory.createConnection(session);
    }

    @Override
    protected void onConnected(DefaultNHttpServerConnection conn) {
        try {
            this.handler.connected(conn);
        }
        catch (Exception ex) {
            this.handler.exception(conn, ex);
        }
    }

    @Override
    protected void onClosed(DefaultNHttpServerConnection conn) {
        this.handler.closed(conn);
    }

    @Override
    protected void onException(DefaultNHttpServerConnection conn, IOException ex) {
        this.handler.exception(conn, ex);
    }

    @Override
    protected void onInputReady(DefaultNHttpServerConnection conn) {
        conn.consumeInput(this.handler);
    }

    @Override
    protected void onOutputReady(DefaultNHttpServerConnection conn) {
        conn.produceOutput(this.handler);
    }

    @Override
    protected void onTimeout(DefaultNHttpServerConnection conn) {
        try {
            this.handler.timeout(conn);
        }
        catch (Exception ex) {
            this.handler.exception(conn, ex);
        }
    }
}

