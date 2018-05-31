/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.net.packet.status.client;

import com.azureusnation.coreserver.net.PacketBuffer;
import com.azureusnation.coreserver.net.packet.ClientBoundPacket;
import java.io.IOException;

public class PacketCB00StatusResponse
extends ClientBoundPacket {
    String jsonResponse;

    public PacketCB00StatusResponse() {
    }

    public PacketCB00StatusResponse(String jsonResponse) {
        this.jsonResponse = jsonResponse;
    }

    @Override
    public void write(PacketBuffer input) throws IOException {
        input.writeString(this.jsonResponse);
    }
}

