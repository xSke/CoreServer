/*
 * Decompiled with CFR 0_129.
 */
package com.flowpowered.nbt.holder;

import com.flowpowered.nbt.CompoundMap;
import com.flowpowered.nbt.CompoundTag;
import com.flowpowered.nbt.Tag;
import com.flowpowered.nbt.holder.Field;
import com.flowpowered.nbt.holder.FieldHolder;
import com.flowpowered.nbt.holder.FieldUtils;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class FieldHolderField<T extends FieldHolder>
implements Field<T> {
    private final Class<T> type;
    private final Constructor<T> typeConst;

    public FieldHolderField(Class<T> type) {
        this.type = type;
        try {
            this.typeConst = type.getConstructor(new Class[0]);
            this.typeConst.setAccessible(true);
        }
        catch (NoSuchMethodException e) {
            throw new ExceptionInInitializerError("Type must have zero-arg constructor!");
        }
    }

    @Override
    public T getValue(Tag<?> rawTag) throws IllegalArgumentException {
        CompoundTag tag = FieldUtils.checkTagCast(rawTag, CompoundTag.class);
        FieldHolder value = null;
        try {
            value = (FieldHolder)this.typeConst.newInstance(new Object[0]);
            value.load(tag);
        }
        catch (InstantiationException e) {
        }
        catch (IllegalAccessException e) {
        }
        catch (InvocationTargetException e) {
            // empty catch block
        }
        return (T)value;
    }

    @Override
    public Tag<?> getValue(String name, T value) {
        return new CompoundTag(name, value.save());
    }
}

