/*
 * Decompiled with CFR 0_129.
 */
package com.flowpowered.nbt.util;

import com.flowpowered.nbt.Tag;

public class NBTMapper {
    public static Object toTagValue(Tag<?> t) {
        if (t == null) {
            return null;
        }
        return t.getValue();
    }

    public static <T> T getTagValue(Tag<?> t, Class<? extends T> clazz) {
        Object o = NBTMapper.toTagValue(t);
        if (o == null) {
            return null;
        }
        try {
            return clazz.cast(o);
        }
        catch (ClassCastException e) {
            return null;
        }
    }

    public static <T, U extends T> T toTagValue(Tag<?> t, Class<? extends T> clazz, U defaultValue) {
        Object o = NBTMapper.toTagValue(t);
        if (o == null) {
            return (T)defaultValue;
        }
        try {
            T value = clazz.cast(o);
            return value;
        }
        catch (ClassCastException e) {
            return (T)defaultValue;
        }
    }
}

