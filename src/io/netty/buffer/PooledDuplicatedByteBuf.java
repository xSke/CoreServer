/*
 * Decompiled with CFR 0_129.
 */
package io.netty.buffer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.DuplicatedByteBuf;
import io.netty.buffer.PooledSlicedByteBuf;
import io.netty.util.Recycler;

final class PooledDuplicatedByteBuf
extends DuplicatedByteBuf {
    private static final Recycler<PooledDuplicatedByteBuf> RECYCLER = new Recycler<PooledDuplicatedByteBuf>(){

        @Override
        protected PooledDuplicatedByteBuf newObject(Recycler.Handle handle) {
            return new PooledDuplicatedByteBuf(handle);
        }
    };
    private final Recycler.Handle recyclerHandle;

    static PooledDuplicatedByteBuf newInstance(ByteBuf buffer) {
        PooledDuplicatedByteBuf buf = RECYCLER.get();
        buf.init(buffer);
        return buf;
    }

    private PooledDuplicatedByteBuf(Recycler.Handle recyclerHandle) {
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

