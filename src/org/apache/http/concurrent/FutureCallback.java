/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.concurrent;

public interface FutureCallback<T> {
    public void completed(T var1);

    public void failed(Exception var1);

    public void cancelled();
}

