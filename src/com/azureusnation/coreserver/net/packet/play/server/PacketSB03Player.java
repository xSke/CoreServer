/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.net.packet.play.server;

import com.azureusnation.coreserver.net.PacketBuffer;
import com.azureusnation.coreserver.net.packet.ServerBoundPacket;
import java.io.IOException;

public class PacketSB03Player
extends ServerBoundPacket {
    private boolean onground;

    public boolean isOnground() {
        return this.onground;
    }

    @Override
    public void read(PacketBuffer input) throws IOException {
        this.onground = input.readBoolean();
    }
}

