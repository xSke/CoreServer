/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.net.packet;

import com.google.gson.Gson;

public abstract class Packet {
    public String toString() {
        try {
            return this.getClass().getSimpleName() + " - " + new Gson().toJson(this);
        }
        catch (UnsupportedOperationException e) {
            return this.getClass().getSimpleName() + " - couldn't serialize";
        }
    }
}

