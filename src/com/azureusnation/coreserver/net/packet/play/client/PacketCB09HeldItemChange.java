/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.net.packet.play.client;

import com.azureusnation.coreserver.net.PacketBuffer;
import com.azureusnation.coreserver.net.packet.ClientBoundPacket;
import io.netty.buffer.ByteBuf;
import java.io.IOException;

public class PacketCB09HeldItemChange
extends ClientBoundPacket {
    private byte slotSelected;

    public PacketCB09HeldItemChange(byte slotSelected) {
        this.slotSelected = slotSelected;
    }

    @Override
    public void write(PacketBuffer input) throws IOException {
        input.writeByte(this.slotSelected);
    }
}

