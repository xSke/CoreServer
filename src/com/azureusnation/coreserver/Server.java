/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver;

import com.azureusnation.coreserver.minigame.Hub;
import com.azureusnation.coreserver.minigame.MarkerSetup;
import com.azureusnation.coreserver.minigame.Minigame;
import com.azureusnation.coreserver.minigame.MobHunt;
import com.azureusnation.coreserver.net.NetworkManager;
import com.azureusnation.coreserver.room.Room;
import com.azureusnation.coreserver.room.RoomDef;
import com.azureusnation.coreserver.schematic.Schematic;
import com.azureusnation.coreserver.schematic.SchematicLoader;
import com.azureusnation.coreserver.util.ServerConfig;
import com.google.common.io.PatternFilenameFilter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

public class Server {
    public static Server instance = null;
    private ServerConfig serverConfig;
    private KeyPair keyPair;
    private Map<String, Schematic> schematics;
    private Map<String, Supplier<Minigame>> minigames;
    private Map<String, Room> rooms;
    private Map<Integer, Room> roomPortMap;
    private Set<UUID> loggedInPlayers = new HashSet<UUID>();
    private boolean isServerRunning = true;

    public Server() {
        instance = this;
    }

    public ServerConfig getServerConfig() {
        return this.serverConfig;
    }

    public Set<UUID> getLoggedInPlayers() {
        return this.loggedInPlayers;
    }

    public void start() {
        this.loadConfig();
        try {
            System.out.println("Generating key pair...");
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(1024);
            this.keyPair = kpg.generateKeyPair();
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        this.schematics = new HashMap<String, Schematic>();
        for (File schematic : new File("schematics/").listFiles(new PatternFilenameFilter(".*\\.schematic"))) {
            Schematic schem = SchematicLoader.load(schematic);
            this.schematics.put(schematic.getName().split("\\.")[0], schem);
        }
        this.rooms = new HashMap<String, Room>();
        this.roomPortMap = new HashMap<Integer, Room>();
        this.minigames = new HashMap<String, Supplier<Minigame>>();
        this.minigames.put("Hub", Hub::new);
        this.minigames.put("MarkerSetup", MarkerSetup::new);
        this.minigames.put("MobHunt", MobHunt::new);
        NetworkManager.getInstance().init();
        for (Map.Entry roomDef : this.serverConfig.getRooms().entrySet()) {
            Room room = new Room((String)roomDef.getKey(), this.minigames.get(((RoomDef)roomDef.getValue()).getMinigame()).get(), this.schematics.get(((RoomDef)roomDef.getValue()).getSchematic()));
            room.getMinigame().setRoom(room);
            this.rooms.put((String)roomDef.getKey(), room);
            this.roomPortMap.put(((RoomDef)roomDef.getValue()).getPort(), room);
            NetworkManager.getInstance().listen(((RoomDef)roomDef.getValue()).getPort());
        }
        while (this.isServerRunning) {
            long time = System.nanoTime();
            try {
                this.tick();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(Math.max(0L, 50L - (System.nanoTime() - time) / 1000000L));
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        this.shutdown();
    }

    private void shutdown() {
        System.out.println("Shutting down...");
        NetworkManager.getInstance().shutdown();
    }

    private void tick() {
        NetworkManager.getInstance().tick();
        for (Room room : this.rooms.values()) {
            room.tick();
        }
    }

    public KeyPair getKeyPair() {
        return this.keyPair;
    }

    private void loadConfig() {
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            if (new File("./config.json").exists()) {
                BufferedReader bufferedReader = new BufferedReader(new FileReader("./config.json"));
                this.serverConfig = gson.fromJson((Reader)bufferedReader, ServerConfig.class);
            } else {
                this.serverConfig = new ServerConfig();
            }
            FileWriter fileWriter = new FileWriter("./config.json");
            gson.toJson((Object)this.serverConfig, (Appendable)fileWriter);
            fileWriter.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<Integer, Room> getRoomPortMap() {
        return this.roomPortMap;
    }
}

