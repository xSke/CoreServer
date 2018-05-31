/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.net.packet.play.server;

import com.azureusnation.coreserver.net.PacketBuffer;
import com.azureusnation.coreserver.net.packet.ServerBoundPacket;
import java.io.IOException;

public class PacketSB11EnchantItem
extends ServerBoundPacket {
    private int windowsId;
    private int enchantment;

    public int getWindowsId() {
        return this.windowsId;
    }

    public int getEnchantment() {
        return this.enchantment;
    }

    @Override
    public void read(PacketBuffer input) throws IOException {
        this.windowsId = input.readByte();
        this.enchantment = input.readByte();
    }
}

