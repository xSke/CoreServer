/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.net.packet.play.server;

import com.azureusnation.coreserver.net.PacketBuffer;
import com.azureusnation.coreserver.net.packet.ServerBoundPacket;
import java.io.IOException;
import java.util.UUID;

public class PacketSB18Spectate
extends ServerBoundPacket {
    private UUID uuid;

    public UUID getUuid() {
        return this.uuid;
    }

    @Override
    public void read(PacketBuffer input) throws IOException {
        this.uuid = input.readUuid();
    }
}

