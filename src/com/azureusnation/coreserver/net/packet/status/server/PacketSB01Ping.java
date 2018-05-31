/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.net.packet.status.server;

import com.azureusnation.coreserver.net.PacketBuffer;
import com.azureusnation.coreserver.net.packet.ServerBoundPacket;
import java.io.IOException;

public class PacketSB01Ping
extends ServerBoundPacket {
    long time;

    @Override
    public void read(PacketBuffer input) throws IOException {
        this.time = input.readLong();
    }

    public long getTime() {
        return this.time;
    }
}

