/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.net.packet.play.client;

import com.azureusnation.coreserver.entity.EntityType;
import com.azureusnation.coreserver.entity.metadata.EntityMetadata;
import com.azureusnation.coreserver.net.PacketBuffer;
import com.azureusnation.coreserver.net.packet.ClientBoundPacket;
import io.netty.buffer.ByteBuf;
import java.io.IOException;
import mikera.vectorz.Vector2;
import mikera.vectorz.Vector3;

public class PacketCB0FSpawnMob
extends ClientBoundPacket {
    private int entityId;
    private EntityType type;
    private Vector3 position;
    private Vector2 rotation;
    private double headPitch;
    private Vector3 velocity;
    private EntityMetadata metadata;

    public PacketCB0FSpawnMob(int entityId, EntityType type, Vector3 position, Vector2 rotation, double headPitch, Vector3 velocity, EntityMetadata metadata) {
        this.entityId = entityId;
        this.type = type;
        this.position = position;
        this.rotation = rotation;
        this.headPitch = headPitch;
        this.velocity = velocity;
        this.metadata = metadata;
    }

    @Override
    public void write(PacketBuffer input) throws IOException {
        input.writeVarInt(this.entityId);
        input.writeByte(this.type.getId());
        input.writeFixedPointInt(this.position.x);
        input.writeFixedPointInt(this.position.y);
        input.writeFixedPointInt(this.position.z);
        input.writeAngleByte(this.rotation.x);
        input.writeAngleByte(this.rotation.y);
        input.writeAngleByte(this.headPitch);
        input.writeVelocity(this.velocity);
        input.writeEntityMetadata(this.metadata);
    }
}

