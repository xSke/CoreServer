/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.net.packet.play.server;

import com.azureusnation.coreserver.net.PacketBuffer;
import com.azureusnation.coreserver.net.packet.ServerBoundPacket;
import java.io.IOException;

public class PacketSB13PlayerAbilities
extends ServerBoundPacket {
    private int flags;
    private float flyingSpeed;
    private float walkingSpeed;

    public int getFlags() {
        return this.flags;
    }

    public float getFlyingSpeed() {
        return this.flyingSpeed;
    }

    public float getWalkingSpeed() {
        return this.walkingSpeed;
    }

    @Override
    public void read(PacketBuffer input) throws IOException {
        this.flags = input.readByte();
        this.flyingSpeed = input.readFloat();
        this.walkingSpeed = input.readFloat();
    }
}

