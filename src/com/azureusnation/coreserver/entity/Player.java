/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.entity;

import com.azureusnation.coreserver.chat.Chat;
import com.azureusnation.coreserver.chat.ChatPosition;
import com.azureusnation.coreserver.entity.Entity;
import com.azureusnation.coreserver.entity.EntityType;
import com.azureusnation.coreserver.entity.EntityUtils;
import com.azureusnation.coreserver.entity.GameMode;
import com.azureusnation.coreserver.net.PlayerConnection;
import com.azureusnation.coreserver.net.packet.ClientBoundPacket;
import com.azureusnation.coreserver.net.packet.play.client.PacketCB02ChatMessage;
import com.azureusnation.coreserver.net.packet.play.client.PacketCB08PlayerPositionAndLook;
import com.azureusnation.coreserver.net.packet.play.client.PacketCB13DestroyEntities;
import com.azureusnation.coreserver.net.state.State;
import com.azureusnation.coreserver.net.state.State00Play;
import com.azureusnation.coreserver.net.state.State02Login;
import com.azureusnation.coreserver.room.Room;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import mikera.vectorz.Vector2;
import mikera.vectorz.Vector3;

public class Player
extends Entity {
    private PlayerConnection connection;
    private final Map<String, State02Login.YggdrasilPropertyEntry> yggdrasilProperties;
    private State00Play playState;
    private Set<Vector2> loadedChunks;
    private String name;
    private GameMode gameMode;
    private int ping;
    private boolean invisible;

    public Player(int entityId, PlayerConnection connection, UUID uuid, Map<String, State02Login.YggdrasilPropertyEntry> yggdrasilProperties, Room room, Vector3 position, Vector2 rotation, String name, GameMode gameMode) {
        super(uuid, entityId, position, rotation, room);
        this.yggdrasilProperties = yggdrasilProperties;
        this.connection = connection;
        this.name = name;
        this.gameMode = gameMode;
        this.playState = (State00Play)connection.getConnectionState();
        this.setClientUpdateEvery(2);
    }

    public State00Play getPlayState() {
        return this.playState;
    }

    public void sendMessage(Chat message, ChatPosition position) {
        this.playState.sendPacket(new PacketCB02ChatMessage(message, (byte)position.getId()));
    }

    @Override
    public void setPosition(Vector3 position) {
        this.setPosition(position, true);
    }

    public void setPosition(Vector3 position, boolean send) {
        super.setPosition(position);
        if (send) {
            this.playState.sendPacket(new PacketCB08PlayerPositionAndLook(position, new Vector2(), EnumSet.of(PacketCB08PlayerPositionAndLook.RelativeCoordinates.X_ROT, PacketCB08PlayerPositionAndLook.RelativeCoordinates.Y_ROT)));
        }
    }

    @Override
    public void setRotation(Vector2 rotation) {
        this.setRotation(rotation, true);
    }

    public void setRotation(Vector2 rotation, boolean send) {
        super.setRotation(rotation);
        if (send) {
            this.playState.sendPacket(new PacketCB08PlayerPositionAndLook(new Vector3(), rotation, EnumSet.of(PacketCB08PlayerPositionAndLook.RelativeCoordinates.X, PacketCB08PlayerPositionAndLook.RelativeCoordinates.Y, PacketCB08PlayerPositionAndLook.RelativeCoordinates.Z)));
        }
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public EntityType getType() {
        return EntityType.PLAYER;
    }

    public String getName() {
        return this.name;
    }

    public GameMode getGameMode() {
        return this.gameMode;
    }

    public int getPing() {
        return this.ping;
    }

    public void setPing(int ping) {
        this.ping = ping;
    }

    public Map<String, State02Login.YggdrasilPropertyEntry> getYggdrasilProperties() {
        return this.yggdrasilProperties;
    }

    public void sendMessage(Chat message) {
        this.sendMessage(message, ChatPosition.CHAT);
    }

    public boolean isInvisible() {
        return this.invisible;
    }

    public void setInvisible(boolean invisible) {
        if (invisible && !this.invisible) {
            PacketCB13DestroyEntities packetCB13DestroyEntities = new PacketCB13DestroyEntities(this);
            this.getRoom().broadcastPacketExcept(packetCB13DestroyEntities, this);
        } else if (!invisible && this.invisible) {
            this.getRoom().broadcastPacketExcept(EntityUtils.createSpawnPacket(this), this);
        }
        this.invisible = invisible;
    }
}

