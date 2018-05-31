/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.net.packet.play.server;

import com.azureusnation.coreserver.net.PacketBuffer;
import com.azureusnation.coreserver.net.packet.ServerBoundPacket;
import java.io.IOException;

public class PacketSB09HeldItemChange
extends ServerBoundPacket {
    private int slot;

    public int getSlot() {
        return this.slot;
    }

    @Override
    public void read(PacketBuffer input) throws IOException {
        this.slot = input.readShort();
    }
}

