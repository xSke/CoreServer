/*
 * Decompiled with CFR 0_129.
 */
package com.google.common.util.concurrent;

import com.google.common.annotations.Beta;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Beta
public interface CheckedFuture<V, X extends Exception>
extends ListenableFuture<V> {
    public V checkedGet() throws Exception;

    public V checkedGet(long var1, TimeUnit var3) throws TimeoutException, Exception;
}

