/*
 * Decompiled with CFR 0_129.
 */
package io.netty.util.internal;

import io.netty.util.internal.MpscArrayQueueL1Pad;
import io.netty.util.internal.PlatformDependent0;
import java.lang.reflect.Field;
import sun.misc.Unsafe;

abstract class MpscArrayQueueTailField<E>
extends MpscArrayQueueL1Pad<E> {
    private static final long P_INDEX_OFFSET;
    private volatile long producerIndex;

    public MpscArrayQueueTailField(int capacity) {
        super(capacity);
    }

    protected final long lvProducerIndex() {
        return this.producerIndex;
    }

    protected final boolean casProducerIndex(long expect, long newValue) {
        return PlatformDependent0.UNSAFE.compareAndSwapLong(this, P_INDEX_OFFSET, expect, newValue);
    }

    static {
        try {
            P_INDEX_OFFSET = PlatformDependent0.UNSAFE.objectFieldOffset(MpscArrayQueueTailField.class.getDeclaredField("producerIndex"));
        }
        catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
}

