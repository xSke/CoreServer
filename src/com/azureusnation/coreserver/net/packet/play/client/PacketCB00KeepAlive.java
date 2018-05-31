/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.net.packet.play.client;

import com.azureusnation.coreserver.net.PacketBuffer;
import com.azureusnation.coreserver.net.packet.ClientBoundPacket;
import java.io.IOException;

public class PacketCB00KeepAlive
extends ClientBoundPacket {
    int id;

    public PacketCB00KeepAlive(int id) {
        this.id = id;
    }

    @Override
    public void write(PacketBuffer input) throws IOException {
        input.writeVarInt(this.id);
    }
}

