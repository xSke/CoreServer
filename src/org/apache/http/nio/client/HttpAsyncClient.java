/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.client;

import java.util.concurrent.Future;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;
import org.apache.http.nio.protocol.HttpAsyncResponseConsumer;
import org.apache.http.protocol.HttpContext;

public interface HttpAsyncClient {
    public <T> Future<T> execute(HttpAsyncRequestProducer var1, HttpAsyncResponseConsumer<T> var2, HttpContext var3, FutureCallback<T> var4);

    public <T> Future<T> execute(HttpAsyncRequestProducer var1, HttpAsyncResponseConsumer<T> var2, FutureCallback<T> var3);

    public Future<HttpResponse> execute(HttpHost var1, HttpRequest var2, HttpContext var3, FutureCallback<HttpResponse> var4);

    public Future<HttpResponse> execute(HttpHost var1, HttpRequest var2, FutureCallback<HttpResponse> var3);

    public Future<HttpResponse> execute(HttpUriRequest var1, HttpContext var2, FutureCallback<HttpResponse> var3);

    public Future<HttpResponse> execute(HttpUriRequest var1, FutureCallback<HttpResponse> var2);
}

