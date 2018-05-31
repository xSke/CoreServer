/*
 * Decompiled with CFR 0_129.
 */
package io.netty.util.internal;

import io.netty.util.internal.MpscArrayQueueConsumerField;

final class MpscArrayQueue<E>
extends MpscArrayQueueConsumerField<E> {
    long p40;
    long p41;
    long p42;
    long p43;
    long p44;
    long p45;
    long p46;
    long p30;
    long p31;
    long p32;
    long p33;
    long p34;
    long p35;
    long p36;
    long p37;

    public MpscArrayQueue(int capacity) {
        super(capacity);
    }

    @Override
    public boolean offer(E e) {
        long currentProducerIndex;
        if (null == e) {
            throw new NullPointerException("Null is not a valid element");
        }
        long mask = this.mask;
        long capacity = mask + 1L;
        long consumerIndexCache = this.lvConsumerIndexCache();
        do {
            long wrapPoint;
            if (consumerIndexCache > (wrapPoint = (currentProducerIndex = this.lvProducerIndex()) - capacity)) continue;
            long currHead = this.lvConsumerIndex();
            if (currHead <= wrapPoint) {
                return false;
            }
            this.svConsumerIndexCache(currHead);
            consumerIndexCache = currHead;
        } while (!this.casProducerIndex(currentProducerIndex, currentProducerIndex + 1L));
        long offset = MpscArrayQueue.calcElementOffset(currentProducerIndex, mask);
        this.soElement(offset, e);
        return true;
    }

    public int weakOffer(E e) {
        long wrapPoint;
        if (null == e) {
            throw new NullPointerException("Null is not a valid element");
        }
        long mask = this.mask;
        long capacity = mask + 1L;
        long currentTail = this.lvProducerIndex();
        long consumerIndexCache = this.lvConsumerIndexCache();
        if (consumerIndexCache <= (wrapPoint = currentTail - capacity)) {
            long currHead = this.lvConsumerIndex();
            if (currHead <= wrapPoint) {
                return 1;
            }
            this.svConsumerIndexCache(currHead);
        }
        if (!this.casProducerIndex(currentTail, currentTail + 1L)) {
            return -1;
        }
        long offset = MpscArrayQueue.calcElementOffset(currentTail, mask);
        this.soElement(offset, e);
        return 0;
    }

    @Override
    public E poll() {
        Object[] buffer = this.buffer;
        long consumerIndex = this.lvConsumerIndex();
        long offset = this.calcElementOffset(consumerIndex);
        Object e = MpscArrayQueue.lvElement(buffer, offset);
        if (null == e) {
            if (consumerIndex != this.lvProducerIndex()) {
                while ((e = MpscArrayQueue.lvElement(buffer, offset)) == null) {
                }
            } else {
                return null;
            }
        }
        MpscArrayQueue.spElement(buffer, offset, null);
        this.soConsumerIndex(consumerIndex + 1L);
        return (E)e;
    }

    @Override
    public E peek() {
        Object[] buffer = this.buffer;
        long consumerIndex = this.lvConsumerIndex();
        long offset = this.calcElementOffset(consumerIndex);
        Object e = MpscArrayQueue.lvElement(buffer, offset);
        if (null == e) {
            if (consumerIndex != this.lvProducerIndex()) {
                while ((e = MpscArrayQueue.lvElement(buffer, offset)) == null) {
                }
            } else {
                return null;
            }
        }
        return (E)e;
    }

    @Override
    public int size() {
        long currentProducerIndex;
        long before;
        long after = this.lvConsumerIndex();
        do {
            before = after;
            currentProducerIndex = this.lvProducerIndex();
        } while (before != (after = this.lvConsumerIndex()));
        return (int)(currentProducerIndex - after);
    }

    @Override
    public boolean isEmpty() {
        return this.lvConsumerIndex() == this.lvProducerIndex();
    }
}

