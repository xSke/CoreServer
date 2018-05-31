/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.net.packet.play.server;

import com.azureusnation.coreserver.net.PacketBuffer;
import com.azureusnation.coreserver.net.packet.ServerBoundPacket;
import java.io.IOException;

public class PacketSB15ClientSettings
extends ServerBoundPacket {
    private String locale;
    private int viewDistance;
    private int chatMode;
    private boolean chatColors;
    private int skinParts;

    public String getLocale() {
        return this.locale;
    }

    public int getViewDistance() {
        return this.viewDistance;
    }

    public int getChatMode() {
        return this.chatMode;
    }

    public boolean isChatColors() {
        return this.chatColors;
    }

    public int getSkinParts() {
        return this.skinParts;
    }

    @Override
    public void read(PacketBuffer input) throws IOException {
        this.locale = input.readString();
        this.viewDistance = input.readByte();
        this.chatMode = input.readByte();
        this.chatColors = input.readBoolean();
        this.skinParts = input.readByte();
    }
}

