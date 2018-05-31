/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.net.packet;

import com.azureusnation.coreserver.net.PacketBuffer;
import com.azureusnation.coreserver.net.packet.Packet;
import java.io.IOException;

public abstract class ClientBoundPacket
extends Packet {
    public abstract void write(PacketBuffer var1) throws IOException;
}

