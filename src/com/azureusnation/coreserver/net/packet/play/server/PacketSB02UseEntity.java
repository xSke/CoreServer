/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.net.packet.play.server;

import com.azureusnation.coreserver.net.PacketBuffer;
import com.azureusnation.coreserver.net.packet.ServerBoundPacket;
import java.io.IOException;

public class PacketSB02UseEntity
extends ServerBoundPacket {
    private int target;
    private int type;
    private float targetX;
    private float targetY;
    private float targetZ;

    public int getTarget() {
        return this.target;
    }

    public int getType() {
        return this.type;
    }

    public float getTargetX() {
        return this.targetX;
    }

    public float getTargetY() {
        return this.targetY;
    }

    public float getTargetZ() {
        return this.targetZ;
    }

    @Override
    public void read(PacketBuffer input) throws IOException {
        this.target = input.readVarInt();
        this.type = input.readVarInt();
        if (this.type == 2) {
            this.targetX = input.readFloat();
            this.targetY = input.readFloat();
            this.targetZ = input.readFloat();
        }
    }
}

