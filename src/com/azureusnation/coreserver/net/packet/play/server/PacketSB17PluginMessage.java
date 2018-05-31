/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.net.packet.play.server;

import com.azureusnation.coreserver.net.PacketBuffer;
import com.azureusnation.coreserver.net.packet.ServerBoundPacket;
import io.netty.buffer.ByteBuf;
import java.io.IOException;

public class PacketSB17PluginMessage
extends ServerBoundPacket {
    private String channel;
    private byte[] message;

    public String getChannel() {
        return this.channel;
    }

    public byte[] getMessage() {
        return this.message;
    }

    @Override
    public void read(PacketBuffer input) throws IOException {
        this.channel = input.readString();
        this.message = new byte[0];
        input.readBytes(this.message);
    }
}

