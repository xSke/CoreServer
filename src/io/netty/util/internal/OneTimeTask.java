/*
 * Decompiled with CFR 0_129.
 */
package io.netty.util.internal;

import io.netty.util.internal.MpscLinkedQueueNode;

public abstract class OneTimeTask
extends MpscLinkedQueueNode<Runnable>
implements Runnable {
    @Override
    public Runnable value() {
        return this;
    }
}

