/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.net.pipeline;

import com.azureusnation.coreserver.net.PacketBuffer;
import com.azureusnation.coreserver.net.PlayerConnection;
import com.azureusnation.coreserver.net.packet.ClientBoundPacket;
import com.azureusnation.coreserver.net.packet.Packet;
import com.azureusnation.coreserver.net.packet.ServerBoundPacket;
import com.azureusnation.coreserver.net.state.State;
import com.azureusnation.coreserver.net.util.NetUtil;
import com.google.common.collect.BiMap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import java.io.PrintStream;
import java.util.List;

public class PacketCodecHandler
extends MessageToMessageCodec<ByteBuf, Packet> {
    private PlayerConnection connection;

    public PacketCodecHandler(PlayerConnection connection) {
        this.connection = connection;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Packet msg, List<Object> out) throws Exception {
        ByteBuf header = ctx.alloc().buffer(8);
        Integer packetId = this.connection.getConnectionState().getClientBoundPacketClasses().inverse().get(msg.getClass());
        if (packetId == null) {
            throw new RuntimeException("Packet ID for class " + msg.getClass().getSimpleName() + " not registered in state");
        }
        NetUtil.writeVarInt(header, packetId);
        ByteBuf body = ctx.alloc().buffer();
        ((ClientBoundPacket)msg).write(new PacketBuffer(body));
        out.add(Unpooled.wrappedBuffer(header, body));
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        msg.markReaderIndex();
        int packetId = NetUtil.readVarInt(msg);
        Class<? extends ServerBoundPacket> packetClass = this.connection.getConnectionState().getServerBoundPacketClasses().get(packetId);
        if (packetClass == null) {
            System.out.println("Unknown packet ID 0x" + Integer.toHexString(packetId));
            msg.resetReaderIndex();
            return;
        }
        ServerBoundPacket packet = packetClass.newInstance();
        packet.read(new PacketBuffer(msg));
        out.add(packet);
    }
}

