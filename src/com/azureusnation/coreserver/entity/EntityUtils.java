/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.entity;

import com.azureusnation.coreserver.entity.Entity;
import com.azureusnation.coreserver.entity.EntityType;
import com.azureusnation.coreserver.entity.metadata.EntityMetadata;
import com.azureusnation.coreserver.net.packet.ClientBoundPacket;
import com.azureusnation.coreserver.net.packet.play.client.PacketCB0CSpawnPlayer;
import com.azureusnation.coreserver.net.packet.play.client.PacketCB0FSpawnMob;
import java.util.UUID;
import mikera.vectorz.Vector2;
import mikera.vectorz.Vector3;

public class EntityUtils {
    public static ClientBoundPacket createSpawnPacket(Entity entity) {
        EntityType type = entity.getType();
        switch (type) {
            case PLAYER: {
                return new PacketCB0CSpawnPlayer(entity.getId(), entity.getUuid(), entity.getPosition().x, entity.getPosition().y, entity.getPosition().z, entity.getRotation().x, entity.getRotation().y, 0, entity.getMetadata());
            }
            case ZOMBIE: {
                return new PacketCB0FSpawnMob(entity.getId(), type, entity.getPosition(), entity.getRotation(), entity.getRotation().y, new Vector3(), entity.getMetadata());
            }
        }
        return null;
    }

}

