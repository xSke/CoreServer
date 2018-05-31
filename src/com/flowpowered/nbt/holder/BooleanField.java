/*
 * Decompiled with CFR 0_129.
 */
package com.flowpowered.nbt.holder;

import com.flowpowered.nbt.ByteTag;
import com.flowpowered.nbt.Tag;
import com.flowpowered.nbt.holder.Field;
import com.flowpowered.nbt.holder.FieldUtils;

public class BooleanField
implements Field<Boolean> {
    public static final BooleanField INSTANCE = new BooleanField();

    @Override
    public Boolean getValue(Tag<?> tag) throws IllegalArgumentException {
        return FieldUtils.checkTagCast(tag, ByteTag.class).getBooleanValue();
    }

    @Override
    public Tag<?> getValue(String name, Boolean value) {
        return new ByteTag(name, value);
    }
}

