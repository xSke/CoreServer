/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.net.packet.play.client;

import com.azureusnation.coreserver.entity.metadata.EntityMetadata;
import com.azureusnation.coreserver.net.PacketBuffer;
import com.azureusnation.coreserver.net.packet.ClientBoundPacket;
import io.netty.buffer.ByteBuf;
import java.io.IOException;
import java.util.UUID;

public class PacketCB0CSpawnPlayer
extends ClientBoundPacket {
    private int entityId;
    private UUID uuid;
    private double x;
    private double y;
    private double z;
    private double yaw;
    private double pitch;
    private short currentItem;
    private EntityMetadata metadata;

    public PacketCB0CSpawnPlayer(int entityId, UUID uuid, double x, double y, double z, double yaw, double pitch, short currentItem, EntityMetadata metadata) {
        this.entityId = entityId;
        this.uuid = uuid;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.currentItem = currentItem;
        this.metadata = metadata;
    }

    @Override
    public void write(PacketBuffer input) throws IOException {
        input.writeVarInt(this.entityId);
        input.writeLong(this.uuid.getMostSignificantBits());
        input.writeLong(this.uuid.getLeastSignificantBits());
        input.writeFixedPointInt(this.x);
        input.writeFixedPointInt(this.y);
        input.writeFixedPointInt(this.z);
        input.writeAngleByte(this.yaw);
        input.writeAngleByte(this.pitch);
        input.writeShort(this.currentItem);
        input.writeEntityMetadata(this.metadata);
    }
}

