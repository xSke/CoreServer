/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.client.methods;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.client.methods.ZeroCopyConsumer;
import org.apache.http.nio.client.methods.ZeroCopyPost;
import org.apache.http.nio.client.methods.ZeroCopyPut;
import org.apache.http.nio.entity.HttpAsyncContentProducer;
import org.apache.http.nio.entity.NByteArrayEntity;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.nio.protocol.BasicAsyncRequestProducer;
import org.apache.http.nio.protocol.BasicAsyncResponseConsumer;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;
import org.apache.http.nio.protocol.HttpAsyncResponseConsumer;
import org.apache.http.util.Args;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public final class HttpAsyncMethods {
    public static HttpAsyncRequestProducer create(HttpHost target, HttpRequest request) {
        Args.notNull(target, "HTTP host");
        Args.notNull(request, "HTTP request");
        return new RequestProducerImpl(target, request);
    }

    public static HttpAsyncRequestProducer create(HttpUriRequest request) {
        Args.notNull(request, "HTTP request");
        HttpHost target = URIUtils.extractHost(request.getURI());
        return new RequestProducerImpl(target, request);
    }

    public static HttpAsyncRequestProducer createGet(URI requestURI) {
        return HttpAsyncMethods.create(new HttpGet(requestURI));
    }

    public static HttpAsyncRequestProducer createGet(String requestURI) {
        return HttpAsyncMethods.create(new HttpGet(URI.create(requestURI)));
    }

    public static HttpAsyncRequestProducer createHead(URI requestURI) {
        return HttpAsyncMethods.create(new HttpGet(requestURI));
    }

    public static HttpAsyncRequestProducer createHead(String requestURI) {
        return HttpAsyncMethods.create(new HttpGet(URI.create(requestURI)));
    }

    public static HttpAsyncRequestProducer createDelete(URI requestURI) {
        return HttpAsyncMethods.create(new HttpDelete(requestURI));
    }

    public static HttpAsyncRequestProducer createDelete(String requestURI) {
        return HttpAsyncMethods.create(new HttpDelete(URI.create(requestURI)));
    }

    public static HttpAsyncRequestProducer createOptions(URI requestURI) {
        return HttpAsyncMethods.create(new HttpOptions(requestURI));
    }

    public static HttpAsyncRequestProducer createOptions(String requestURI) {
        return HttpAsyncMethods.create(new HttpOptions(URI.create(requestURI)));
    }

    public static HttpAsyncRequestProducer createTrace(URI requestURI) {
        return HttpAsyncMethods.create(new HttpTrace(requestURI));
    }

    public static HttpAsyncRequestProducer createTrace(String requestURI) {
        return HttpAsyncMethods.create(new HttpTrace(URI.create(requestURI)));
    }

    public static HttpAsyncRequestProducer createPost(URI requestURI, String content, ContentType contentType) throws UnsupportedEncodingException {
        HttpPost httppost = new HttpPost(requestURI);
        NStringEntity entity = new NStringEntity(content, contentType);
        httppost.setEntity(entity);
        HttpHost target = URIUtils.extractHost(requestURI);
        return new RequestProducerImpl(target, httppost, entity);
    }

    public static HttpAsyncRequestProducer createPost(String requestURI, String content, ContentType contentType) throws UnsupportedEncodingException {
        return HttpAsyncMethods.createPost(URI.create(requestURI), content, contentType);
    }

    public static HttpAsyncRequestProducer createPost(URI requestURI, byte[] content, ContentType contentType) {
        HttpPost httppost = new HttpPost(requestURI);
        NByteArrayEntity entity = new NByteArrayEntity(content, contentType);
        HttpHost target = URIUtils.extractHost(requestURI);
        return new RequestProducerImpl(target, httppost, entity);
    }

    public static HttpAsyncRequestProducer createPost(String requestURI, byte[] content, ContentType contentType) {
        return HttpAsyncMethods.createPost(URI.create(requestURI), content, contentType);
    }

    public static HttpAsyncRequestProducer createPut(URI requestURI, String content, ContentType contentType) throws UnsupportedEncodingException {
        HttpPut httpput = new HttpPut(requestURI);
        NStringEntity entity = new NStringEntity(content, contentType);
        httpput.setEntity(entity);
        HttpHost target = URIUtils.extractHost(requestURI);
        return new RequestProducerImpl(target, httpput, entity);
    }

    public static HttpAsyncRequestProducer createPut(String requestURI, String content, ContentType contentType) throws UnsupportedEncodingException {
        return HttpAsyncMethods.createPut(URI.create(requestURI), content, contentType);
    }

    public static HttpAsyncRequestProducer createPut(URI requestURI, byte[] content, ContentType contentType) {
        HttpPut httpput = new HttpPut(requestURI);
        NByteArrayEntity entity = new NByteArrayEntity(content, contentType);
        HttpHost target = URIUtils.extractHost(requestURI);
        return new RequestProducerImpl(target, httpput, entity);
    }

    public static HttpAsyncRequestProducer createPut(String requestURI, byte[] content, ContentType contentType) {
        return HttpAsyncMethods.createPut(URI.create(requestURI), content, contentType);
    }

    public static HttpAsyncRequestProducer createZeroCopyPost(URI requestURI, File content, ContentType contentType) throws FileNotFoundException {
        return new ZeroCopyPost(requestURI, content, contentType);
    }

    public static HttpAsyncRequestProducer createZeroCopyPost(String requestURI, File content, ContentType contentType) throws FileNotFoundException {
        return new ZeroCopyPost(URI.create(requestURI), content, contentType);
    }

    public static HttpAsyncRequestProducer createZeroCopyPut(URI requestURI, File content, ContentType contentType) throws FileNotFoundException {
        return new ZeroCopyPut(requestURI, content, contentType);
    }

    public static HttpAsyncRequestProducer createZeroCopyPut(String requestURI, File content, ContentType contentType) throws FileNotFoundException {
        return new ZeroCopyPut(URI.create(requestURI), content, contentType);
    }

    public static HttpAsyncResponseConsumer<HttpResponse> createConsumer() {
        return new BasicAsyncResponseConsumer();
    }

    public static HttpAsyncResponseConsumer<HttpResponse> createZeroCopyConsumer(File file) throws FileNotFoundException {
        return new ZeroCopyConsumer<HttpResponse>(file){

            @Override
            protected HttpResponse process(HttpResponse response, File file, ContentType contentType) {
                return response;
            }
        };
    }

    static class RequestProducerImpl
    extends BasicAsyncRequestProducer {
        protected RequestProducerImpl(HttpHost target, HttpEntityEnclosingRequest request, HttpAsyncContentProducer producer) {
            super(target, request, producer);
        }

        public RequestProducerImpl(HttpHost target, HttpRequest request) {
            super(target, request);
        }
    }

}

