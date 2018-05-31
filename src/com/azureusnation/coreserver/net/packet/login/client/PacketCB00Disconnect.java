/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.net.packet.login.client;

import com.azureusnation.coreserver.chat.Chat;
import com.azureusnation.coreserver.chat.ChatBuilder;
import com.azureusnation.coreserver.net.PacketBuffer;
import com.azureusnation.coreserver.net.packet.ClientBoundPacket;
import java.io.IOException;

public class PacketCB00Disconnect
extends ClientBoundPacket {
    Chat message;

    public PacketCB00Disconnect() {
    }

    public PacketCB00Disconnect(Chat message) {
        this.message = message;
    }

    public PacketCB00Disconnect(String s) {
        this(Chat.builder().text(s).build());
    }

    @Override
    public void write(PacketBuffer input) throws IOException {
        input.writeChat(this.message);
    }
}

