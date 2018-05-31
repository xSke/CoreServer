/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.net.state;

import com.azureusnation.coreserver.Server;
import com.azureusnation.coreserver.chat.Chat;
import com.azureusnation.coreserver.chat.ChatBuilder;
import com.azureusnation.coreserver.net.PlayerConnection;
import com.azureusnation.coreserver.net.packet.ClientBoundPacket;
import com.azureusnation.coreserver.net.packet.ServerBoundPacket;
import com.azureusnation.coreserver.net.packet.login.client.PacketCB00Disconnect;
import com.azureusnation.coreserver.net.packet.login.client.PacketCB01EncryptionRequest;
import com.azureusnation.coreserver.net.packet.login.client.PacketCB02LoginSuccess;
import com.azureusnation.coreserver.net.packet.login.client.PacketCB03SetCompression;
import com.azureusnation.coreserver.net.packet.login.server.PacketSB00LoginStart;
import com.azureusnation.coreserver.net.packet.login.server.PacketSB01EncryptionResponse;
import com.azureusnation.coreserver.net.pipeline.CompressionHandler;
import com.azureusnation.coreserver.net.pipeline.EncryptionHandler;
import com.azureusnation.coreserver.net.state.State;
import com.azureusnation.coreserver.net.state.State00Play;
import com.azureusnation.coreserver.util.ServerConfig;
import com.google.common.base.Charsets;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.BaseEncoding;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.async.Callback;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequest;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Future;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class State02Login
extends State {
    private LoginState state = LoginState.WAITING_FOR_LOGIN_START;
    private String username;
    private byte[] verifyToken;
    private Future<HttpResponse<String>> loginRequest;

    public State02Login(PlayerConnection pc) {
        super(pc, (BiMap<Integer, Class<? extends ServerBoundPacket>>)((Object)ImmutableBiMap.builder().put((Object)0, PacketSB00LoginStart.class).put((Object)1, PacketSB01EncryptionResponse.class).build()), (BiMap<Integer, Class<? extends ClientBoundPacket>>)((Object)ImmutableBiMap.builder().put((Object)0, PacketCB00Disconnect.class).put((Object)1, PacketCB01EncryptionRequest.class).put((Object)2, PacketCB02LoginSuccess.class).put((Object)3, PacketCB03SetCompression.class).build()));
    }

    private static byte[] twosCompliment(byte[] p) {
        boolean carry = true;
        for (int i = p.length - 1; i >= 0; --i) {
            p[i] = ~ p[i];
            if (!carry) continue;
            carry = p[i] == 255;
            byte[] arrby = p;
            int n = i;
            arrby[n] = (byte)(arrby[n] + 1);
        }
        return p;
    }

    @Override
    public void receivePacket(ServerBoundPacket packet) {
        if (packet instanceof PacketSB00LoginStart) {
            if (Server.instance.getServerConfig().isOfflineMode()) {
                UUID fakePlayer = UUID.nameUUIDFromBytes(("OfflinePlayer:" + ((PacketSB00LoginStart)packet).getName()).getBytes(Charsets.UTF_8));
                this.finishLogin(((PacketSB00LoginStart)packet).getName(), fakePlayer.toString(), new HashMap<String, YggdrasilPropertyEntry>());
            } else if (this.state == LoginState.WAITING_FOR_LOGIN_START) {
                this.username = ((PacketSB00LoginStart)packet).getName();
                KeyPair kp = Server.instance.getKeyPair();
                this.verifyToken = new byte[4];
                new SecureRandom().nextBytes(this.verifyToken);
                PacketCB01EncryptionRequest packetToSend = new PacketCB01EncryptionRequest("", kp.getPublic().getEncoded().length, kp.getPublic().getEncoded(), this.verifyToken.length, this.verifyToken);
                this.sendPacket(packetToSend);
                this.state = LoginState.WAITING_FOR_ENCRYPTION_RESPONSE;
            }
        } else if (packet instanceof PacketSB01EncryptionResponse && this.state == LoginState.WAITING_FOR_ENCRYPTION_RESPONSE) {
            KeyPair kp = Server.instance.getKeyPair();
            PacketSB01EncryptionResponse packetIn = (PacketSB01EncryptionResponse)packet;
            try {
                Cipher cipher = Cipher.getInstance("RSA");
                cipher.init(2, kp.getPrivate());
                byte[] decryptedVerifyToken = cipher.doFinal(packetIn.getVerifyToken());
                if (Arrays.equals(decryptedVerifyToken, this.verifyToken)) {
                    boolean negative;
                    byte[] decryptedSharedSecret = cipher.doFinal(packetIn.getSharedSecret());
                    MessageDigest md = MessageDigest.getInstance("SHA-1");
                    md.update(decryptedSharedSecret);
                    md.update(kp.getPublic().getEncoded());
                    byte[] hash = md.digest();
                    boolean bl = negative = (hash[0] & 128) == 128;
                    if (negative) {
                        hash = State02Login.twosCompliment(hash);
                    }
                    String serverID = BaseEncoding.base16().encode(hash).toLowerCase();
                    while (serverID.startsWith("0")) {
                        serverID = serverID.substring(1);
                    }
                    if (negative) {
                        serverID = "-" + serverID;
                    }
                    this.getPlayerConnection().getSocket().pipeline().replace("encryption", "encryption", (ChannelHandler)new EncryptionHandler(new SecretKeySpec(decryptedSharedSecret, "AES")));
                    this.loginRequest = Unirest.get("https://sessionserver.mojang.com/session/minecraft/hasJoined").queryString("username", this.username).queryString("serverId", serverID).asStringAsync(new Callback<String>(){

                        @Override
                        public void completed(HttpResponse<String> response) {
                            try {
                                if (State02Login.this.state == LoginState.LOGGING_IN_VIA_MOJANG) {
                                    if (response.getStatus() == 200) {
                                        JsonObject json = new Gson().fromJson(response.getBody(), JsonObject.class);
                                        String username = json.get("name").getAsString();
                                        String uuid = json.get("id").getAsString();
                                        JsonArray propertiesArray = json.getAsJsonArray("properties");
                                        if (propertiesArray == null) {
                                            propertiesArray = new JsonArray();
                                        }
                                        HashMap<String, YggdrasilPropertyEntry> stringMap = new HashMap<String, YggdrasilPropertyEntry>();
                                        for (JsonElement entry : propertiesArray) {
                                            stringMap.put(entry.getAsJsonObject().get("name").getAsString(), new YggdrasilPropertyEntry(entry.getAsJsonObject().get("value").getAsString(), entry.getAsJsonObject().has("signature") ? entry.getAsJsonObject().get("signature").getAsString() : null));
                                        }
                                        State02Login.this.finishLogin(username, uuid, stringMap);
                                    } else {
                                        State02Login.this.sendPacket(new PacketCB00Disconnect("\"Error authenticating with Mojang\""));
                                    }
                                }
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void failed(UnirestException e) {
                            State02Login.this.sendPacket(new PacketCB00Disconnect("Error authenticating with Mojang"));
                        }

                        @Override
                        public void cancelled() {
                        }
                    });
                    this.state = LoginState.LOGGING_IN_VIA_MOJANG;
                } else {
                    this.sendPacket(new PacketCB00Disconnect("Decrypted client-provided verify token is not equal to original verify token"));
                }
            }
            catch (InvalidKeyException | NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void disconnect() {
        if (this.loginRequest != null) {
            this.loginRequest.cancel(true);
        }
    }

    private void finishLogin(String username, String uuid, Map<String, YggdrasilPropertyEntry> properties) {
        uuid = uuid.substring(0, 8) + "-" + uuid.substring(8, 12) + "-" + uuid.substring(12, 16) + "-" + uuid.substring(16, 20) + "-" + uuid.substring(20, 32);
        if (Server.instance.getLoggedInPlayers().contains(UUID.fromString(uuid))) {
            this.sendPacket(new PacketCB00Disconnect(Chat.builder().text("You are already logged in").build()));
        }
        this.sendPacket(new PacketCB03SetCompression(300));
        this.getPlayerConnection().getSocket().pipeline().replace("compression", "compression", (ChannelHandler)new CompressionHandler());
        this.sendPacket(new PacketCB02LoginSuccess(uuid, username));
        System.out.println("Player " + username + " (" + uuid + ") logged in");
        this.getPlayerConnection().setConnectionState(new State00Play(this.getPlayerConnection(), uuid, username, properties));
    }

    public static class YggdrasilPropertyEntry {
        private String value;
        private String signature;

        public YggdrasilPropertyEntry(String value, String signature) {
            this.value = value;
            this.signature = signature;
        }

        public String getValue() {
            return this.value;
        }

        public String getSignature() {
            return this.signature;
        }
    }

    public static enum LoginState {
        WAITING_FOR_LOGIN_START,
        WAITING_FOR_ENCRYPTION_RESPONSE,
        LOGGING_IN_VIA_MOJANG;
        

        private LoginState() {
        }
    }

}

