/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.net.state;

import com.azureusnation.coreserver.Server;
import com.azureusnation.coreserver.block.BlockFace;
import com.azureusnation.coreserver.entity.GameMode;
import com.azureusnation.coreserver.entity.Player;
import com.azureusnation.coreserver.event.PlayerDigBlockEvent;
import com.azureusnation.coreserver.event.PlayerLookEvent;
import com.azureusnation.coreserver.event.PlayerMoveEvent;
import com.azureusnation.coreserver.event.PlayerPlaceBlockEvent;
import com.azureusnation.coreserver.event.PlayerSendMessageEvent;
import com.azureusnation.coreserver.item.ItemStack;
import com.azureusnation.coreserver.marker.Marker;
import com.azureusnation.coreserver.net.PlayerConnection;
import com.azureusnation.coreserver.net.packet.ClientBoundPacket;
import com.azureusnation.coreserver.net.packet.ServerBoundPacket;
import com.azureusnation.coreserver.net.packet.play.client.PacketCB00KeepAlive;
import com.azureusnation.coreserver.net.packet.play.client.PacketCB01JoinGame;
import com.azureusnation.coreserver.net.packet.play.client.PacketCB02ChatMessage;
import com.azureusnation.coreserver.net.packet.play.client.PacketCB03TimeUpdate;
import com.azureusnation.coreserver.net.packet.play.client.PacketCB05SpawnPosition;
import com.azureusnation.coreserver.net.packet.play.client.PacketCB06UpdateHealth;
import com.azureusnation.coreserver.net.packet.play.client.PacketCB07Respawn;
import com.azureusnation.coreserver.net.packet.play.client.PacketCB08PlayerPositionAndLook;
import com.azureusnation.coreserver.net.packet.play.client.PacketCB09HeldItemChange;
import com.azureusnation.coreserver.net.packet.play.client.PacketCB0AUseBed;
import com.azureusnation.coreserver.net.packet.play.client.PacketCB0BAnimation;
import com.azureusnation.coreserver.net.packet.play.client.PacketCB0CSpawnPlayer;
import com.azureusnation.coreserver.net.packet.play.client.PacketCB0FSpawnMob;
import com.azureusnation.coreserver.net.packet.play.client.PacketCB13DestroyEntities;
import com.azureusnation.coreserver.net.packet.play.client.PacketCB15EntityRelativeMove;
import com.azureusnation.coreserver.net.packet.play.client.PacketCB16EntityLook;
import com.azureusnation.coreserver.net.packet.play.client.PacketCB17EntityLookAndRelativeMove;
import com.azureusnation.coreserver.net.packet.play.client.PacketCB18EntityTeleport;
import com.azureusnation.coreserver.net.packet.play.client.PacketCB19EntityHeadLook;
import com.azureusnation.coreserver.net.packet.play.client.PacketCB26MapChunkBulk;
import com.azureusnation.coreserver.net.packet.play.client.PacketCB38PlayerListItem;
import com.azureusnation.coreserver.net.packet.play.client.PacketCB40Disconnect;
import com.azureusnation.coreserver.net.packet.play.client.PacketCB47PlayerListHeaderFooter;
import com.azureusnation.coreserver.net.packet.play.server.PacketSB00KeepAlive;
import com.azureusnation.coreserver.net.packet.play.server.PacketSB01ChatMessage;
import com.azureusnation.coreserver.net.packet.play.server.PacketSB02UseEntity;
import com.azureusnation.coreserver.net.packet.play.server.PacketSB03Player;
import com.azureusnation.coreserver.net.packet.play.server.PacketSB04PlayerPosition;
import com.azureusnation.coreserver.net.packet.play.server.PacketSB05PlayerLook;
import com.azureusnation.coreserver.net.packet.play.server.PacketSB06PlayerPositionAndLook;
import com.azureusnation.coreserver.net.packet.play.server.PacketSB07PlayerDigging;
import com.azureusnation.coreserver.net.packet.play.server.PacketSB08PlayerBlockPlacement;
import com.azureusnation.coreserver.net.packet.play.server.PacketSB09HeldItemChange;
import com.azureusnation.coreserver.net.packet.play.server.PacketSB0AAnimation;
import com.azureusnation.coreserver.net.packet.play.server.PacketSB0BEntityAction;
import com.azureusnation.coreserver.net.packet.play.server.PacketSB0CSteerVehicle;
import com.azureusnation.coreserver.net.packet.play.server.PacketSB0DCloseWindow;
import com.azureusnation.coreserver.net.packet.play.server.PacketSB0FConfirmTransaction;
import com.azureusnation.coreserver.net.packet.play.server.PacketSB11EnchantItem;
import com.azureusnation.coreserver.net.packet.play.server.PacketSB12UpdateSign;
import com.azureusnation.coreserver.net.packet.play.server.PacketSB13PlayerAbilities;
import com.azureusnation.coreserver.net.packet.play.server.PacketSB14TabComplete;
import com.azureusnation.coreserver.net.packet.play.server.PacketSB15ClientSettings;
import com.azureusnation.coreserver.net.packet.play.server.PacketSB16ClientStatus;
import com.azureusnation.coreserver.net.packet.play.server.PacketSB17PluginMessage;
import com.azureusnation.coreserver.net.packet.play.server.PacketSB18Spectate;
import com.azureusnation.coreserver.net.packet.play.server.PacketSB19ResourcePackStatus;
import com.azureusnation.coreserver.net.state.State;
import com.azureusnation.coreserver.net.state.State02Login;
import com.azureusnation.coreserver.room.Room;
import com.azureusnation.coreserver.schematic.Schematic;
import com.azureusnation.coreserver.util.StringUtil;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import io.netty.channel.socket.SocketChannel;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import mikera.vectorz.Vector2;
import mikera.vectorz.Vector3;

public class State00Play
extends State {
    private final String username;
    private final Map<String, State02Login.YggdrasilPropertyEntry> yggdrasilProperties;
    private Vector3 spawnPosition = null;
    private int lastKeepAliveId;
    private long lastKeepAliveSentNanoTime;
    private long lastKeepAliveReceivedNanoTime = System.nanoTime();
    private int keepAliveTimer;
    private String uuid;
    private boolean isFirstTick = true;
    private boolean isFirstPosition = true;
    private Player player;
    private Room room;
    private int entityId;

    public State00Play(PlayerConnection pc, String uuid, String username, Map<String, State02Login.YggdrasilPropertyEntry> yggdrasilProperties) {
        super(pc, (BiMap<Integer, Class<? extends ServerBoundPacket>>)((Object)ImmutableBiMap.builder().put((Object)10, PacketSB0AAnimation.class).put((Object)11, PacketSB0BEntityAction.class).put((Object)12, PacketSB0CSteerVehicle.class).put((Object)13, PacketSB0DCloseWindow.class).put((Object)15, PacketSB0FConfirmTransaction.class).put((Object)0, PacketSB00KeepAlive.class).put((Object)1, PacketSB01ChatMessage.class).put((Object)2, PacketSB02UseEntity.class).put((Object)3, PacketSB03Player.class).put((Object)4, PacketSB04PlayerPosition.class).put((Object)5, PacketSB05PlayerLook.class).put((Object)6, PacketSB06PlayerPositionAndLook.class).put((Object)7, PacketSB07PlayerDigging.class).put((Object)8, PacketSB08PlayerBlockPlacement.class).put((Object)9, PacketSB09HeldItemChange.class).put((Object)17, PacketSB11EnchantItem.class).put((Object)18, PacketSB12UpdateSign.class).put((Object)19, PacketSB13PlayerAbilities.class).put((Object)20, PacketSB14TabComplete.class).put((Object)21, PacketSB15ClientSettings.class).put((Object)22, PacketSB16ClientStatus.class).put((Object)23, PacketSB17PluginMessage.class).put((Object)24, PacketSB18Spectate.class).put((Object)25, PacketSB19ResourcePackStatus.class).build()), (BiMap<Integer, Class<? extends ClientBoundPacket>>)((Object)ImmutableBiMap.builder().put((Object)10, PacketCB0AUseBed.class).put((Object)11, PacketCB0BAnimation.class).put((Object)12, PacketCB0CSpawnPlayer.class).put((Object)15, PacketCB0FSpawnMob.class).put((Object)0, PacketCB00KeepAlive.class).put((Object)1, PacketCB01JoinGame.class).put((Object)2, PacketCB02ChatMessage.class).put((Object)3, PacketCB03TimeUpdate.class).put((Object)5, PacketCB05SpawnPosition.class).put((Object)6, PacketCB06UpdateHealth.class).put((Object)7, PacketCB07Respawn.class).put((Object)8, PacketCB08PlayerPositionAndLook.class).put((Object)9, PacketCB09HeldItemChange.class).put((Object)19, PacketCB13DestroyEntities.class).put((Object)21, PacketCB15EntityRelativeMove.class).put((Object)22, PacketCB16EntityLook.class).put((Object)23, PacketCB17EntityLookAndRelativeMove.class).put((Object)24, PacketCB18EntityTeleport.class).put((Object)25, PacketCB19EntityHeadLook.class).put((Object)38, PacketCB26MapChunkBulk.class).put((Object)56, PacketCB38PlayerListItem.class).put((Object)64, PacketCB40Disconnect.class).put((Object)71, PacketCB47PlayerListHeaderFooter.class).build()));
        this.uuid = uuid;
        this.username = username;
        this.yggdrasilProperties = yggdrasilProperties;
        Server.instance.getLoggedInPlayers().add(UUID.fromString(uuid));
        this.room = Server.instance.getRoomPortMap().get(this.getPlayerConnection().getSocket().localAddress().getPort());
        Set<Marker> spawnMarkers = this.room.getSchematic().getMarkersWithKey("spawn");
        this.spawnPosition = spawnMarkers.isEmpty() ? new Vector3() : StringUtil.stringToVector(spawnMarkers.iterator().next().getProperties().get("spawn"));
    }

    @Override
    public void disconnect() {
        if (this.player != null) {
            this.player.getRoom().remove(this.player);
        }
        Server.instance.getLoggedInPlayers().remove(UUID.fromString(this.uuid));
    }

    @Override
    public void receivePacket(ServerBoundPacket packet) {
        if (packet instanceof PacketSB00KeepAlive) {
            if (this.lastKeepAliveId == ((PacketSB00KeepAlive)packet).getId()) {
                this.lastKeepAliveReceivedNanoTime = System.nanoTime();
                int pingMillis = (int)((this.lastKeepAliveReceivedNanoTime - this.lastKeepAliveSentNanoTime) / 1000000L);
                if (this.player != null) {
                    this.player.setPing(pingMillis);
                }
            }
        } else if (packet instanceof PacketSB06PlayerPositionAndLook && this.isFirstPosition) {
            this.isFirstPosition = false;
            this.player = new Player(this.entityId, this.getPlayerConnection(), UUID.fromString(this.uuid), this.yggdrasilProperties, this.room, this.spawnPosition, new Vector2(), this.username, GameMode.CREATIVE);
            this.room.join(this.player);
        }
        if (this.player != null) {
            if (packet instanceof PacketSB07PlayerDigging) {
                PacketSB07PlayerDigging packetDigging = (PacketSB07PlayerDigging)packet;
                new PlayerDigBlockEvent(this.player, PlayerDigBlockEvent.Status.values()[packetDigging.getStatus()], packetDigging.getPosition(), BlockFace.values()[packetDigging.getFace()]).send(this.player.getRoom());
            } else if (packet instanceof PacketSB08PlayerBlockPlacement) {
                PacketSB08PlayerBlockPlacement packetBlockPlace = (PacketSB08PlayerBlockPlacement)packet;
                new PlayerPlaceBlockEvent(packetBlockPlace.getItemStack(), packetBlockPlace.getPosition(), BlockFace.values()[packetBlockPlace.getFace()], packetBlockPlace.getCursorPosition()).send(this.player.getRoom());
            } else if (packet instanceof PacketSB0AAnimation) {
                this.player.getRoom().broadcastPacketExcept(new PacketCB0BAnimation(this.player.getId(), 0), this.player);
            } else if (packet instanceof PacketSB04PlayerPosition || packet instanceof PacketSB06PlayerPositionAndLook) {
                Vector3 newPosition;
                boolean hacking;
                Vector3 oldPosition = this.player.getPosition().clone();
                if (packet instanceof PacketSB06PlayerPositionAndLook) {
                    PacketSB06PlayerPositionAndLook pal = (PacketSB06PlayerPositionAndLook)packet;
                    newPosition = new Vector3(pal.getX(), pal.getY(), pal.getZ());
                } else {
                    PacketSB04PlayerPosition p = (PacketSB04PlayerPosition)packet;
                    newPosition = new Vector3(p.getX(), p.getY(), p.getZ());
                }
                float distanceSq = (float)oldPosition.distanceSquared(newPosition);
                boolean bl = hacking = distanceSq > 5.0f;
                if (hacking) {
                    System.out.println(this.player.getName() + " moved too quickly (hacking?)");
                }
                if (hacking || new PlayerMoveEvent(this.player, oldPosition, newPosition).send(this.player.getRoom())) {
                    this.player.setPosition(oldPosition);
                } else {
                    this.player.setPosition(newPosition, false);
                }
            }
            if (packet instanceof PacketSB05PlayerLook || packet instanceof PacketSB06PlayerPositionAndLook) {
                Vector2 newRotation;
                Vector2 oldRotation = this.player.getRotation().clone();
                if (packet instanceof PacketSB06PlayerPositionAndLook) {
                    PacketSB06PlayerPositionAndLook pal = (PacketSB06PlayerPositionAndLook)packet;
                    newRotation = new Vector2(pal.getYaw(), pal.getPitch());
                } else {
                    PacketSB05PlayerLook l = (PacketSB05PlayerLook)packet;
                    newRotation = new Vector2(l.getYaw(), l.getPitch());
                }
                if (new PlayerLookEvent(this.player, oldRotation, newRotation).send(this.player.getRoom())) {
                    this.player.setRotation(oldRotation);
                } else {
                    this.player.setRotation(newRotation, false);
                }
            } else if (packet instanceof PacketSB01ChatMessage) {
                new PlayerSendMessageEvent(this.player, ((PacketSB01ChatMessage)packet).getMessage()).send(this.player.getRoom());
            }
        }
    }

    @Override
    public void tick() {
        if (System.nanoTime() - this.lastKeepAliveReceivedNanoTime > 30000000000L) {
            this.sendPacket(new PacketCB40Disconnect("\"Timed out\""));
        }
        ++this.keepAliveTimer;
        if (this.keepAliveTimer % 40 == 0) {
            this.lastKeepAliveId = new Random().nextInt();
            this.lastKeepAliveSentNanoTime = System.nanoTime();
            this.sendPacket(new PacketCB00KeepAlive(this.lastKeepAliveId));
        }
        if (this.isFirstTick) {
            this.isFirstTick = false;
            this.entityId = com.azureusnation.coreserver.entity.Entity.nextId++;
            PacketCB01JoinGame joinGame = new PacketCB01JoinGame(this.entityId, GameMode.CREATIVE.getId(), 0, 2, 10, "flat", false);
            this.sendPacket(joinGame);
            PacketCB05SpawnPosition spawnPositionPacket = new PacketCB05SpawnPosition(this.spawnPosition);
            this.sendPacket(spawnPositionPacket);
            PacketCB08PlayerPositionAndLook playerPositionAndLook = new PacketCB08PlayerPositionAndLook(this.spawnPosition, new Vector2(), EnumSet.noneOf(PacketCB08PlayerPositionAndLook.RelativeCoordinates.class));
            this.sendPacket(playerPositionAndLook);
        }
        this.sendPacket(new PacketCB03TimeUpdate(0L, 6000L));
    }
}

