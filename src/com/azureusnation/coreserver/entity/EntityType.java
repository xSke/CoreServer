/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.entity;

public enum EntityType {
    PLAYER(-1),
    ZOMBIE(54);
    
    private int id;

    private EntityType(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }
}

