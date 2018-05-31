/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.net.packet.play.server;

import com.azureusnation.coreserver.net.PacketBuffer;
import com.azureusnation.coreserver.net.packet.ServerBoundPacket;
import java.io.IOException;

public class PacketSB00KeepAlive
extends ServerBoundPacket {
    private int id;

    public int getId() {
        return this.id;
    }

    @Override
    public void read(PacketBuffer input) throws IOException {
        this.id = input.readVarInt();
    }
}

