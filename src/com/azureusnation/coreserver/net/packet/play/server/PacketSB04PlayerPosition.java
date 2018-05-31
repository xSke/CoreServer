/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.net.packet.play.server;

import com.azureusnation.coreserver.net.PacketBuffer;
import com.azureusnation.coreserver.net.packet.ServerBoundPacket;
import java.io.IOException;

public class PacketSB04PlayerPosition
extends ServerBoundPacket {
    private double x;
    private double y;
    private double z;
    private boolean onGround;

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getZ() {
        return this.z;
    }

    public boolean isOnGround() {
        return this.onGround;
    }

    @Override
    public void read(PacketBuffer input) throws IOException {
        this.x = input.readDouble();
        this.y = input.readDouble();
        this.z = input.readDouble();
        this.onGround = input.readBoolean();
    }
}

