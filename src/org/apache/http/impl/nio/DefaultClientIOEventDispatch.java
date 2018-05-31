/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.impl.nio;

import java.io.IOException;
import org.apache.http.HttpResponseFactory;
import org.apache.http.annotation.Immutable;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.nio.DefaultNHttpClientConnection;
import org.apache.http.impl.nio.reactor.AbstractIODispatch;
import org.apache.http.nio.NHttpClientConnection;
import org.apache.http.nio.NHttpClientHandler;
import org.apache.http.nio.NHttpClientIOTarget;
import org.apache.http.nio.reactor.IOSession;
import org.apache.http.nio.util.ByteBufferAllocator;
import org.apache.http.nio.util.HeapByteBufferAllocator;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Args;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
@Deprecated
@Immutable
public class DefaultClientIOEventDispatch
extends AbstractIODispatch<NHttpClientIOTarget> {
    protected final NHttpClientHandler handler;
    protected final ByteBufferAllocator allocator;
    protected final HttpParams params;

    public DefaultClientIOEventDispatch(NHttpClientHandler handler, HttpParams params) {
        Args.notNull(handler, "HTTP client handler");
        Args.notNull(params, "HTTP parameters");
        this.allocator = this.createByteBufferAllocator();
        this.handler = handler;
        this.params = params;
    }

    protected ByteBufferAllocator createByteBufferAllocator() {
        return HeapByteBufferAllocator.INSTANCE;
    }

    protected HttpResponseFactory createHttpResponseFactory() {
        return DefaultHttpResponseFactory.INSTANCE;
    }

    @Override
    protected NHttpClientIOTarget createConnection(IOSession session) {
        return new DefaultNHttpClientConnection(session, this.createHttpResponseFactory(), this.allocator, this.params);
    }

    @Override
    protected void onConnected(NHttpClientIOTarget conn) {
        int timeout = HttpConnectionParams.getSoTimeout(this.params);
        conn.setSocketTimeout(timeout);
        Object attachment = conn.getContext().getAttribute("http.session.attachment");
        this.handler.connected(conn, attachment);
    }

    @Override
    protected void onClosed(NHttpClientIOTarget conn) {
        this.handler.closed(conn);
    }

    @Override
    protected void onException(NHttpClientIOTarget conn, IOException ex) {
        this.handler.exception((NHttpClientConnection)conn, ex);
    }

    @Override
    protected void onInputReady(NHttpClientIOTarget conn) {
        conn.consumeInput(this.handler);
    }

    @Override
    protected void onOutputReady(NHttpClientIOTarget conn) {
        conn.produceOutput(this.handler);
    }

    @Override
    protected void onTimeout(NHttpClientIOTarget conn) {
        this.handler.timeout(conn);
    }
}

