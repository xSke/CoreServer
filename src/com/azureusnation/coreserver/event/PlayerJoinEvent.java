/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.event;

import com.azureusnation.coreserver.entity.Player;
import com.azureusnation.coreserver.event.Event;

public class PlayerJoinEvent
extends Event {
    private Player player;

    public PlayerJoinEvent(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return this.player;
    }
}

