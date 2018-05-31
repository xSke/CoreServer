/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.event;

import com.azureusnation.coreserver.entity.Player;
import com.azureusnation.coreserver.event.Event;

public class PlayerSendMessageEvent
extends Event {
    private Player player;
    private String message;

    public PlayerSendMessageEvent(Player player, String message) {
        this.player = player;
        this.message = message;
    }

    public Player getPlayer() {
        return this.player;
    }

    public String getMessage() {
        return this.message;
    }
}

