/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.event;

import com.azureusnation.coreserver.entity.Player;
import com.azureusnation.coreserver.event.Event;
import mikera.vectorz.Vector3;

public class PlayerMoveEvent
extends Event {
    private Player player;
    private Vector3 from;
    private Vector3 to;

    public PlayerMoveEvent(Player player, Vector3 from, Vector3 to) {
        this.player = player;
        this.from = from;
        this.to = to;
    }

    public Player getPlayer() {
        return this.player;
    }

    public Vector3 getFrom() {
        return this.from;
    }

    public Vector3 getTo() {
        return this.to;
    }
}

