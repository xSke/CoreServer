/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.impl.nio.client;

import java.util.Queue;
import org.apache.http.concurrent.BasicFuture;
import org.apache.http.impl.nio.client.ResultCallback;
import org.apache.http.nio.protocol.HttpAsyncRequestExecutionHandler;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
@Deprecated
class DefaultResultCallback<T>
implements ResultCallback<T> {
    private final BasicFuture<T> future;
    private final Queue<HttpAsyncRequestExecutionHandler<?>> queue;

    DefaultResultCallback(BasicFuture<T> future, Queue<HttpAsyncRequestExecutionHandler<?>> queue) {
        this.future = future;
        this.queue = queue;
    }

    @Override
    public void completed(T result, HttpAsyncRequestExecutionHandler<T> handler) {
        this.future.completed(result);
        this.queue.remove(handler);
    }

    @Override
    public void failed(Exception ex, HttpAsyncRequestExecutionHandler<T> handler) {
        this.future.failed(ex);
        this.queue.remove(handler);
    }

    @Override
    public void cancelled(HttpAsyncRequestExecutionHandler<T> handler) {
        this.future.cancel(true);
        this.queue.remove(handler);
    }

    @Override
    public boolean isDone() {
        return this.future.isDone();
    }
}

