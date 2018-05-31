/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.protocol;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import org.apache.http.HttpRequest;
import org.apache.http.nio.ContentDecoder;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.protocol.HttpAsyncRequestConsumer;
import org.apache.http.protocol.HttpContext;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
class NullRequestConsumer
implements HttpAsyncRequestConsumer<Object> {
    private final ByteBuffer buffer = ByteBuffer.allocate(2048);
    private volatile boolean completed;

    NullRequestConsumer() {
    }

    @Override
    public void requestReceived(HttpRequest request) {
    }

    @Override
    public void consumeContent(ContentDecoder decoder, IOControl ioctrl) throws IOException {
        int lastRead;
        do {
            this.buffer.clear();
        } while ((lastRead = decoder.read(this.buffer)) > 0);
    }

    @Override
    public void requestCompleted(HttpContext context) {
        this.completed = true;
    }

    @Override
    public void failed(Exception ex) {
        this.completed = true;
    }

    @Override
    public Object getResult() {
        return this.completed;
    }

    @Override
    public Exception getException() {
        return null;
    }

    @Override
    public void close() throws IOException {
        this.completed = true;
    }

    @Override
    public boolean isDone() {
        return this.completed;
    }
}

