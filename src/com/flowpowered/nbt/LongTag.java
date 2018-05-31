/*
 * Decompiled with CFR 0_129.
 */
package com.flowpowered.nbt;

import com.flowpowered.nbt.Tag;
import com.flowpowered.nbt.TagType;

public final class LongTag
extends Tag<Long> {
    private final long value;

    public LongTag(String name, long value) {
        super(TagType.TAG_LONG, name);
        this.value = value;
    }

    @Override
    public Long getValue() {
        return this.value;
    }

    public String toString() {
        String name = this.getName();
        String append = "";
        if (name != null && !name.equals("")) {
            append = "(\"" + this.getName() + "\")";
        }
        return "TAG_Long" + append + ": " + this.value;
    }

    @Override
    public LongTag clone() {
        return new LongTag(this.getName(), this.value);
    }
}

