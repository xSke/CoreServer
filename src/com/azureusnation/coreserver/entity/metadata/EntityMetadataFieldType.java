/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.entity.metadata;

public enum EntityMetadataFieldType {
    BYTE(0),
    SHORT(1),
    INT(2),
    FLOAT(3),
    STRING(4),
    SLOT(5),
    VECTOR3F(7);
    
    private int id;

    private EntityMetadataFieldType(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }
}

