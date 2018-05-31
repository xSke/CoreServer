/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.impl.nio;

import java.io.IOException;
import org.apache.http.HttpRequestFactory;
import org.apache.http.annotation.Immutable;
import org.apache.http.impl.DefaultHttpRequestFactory;
import org.apache.http.impl.nio.DefaultNHttpServerConnection;
import org.apache.http.impl.nio.reactor.AbstractIODispatch;
import org.apache.http.nio.NHttpServerConnection;
import org.apache.http.nio.NHttpServerIOTarget;
import org.apache.http.nio.NHttpServiceHandler;
import org.apache.http.nio.reactor.IOSession;
import org.apache.http.nio.util.ByteBufferAllocator;
import org.apache.http.nio.util.HeapByteBufferAllocator;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.Args;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
@Deprecated
@Immutable
public class DefaultServerIOEventDispatch
extends AbstractIODispatch<NHttpServerIOTarget> {
    protected final ByteBufferAllocator allocator;
    protected final NHttpServiceHandler handler;
    protected final HttpParams params;

    public DefaultServerIOEventDispatch(NHttpServiceHandler handler, HttpParams params) {
        Args.notNull(handler, "HTTP service handler");
        Args.notNull(params, "HTTP parameters");
        this.allocator = this.createByteBufferAllocator();
        this.handler = handler;
        this.params = params;
    }

    protected ByteBufferAllocator createByteBufferAllocator() {
        return HeapByteBufferAllocator.INSTANCE;
    }

    protected HttpRequestFactory createHttpRequestFactory() {
        return DefaultHttpRequestFactory.INSTANCE;
    }

    @Override
    protected NHttpServerIOTarget createConnection(IOSession session) {
        return new DefaultNHttpServerConnection(session, this.createHttpRequestFactory(), this.allocator, this.params);
    }

    @Override
    protected void onConnected(NHttpServerIOTarget conn) {
        int timeout = HttpConnectionParams.getSoTimeout(this.params);
        conn.setSocketTimeout(timeout);
        this.handler.connected(conn);
    }

    @Override
    protected void onClosed(NHttpServerIOTarget conn) {
        this.handler.closed(conn);
    }

    @Override
    protected void onException(NHttpServerIOTarget conn, IOException ex) {
        this.handler.exception((NHttpServerConnection)conn, ex);
    }

    @Override
    protected void onInputReady(NHttpServerIOTarget conn) {
        conn.consumeInput(this.handler);
    }

    @Override
    protected void onOutputReady(NHttpServerIOTarget conn) {
        conn.produceOutput(this.handler);
    }

    @Override
    protected void onTimeout(NHttpServerIOTarget conn) {
        this.handler.timeout(conn);
    }
}

