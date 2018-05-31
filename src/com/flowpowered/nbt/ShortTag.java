/*
 * Decompiled with CFR 0_129.
 */
package com.flowpowered.nbt;

import com.flowpowered.nbt.Tag;
import com.flowpowered.nbt.TagType;

public final class ShortTag
extends Tag<Short> {
    private final short value;

    public ShortTag(String name, short value) {
        super(TagType.TAG_SHORT, name);
        this.value = value;
    }

    @Override
    public Short getValue() {
        return this.value;
    }

    public String toString() {
        String name = this.getName();
        String append = "";
        if (name != null && !name.equals("")) {
            append = "(\"" + this.getName() + "\")";
        }
        return "TAG_Short" + append + ": " + this.value;
    }

    @Override
    public ShortTag clone() {
        return new ShortTag(this.getName(), this.value);
    }
}

