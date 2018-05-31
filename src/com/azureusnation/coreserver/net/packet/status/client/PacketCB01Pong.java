/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.net.packet.status.client;

import com.azureusnation.coreserver.net.PacketBuffer;
import com.azureusnation.coreserver.net.packet.ClientBoundPacket;
import io.netty.buffer.ByteBuf;
import java.io.IOException;

public class PacketCB01Pong
extends ClientBoundPacket {
    long time;

    public PacketCB01Pong() {
    }

    public PacketCB01Pong(long time) {
        this.time = time;
    }

    @Override
    public void write(PacketBuffer input) throws IOException {
        input.writeLong(this.time);
    }
}

