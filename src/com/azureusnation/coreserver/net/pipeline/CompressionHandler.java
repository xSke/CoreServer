/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.net.pipeline;

import com.azureusnation.coreserver.net.util.NetUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class CompressionHandler
extends MessageToMessageCodec<ByteBuf, ByteBuf> {
    private Inflater inflater = new Inflater();
    private Deflater deflater = new Deflater();

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        ByteBuf body;
        ByteBuf header = ctx.alloc().buffer(5);
        if (msg.readableBytes() >= 300) {
            int idx = msg.readerIndex();
            int len = msg.readableBytes();
            byte[] sourceData = new byte[len];
            msg.readBytes(sourceData);
            this.deflater.setInput(sourceData);
            this.deflater.finish();
            byte[] compressedData = new byte[len];
            int compressedLength = this.deflater.deflate(compressedData);
            this.deflater.reset();
            if (compressedLength == 0) {
                throw new RuntimeException("Some error, wat");
            }
            if (compressedLength >= len) {
                NetUtil.writeVarInt(header, 0);
                msg.readerIndex(idx);
                msg.retain();
                body = msg;
            } else {
                NetUtil.writeVarInt(header, len);
                body = Unpooled.wrappedBuffer(compressedData, 0, compressedLength);
            }
        } else {
            NetUtil.writeVarInt(header, 0);
            msg.retain();
            body = msg;
        }
        out.add(Unpooled.wrappedBuffer(header, body));
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        int idx = msg.readerIndex();
        int uncompressedSize = NetUtil.readVarInt(msg);
        if (uncompressedSize == 0) {
            int length = msg.readableBytes();
            if (length >= 300) {
                throw new RuntimeException("Received uncompressed message larger than threshold, wut");
            }
            ByteBuf buf = ctx.alloc().buffer(length);
            msg.readBytes(buf, length);
            out.add(buf);
        } else {
            byte[] sourceData = new byte[msg.readableBytes()];
            msg.readBytes(sourceData);
            this.inflater.setInput(sourceData);
            byte[] destData = new byte[uncompressedSize];
            int resultLength = this.inflater.inflate(destData);
            this.inflater.reset();
            if (resultLength == 0) {
                msg.readerIndex(idx);
                msg.retain();
                out.add(msg);
            } else {
                if (resultLength != uncompressedSize) {
                    throw new RuntimeException("Received wrong size from compressed packet");
                }
                out.add(Unpooled.wrappedBuffer(destData));
            }
        }
    }
}

