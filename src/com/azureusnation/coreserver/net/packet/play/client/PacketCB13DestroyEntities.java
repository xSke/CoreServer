/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.net.packet.play.client;

import com.azureusnation.coreserver.entity.Entity;
import com.azureusnation.coreserver.net.PacketBuffer;
import com.azureusnation.coreserver.net.packet.ClientBoundPacket;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PacketCB13DestroyEntities
extends ClientBoundPacket {
    int count;
    List<Entity> entities;

    public PacketCB13DestroyEntities(Entity entity) {
        ArrayList<Entity> entities = new ArrayList<Entity>();
        entities.add(entity);
        this.entities = entities;
        this.count = entities.size();
    }

    public PacketCB13DestroyEntities(List<Entity> entities) {
        this.count = entities.size();
        this.entities = entities;
    }

    @Override
    public void write(PacketBuffer input) throws IOException {
        input.writeVarInt(this.count);
        for (Entity entity : this.entities) {
            input.writeVarInt(entity.getId());
        }
    }
}

