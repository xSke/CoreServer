/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.net.pipeline;

import com.azureusnation.coreserver.net.util.NetUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import java.util.List;

public class FramingHandler
extends ByteToMessageCodec<ByteBuf> {
    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) throws Exception {
        NetUtil.writeVarInt(out, msg.readableBytes());
        out.writeBytes(msg);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        in.markReaderIndex();
        if (!NetUtil.canReadVarInt(in)) {
            return;
        }
        int len = NetUtil.readVarInt(in);
        if (in.readableBytes() < len) {
            in.resetReaderIndex();
            return;
        }
        ByteBuf buf = ctx.alloc().buffer(len);
        in.readBytes(buf, len);
        out.add(buf);
    }
}

