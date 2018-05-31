/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.net.packet.play.server;

import com.azureusnation.coreserver.net.PacketBuffer;
import com.azureusnation.coreserver.net.packet.ServerBoundPacket;
import java.io.IOException;

public class PacketSB0FConfirmTransaction
extends ServerBoundPacket {
    private int windowsId;
    private int actionNumber;
    private boolean accepted;

    public int getWindowsId() {
        return this.windowsId;
    }

    public int getActionNumber() {
        return this.actionNumber;
    }

    public boolean isAccepted() {
        return this.accepted;
    }

    @Override
    public void read(PacketBuffer input) throws IOException {
        this.windowsId = input.readByte();
        this.actionNumber = input.readShort();
        this.accepted = input.readBoolean();
    }
}

