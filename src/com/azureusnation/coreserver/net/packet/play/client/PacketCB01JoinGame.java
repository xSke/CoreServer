/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.net.packet.play.client;

import com.azureusnation.coreserver.net.PacketBuffer;
import com.azureusnation.coreserver.net.packet.ClientBoundPacket;
import io.netty.buffer.ByteBuf;
import java.io.IOException;

public class PacketCB01JoinGame
extends ClientBoundPacket {
    int entityID;
    int gamemode;
    byte dimension;
    int difficulty;
    int maxPlayers;
    String levelType;
    boolean reducedDebugInfo;

    public PacketCB01JoinGame(int entityID, int gamemode, byte dimension, int difficulty, int maxPlayers, String levelType, boolean reducedDebugInfo) {
        this.entityID = entityID;
        this.gamemode = gamemode;
        this.dimension = dimension;
        this.difficulty = difficulty;
        this.maxPlayers = maxPlayers;
        this.levelType = levelType;
        this.reducedDebugInfo = reducedDebugInfo;
    }

    @Override
    public void write(PacketBuffer input) throws IOException {
        input.writeInt(this.entityID);
        input.writeByte(this.gamemode);
        input.writeByte(this.dimension);
        input.writeByte(this.difficulty);
        input.writeByte(this.maxPlayers);
        input.writeString(this.levelType);
        input.writeBoolean(this.reducedDebugInfo);
    }
}

