/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.net.packet.play.server;

import com.azureusnation.coreserver.net.PacketBuffer;
import com.azureusnation.coreserver.net.packet.ServerBoundPacket;
import java.io.IOException;

public class PacketSB0CSteerVehicle
extends ServerBoundPacket {
    private float sideways;
    private float forward;
    private int flags;

    public float getSideways() {
        return this.sideways;
    }

    public float getForward() {
        return this.forward;
    }

    public int getFlags() {
        return this.flags;
    }

    @Override
    public void read(PacketBuffer input) throws IOException {
        this.sideways = input.readFloat();
        this.forward = input.readFloat();
        this.flags = input.readByte();
    }
}

