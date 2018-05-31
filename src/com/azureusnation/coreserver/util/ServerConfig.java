/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.util;

import com.azureusnation.coreserver.room.RoomDef;
import java.util.HashMap;
import java.util.Map;

public class ServerConfig {
    private String faviconLocation = "./favicon.png";
    private int maxPlayers = 5000;
    private String motd = "\u00a71G\u00a74o\u00a7eo\u00a71g\u00a72l\u00a74e";
    private boolean offlineMode = false;
    private Map<String, RoomDef> rooms = new HashMap<String, RoomDef>();

    public String getMotd() {
        return this.motd;
    }

    public String getFaviconLocation() {
        return this.faviconLocation;
    }

    public int getMaxPlayers() {
        return this.maxPlayers;
    }

    public boolean isOfflineMode() {
        return this.offlineMode;
    }

    public Map<String, RoomDef> getRooms() {
        return this.rooms;
    }
}

