/*
 * Decompiled with CFR 0_129.
 */
package com.flowpowered.nbt;

import com.flowpowered.nbt.ByteArrayTag;
import com.flowpowered.nbt.ByteTag;
import com.flowpowered.nbt.CompoundTag;
import com.flowpowered.nbt.DoubleTag;
import com.flowpowered.nbt.EndTag;
import com.flowpowered.nbt.FloatTag;
import com.flowpowered.nbt.IntArrayTag;
import com.flowpowered.nbt.IntTag;
import com.flowpowered.nbt.ListTag;
import com.flowpowered.nbt.LongTag;
import com.flowpowered.nbt.ShortArrayTag;
import com.flowpowered.nbt.ShortTag;
import com.flowpowered.nbt.StringTag;
import com.flowpowered.nbt.Tag;
import java.util.HashMap;
import java.util.Map;

public enum TagType {
    TAG_END(EndTag.class, "TAG_End", 0),
    TAG_BYTE(ByteTag.class, "TAG_Byte", 1),
    TAG_SHORT(ShortTag.class, "TAG_Short", 2),
    TAG_INT(IntTag.class, "TAG_Int", 3),
    TAG_LONG(LongTag.class, "TAG_Long", 4),
    TAG_FLOAT(FloatTag.class, "TAG_Float", 5),
    TAG_DOUBLE(DoubleTag.class, "TAG_Double", 6),
    TAG_BYTE_ARRAY(ByteArrayTag.class, "TAG_Byte_Array", 7),
    TAG_STRING(StringTag.class, "TAG_String", 8),
    TAG_LIST(ListTag.class, "TAG_List", 9),
    TAG_COMPOUND(CompoundTag.class, "TAG_Compound", 10),
    TAG_INT_ARRAY(IntArrayTag.class, "TAG_Int_Array", 11),
    TAG_SHORT_ARRAY(ShortArrayTag.class, "TAG_Short_Array", 100);
    
    private static final Map<Class<? extends Tag<?>>, TagType> BY_CLASS;
    private static final Map<String, TagType> BY_NAME;
    private static final TagType[] BY_ID;
    private final Class<? extends Tag<?>> tagClass;
    private final String typeName;
    private final int id;

    private TagType(Class<? extends Tag<?>> tagClass, String typeName, int id) {
        this.tagClass = tagClass;
        this.typeName = typeName;
        this.id = id;
        if (this.id > maxId) {
            maxId = this.id;
        }
    }

    public Class<? extends Tag<?>> getTagClass() {
        return this.tagClass;
    }

    public String getTypeName() {
        return this.typeName;
    }

    public int getId() {
        return this.id;
    }

    public static TagType getByTagClass(Class<? extends Tag<?>> clazz) {
        TagType ret = BY_CLASS.get(clazz);
        if (ret == null) {
            throw new IllegalArgumentException("Tag type " + clazz + " is unknown!");
        }
        return ret;
    }

    public static TagType getByTypeName(String typeName) {
        TagType ret = BY_NAME.get(typeName);
        if (ret == null) {
            throw new IllegalArgumentException("Tag type " + typeName + " is unknown!");
        }
        return ret;
    }

    public static TagType getById(int id) {
        if (id >= 0 && id < BY_ID.length) {
            TagType ret = BY_ID[id];
            if (ret == null) {
                throw new IllegalArgumentException("Tag type id " + id + " is unknown!");
            }
            return ret;
        }
        throw new IndexOutOfBoundsException("Tag type id " + id + " is out of bounds!");
    }

    static {
        BY_CLASS = new HashMap();
        BY_NAME = new HashMap<String, TagType>();
        BY_ID = new TagType[maxId + 1];
        for (TagType type : TagType.values()) {
            BY_CLASS.put(type.getTagClass(), type);
            BY_NAME.put(type.getTypeName(), type);
            TagType.BY_ID[type.getId()] = type;
        }
    }

    private static class BaseData {
        private static int maxId = 0;

        private BaseData() {
        }
    }

}

