/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.event;

import com.azureusnation.coreserver.room.Room;
import com.google.common.eventbus.EventBus;

public abstract class Event {
    private boolean cancelled = false;

    public boolean isCancelled() {
        return this.cancelled;
    }

    public void setCancelled() {
        this.cancelled = true;
    }

    public boolean send(Room room) {
        room.getEventBus().post(this);
        return this.cancelled;
    }
}

