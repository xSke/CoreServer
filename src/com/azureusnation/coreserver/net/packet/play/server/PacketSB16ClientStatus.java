/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.net.packet.play.server;

import com.azureusnation.coreserver.net.PacketBuffer;
import com.azureusnation.coreserver.net.packet.ServerBoundPacket;
import java.io.IOException;

public class PacketSB16ClientStatus
extends ServerBoundPacket {
    private int actionId;

    public int getActionId() {
        return this.actionId;
    }

    @Override
    public void read(PacketBuffer input) throws IOException {
        this.actionId = input.readVarInt();
    }
}

