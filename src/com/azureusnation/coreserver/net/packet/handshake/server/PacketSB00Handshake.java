/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.net.packet.handshake.server;

import com.azureusnation.coreserver.net.PacketBuffer;
import com.azureusnation.coreserver.net.packet.ServerBoundPacket;
import java.io.IOException;

public class PacketSB00Handshake
extends ServerBoundPacket {
    private int protocolVersion;
    private String address;
    private int port;
    private int nextState;

    @Override
    public void read(PacketBuffer input) throws IOException {
        this.protocolVersion = input.readVarInt();
        this.address = input.readString();
        this.port = input.readUnsignedShort();
        this.nextState = input.readVarInt();
    }

    public int getProtocolVersion() {
        return this.protocolVersion;
    }

    public String getAddress() {
        return this.address;
    }

    public int getPort() {
        return this.port;
    }

    public int getNextState() {
        return this.nextState;
    }
}

