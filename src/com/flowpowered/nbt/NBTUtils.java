/*
 * Decompiled with CFR 0_129.
 */
package com.flowpowered.nbt;

import com.flowpowered.nbt.Tag;
import com.flowpowered.nbt.TagType;

public final class NBTUtils {
    @Deprecated
    public static String getTypeName(Class<? extends Tag<?>> clazz) {
        return TagType.getByTagClass(clazz).getTypeName();
    }

    @Deprecated
    public static int getTypeCode(Class<? extends Tag<?>> clazz) {
        return TagType.getByTagClass(clazz).getId();
    }

    @Deprecated
    public static Class<? extends Tag> getTypeClass(int type) {
        return TagType.getById(type).getTagClass();
    }

    private NBTUtils() {
    }
}

