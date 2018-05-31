/*
 * Decompiled with CFR 0_129.
 */
package io.netty.channel.epoll;

import io.netty.buffer.CompositeByteBuf;
import io.netty.channel.ChannelOutboundBuffer;
import io.netty.channel.epoll.IovArray;
import io.netty.util.concurrent.FastThreadLocal;

final class IovArrayThreadLocal {
    private static final FastThreadLocal<IovArray> ARRAY = new FastThreadLocal<IovArray>(){

        @Override
        protected IovArray initialValue() throws Exception {
            return new IovArray();
        }

        @Override
        protected void onRemoval(IovArray value) throws Exception {
            value.release();
        }
    };

    static IovArray get(ChannelOutboundBuffer buffer) throws Exception {
        IovArray array = ARRAY.get();
        array.clear();
        buffer.forEachFlushedMessage(array);
        return array;
    }

    static IovArray get(CompositeByteBuf buf) throws Exception {
        IovArray array = ARRAY.get();
        array.clear();
        array.add(buf);
        return array;
    }

    private IovArrayThreadLocal() {
    }

}

