/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.net.packet.play.client;

import com.azureusnation.coreserver.net.PacketBuffer;
import com.azureusnation.coreserver.net.packet.ClientBoundPacket;
import io.netty.buffer.ByteBuf;
import java.io.IOException;

public class PacketCB06UpdateHealth
extends ClientBoundPacket {
    private float health;
    private int food;
    private float saturation;

    public PacketCB06UpdateHealth(float health, int food, float saturation) {
        this.health = health;
        this.food = food;
        this.saturation = saturation;
    }

    @Override
    public void write(PacketBuffer input) throws IOException {
        input.writeFloat(this.health);
        input.writeVarInt(this.food);
        input.writeFloat(this.saturation);
    }
}

