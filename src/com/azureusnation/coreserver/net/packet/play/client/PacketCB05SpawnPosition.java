/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.net.packet.play.client;

import com.azureusnation.coreserver.net.PacketBuffer;
import com.azureusnation.coreserver.net.packet.ClientBoundPacket;
import java.io.IOException;
import mikera.vectorz.Vector3;

public class PacketCB05SpawnPosition
extends ClientBoundPacket {
    Vector3 position;

    public PacketCB05SpawnPosition(Vector3 position) {
        this.position = position;
    }

    @Override
    public void write(PacketBuffer input) throws IOException {
        input.writePosition(this.position);
    }
}

