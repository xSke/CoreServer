/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.net.packet.play.server;

import com.azureusnation.coreserver.net.PacketBuffer;
import com.azureusnation.coreserver.net.packet.ServerBoundPacket;
import java.io.IOException;
import mikera.vectorz.Vector3;

public class PacketSB07PlayerDigging
extends ServerBoundPacket {
    private byte status;
    private Vector3 position;
    private byte face;

    public byte getStatus() {
        return this.status;
    }

    public Vector3 getPosition() {
        return this.position;
    }

    public byte getFace() {
        return this.face;
    }

    @Override
    public void read(PacketBuffer input) throws IOException {
        this.status = input.readByte();
        this.position = input.readPosition();
        this.face = input.readByte();
    }
}

