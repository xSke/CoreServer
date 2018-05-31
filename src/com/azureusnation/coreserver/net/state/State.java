/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.net.state;

import com.azureusnation.coreserver.net.PlayerConnection;
import com.azureusnation.coreserver.net.packet.ClientBoundPacket;
import com.azureusnation.coreserver.net.packet.ServerBoundPacket;
import com.google.common.collect.BiMap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.socket.SocketChannel;

public abstract class State {
    private final BiMap<Integer, Class<? extends ServerBoundPacket>> serverBoundPacketClasses;
    private final BiMap<Integer, Class<? extends ClientBoundPacket>> clientBoundPacketClasses;
    private PlayerConnection playerConnection;

    public State(PlayerConnection playerConnection, BiMap<Integer, Class<? extends ServerBoundPacket>> serverBoundPacketClasses, BiMap<Integer, Class<? extends ClientBoundPacket>> clientBoundPacketClasses) {
        this.playerConnection = playerConnection;
        this.serverBoundPacketClasses = serverBoundPacketClasses;
        this.clientBoundPacketClasses = clientBoundPacketClasses;
    }

    public PlayerConnection getPlayerConnection() {
        return this.playerConnection;
    }

    public BiMap<Integer, Class<? extends ServerBoundPacket>> getServerBoundPacketClasses() {
        return this.serverBoundPacketClasses;
    }

    public BiMap<Integer, Class<? extends ClientBoundPacket>> getClientBoundPacketClasses() {
        return this.clientBoundPacketClasses;
    }

    public abstract void receivePacket(ServerBoundPacket var1);

    public void disconnect() {
    }

    public void sendPacket(ClientBoundPacket packet) {
        try {
            this.playerConnection.getSocket().writeAndFlush(packet).sync();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void tick() {
    }
}

