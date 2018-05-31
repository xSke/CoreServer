/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.impl.nio.client;

import org.apache.http.nio.protocol.HttpAsyncRequestExecutionHandler;

@Deprecated
interface ResultCallback<T> {
    public void completed(T var1, HttpAsyncRequestExecutionHandler<T> var2);

    public void failed(Exception var1, HttpAsyncRequestExecutionHandler<T> var2);

    public void cancelled(HttpAsyncRequestExecutionHandler<T> var1);

    public boolean isDone();
}

