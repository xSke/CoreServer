/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.net.packet.play.server;

import com.azureusnation.coreserver.net.PacketBuffer;
import com.azureusnation.coreserver.net.packet.ServerBoundPacket;
import java.io.IOException;
import mikera.vectorz.Vector3;

public class PacketSB12UpdateSign
extends ServerBoundPacket {
    private Vector3 location;
    private String line1;
    private String line2;
    private String line3;
    private String line4;

    public Vector3 getLocation() {
        return this.location;
    }

    public String getLine1() {
        return this.line1;
    }

    public String getLine2() {
        return this.line2;
    }

    public String getLine3() {
        return this.line3;
    }

    public String getLine4() {
        return this.line4;
    }

    @Override
    public void read(PacketBuffer input) throws IOException {
        this.location = input.readPosition();
        this.line1 = input.readString();
        this.line2 = input.readString();
        this.line3 = input.readString();
        this.line4 = input.readString();
    }
}

