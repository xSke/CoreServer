/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.room;

import com.azureusnation.coreserver.chat.Chat;
import com.azureusnation.coreserver.chat.ChatBuilder;
import com.azureusnation.coreserver.chat.ChatPosition;
import com.azureusnation.coreserver.entity.Entity;
import com.azureusnation.coreserver.entity.EntityUtils;
import com.azureusnation.coreserver.entity.GameMode;
import com.azureusnation.coreserver.entity.Player;
import com.azureusnation.coreserver.event.PlayerJoinEvent;
import com.azureusnation.coreserver.minigame.Minigame;
import com.azureusnation.coreserver.net.packet.ClientBoundPacket;
import com.azureusnation.coreserver.net.packet.play.client.PacketCB13DestroyEntities;
import com.azureusnation.coreserver.net.packet.play.client.PacketCB26MapChunkBulk;
import com.azureusnation.coreserver.net.packet.play.client.PacketCB38PlayerListItem;
import com.azureusnation.coreserver.net.packet.play.client.PacketCB47PlayerListHeaderFooter;
import com.azureusnation.coreserver.net.state.State00Play;
import com.azureusnation.coreserver.net.state.State02Login;
import com.azureusnation.coreserver.schematic.Schematic;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class Room {
    private String name;
    private Minigame minigame;
    private Schematic schematic;
    private Set<Entity> entities;
    private HashMap<UUID, Player> players;
    private EventBus eventBus;
    private Chat playerListHeader;
    private Chat playerListFooter;
    private float timer;

    public Room(String name, Minigame minigame, Schematic schematic) {
        this.name = name;
        this.minigame = minigame;
        this.schematic = schematic;
        this.players = new HashMap();
        this.entities = new HashSet<Entity>();
        this.eventBus = new EventBus(new SubscriberExceptionHandler(){

            @Override
            public void handleException(Throwable exception, SubscriberExceptionContext context) {
                exception.printStackTrace();
            }
        });
        this.eventBus.register(minigame);
    }

    public void tick() {
        this.minigame.tick();
        this.timer += 0.05f;
        if (this.timer % 10.0f == 0.0f) {
            this.updatePlayerListPing();
        }
        for (Entity entity : this.entities) {
            entity.tick();
        }
    }

    public void updatePlayerListPing() {
        ArrayList<PacketCB38PlayerListItem.PlayerListPlayer.PlayerListUpdateLatency> plaps = new ArrayList<PacketCB38PlayerListItem.PlayerListPlayer.PlayerListUpdateLatency>();
        for (Player playerToSend : this.getPlayers()) {
            PacketCB38PlayerListItem.PlayerListPlayer.PlayerListUpdateLatency plap = new PacketCB38PlayerListItem.PlayerListPlayer.PlayerListUpdateLatency(playerToSend.getUuid(), playerToSend.getPing());
            plaps.add(plap);
        }
        PacketCB38PlayerListItem<PacketCB38PlayerListItem.PlayerListPlayer.PlayerListUpdateLatency> packet = new PacketCB38PlayerListItem<PacketCB38PlayerListItem.PlayerListPlayer.PlayerListUpdateLatency>(PacketCB38PlayerListItem.PlayerListPlayer.PlayerListUpdateLatency.class, plaps);
        this.broadcastPacket(packet);
    }

    public void addEntity(Entity entity) {
        this.entities.add(entity);
        if (entity instanceof Player) {
            this.broadcastPacketExcept(EntityUtils.createSpawnPacket(entity), (Player)entity);
        } else {
            this.broadcastPacket(EntityUtils.createSpawnPacket(entity));
        }
    }

    public void sendWorldDataToPlayer(Player player) {
        for (int chunkX = 0; chunkX <= this.schematic.getWidth() / 16; ++chunkX) {
            for (int chunkZ = 0; chunkZ <= this.schematic.getLength() / 16; ++chunkZ) {
                int chunkY;
                ByteBuffer buf = ByteBuffer.allocate(196864);
                buf.order(ByteOrder.LITTLE_ENDIAN);
                for (chunkY = 0; chunkY < 16; ++chunkY) {
                    for (int y = chunkY * 16; y < (chunkY + 1) * 16; ++y) {
                        for (int z = 0; z < 16; ++z) {
                            for (int x = 0; x < 16; ++x) {
                                buf.putShort((short)(this.schematic.getBlock(chunkX * 16 + x, y, chunkZ * 16 + z) << 4 | this.schematic.getBlockData(chunkX * 16 + x, y, chunkZ * 16 + z) & 15));
                            }
                        }
                    }
                }
                for (chunkY = 0; chunkY < 16; ++chunkY) {
                    byte[] light = new byte[2048];
                    Arrays.fill(light, (byte)-1);
                    buf.put(light);
                    byte[] skylight = new byte[2048];
                    Arrays.fill(skylight, (byte)-1);
                    buf.put(skylight);
                }
                byte[] biomes = new byte[256];
                Arrays.fill(biomes, (byte)0);
                buf.put(biomes);
                byte[] data = buf.array();
                PacketCB26MapChunkBulk packet = new PacketCB26MapChunkBulk(true, chunkX, chunkZ, -1, data);
                player.getPlayState().sendPacket(packet);
            }
        }
    }

    public void join(Player player) {
        this.players.put(player.getUuid(), player);
        this.sendWorldDataToPlayer(player);
        this.sendPlayerListToPlayer(player);
        this.sendEntitiesToPlayer(player);
        ArrayList<PacketCB38PlayerListItem.PlayerListPlayer.PlayerListAddPlayer> plaps = new ArrayList<PacketCB38PlayerListItem.PlayerListPlayer.PlayerListAddPlayer>();
        PacketCB38PlayerListItem.PlayerListPlayer.PlayerListAddPlayer plap = new PacketCB38PlayerListItem.PlayerListPlayer.PlayerListAddPlayer(player.getUuid(), player.getName(), player.getYggdrasilProperties(), player.getGameMode().getId(), player.getPing(), null);
        plaps.add(plap);
        PacketCB38PlayerListItem<PacketCB38PlayerListItem.PlayerListPlayer.PlayerListAddPlayer> packet = new PacketCB38PlayerListItem<PacketCB38PlayerListItem.PlayerListPlayer.PlayerListAddPlayer>(PacketCB38PlayerListItem.PlayerListPlayer.PlayerListAddPlayer.class, plaps);
        this.broadcastPacketExcept(packet, player);
        this.addEntity(player);
        new PlayerJoinEvent(player).send(this);
    }

    public void sendEntitiesToPlayer(Player player) {
        for (Entity entity : this.entities) {
            if (entity == player) continue;
            player.getPlayState().sendPacket(EntityUtils.createSpawnPacket(entity));
        }
    }

    public void remove(Player player) {
        this.players.remove(player.getUuid());
        this.entities.remove(player);
        ArrayList<PacketCB38PlayerListItem.PlayerListPlayer.PlayerListRemovePlayer> plaps = new ArrayList<PacketCB38PlayerListItem.PlayerListPlayer.PlayerListRemovePlayer>();
        PacketCB38PlayerListItem.PlayerListPlayer.PlayerListRemovePlayer plrp = new PacketCB38PlayerListItem.PlayerListPlayer.PlayerListRemovePlayer(player.getUuid());
        plaps.add(plrp);
        PacketCB13DestroyEntities packetCB13DestroyEntities = new PacketCB13DestroyEntities(player);
        this.broadcastPacketExcept(packetCB13DestroyEntities, player);
        PacketCB38PlayerListItem<PacketCB38PlayerListItem.PlayerListPlayer.PlayerListRemovePlayer> packet = new PacketCB38PlayerListItem<PacketCB38PlayerListItem.PlayerListPlayer.PlayerListRemovePlayer>(PacketCB38PlayerListItem.PlayerListPlayer.PlayerListRemovePlayer.class, plaps);
        this.broadcastPacket(packet);
    }

    public void sendPlayerListToPlayer(Player player) {
        PacketCB47PlayerListHeaderFooter packetHeaderFooter = new PacketCB47PlayerListHeaderFooter(this.playerListHeader == null ? Chat.builder().text("").build() : this.playerListHeader, this.playerListFooter == null ? Chat.builder().text("").build() : this.playerListFooter);
        player.getPlayState().sendPacket(packetHeaderFooter);
        ArrayList<PacketCB38PlayerListItem.PlayerListPlayer.PlayerListAddPlayer> plaps = new ArrayList<PacketCB38PlayerListItem.PlayerListPlayer.PlayerListAddPlayer>();
        for (Player playerToSend : this.getPlayers()) {
            PacketCB38PlayerListItem.PlayerListPlayer.PlayerListAddPlayer plap = new PacketCB38PlayerListItem.PlayerListPlayer.PlayerListAddPlayer(playerToSend.getUuid(), playerToSend.getName(), playerToSend.getYggdrasilProperties(), playerToSend.getGameMode().getId(), playerToSend.getPing(), null);
            plaps.add(plap);
        }
        PacketCB38PlayerListItem<PacketCB38PlayerListItem.PlayerListPlayer.PlayerListAddPlayer> packetPlap = new PacketCB38PlayerListItem<PacketCB38PlayerListItem.PlayerListPlayer.PlayerListAddPlayer>(PacketCB38PlayerListItem.PlayerListPlayer.PlayerListAddPlayer.class, plaps);
        player.getPlayState().sendPacket(packetPlap);
    }

    public void setPlayerListHeader(Chat playerListHeader) {
        this.playerListHeader = playerListHeader;
        this.updatePlayerListHeaderFooter();
    }

    public void updatePlayerListHeaderFooter() {
        PacketCB47PlayerListHeaderFooter packet = new PacketCB47PlayerListHeaderFooter(this.playerListHeader == null ? Chat.builder().text("").build() : this.playerListHeader, this.playerListFooter == null ? Chat.builder().text("").build() : this.playerListFooter);
        this.broadcastPacket(packet);
    }

    public void setPlayerListFooter(Chat playerListFooter) {
        this.playerListFooter = playerListFooter;
        this.updatePlayerListHeaderFooter();
    }

    public Minigame getMinigame() {
        return this.minigame;
    }

    public String getName() {
        return this.name;
    }

    public EventBus getEventBus() {
        return this.eventBus;
    }

    public Optional<Player> getPlayer(UUID uuid) {
        return Optional.ofNullable(this.players.get(uuid));
    }

    public Collection<Player> getPlayers() {
        return this.players.values();
    }

    public void broadcastPacketExcept(ClientBoundPacket packet, Player except) {
        for (Player player : this.players.values()) {
            if (player == except) continue;
            player.getPlayState().sendPacket(packet);
        }
    }

    public void broadcastPacket(ClientBoundPacket packet) {
        for (Player player : this.players.values()) {
            player.getPlayState().sendPacket(packet);
        }
    }

    public void broadcastMessage(Chat chat, ChatPosition pos) {
        for (Player player : this.players.values()) {
            player.sendMessage(chat, pos);
        }
    }

    public Schematic getSchematic() {
        return this.schematic;
    }

}

