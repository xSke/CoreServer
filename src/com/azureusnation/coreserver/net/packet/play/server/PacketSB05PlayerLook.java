/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.net.packet.play.server;

import com.azureusnation.coreserver.net.PacketBuffer;
import com.azureusnation.coreserver.net.packet.ServerBoundPacket;
import java.io.IOException;

public class PacketSB05PlayerLook
extends ServerBoundPacket {
    private float yaw;
    private float pitch;
    private boolean onGround;

    public float getYaw() {
        return this.yaw;
    }

    public float getPitch() {
        return this.pitch;
    }

    public boolean isOnGround() {
        return this.onGround;
    }

    @Override
    public void read(PacketBuffer input) throws IOException {
        this.yaw = input.readFloat();
        this.pitch = input.readFloat();
        this.onGround = input.readBoolean();
    }
}

