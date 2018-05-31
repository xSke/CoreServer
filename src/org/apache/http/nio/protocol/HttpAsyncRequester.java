/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.protocol;

import java.io.IOException;
import java.util.concurrent.Future;
import org.apache.http.ConnectionClosedException;
import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.HttpHost;
import org.apache.http.annotation.Immutable;
import org.apache.http.concurrent.BasicFuture;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.nio.NHttpClientConnection;
import org.apache.http.nio.protocol.BasicAsyncClientExchangeHandler;
import org.apache.http.nio.protocol.HttpAsyncClientExchangeHandler;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;
import org.apache.http.nio.protocol.HttpAsyncResponseConsumer;
import org.apache.http.params.HttpParams;
import org.apache.http.pool.ConnPool;
import org.apache.http.pool.PoolEntry;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.util.Args;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
@Immutable
public class HttpAsyncRequester {
    private final HttpProcessor httpprocessor;
    private final ConnectionReuseStrategy connReuseStrategy;

    @Deprecated
    public HttpAsyncRequester(HttpProcessor httpprocessor, ConnectionReuseStrategy reuseStrategy, HttpParams params) {
        this(httpprocessor, reuseStrategy);
    }

    public HttpAsyncRequester(HttpProcessor httpprocessor, ConnectionReuseStrategy connReuseStrategy) {
        this.httpprocessor = Args.notNull(httpprocessor, "HTTP processor");
        this.connReuseStrategy = connReuseStrategy != null ? connReuseStrategy : DefaultConnectionReuseStrategy.INSTANCE;
    }

    public HttpAsyncRequester(HttpProcessor httpprocessor) {
        this(httpprocessor, null);
    }

    public <T> Future<T> execute(HttpAsyncRequestProducer requestProducer, HttpAsyncResponseConsumer<T> responseConsumer, NHttpClientConnection conn, HttpContext context, FutureCallback<T> callback) {
        Args.notNull(requestProducer, "HTTP request producer");
        Args.notNull(responseConsumer, "HTTP response consumer");
        Args.notNull(conn, "HTTP connection");
        Args.notNull(context, "HTTP context");
        BasicAsyncClientExchangeHandler<T> handler = new BasicAsyncClientExchangeHandler<T>(requestProducer, responseConsumer, callback, context, conn, this.httpprocessor, this.connReuseStrategy);
        this.initExection(handler, conn);
        return handler.getFuture();
    }

    private void initExection(HttpAsyncClientExchangeHandler handler, NHttpClientConnection conn) {
        conn.getContext().setAttribute("http.nio.exchange-handler", handler);
        if (!conn.isOpen()) {
            handler.failed(new ConnectionClosedException("Connection closed"));
            try {
                handler.close();
            }
            catch (IOException ex) {
                this.log(ex);
            }
        } else {
            conn.requestOutput();
        }
    }

    public <T> Future<T> execute(HttpAsyncRequestProducer requestProducer, HttpAsyncResponseConsumer<T> responseConsumer, NHttpClientConnection conn, HttpContext context) {
        return this.execute(requestProducer, responseConsumer, conn, context, null);
    }

    public <T> Future<T> execute(HttpAsyncRequestProducer requestProducer, HttpAsyncResponseConsumer<T> responseConsumer, NHttpClientConnection conn) {
        return this.execute(requestProducer, responseConsumer, conn, (HttpContext)new BasicHttpContext());
    }

    public <T, E extends PoolEntry<HttpHost, NHttpClientConnection>> Future<T> execute(HttpAsyncRequestProducer requestProducer, HttpAsyncResponseConsumer<T> responseConsumer, ConnPool<HttpHost, E> connPool, HttpContext context, FutureCallback<T> callback) {
        Args.notNull(requestProducer, "HTTP request producer");
        Args.notNull(responseConsumer, "HTTP response consumer");
        Args.notNull(connPool, "HTTP connection pool");
        Args.notNull(context, "HTTP context");
        BasicFuture<T> future = new BasicFuture<T>(callback);
        HttpHost target = requestProducer.getTarget();
        connPool.lease(target, null, new ConnRequestCallback<T, E>(future, requestProducer, responseConsumer, connPool, context));
        return future;
    }

    public <T, E extends PoolEntry<HttpHost, NHttpClientConnection>> Future<T> execute(HttpAsyncRequestProducer requestProducer, HttpAsyncResponseConsumer<T> responseConsumer, E poolEntry, ConnPool<HttpHost, E> connPool, HttpContext context, FutureCallback<T> callback) {
        Args.notNull(requestProducer, "HTTP request producer");
        Args.notNull(responseConsumer, "HTTP response consumer");
        Args.notNull(connPool, "HTTP connection pool");
        Args.notNull(poolEntry, "Pool entry");
        Args.notNull(context, "HTTP context");
        BasicFuture<T> future = new BasicFuture<T>(callback);
        NHttpClientConnection conn = (NHttpClientConnection)poolEntry.getConnection();
        BasicAsyncClientExchangeHandler<T> handler = new BasicAsyncClientExchangeHandler<T>(requestProducer, responseConsumer, new RequestExecutionCallback(this, future, poolEntry, connPool), context, conn, this.httpprocessor, this.connReuseStrategy);
        this.initExection(handler, conn);
        return future;
    }

    public <T, E extends PoolEntry<HttpHost, NHttpClientConnection>> Future<T> execute(HttpAsyncRequestProducer requestProducer, HttpAsyncResponseConsumer<T> responseConsumer, ConnPool<HttpHost, E> connPool, HttpContext context) {
        return this.execute(requestProducer, responseConsumer, connPool, context, null);
    }

    public <T, E extends PoolEntry<HttpHost, NHttpClientConnection>> Future<T> execute(HttpAsyncRequestProducer requestProducer, HttpAsyncResponseConsumer<T> responseConsumer, ConnPool<HttpHost, E> connPool) {
        return this.execute(requestProducer, responseConsumer, connPool, (HttpContext)new BasicHttpContext());
    }

    protected void log(Exception ex) {
    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    class RequestExecutionCallback<T, E extends PoolEntry<HttpHost, NHttpClientConnection>>
    implements FutureCallback<T> {
        private final BasicFuture<T> future;
        private final E poolEntry;
        private final ConnPool<HttpHost, E> connPool;

        RequestExecutionCallback(E future, ConnPool<HttpHost, E> poolEntry) {
            this.future = future;
            this.poolEntry = poolEntry;
            this.connPool = connPool;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void completed(T result) {
            try {
                this.connPool.release(this.poolEntry, true);
            }
            finally {
                this.future.completed(result);
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void failed(Exception ex) {
            try {
                this.connPool.release(this.poolEntry, false);
            }
            finally {
                this.future.failed(ex);
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void cancelled() {
            try {
                this.connPool.release(this.poolEntry, false);
            }
            finally {
                this.future.cancel(true);
            }
        }
    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    class ConnRequestCallback<T, E extends PoolEntry<HttpHost, NHttpClientConnection>>
    implements FutureCallback<E> {
        private final BasicFuture<T> requestFuture;
        private final HttpAsyncRequestProducer requestProducer;
        private final HttpAsyncResponseConsumer<T> responseConsumer;
        private final ConnPool<HttpHost, E> connPool;
        private final HttpContext context;

        ConnRequestCallback(BasicFuture<T> requestFuture, HttpAsyncRequestProducer requestProducer, HttpAsyncResponseConsumer<T> responseConsumer, ConnPool<HttpHost, E> connPool, HttpContext context) {
            this.requestFuture = requestFuture;
            this.requestProducer = requestProducer;
            this.responseConsumer = responseConsumer;
            this.connPool = connPool;
            this.context = context;
        }

        @Override
        public void completed(E result) {
            if (this.requestFuture.isDone()) {
                this.connPool.release(result, true);
                return;
            }
            NHttpClientConnection conn = (NHttpClientConnection)result.getConnection();
            BasicAsyncClientExchangeHandler<T> handler = new BasicAsyncClientExchangeHandler<T>(this.requestProducer, this.responseConsumer, new RequestExecutionCallback(HttpAsyncRequester.this, this.requestFuture, result, this.connPool), this.context, conn, HttpAsyncRequester.this.httpprocessor, HttpAsyncRequester.this.connReuseStrategy);
            HttpAsyncRequester.this.initExection(handler, conn);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void failed(Exception ex) {
            try {
                try {
                    this.responseConsumer.failed(ex);
                }
                finally {
                    this.releaseResources();
                }
            }
            finally {
                this.requestFuture.failed(ex);
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void cancelled() {
            try {
                try {
                    this.responseConsumer.cancel();
                }
                finally {
                    this.releaseResources();
                }
            }
            finally {
                this.requestFuture.cancel(true);
            }
        }

        public void releaseResources() {
            try {
                this.requestProducer.close();
            }
            catch (IOException ioex) {
                HttpAsyncRequester.this.log(ioex);
            }
            try {
                this.responseConsumer.close();
            }
            catch (IOException ioex) {
                HttpAsyncRequester.this.log(ioex);
            }
        }
    }

}

