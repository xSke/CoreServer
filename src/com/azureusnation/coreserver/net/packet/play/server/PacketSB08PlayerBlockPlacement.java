/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.net.packet.play.server;

import com.azureusnation.coreserver.item.ItemStack;
import com.azureusnation.coreserver.net.PacketBuffer;
import com.azureusnation.coreserver.net.packet.ServerBoundPacket;
import java.io.IOException;
import mikera.vectorz.Vector3;

public class PacketSB08PlayerBlockPlacement
extends ServerBoundPacket {
    Vector3 position;
    byte face;
    ItemStack itemStack;
    Vector3 cursorPosition;

    public Vector3 getPosition() {
        return this.position;
    }

    public byte getFace() {
        return this.face;
    }

    public ItemStack getItemStack() {
        return this.itemStack;
    }

    public Vector3 getCursorPosition() {
        return this.cursorPosition;
    }

    @Override
    public void read(PacketBuffer input) throws IOException {
        this.position = input.readPosition();
        this.face = input.readByte();
        this.itemStack = input.readItemStack();
        this.cursorPosition = new Vector3((double)input.readByte() / 16.0, (double)input.readByte() / 16.0, (double)input.readByte() / 16.0);
    }
}

