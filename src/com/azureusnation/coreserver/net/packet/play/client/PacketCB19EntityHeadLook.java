/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.net.packet.play.client;

import com.azureusnation.coreserver.net.PacketBuffer;
import com.azureusnation.coreserver.net.packet.ClientBoundPacket;
import java.io.IOException;

public class PacketCB19EntityHeadLook
extends ClientBoundPacket {
    private int entityId;
    private double headYaw;

    public PacketCB19EntityHeadLook(int entityId, double headYaw) {
        this.entityId = entityId;
        this.headYaw = headYaw;
    }

    @Override
    public void write(PacketBuffer input) throws IOException {
        input.writeVarInt(this.entityId);
        input.writeAngleByte(this.headYaw);
    }
}

