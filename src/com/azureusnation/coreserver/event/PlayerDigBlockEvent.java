/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.event;

import com.azureusnation.coreserver.block.BlockFace;
import com.azureusnation.coreserver.entity.Player;
import com.azureusnation.coreserver.event.Event;
import mikera.vectorz.Vector3;

public class PlayerDigBlockEvent
extends Event {
    Player player;
    Status status;
    Vector3 position;
    BlockFace face;

    public PlayerDigBlockEvent(Player player, Status status, Vector3 position, BlockFace face) {
        this.player = player;
        this.status = status;
        this.position = position;
        this.face = face;
    }

    public Player getPlayer() {
        return this.player;
    }

    public Status getStatus() {
        return this.status;
    }

    public Vector3 getPosition() {
        return this.position;
    }

    public BlockFace getFace() {
        return this.face;
    }

    public static enum Status {
        STARTED_DIGGING,
        CANCELLED_DIGGING,
        FINISHED_DIGGING,
        DROP_ITEM_STACK,
        DROP_ITEM,
        SHOOT;
        

        private Status() {
        }
    }

}

