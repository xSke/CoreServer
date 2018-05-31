/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.net.packet.play.server;

import com.azureusnation.coreserver.net.PacketBuffer;
import com.azureusnation.coreserver.net.packet.ServerBoundPacket;
import java.io.IOException;

public class PacketSB19ResourcePackStatus
extends ServerBoundPacket {
    private String hash;
    private int result;

    public String getHash() {
        return this.hash;
    }

    public int getResult() {
        return this.result;
    }

    @Override
    public void read(PacketBuffer input) throws IOException {
        this.hash = input.readString();
        this.result = input.readVarInt();
    }
}

