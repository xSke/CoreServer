/*
 * Decompiled with CFR 0_129.
 */
package io.netty.util.internal;

import io.netty.util.internal.ConcurrentCircularArrayQueueL0Pad;
import io.netty.util.internal.PlatformDependent0;
import java.util.Iterator;
import sun.misc.Unsafe;

abstract class ConcurrentCircularArrayQueue<E>
extends ConcurrentCircularArrayQueueL0Pad<E> {
    protected static final int REF_BUFFER_PAD;
    private static final long REF_ARRAY_BASE;
    private static final int REF_ELEMENT_SHIFT;
    protected final long mask;
    protected final E[] buffer;

    public ConcurrentCircularArrayQueue(int capacity) {
        int actualCapacity = ConcurrentCircularArrayQueue.roundToPowerOfTwo(capacity);
        this.mask = actualCapacity - 1;
        this.buffer = new Object[actualCapacity + REF_BUFFER_PAD * 2];
    }

    private static int roundToPowerOfTwo(int value) {
        return 1 << 32 - Integer.numberOfLeadingZeros(value - 1);
    }

    protected final long calcElementOffset(long index) {
        return ConcurrentCircularArrayQueue.calcElementOffset(index, this.mask);
    }

    protected static final long calcElementOffset(long index, long mask) {
        return REF_ARRAY_BASE + ((index & mask) << REF_ELEMENT_SHIFT);
    }

    protected final void spElement(long offset, E e) {
        ConcurrentCircularArrayQueue.spElement(this.buffer, offset, e);
    }

    protected static final <E> void spElement(E[] buffer, long offset, E e) {
        PlatformDependent0.UNSAFE.putObject(buffer, offset, e);
    }

    protected final void soElement(long offset, E e) {
        ConcurrentCircularArrayQueue.soElement(this.buffer, offset, e);
    }

    protected static final <E> void soElement(E[] buffer, long offset, E e) {
        PlatformDependent0.UNSAFE.putOrderedObject(buffer, offset, e);
    }

    protected final E lpElement(long offset) {
        return ConcurrentCircularArrayQueue.lpElement(this.buffer, offset);
    }

    protected static final <E> E lpElement(E[] buffer, long offset) {
        return (E)PlatformDependent0.UNSAFE.getObject(buffer, offset);
    }

    protected final E lvElement(long offset) {
        return ConcurrentCircularArrayQueue.lvElement(this.buffer, offset);
    }

    protected static final <E> E lvElement(E[] buffer, long offset) {
        return (E)PlatformDependent0.UNSAFE.getObjectVolatile(buffer, offset);
    }

    @Override
    public Iterator<E> iterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        while (this.poll() != null || !this.isEmpty()) {
        }
    }

    public int capacity() {
        return (int)(this.mask + 1L);
    }

    static {
        int scale = PlatformDependent0.UNSAFE.arrayIndexScale(Object[].class);
        if (4 == scale) {
            REF_ELEMENT_SHIFT = 2;
        } else if (8 == scale) {
            REF_ELEMENT_SHIFT = 3;
        } else {
            throw new IllegalStateException("Unknown pointer size");
        }
        REF_BUFFER_PAD = 128 / scale;
        REF_ARRAY_BASE = PlatformDependent0.UNSAFE.arrayBaseOffset(Object[].class) + REF_BUFFER_PAD * scale;
    }
}

