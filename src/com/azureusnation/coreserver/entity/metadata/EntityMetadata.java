/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.entity.metadata;

import com.azureusnation.coreserver.entity.metadata.EntityMetadataField;
import com.azureusnation.coreserver.entity.metadata.EntityMetadataFieldType;
import com.azureusnation.coreserver.net.PacketBuffer;
import io.netty.buffer.ByteBuf;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import mikera.vectorz.Vector3;

public class EntityMetadata {
    private Map<Integer, EntityMetadataField> fields = new HashMap<Integer, EntityMetadataField>();

    public Object get(int index) {
        return this.fields.get(index).getValue();
    }

    public void set(int index, float value) {
        this.fields.put(index, new EntityMetadataField(EntityMetadataFieldType.FLOAT, Float.valueOf(value)));
    }

    public void set(int index, byte value) {
        this.fields.put(index, new EntityMetadataField(EntityMetadataFieldType.BYTE, value));
    }

    public void serialize(PacketBuffer writer) throws IOException {
        block9 : for (Map.Entry<Integer, EntityMetadataField> field : this.fields.entrySet()) {
            int leading = field.getValue().getType().getId() << 5 | field.getKey();
            writer.writeByte(leading);
            switch (field.getValue().getType()) {
                case BYTE: {
                    writer.writeByte(((Number)field.getValue().getValue()).byteValue());
                    continue block9;
                }
                case SHORT: {
                    writer.writeShort(((Number)field.getValue().getValue()).shortValue());
                    continue block9;
                }
                case INT: {
                    writer.writeInt(((Number)field.getValue().getValue()).intValue());
                    continue block9;
                }
                case FLOAT: {
                    writer.writeFloat(((Number)field.getValue().getValue()).floatValue());
                    continue block9;
                }
                case STRING: {
                    writer.writeString((String)field.getValue().getValue());
                    continue block9;
                }
                case SLOT: {
                    throw new RuntimeException("TODO");
                }
                case VECTOR3F: {
                    writer.writeFloat((float)((Vector3)field.getValue().getValue()).x);
                    writer.writeFloat((float)((Vector3)field.getValue().getValue()).y);
                    writer.writeFloat((float)((Vector3)field.getValue().getValue()).z);
                    continue block9;
                }
            }
            throw new RuntimeException("Not implemented, wut");
        }
        writer.writeByte(127);
    }

}

