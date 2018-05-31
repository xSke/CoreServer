/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.impl.nio.client;

import java.io.Closeable;
import java.net.URI;
import java.util.concurrent.Future;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.annotation.ThreadSafe;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.concurrent.BasicFuture;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.http.nio.client.methods.HttpAsyncMethods;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;
import org.apache.http.nio.protocol.HttpAsyncResponseConsumer;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Args;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
@ThreadSafe
public abstract class CloseableHttpAsyncClient
implements HttpAsyncClient,
Closeable {
    public abstract boolean isRunning();

    public abstract void start();

    @Override
    public <T> Future<T> execute(HttpAsyncRequestProducer requestProducer, HttpAsyncResponseConsumer<T> responseConsumer, FutureCallback<T> callback) {
        return this.execute(requestProducer, responseConsumer, (HttpContext)new BasicHttpContext(), callback);
    }

    @Override
    public Future<HttpResponse> execute(HttpHost target, HttpRequest request, HttpContext context, FutureCallback<HttpResponse> callback) {
        return this.execute(HttpAsyncMethods.create(target, request), HttpAsyncMethods.createConsumer(), context, callback);
    }

    @Override
    public Future<HttpResponse> execute(HttpHost target, HttpRequest request, FutureCallback<HttpResponse> callback) {
        return this.execute(target, request, new BasicHttpContext(), callback);
    }

    @Override
    public Future<HttpResponse> execute(HttpUriRequest request, FutureCallback<HttpResponse> callback) {
        return this.execute(request, new BasicHttpContext(), callback);
    }

    @Override
    public Future<HttpResponse> execute(HttpUriRequest request, HttpContext context, FutureCallback<HttpResponse> callback) {
        HttpHost target;
        try {
            target = this.determineTarget(request);
        }
        catch (ClientProtocolException ex) {
            BasicFuture<HttpResponse> future = new BasicFuture<HttpResponse>(callback);
            future.failed(ex);
            return future;
        }
        return this.execute(target, request, context, callback);
    }

    private HttpHost determineTarget(HttpUriRequest request) throws ClientProtocolException {
        Args.notNull(request, "HTTP request");
        HttpHost target = null;
        URI requestURI = request.getURI();
        if (requestURI.isAbsolute() && (target = URIUtils.extractHost(requestURI)) == null) {
            throw new ClientProtocolException("URI does not specify a valid host name: " + requestURI);
        }
        return target;
    }
}

