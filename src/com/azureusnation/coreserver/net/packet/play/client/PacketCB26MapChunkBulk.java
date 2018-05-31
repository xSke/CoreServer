/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.net.packet.play.client;

import com.azureusnation.coreserver.net.PacketBuffer;
import com.azureusnation.coreserver.net.packet.ClientBoundPacket;
import io.netty.buffer.ByteBuf;
import java.io.IOException;

public class PacketCB26MapChunkBulk
extends ClientBoundPacket {
    boolean skyLightSent;
    int chunkX;
    int chunkZ;
    short bitmask;
    byte[] data;

    public PacketCB26MapChunkBulk(boolean skyLightSent, int chunkX, int chunkZ, short bitmask, byte[] data) {
        this.skyLightSent = skyLightSent;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.bitmask = bitmask;
        this.data = data;
    }

    @Override
    public void write(PacketBuffer input) throws IOException {
        input.writeBoolean(this.skyLightSent);
        input.writeVarInt(1);
        input.writeInt(this.chunkX);
        input.writeInt(this.chunkZ);
        input.writeShort(this.bitmask);
        input.writeBytes(this.data);
    }
}

