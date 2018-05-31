/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.net.packet.play.server;

import com.azureusnation.coreserver.net.PacketBuffer;
import com.azureusnation.coreserver.net.packet.ServerBoundPacket;
import java.io.IOException;
import mikera.vectorz.Vector3;

public class PacketSB14TabComplete
extends ServerBoundPacket {
    private String text;
    private boolean hasPosition;
    private Vector3 location;

    public String getText() {
        return this.text;
    }

    public boolean isHasPosition() {
        return this.hasPosition;
    }

    public Vector3 getLocation() {
        return this.location;
    }

    @Override
    public void read(PacketBuffer input) throws IOException {
        this.text = input.readString();
        this.hasPosition = input.readBoolean();
        if (this.hasPosition) {
            this.location = input.readPosition();
        }
    }
}

