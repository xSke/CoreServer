/*
 * Decompiled with CFR 0_129.
 */
package io.netty.channel;

public interface MessageSizeEstimator {
    public Handle newHandle();

    public static interface Handle {
        public int size(Object var1);
    }

}

