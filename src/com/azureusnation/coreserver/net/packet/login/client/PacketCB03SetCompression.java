/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.net.packet.login.client;

import com.azureusnation.coreserver.net.PacketBuffer;
import com.azureusnation.coreserver.net.packet.ClientBoundPacket;
import java.io.IOException;

public class PacketCB03SetCompression
extends ClientBoundPacket {
    int threshold;

    public PacketCB03SetCompression(int threshold) {
        this.threshold = threshold;
    }

    @Override
    public void write(PacketBuffer input) throws IOException {
        input.writeVarInt(this.threshold);
    }
}

