/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.net.packet.play.client;

import com.azureusnation.coreserver.net.PacketBuffer;
import com.azureusnation.coreserver.net.packet.ClientBoundPacket;
import io.netty.buffer.ByteBuf;
import java.io.IOException;

public class PacketCB0BAnimation
extends ClientBoundPacket {
    private int entityId;
    private byte animation;

    public PacketCB0BAnimation(int entityId, byte animation) {
        this.entityId = entityId;
        this.animation = animation;
    }

    @Override
    public void write(PacketBuffer input) throws IOException {
        input.writeVarInt(this.entityId);
        input.writeByte(this.animation);
    }
}

