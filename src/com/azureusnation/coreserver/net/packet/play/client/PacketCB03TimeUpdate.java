/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.net.packet.play.client;

import com.azureusnation.coreserver.net.PacketBuffer;
import com.azureusnation.coreserver.net.packet.ClientBoundPacket;
import io.netty.buffer.ByteBuf;
import java.io.IOException;

public class PacketCB03TimeUpdate
extends ClientBoundPacket {
    private long worldAge;
    private long timeOfDay;

    public PacketCB03TimeUpdate(long worldAge, long timeOfDay) {
        this.worldAge = worldAge;
        this.timeOfDay = timeOfDay;
    }

    @Override
    public void write(PacketBuffer input) throws IOException {
        input.writeLong(this.worldAge);
        input.writeLong(this.timeOfDay);
    }
}

