/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.net.packet.play.server;

import com.azureusnation.coreserver.net.PacketBuffer;
import com.azureusnation.coreserver.net.packet.ServerBoundPacket;
import java.io.IOException;

public class PacketSB0BEntityAction
extends ServerBoundPacket {
    private int entityId;
    private int actionId;
    private int jumpBoost;

    public int getEntityId() {
        return this.entityId;
    }

    public int getActionId() {
        return this.actionId;
    }

    public int getJumpBoost() {
        return this.jumpBoost;
    }

    @Override
    public void read(PacketBuffer input) throws IOException {
        this.entityId = input.readVarInt();
        this.actionId = input.readVarInt();
        this.jumpBoost = input.readVarInt();
    }
}

