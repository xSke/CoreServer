/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.protocol;

import java.io.IOException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.annotation.ThreadSafe;
import org.apache.http.nio.ContentEncoder;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.entity.EntityAsyncContentProducer;
import org.apache.http.nio.entity.HttpAsyncContentProducer;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Args;

@ThreadSafe
public class BasicAsyncRequestProducer
implements HttpAsyncRequestProducer {
    private final HttpHost target;
    private final HttpRequest request;
    private final HttpAsyncContentProducer producer;

    protected BasicAsyncRequestProducer(HttpHost target, HttpEntityEnclosingRequest request, HttpAsyncContentProducer producer) {
        Args.notNull(target, "HTTP host");
        Args.notNull(request, "HTTP request");
        Args.notNull(producer, "HTTP content producer");
        this.target = target;
        this.request = request;
        this.producer = producer;
    }

    public BasicAsyncRequestProducer(HttpHost target, HttpRequest request) {
        HttpEntity entity;
        Args.notNull(target, "HTTP host");
        Args.notNull(request, "HTTP request");
        this.target = target;
        this.request = request;
        this.producer = request instanceof HttpEntityEnclosingRequest ? ((entity = ((HttpEntityEnclosingRequest)request).getEntity()) != null ? (entity instanceof HttpAsyncContentProducer ? (HttpAsyncContentProducer)((Object)entity) : new EntityAsyncContentProducer(entity)) : null) : null;
    }

    public synchronized HttpRequest generateRequest() {
        return this.request;
    }

    public HttpHost getTarget() {
        return this.target;
    }

    public synchronized void produceContent(ContentEncoder encoder, IOControl ioctrl) throws IOException {
        if (this.producer != null) {
            this.producer.produceContent(encoder, ioctrl);
            if (encoder.isCompleted()) {
                this.producer.close();
            }
        }
    }

    public void requestCompleted(HttpContext context) {
    }

    public void failed(Exception ex) {
    }

    public synchronized boolean isRepeatable() {
        return this.producer == null || this.producer.isRepeatable();
    }

    public synchronized void resetRequest() throws IOException {
        if (this.producer != null) {
            this.producer.close();
        }
    }

    public synchronized void close() throws IOException {
        if (this.producer != null) {
            this.producer.close();
        }
    }
}

