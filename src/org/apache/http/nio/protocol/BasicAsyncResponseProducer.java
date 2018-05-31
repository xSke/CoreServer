/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.protocol;

import java.io.IOException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.annotation.ThreadSafe;
import org.apache.http.nio.ContentEncoder;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.entity.EntityAsyncContentProducer;
import org.apache.http.nio.entity.HttpAsyncContentProducer;
import org.apache.http.nio.protocol.HttpAsyncResponseProducer;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Args;

@ThreadSafe
public class BasicAsyncResponseProducer
implements HttpAsyncResponseProducer {
    private final HttpResponse response;
    private final HttpAsyncContentProducer producer;

    protected BasicAsyncResponseProducer(HttpResponse response, HttpAsyncContentProducer producer) {
        Args.notNull(response, "HTTP response");
        Args.notNull(producer, "HTTP content producer");
        this.response = response;
        this.producer = producer;
    }

    public BasicAsyncResponseProducer(HttpResponse response) {
        Args.notNull(response, "HTTP response");
        this.response = response;
        HttpEntity entity = response.getEntity();
        this.producer = entity != null ? (entity instanceof HttpAsyncContentProducer ? (HttpAsyncContentProducer)((Object)entity) : new EntityAsyncContentProducer(entity)) : null;
    }

    public synchronized HttpResponse generateResponse() {
        return this.response;
    }

    public synchronized void produceContent(ContentEncoder encoder, IOControl ioctrl) throws IOException {
        if (this.producer != null) {
            this.producer.produceContent(encoder, ioctrl);
            if (encoder.isCompleted()) {
                this.producer.close();
            }
        }
    }

    public void responseCompleted(HttpContext context) {
    }

    public void failed(Exception ex) {
    }

    public synchronized void close() throws IOException {
        if (this.producer != null) {
            this.producer.close();
        }
    }
}

