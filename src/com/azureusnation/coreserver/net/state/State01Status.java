/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.net.state;

import com.azureusnation.coreserver.Server;
import com.azureusnation.coreserver.net.PlayerConnection;
import com.azureusnation.coreserver.net.packet.ClientBoundPacket;
import com.azureusnation.coreserver.net.packet.ServerBoundPacket;
import com.azureusnation.coreserver.net.packet.status.client.PacketCB00StatusResponse;
import com.azureusnation.coreserver.net.packet.status.client.PacketCB01Pong;
import com.azureusnation.coreserver.net.packet.status.server.PacketSB00StatusRequest;
import com.azureusnation.coreserver.net.packet.status.server.PacketSB01Ping;
import com.azureusnation.coreserver.net.state.State;
import com.azureusnation.coreserver.util.ServerConfig;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import javax.imageio.ImageIO;

public class State01Status
extends State {
    public State01Status(PlayerConnection pc) {
        super(pc, (BiMap<Integer, Class<? extends ServerBoundPacket>>)((Object)ImmutableBiMap.builder().put((Object)0, PacketSB00StatusRequest.class).put((Object)1, PacketSB01Ping.class).build()), (BiMap<Integer, Class<? extends ClientBoundPacket>>)((Object)ImmutableBiMap.builder().put((Object)0, PacketCB00StatusResponse.class).put((Object)1, PacketCB01Pong.class).build()));
    }

    @Override
    public void receivePacket(ServerBoundPacket packet) {
        if (packet instanceof PacketSB00StatusRequest) {
            JsonObject root = new JsonObject();
            JsonObject version = new JsonObject();
            version.add("name", new JsonPrimitive("1.8"));
            version.add("protocol", new JsonPrimitive(47));
            root.add("version", version);
            JsonObject players = new JsonObject();
            players.add("max", new JsonPrimitive(Server.instance.getServerConfig().getMaxPlayers()));
            players.add("online", new JsonPrimitive(0));
            root.add("players", players);
            JsonObject description = new JsonObject();
            description.add("text", new JsonPrimitive(Server.instance.getServerConfig().getMotd()));
            root.add("description", description);
            File favicon = new File(Server.instance.getServerConfig().getFaviconLocation());
            if (favicon.exists()) {
                try {
                    BufferedImage bufferedImage = ImageIO.read(favicon);
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    ImageIO.write((RenderedImage)bufferedImage, "png", byteArrayOutputStream);
                    byteArrayOutputStream.flush();
                    byte[] imageInByte = byteArrayOutputStream.toByteArray();
                    String iconBase64 = Base64.getEncoder().encodeToString(imageInByte);
                    byteArrayOutputStream.close();
                    root.add("favicon", new JsonPrimitive("data:image/png;base64," + iconBase64));
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            String jsonString = new Gson().toJson(root);
            this.sendPacket(new PacketCB00StatusResponse(jsonString));
        } else if (packet instanceof PacketSB01Ping) {
            this.sendPacket(new PacketCB01Pong(((PacketSB01Ping)packet).getTime()));
        }
    }
}

