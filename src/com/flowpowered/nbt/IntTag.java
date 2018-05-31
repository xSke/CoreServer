/*
 * Decompiled with CFR 0_129.
 */
package com.flowpowered.nbt;

import com.flowpowered.nbt.Tag;
import com.flowpowered.nbt.TagType;

public final class IntTag
extends Tag<Integer> {
    private final int value;

    public IntTag(String name, int value) {
        super(TagType.TAG_INT, name);
        this.value = value;
    }

    @Override
    public Integer getValue() {
        return this.value;
    }

    public String toString() {
        String name = this.getName();
        String append = "";
        if (name != null && !name.equals("")) {
            append = "(\"" + this.getName() + "\")";
        }
        return "TAG_Int" + append + ": " + this.value;
    }

    @Override
    public IntTag clone() {
        return new IntTag(this.getName(), this.value);
    }
}

