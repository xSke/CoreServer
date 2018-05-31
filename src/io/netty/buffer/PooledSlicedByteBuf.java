/*
 * Decompiled with CFR 0_129.
 */
package io.netty.buffer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledDuplicatedByteBuf;
import io.netty.buffer.SlicedByteBuf;
import io.netty.util.Recycler;

final class PooledSlicedByteBuf
extends SlicedByteBuf {
    private static final Recycler<PooledSlicedByteBuf> RECYCLER = new Recycler<PooledSlicedByteBuf>(){

        @Override
        protected PooledSlicedByteBuf newObject(Recycler.Handle handle) {
            return new PooledSlicedByteBuf(handle);
        }
    };
    private final Recycler.Handle recyclerHandle;

    static PooledSlicedByteBuf newInstance(ByteBuf buffer, int index, int length) {
        PooledSlicedByteBuf buf = RECYCLER.get();
        buf.init(buffer, index, length);
        return buf;
    }

    private PooledSlicedByteBuf(Recycler.Handle recyclerHandle) {
        super(0);
        this.recyclerHandle = recyclerHandle;
    }

    @Override
    public ByteBuf slice(int index, int length) {
        return PooledSlicedByteBuf.newInstance(this, index, length);
    }

    @Override
    public ByteBuf duplicate() {
        return PooledDuplicatedByteBuf.newInstance(this);
    }

    @Override
    protected void deallocate() {
        RECYCLER.recycle(this, this.recyclerHandle);
    }

}

