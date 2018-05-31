/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.protocol;

import java.io.IOException;
import java.util.concurrent.Future;
import org.apache.http.ConnectionClosedException;
import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.concurrent.BasicFuture;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.nio.ContentDecoder;
import org.apache.http.nio.ContentEncoder;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.NHttpClientConnection;
import org.apache.http.nio.protocol.HttpAsyncClientExchangeHandler;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;
import org.apache.http.nio.protocol.HttpAsyncResponseConsumer;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.util.Args;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class BasicAsyncClientExchangeHandler<T>
implements HttpAsyncClientExchangeHandler {
    private final HttpAsyncRequestProducer requestProducer;
    private final HttpAsyncResponseConsumer<T> responseConsumer;
    private final BasicFuture<T> future;
    private final HttpContext localContext;
    private final NHttpClientConnection conn;
    private final HttpProcessor httppocessor;
    private final ConnectionReuseStrategy connReuseStrategy;
    private volatile boolean requestSent;
    private volatile boolean keepAlive;

    public BasicAsyncClientExchangeHandler(HttpAsyncRequestProducer requestProducer, HttpAsyncResponseConsumer<T> responseConsumer, FutureCallback<T> callback, HttpContext localContext, NHttpClientConnection conn, HttpProcessor httppocessor, ConnectionReuseStrategy connReuseStrategy) {
        this.requestProducer = Args.notNull(requestProducer, "Request producer");
        this.responseConsumer = Args.notNull(responseConsumer, "Response consumer");
        this.future = new BasicFuture<T>(callback);
        this.localContext = Args.notNull(localContext, "HTTP context");
        this.conn = Args.notNull(conn, "HTTP connection");
        this.httppocessor = Args.notNull(httppocessor, "HTTP processor");
        this.connReuseStrategy = connReuseStrategy != null ? connReuseStrategy : DefaultConnectionReuseStrategy.INSTANCE;
    }

    public BasicAsyncClientExchangeHandler(HttpAsyncRequestProducer requestProducer, HttpAsyncResponseConsumer<T> responseConsumer, HttpContext localContext, NHttpClientConnection conn, HttpProcessor httppocessor) {
        this(requestProducer, responseConsumer, null, localContext, conn, httppocessor, null);
    }

    public Future<T> getFuture() {
        return this.future;
    }

    private void releaseResources() {
        try {
            this.responseConsumer.close();
        }
        catch (IOException ex) {
            // empty catch block
        }
        try {
            this.requestProducer.close();
        }
        catch (IOException ex) {
            // empty catch block
        }
    }

    @Override
    public void close() throws IOException {
        this.releaseResources();
        if (!this.future.isDone()) {
            this.future.cancel();
        }
    }

    @Override
    public HttpRequest generateRequest() throws IOException, HttpException {
        HttpRequest request = this.requestProducer.generateRequest();
        this.localContext.setAttribute("http.request", request);
        this.localContext.setAttribute("http.connection", this.conn);
        this.httppocessor.process(request, this.localContext);
        return request;
    }

    @Override
    public void produceContent(ContentEncoder encoder, IOControl ioctrl) throws IOException {
        this.requestProducer.produceContent(encoder, ioctrl);
    }

    @Override
    public void requestCompleted() {
        this.requestProducer.requestCompleted(this.localContext);
        this.requestSent = true;
    }

    @Override
    public void responseReceived(HttpResponse response) throws IOException, HttpException {
        this.localContext.setAttribute("http.response", response);
        this.httppocessor.process(response, this.localContext);
        this.responseConsumer.responseReceived(response);
        this.keepAlive = this.connReuseStrategy.keepAlive(response, this.localContext);
    }

    @Override
    public void consumeContent(ContentDecoder decoder, IOControl ioctrl) throws IOException {
        this.responseConsumer.consumeContent(decoder, ioctrl);
    }

    @Override
    public void responseCompleted() throws IOException {
        try {
            if (!this.keepAlive) {
                this.conn.close();
            }
            this.responseConsumer.responseCompleted(this.localContext);
            T result = this.responseConsumer.getResult();
            Exception ex = this.responseConsumer.getException();
            if (ex == null) {
                this.future.completed(result);
            } else {
                this.future.failed(ex);
            }
            this.releaseResources();
        }
        catch (RuntimeException ex) {
            this.failed(ex);
            throw ex;
        }
    }

    @Override
    public void inputTerminated() {
        this.failed(new ConnectionClosedException("Connection closed"));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void failed(Exception ex) {
        try {
            if (!this.requestSent) {
                this.requestProducer.failed(ex);
            }
            this.responseConsumer.failed(ex);
        }
        finally {
            try {
                this.future.failed(ex);
            }
            finally {
                this.releaseResources();
            }
        }
    }

    @Override
    public boolean cancel() {
        try {
            boolean cancelled = this.responseConsumer.cancel();
            this.future.cancel();
            this.releaseResources();
            return cancelled;
        }
        catch (RuntimeException ex) {
            this.failed(ex);
            throw ex;
        }
    }

    @Override
    public boolean isDone() {
        return this.responseConsumer.isDone();
    }
}

