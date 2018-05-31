/*
 * Decompiled with CFR 0_129.
 */
package io.netty.buffer;

import io.netty.buffer.AbstractByteBuf;
import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCounted;
import java.nio.ByteBuffer;

public abstract class AbstractDerivedByteBuf
extends AbstractByteBuf {
    protected AbstractDerivedByteBuf(int maxCapacity) {
        super(maxCapacity);
    }

    @Override
    public final int refCnt() {
        return this.unwrap().refCnt();
    }

    @Override
    public final ByteBuf retain() {
        this.unwrap().retain();
        return this;
    }

    @Override
    public final ByteBuf retain(int increment) {
        this.unwrap().retain(increment);
        return this;
    }

    @Override
    public final boolean release() {
        if (this.unwrap().release()) {
            this.deallocate();
            return true;
        }
        return false;
    }

    @Override
    public final boolean release(int decrement) {
        if (this.unwrap().release(decrement)) {
            this.deallocate();
            return true;
        }
        return false;
    }

    @Override
    public ByteBuffer internalNioBuffer(int index, int length) {
        return this.nioBuffer(index, length);
    }

    @Override
    public ByteBuffer nioBuffer(int index, int length) {
        return this.unwrap().nioBuffer(index, length);
    }

    protected void deallocate() {
    }
}

