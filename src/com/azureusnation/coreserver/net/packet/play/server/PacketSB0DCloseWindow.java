/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.net.packet.play.server;

import com.azureusnation.coreserver.net.PacketBuffer;
import com.azureusnation.coreserver.net.packet.ServerBoundPacket;
import java.io.IOException;

public class PacketSB0DCloseWindow
extends ServerBoundPacket {
    private int windowsId;

    public int getWindowsId() {
        return this.windowsId;
    }

    @Override
    public void read(PacketBuffer input) throws IOException {
        this.windowsId = input.readByte();
    }
}

