/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.net.packet.play.server;

import com.azureusnation.coreserver.net.PacketBuffer;
import com.azureusnation.coreserver.net.packet.ServerBoundPacket;
import java.io.IOException;

public class PacketSB01ChatMessage
extends ServerBoundPacket {
    private String message;

    public String getMessage() {
        return this.message;
    }

    @Override
    public void read(PacketBuffer input) throws IOException {
        this.message = input.readString();
    }
}

