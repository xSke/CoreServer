/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.net.packet.login.server;

import com.azureusnation.coreserver.net.PacketBuffer;
import com.azureusnation.coreserver.net.packet.ServerBoundPacket;
import java.io.IOException;

public class PacketSB00LoginStart
extends ServerBoundPacket {
    private String name;

    public String getName() {
        return this.name;
    }

    @Override
    public void read(PacketBuffer input) throws IOException {
        this.name = input.readString();
    }
}

