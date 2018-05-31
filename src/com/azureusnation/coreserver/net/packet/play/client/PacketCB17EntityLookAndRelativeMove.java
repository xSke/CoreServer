/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.net.packet.play.client;

import com.azureusnation.coreserver.net.PacketBuffer;
import com.azureusnation.coreserver.net.packet.ClientBoundPacket;
import io.netty.buffer.ByteBuf;
import java.io.IOException;

public class PacketCB17EntityLookAndRelativeMove
extends ClientBoundPacket {
    private int entityId;
    private double deltaX;
    private double deltaY;
    private double deltaZ;
    private double yaw;
    private double pitch;
    private boolean onGround;

    public PacketCB17EntityLookAndRelativeMove(int entityId, double deltaX, double deltaY, double deltaZ, double yaw, double pitch, boolean onGround) {
        this.entityId = entityId;
        this.deltaX = deltaX;
        this.deltaY = deltaY;
        this.deltaZ = deltaZ;
        this.yaw = yaw;
        this.pitch = pitch;
        this.onGround = onGround;
    }

    @Override
    public void write(PacketBuffer input) throws IOException {
        input.writeVarInt(this.entityId);
        input.writeFixedPointByte(this.deltaX);
        input.writeFixedPointByte(this.deltaY);
        input.writeFixedPointByte(this.deltaZ);
        input.writeAngleByte(this.yaw);
        input.writeAngleByte(this.pitch);
        input.writeBoolean(this.onGround);
    }
}

