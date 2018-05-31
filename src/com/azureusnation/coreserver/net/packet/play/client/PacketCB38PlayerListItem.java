/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.net.packet.play.client;

import com.azureusnation.coreserver.chat.Chat;
import com.azureusnation.coreserver.net.PacketBuffer;
import com.azureusnation.coreserver.net.packet.ClientBoundPacket;
import com.azureusnation.coreserver.net.state.State02Login;
import io.netty.buffer.ByteBuf;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PacketCB38PlayerListItem<T extends PlayerListPlayer>
extends ClientBoundPacket {
    private Class<T> clazz;
    private List<T> players;

    public PacketCB38PlayerListItem(Class<T> clazz, List<T> players) {
        this.clazz = clazz;
        this.players = players;
    }

    @Override
    public void write(PacketBuffer input) throws IOException {
        input.writeVarInt(((PlayerListPlayer)this.players.get(0)).getId());
        input.writeVarInt(this.players.size());
        for (PlayerListPlayer player : this.players) {
            input.writeLong(player.uuid.getMostSignificantBits());
            input.writeLong(player.uuid.getLeastSignificantBits());
            if (player instanceof PlayerListPlayer.PlayerListAddPlayer) {
                PlayerListPlayer.PlayerListAddPlayer plap = (PlayerListPlayer.PlayerListAddPlayer)player;
                input.writeString(plap.name);
                input.writeVarInt(plap.properties.size());
                for (Map.Entry entry : plap.properties.entrySet()) {
                    input.writeString((String)entry.getKey());
                    input.writeString(((State02Login.YggdrasilPropertyEntry)entry.getValue()).getValue());
                    if (((State02Login.YggdrasilPropertyEntry)entry.getValue()).getSignature() != null) {
                        input.writeBoolean(true);
                        input.writeString(((State02Login.YggdrasilPropertyEntry)entry.getValue()).getSignature());
                        continue;
                    }
                    input.writeBoolean(false);
                }
                input.writeVarInt(plap.gameMode);
                input.writeVarInt(plap.ping);
                input.writeBoolean(plap.displayName != null);
                if (plap.displayName == null) continue;
                input.writeChat(plap.displayName);
                continue;
            }
            if (player instanceof PlayerListPlayer.PlayerListUpdateGameMode) {
                input.writeVarInt(((PlayerListPlayer.PlayerListUpdateGameMode)player).gameMode);
                continue;
            }
            if (player instanceof PlayerListPlayer.PlayerListUpdateLatency) {
                input.writeVarInt(((PlayerListPlayer.PlayerListUpdateLatency)player).ping);
                continue;
            }
            if (!(player instanceof PlayerListPlayer.PlayerListUpdateDisplayName)) continue;
            input.writeBoolean(((PlayerListPlayer.PlayerListUpdateDisplayName)player).displayName != null);
            input.writeChat(((PlayerListPlayer.PlayerListUpdateDisplayName)player).displayName);
        }
    }

    public static abstract class PlayerListPlayer {
        private UUID uuid;

        public PlayerListPlayer(UUID uuid) {
            this.uuid = uuid;
        }

        public abstract int getId();

        public static class PlayerListRemovePlayer
        extends PlayerListPlayer {
            public PlayerListRemovePlayer(UUID uuid) {
                super(uuid);
            }

            @Override
            public int getId() {
                return 4;
            }
        }

        public static class PlayerListUpdateDisplayName
        extends PlayerListPlayer {
            private Chat displayName;

            public PlayerListUpdateDisplayName(UUID uuid, Chat displayName) {
                super(uuid);
                this.displayName = displayName;
            }

            @Override
            public int getId() {
                return 3;
            }
        }

        public static class PlayerListUpdateLatency
        extends PlayerListPlayer {
            private int ping;

            public PlayerListUpdateLatency(UUID uuid, int ping) {
                super(uuid);
                this.ping = ping;
            }

            @Override
            public int getId() {
                return 2;
            }
        }

        public static class PlayerListUpdateGameMode
        extends PlayerListPlayer {
            private int gameMode;

            public PlayerListUpdateGameMode(UUID uuid, int gameMode) {
                super(uuid);
                this.gameMode = gameMode;
            }

            @Override
            public int getId() {
                return 1;
            }
        }

        public static class PlayerListAddPlayer
        extends PlayerListPlayer {
            private String name;
            private Map<String, State02Login.YggdrasilPropertyEntry> properties;
            private int gameMode;
            private int ping;
            private Chat displayName;

            public PlayerListAddPlayer(UUID uuid, String name, Map<String, State02Login.YggdrasilPropertyEntry> properties, int gameMode, int ping, Chat displayName) {
                super(uuid);
                this.name = name;
                this.properties = properties;
                this.gameMode = gameMode;
                this.ping = ping;
                this.displayName = displayName;
            }

            @Override
            public int getId() {
                return 0;
            }
        }

    }

}

