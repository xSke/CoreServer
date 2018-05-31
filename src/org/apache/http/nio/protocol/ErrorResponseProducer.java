/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.protocol;

import java.io.IOException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.nio.ContentEncoder;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.entity.EntityAsyncContentProducer;
import org.apache.http.nio.entity.HttpAsyncContentProducer;
import org.apache.http.nio.protocol.HttpAsyncResponseProducer;
import org.apache.http.protocol.HttpContext;

class ErrorResponseProducer
implements HttpAsyncResponseProducer {
    private final HttpResponse response;
    private final HttpEntity entity;
    private final HttpAsyncContentProducer contentProducer;
    private final boolean keepAlive;

    ErrorResponseProducer(HttpResponse response, HttpEntity entity, boolean keepAlive) {
        this.response = response;
        this.entity = entity;
        this.contentProducer = entity instanceof HttpAsyncContentProducer ? (HttpAsyncContentProducer)((Object)entity) : new EntityAsyncContentProducer(entity);
        this.keepAlive = keepAlive;
    }

    public HttpResponse generateResponse() {
        if (this.keepAlive) {
            this.response.addHeader("Connection", "Keep-Alive");
        } else {
            this.response.addHeader("Connection", "Close");
        }
        this.response.setEntity(this.entity);
        return this.response;
    }

    public void produceContent(ContentEncoder encoder, IOControl ioctrl) throws IOException {
        this.contentProducer.produceContent(encoder, ioctrl);
    }

    public void responseCompleted(HttpContext context) {
    }

    public void failed(Exception ex) {
    }

    public void close() throws IOException {
        this.contentProducer.close();
    }
}

