/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.net.packet;

import com.azureusnation.coreserver.net.PacketBuffer;
import com.azureusnation.coreserver.net.packet.Packet;
import java.io.IOException;

public abstract class ServerBoundPacket
extends Packet {
    public abstract void read(PacketBuffer var1) throws IOException;
}

