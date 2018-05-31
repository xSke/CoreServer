/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.net.packet.play.client;

import com.azureusnation.coreserver.net.PacketBuffer;
import com.azureusnation.coreserver.net.packet.ClientBoundPacket;
import io.netty.buffer.ByteBuf;
import java.io.IOException;

public class PacketCB07Respawn
extends ClientBoundPacket {
    private int dimension;
    private byte difficulty;
    private byte gamemode;
    private String levelType;

    public PacketCB07Respawn(int dimension, byte difficulty, byte gamemode, String levelType) {
        this.dimension = dimension;
        this.difficulty = difficulty;
        this.gamemode = gamemode;
        this.levelType = levelType;
    }

    @Override
    public void write(PacketBuffer input) throws IOException {
        input.writeInt(this.dimension);
        input.writeByte(this.difficulty);
        input.writeByte(this.gamemode);
        input.writeString(this.levelType);
    }
}

