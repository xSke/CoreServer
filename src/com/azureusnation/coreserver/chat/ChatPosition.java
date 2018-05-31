/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.chat;

public enum ChatPosition {
    CHAT(0),
    SYSTEM(1),
    HOTBAR(2);
    
    private int id;

    private ChatPosition(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }
}

