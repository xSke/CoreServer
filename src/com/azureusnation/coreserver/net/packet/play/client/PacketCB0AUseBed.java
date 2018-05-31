/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.net.packet.play.client;

import com.azureusnation.coreserver.net.PacketBuffer;
import com.azureusnation.coreserver.net.packet.ClientBoundPacket;
import io.netty.buffer.ByteBuf;
import java.io.IOException;
import mikera.vectorz.Vector3;

public class PacketCB0AUseBed
extends ClientBoundPacket {
    private int entityId;
    private Vector3 position;

    public PacketCB0AUseBed(int entityId, Vector3 position) {
        this.entityId = entityId;
        this.position = position;
    }

    @Override
    public void write(PacketBuffer input) throws IOException {
        input.writeInt(this.entityId);
        input.writePosition(this.position);
    }
}

