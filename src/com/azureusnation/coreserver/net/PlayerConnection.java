/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.net;

import com.azureusnation.coreserver.net.packet.ServerBoundPacket;
import com.azureusnation.coreserver.net.state.State;
import com.azureusnation.coreserver.net.state.StateFFHandshake;
import io.netty.channel.ChannelFuture;
import io.netty.channel.socket.SocketChannel;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PlayerConnection {
    private Queue<ServerBoundPacket> packetQueue;
    private SocketChannel socket;
    private State connectionState;

    public PlayerConnection(SocketChannel socket) {
        this.socket = socket;
        this.packetQueue = new ConcurrentLinkedQueue<ServerBoundPacket>();
        this.connectionState = new StateFFHandshake(this);
    }

    public void receivePacket(ServerBoundPacket packet) {
        this.connectionState.receivePacket(packet);
    }

    public Queue<ServerBoundPacket> getPacketQueue() {
        return this.packetQueue;
    }

    public State getConnectionState() {
        return this.connectionState;
    }

    public void setConnectionState(State connectionState) {
        this.connectionState = connectionState;
    }

    public void disconnect() {
        this.socket.disconnect();
    }

    public SocketChannel getSocket() {
        return this.socket;
    }

    public void tick() {
        this.connectionState.tick();
    }
}

