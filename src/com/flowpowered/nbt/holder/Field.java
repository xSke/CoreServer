/*
 * Decompiled with CFR 0_129.
 */
package com.flowpowered.nbt.holder;

import com.flowpowered.nbt.Tag;

public interface Field<T> {
    public T getValue(Tag<?> var1) throws IllegalArgumentException;

    public Tag<?> getValue(String var1, T var2);
}

