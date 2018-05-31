/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.protocol;

import java.io.IOException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.annotation.ThreadSafe;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.ContentDecoder;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.protocol.HttpAsyncResponseConsumer;
import org.apache.http.protocol.HttpContext;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
@ThreadSafe
public abstract class AbstractAsyncResponseConsumer<T>
implements HttpAsyncResponseConsumer<T> {
    private volatile boolean completed;
    private volatile T result;
    private volatile Exception ex;

    protected abstract void onResponseReceived(HttpResponse var1) throws HttpException, IOException;

    protected abstract void onContentReceived(ContentDecoder var1, IOControl var2) throws IOException;

    protected abstract void onEntityEnclosed(HttpEntity var1, ContentType var2) throws IOException;

    protected abstract T buildResult(HttpContext var1) throws Exception;

    protected abstract void releaseResources();

    protected void onClose() throws IOException {
    }

    @Override
    public final synchronized void responseReceived(HttpResponse response) throws IOException, HttpException {
        this.onResponseReceived(response);
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            ContentType contentType = ContentType.getOrDefault(entity);
            this.onEntityEnclosed(entity, contentType);
        }
    }

    @Override
    public final synchronized void consumeContent(ContentDecoder decoder, IOControl ioctrl) throws IOException {
        this.onContentReceived(decoder, ioctrl);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public final synchronized void responseCompleted(HttpContext context) {
        if (this.completed) {
            return;
        }
        this.completed = true;
        try {
            this.result = this.buildResult(context);
        }
        catch (Exception ex) {
            this.ex = ex;
        }
        finally {
            this.releaseResources();
        }
    }

    @Override
    public final synchronized boolean cancel() {
        if (this.completed) {
            return false;
        }
        this.completed = true;
        this.releaseResources();
        return true;
    }

    @Override
    public final synchronized void failed(Exception ex) {
        if (this.completed) {
            return;
        }
        this.completed = true;
        this.ex = ex;
        this.releaseResources();
    }

    @Override
    public final synchronized void close() throws IOException {
        if (this.completed) {
            return;
        }
        this.completed = true;
        this.releaseResources();
        this.onClose();
    }

    @Override
    public Exception getException() {
        return this.ex;
    }

    @Override
    public T getResult() {
        return this.result;
    }

    @Override
    public boolean isDone() {
        return this.completed;
    }
}

