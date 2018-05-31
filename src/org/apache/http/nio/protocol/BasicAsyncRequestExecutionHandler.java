/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.protocol;

import java.io.IOException;
import java.util.concurrent.Future;
import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.concurrent.BasicFuture;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.nio.ContentDecoder;
import org.apache.http.nio.ContentEncoder;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.protocol.HttpAsyncRequestExecutionHandler;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;
import org.apache.http.nio.protocol.HttpAsyncResponseConsumer;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.util.Args;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
@Deprecated
public class BasicAsyncRequestExecutionHandler<T>
implements HttpAsyncRequestExecutionHandler<T> {
    private final HttpAsyncRequestProducer requestProducer;
    private final HttpAsyncResponseConsumer<T> responseConsumer;
    private final BasicFuture<T> future;
    private final HttpContext localContext;
    private final HttpProcessor httppocessor;
    private final ConnectionReuseStrategy reuseStrategy;
    private volatile boolean requestSent;

    public BasicAsyncRequestExecutionHandler(HttpAsyncRequestProducer requestProducer, HttpAsyncResponseConsumer<T> responseConsumer, FutureCallback<T> callback, HttpContext localContext, HttpProcessor httppocessor, ConnectionReuseStrategy reuseStrategy, HttpParams params) {
        Args.notNull(requestProducer, "Request producer");
        Args.notNull(responseConsumer, "Response consumer");
        Args.notNull(localContext, "HTTP context");
        Args.notNull(httppocessor, "HTTP processor");
        Args.notNull(reuseStrategy, "Connection reuse strategy");
        Args.notNull(params, "HTTP parameters");
        this.requestProducer = requestProducer;
        this.responseConsumer = responseConsumer;
        this.future = new BasicFuture<T>(callback);
        this.localContext = localContext;
        this.httppocessor = httppocessor;
        this.reuseStrategy = reuseStrategy;
    }

    public BasicAsyncRequestExecutionHandler(HttpAsyncRequestProducer requestProducer, HttpAsyncResponseConsumer<T> responseConsumer, HttpContext localContext, HttpProcessor httppocessor, ConnectionReuseStrategy reuseStrategy, HttpParams params) {
        this(requestProducer, responseConsumer, null, localContext, httppocessor, reuseStrategy, params);
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
    public HttpHost getTarget() {
        return this.requestProducer.getTarget();
    }

    @Override
    public HttpRequest generateRequest() throws IOException, HttpException {
        return this.requestProducer.generateRequest();
    }

    @Override
    public void produceContent(ContentEncoder encoder, IOControl ioctrl) throws IOException {
        this.requestProducer.produceContent(encoder, ioctrl);
    }

    @Override
    public void requestCompleted(HttpContext context) {
        this.requestProducer.requestCompleted(context);
        this.requestSent = true;
    }

    @Override
    public boolean isRepeatable() {
        return false;
    }

    @Override
    public void resetRequest() {
    }

    @Override
    public void responseReceived(HttpResponse response) throws IOException, HttpException {
        this.responseConsumer.responseReceived(response);
    }

    @Override
    public void consumeContent(ContentDecoder decoder, IOControl ioctrl) throws IOException {
        this.responseConsumer.consumeContent(decoder, ioctrl);
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
    public void responseCompleted(HttpContext context) {
        try {
            this.responseConsumer.responseCompleted(context);
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
    public T getResult() {
        return this.responseConsumer.getResult();
    }

    @Override
    public Exception getException() {
        return this.responseConsumer.getException();
    }

    @Override
    public HttpContext getContext() {
        return this.localContext;
    }

    @Override
    public HttpProcessor getHttpProcessor() {
        return this.httppocessor;
    }

    @Override
    public ConnectionReuseStrategy getConnectionReuseStrategy() {
        return this.reuseStrategy;
    }

    @Override
    public boolean isDone() {
        return this.responseConsumer.isDone();
    }
}

