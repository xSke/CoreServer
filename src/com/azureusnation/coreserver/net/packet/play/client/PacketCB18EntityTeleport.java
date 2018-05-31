/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.net.packet.play.client;

import com.azureusnation.coreserver.net.PacketBuffer;
import com.azureusnation.coreserver.net.packet.ClientBoundPacket;
import io.netty.buffer.ByteBuf;
import java.io.IOException;

public class PacketCB18EntityTeleport
extends ClientBoundPacket {
    private int entityId;
    private double x;
    private double y;
    private double z;
    private double yaw;
    private double pitch;
    private boolean onGround;

    public PacketCB18EntityTeleport(int entityId, double x, double y, double z, double yaw, double pitch, boolean onGround) {
        this.entityId = entityId;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.onGround = onGround;
    }

    @Override
    public void write(PacketBuffer input) throws IOException {
        input.writeVarInt(this.entityId);
        input.writeFixedPointInt(this.x);
        input.writeFixedPointInt(this.y);
        input.writeFixedPointInt(this.z);
        input.writeAngleByte(this.yaw);
        input.writeAngleByte(this.pitch);
        input.writeBoolean(this.onGround);
    }
}

