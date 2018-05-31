/*
 * Decompiled with CFR 0_129.
 */
package io.netty.util;

public interface ResourceLeak {
    public void record();

    public boolean close();
}

