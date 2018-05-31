/*
 * Decompiled with CFR 0_129.
 */
package com.google.common.io;

import java.io.IOException;

@Deprecated
public interface OutputSupplier<T> {
    public T getOutput() throws IOException;
}

