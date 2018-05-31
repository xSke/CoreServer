/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.net.packet.play.client;

import com.azureusnation.coreserver.net.PacketBuffer;
import com.azureusnation.coreserver.net.packet.ClientBoundPacket;
import java.io.IOException;

public class PacketCB40Disconnect
extends ClientBoundPacket {
    String message;

    public PacketCB40Disconnect(String message) {
        this.message = message;
    }

    @Override
    public void write(PacketBuffer input) throws IOException {
        input.writeString(this.message);
    }
}

