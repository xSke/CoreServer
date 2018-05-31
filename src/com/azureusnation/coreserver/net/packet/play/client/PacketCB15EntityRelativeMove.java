/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.net.packet.play.client;

import com.azureusnation.coreserver.net.PacketBuffer;
import com.azureusnation.coreserver.net.packet.ClientBoundPacket;
import io.netty.buffer.ByteBuf;
import java.io.IOException;

public class PacketCB15EntityRelativeMove
extends ClientBoundPacket {
    private int entityId;
    private double deltaX;
    private double deltaY;
    private double deltaZ;
    private boolean onGround;

    public PacketCB15EntityRelativeMove(int entityId, double deltaX, double deltaY, double deltaZ, boolean onGround) {
        this.entityId = entityId;
        this.deltaX = deltaX;
        this.deltaY = deltaY;
        this.deltaZ = deltaZ;
        this.onGround = onGround;
    }

    @Override
    public void write(PacketBuffer input) throws IOException {
        input.writeVarInt(this.entityId);
        input.writeFixedPointByte(this.deltaX);
        input.writeFixedPointByte(this.deltaY);
        input.writeFixedPointByte(this.deltaZ);
        input.writeBoolean(this.onGround);
    }
}

