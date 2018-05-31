/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.entity;

import com.azureusnation.coreserver.entity.EntityType;
import com.azureusnation.coreserver.entity.Player;
import com.azureusnation.coreserver.entity.metadata.EntityMetadata;
import com.azureusnation.coreserver.net.packet.ClientBoundPacket;
import com.azureusnation.coreserver.net.packet.play.client.PacketCB15EntityRelativeMove;
import com.azureusnation.coreserver.net.packet.play.client.PacketCB16EntityLook;
import com.azureusnation.coreserver.net.packet.play.client.PacketCB17EntityLookAndRelativeMove;
import com.azureusnation.coreserver.net.packet.play.client.PacketCB18EntityTeleport;
import com.azureusnation.coreserver.net.packet.play.client.PacketCB19EntityHeadLook;
import com.azureusnation.coreserver.room.Room;
import java.util.UUID;
import mikera.vectorz.AVector;
import mikera.vectorz.Vector2;
import mikera.vectorz.Vector3;

public abstract class Entity {
    public static int nextId;
    private UUID uuid;
    private int id;
    private Vector3 position;
    private Vector2 rotation;
    private Room room;
    private Vector3 positionLastClientUpdate;
    private Vector2 rotationLastClientUpdate;
    private int clientUpdateEvery = 10;
    private int clientUpdateCounter;
    private EntityMetadata metadata;

    public Entity(UUID uuid, int id, Vector3 position, Vector2 rotation, Room room) {
        this.uuid = uuid;
        this.id = id;
        this.position = position;
        this.rotation = rotation;
        this.room = room;
        this.positionLastClientUpdate = position.clone();
        this.rotationLastClientUpdate = rotation.clone();
        this.metadata = new EntityMetadata();
        this.metadata.set(0, (byte)0);
    }

    public Entity(UUID uuid, Vector3 position, Vector2 rotation, Room room) {
        this(uuid, nextId++, position, rotation, room);
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public Vector3 getPosition() {
        return this.position;
    }

    public void setPosition(Vector3 position) {
        this.position.set(position);
    }

    public Vector2 getRotation() {
        return this.rotation;
    }

    public void setRotation(Vector2 rotation) {
        this.rotation.set(rotation);
    }

    public int getId() {
        return this.id;
    }

    public Room getRoom() {
        return this.room;
    }

    public void setPositionAndRotation(Vector3 position, Vector2 rotation) {
        this.setPosition(position);
        this.setRotation(rotation);
    }

    public int getClientUpdateEvery() {
        return this.clientUpdateEvery;
    }

    public void setClientUpdateEvery(int clientUpdateEvery) {
        this.clientUpdateEvery = clientUpdateEvery;
    }

    public void tick() {
        if (this.clientUpdateCounter % this.clientUpdateEvery == 0) {
            this.broadcastUpdate();
            this.positionLastClientUpdate.set(this.position);
            this.rotationLastClientUpdate.set(this.rotation);
        }
        ++this.clientUpdateCounter;
    }

    private void broadcastUpdate() {
        if (!(this instanceof Player) || !((Player)this).isInvisible()) {
            boolean positionChanged = false;
            boolean rotationChanged = false;
            if (!this.positionLastClientUpdate.epsilonEquals(this.position, 0.10000000149011612)) {
                positionChanged = true;
            }
            if (!this.rotationLastClientUpdate.epsilonEquals(this.rotation, 0.009999999776482582)) {
                rotationChanged = true;
            }
            ClientBoundPacket toSend = null;
            if (this.positionLastClientUpdate.distanceSquared(this.position) < 16.0) {
                if (positionChanged && rotationChanged) {
                    toSend = new PacketCB17EntityLookAndRelativeMove(this.id, this.position.x - this.positionLastClientUpdate.x, this.position.y - this.positionLastClientUpdate.y, this.position.z - this.positionLastClientUpdate.z, this.rotation.x, this.rotation.y, true);
                } else if (positionChanged) {
                    toSend = new PacketCB15EntityRelativeMove(this.id, this.position.x - this.positionLastClientUpdate.x, this.position.y - this.positionLastClientUpdate.y, this.position.z - this.positionLastClientUpdate.z, true);
                } else if (rotationChanged) {
                    toSend = new PacketCB16EntityLook(this.id, this.rotation.x, this.rotation.y, true);
                }
                if (rotationChanged) {
                    PacketCB19EntityHeadLook headYaw = new PacketCB19EntityHeadLook(this.id, this.rotation.x);
                    if (this instanceof Player) {
                        this.room.broadcastPacketExcept(headYaw, (Player)this);
                    } else {
                        this.room.broadcastPacket(headYaw);
                    }
                }
            } else {
                toSend = new PacketCB18EntityTeleport(this.id, this.position.x, this.position.y, this.position.z, this.rotation.x, this.rotation.y, true);
            }
            if (toSend != null) {
                if (this instanceof Player) {
                    this.room.broadcastPacketExcept(toSend, (Player)this);
                } else {
                    this.room.broadcastPacket(toSend);
                }
            }
        }
    }

    public EntityMetadata getMetadata() {
        return this.metadata;
    }

    public Vector3 getOldPosition() {
        return this.positionLastClientUpdate;
    }

    public Vector2 getOldRotation() {
        return this.rotationLastClientUpdate;
    }

    public abstract EntityType getType();
}

