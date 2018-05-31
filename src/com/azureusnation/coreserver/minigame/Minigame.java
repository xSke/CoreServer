/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.minigame;

import com.azureusnation.coreserver.room.Room;

public class Minigame {
    private Room room;
    private boolean isFirstTick = true;

    public void start() {
    }

    public void tick() {
        if (this.isFirstTick) {
            this.isFirstTick = false;
            this.start();
        }
    }

    public Room getRoom() {
        return this.room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }
}

