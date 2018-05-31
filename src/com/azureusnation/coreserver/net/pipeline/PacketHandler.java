/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.net.pipeline;

import com.azureusnation.coreserver.net.PlayerConnection;
import com.azureusnation.coreserver.net.packet.ServerBoundPacket;
import com.azureusnation.coreserver.net.state.State;
import com.azureusnation.coreserver.net.state.StateFFHandshake;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.util.Queue;

public class PacketHandler
extends SimpleChannelInboundHandler<ServerBoundPacket> {
    private PlayerConnection connection;

    public PacketHandler(PlayerConnection connection) {
        this.connection = connection;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ServerBoundPacket packet) throws Exception {
        if (this.connection.getConnectionState() instanceof StateFFHandshake) {
            this.connection.getConnectionState().receivePacket(packet);
        } else {
            this.connection.getPacketQueue().add(packet);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }
}

