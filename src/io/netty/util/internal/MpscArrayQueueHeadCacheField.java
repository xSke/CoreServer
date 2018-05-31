/*
 * Decompiled with CFR 0_129.
 */
package io.netty.util.internal;

import io.netty.util.internal.MpscArrayQueueMidPad;

abstract class MpscArrayQueueHeadCacheField<E>
extends MpscArrayQueueMidPad<E> {
    private volatile long headCache;

    public MpscArrayQueueHeadCacheField(int capacity) {
        super(capacity);
    }

    protected final long lvConsumerIndexCache() {
        return this.headCache;
    }

    protected final void svConsumerIndexCache(long v) {
        this.headCache = v;
    }
}

