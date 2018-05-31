/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.event;

import com.azureusnation.coreserver.entity.Player;
import com.azureusnation.coreserver.event.Event;
import mikera.vectorz.Vector2;

public class PlayerLookEvent
extends Event {
    private Player player;
    private Vector2 from;
    private Vector2 to;

    public PlayerLookEvent(Player player, Vector2 from, Vector2 newRotation) {
        this.player = player;
        this.from = from;
        this.to = newRotation;
    }

    public Player getPlayer() {
        return this.player;
    }

    public Vector2 getFrom() {
        return this.from;
    }

    public Vector2 getTo() {
        return this.to;
    }
}

