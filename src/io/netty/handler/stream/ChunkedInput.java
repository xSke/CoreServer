/*
 * Decompiled with CFR 0_129.
 */
package io.netty.handler.stream;

import io.netty.channel.ChannelHandlerContext;

public interface ChunkedInput<B> {
    public boolean isEndOfInput() throws Exception;

    public void close() throws Exception;

    public B readChunk(ChannelHandlerContext var1) throws Exception;
}

