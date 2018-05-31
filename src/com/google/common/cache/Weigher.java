/*
 * Decompiled with CFR 0_129.
 */
package com.google.common.cache;

import com.google.common.annotations.Beta;
import com.google.common.annotations.GwtCompatible;

@Beta
@GwtCompatible
public interface Weigher<K, V> {
    public int weigh(K var1, V var2);
}

