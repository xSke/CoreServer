/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.net.state;

import com.azureusnation.coreserver.net.PlayerConnection;
import com.azureusnation.coreserver.net.packet.ClientBoundPacket;
import com.azureusnation.coreserver.net.packet.ServerBoundPacket;
import com.azureusnation.coreserver.net.packet.handshake.server.PacketSB00Handshake;
import com.azureusnation.coreserver.net.state.State;
import com.azureusnation.coreserver.net.state.State01Status;
import com.azureusnation.coreserver.net.state.State02Login;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;

public class StateFFHandshake
extends State {
    public StateFFHandshake(PlayerConnection pc) {
        super(pc, (BiMap<Integer, Class<? extends ServerBoundPacket>>)((Object)ImmutableBiMap.builder().put((Object)0, PacketSB00Handshake.class).build()), (BiMap<Integer, Class<? extends ClientBoundPacket>>)((Object)ImmutableBiMap.builder().build()));
    }

    @Override
    public void receivePacket(ServerBoundPacket packet) {
        if (packet instanceof PacketSB00Handshake) {
            int nextState = ((PacketSB00Handshake)packet).getNextState();
            if (nextState == 1) {
                this.getPlayerConnection().setConnectionState(new State01Status(this.getPlayerConnection()));
            }
            if (nextState == 2) {
                this.getPlayerConnection().setConnectionState(new State02Login(this.getPlayerConnection()));
            }
        }
    }
}

