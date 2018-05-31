/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.net.packet.login.client;

import com.azureusnation.coreserver.net.PacketBuffer;
import com.azureusnation.coreserver.net.packet.ClientBoundPacket;
import java.io.IOException;

public class PacketCB02LoginSuccess
extends ClientBoundPacket {
    String uuid;
    String username;

    public PacketCB02LoginSuccess() {
    }

    public PacketCB02LoginSuccess(String uuid, String username) {
        this.uuid = uuid;
        this.username = username;
    }

    @Override
    public void write(PacketBuffer input) throws IOException {
        input.writeString(this.uuid);
        input.writeString(this.username);
    }
}

