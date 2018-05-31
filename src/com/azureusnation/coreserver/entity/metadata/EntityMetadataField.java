/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.entity.metadata;

import com.azureusnation.coreserver.entity.metadata.EntityMetadataFieldType;

public class EntityMetadataField {
    private EntityMetadataFieldType type;
    private Object value;

    public EntityMetadataField(EntityMetadataFieldType type, Object value) {
        this.type = type;
        this.value = value;
    }

    public EntityMetadataFieldType getType() {
        return this.type;
    }

    public Object getValue() {
        return this.value;
    }
}

