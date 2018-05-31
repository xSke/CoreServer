/*
 * Decompiled with CFR 0_129.
 */
package io.netty.util.internal;

import io.netty.util.internal.MpscArrayQueueL2Pad;
import io.netty.util.internal.PlatformDependent0;
import java.lang.reflect.Field;
import sun.misc.Unsafe;

abstract class MpscArrayQueueConsumerField<E>
extends MpscArrayQueueL2Pad<E> {
    private static final long C_INDEX_OFFSET;
    private volatile long consumerIndex;

    public MpscArrayQueueConsumerField(int capacity) {
        super(capacity);
    }

    protected final long lvConsumerIndex() {
        return this.consumerIndex;
    }

    protected void soConsumerIndex(long l) {
        PlatformDependent0.UNSAFE.putOrderedLong(this, C_INDEX_OFFSET, l);
    }

    static {
        try {
            C_INDEX_OFFSET = PlatformDependent0.UNSAFE.objectFieldOffset(MpscArrayQueueConsumerField.class.getDeclaredField("consumerIndex"));
        }
        catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
}

