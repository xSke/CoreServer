/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.net.packet.play.client;

import com.azureusnation.coreserver.net.PacketBuffer;
import com.azureusnation.coreserver.net.packet.ClientBoundPacket;
import io.netty.buffer.ByteBuf;
import java.io.IOException;

public class PacketCB16EntityLook
extends ClientBoundPacket {
    private int entityId;
    private double yaw;
    private double pitch;
    private boolean onGround;

    public PacketCB16EntityLook(int entityId, double yaw, double pitch, boolean onGround) {
        this.entityId = entityId;
        this.yaw = yaw;
        this.pitch = pitch;
        this.onGround = onGround;
    }

    @Override
    public void write(PacketBuffer input) throws IOException {
        input.writeVarInt(this.entityId);
        input.writeAngleByte(this.yaw);
        input.writeAngleByte(this.pitch);
        input.writeBoolean(this.onGround);
    }
}

