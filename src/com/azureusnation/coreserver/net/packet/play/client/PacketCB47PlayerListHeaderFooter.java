/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.net.packet.play.client;

import com.azureusnation.coreserver.chat.Chat;
import com.azureusnation.coreserver.net.PacketBuffer;
import com.azureusnation.coreserver.net.packet.ClientBoundPacket;
import java.io.IOException;

public class PacketCB47PlayerListHeaderFooter
extends ClientBoundPacket {
    private Chat header;
    private Chat footer;

    public PacketCB47PlayerListHeaderFooter(Chat header, Chat footer) {
        this.header = header;
        this.footer = footer;
    }

    @Override
    public void write(PacketBuffer input) throws IOException {
        input.writeChat(this.header);
        input.writeChat(this.footer);
    }
}

