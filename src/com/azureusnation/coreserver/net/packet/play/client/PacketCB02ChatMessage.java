/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.net.packet.play.client;

import com.azureusnation.coreserver.chat.Chat;
import com.azureusnation.coreserver.net.PacketBuffer;
import com.azureusnation.coreserver.net.packet.ClientBoundPacket;
import io.netty.buffer.ByteBuf;
import java.io.IOException;

public class PacketCB02ChatMessage
extends ClientBoundPacket {
    private Chat chat;
    private byte chatPosition;

    public PacketCB02ChatMessage(Chat chat, byte chatPosition) {
        this.chat = chat;
        this.chatPosition = chatPosition;
    }

    @Override
    public void write(PacketBuffer input) throws IOException {
        input.writeChat(this.chat);
        input.writeByte(this.chatPosition);
    }
}

