/*
 * Decompiled with CFR 0_129.
 */
package com.flowpowered.nbt;

import com.flowpowered.nbt.Tag;
import com.flowpowered.nbt.TagType;

public final class StringTag
extends Tag<String> {
    private final String value;

    public StringTag(String name, String value) {
        super(TagType.TAG_STRING, name);
        this.value = value;
    }

    @Override
    public String getValue() {
        return this.value;
    }

    public String toString() {
        String name = this.getName();
        String append = "";
        if (name != null && !name.equals("")) {
            append = "(\"" + this.getName() + "\")";
        }
        return "TAG_String" + append + ": " + this.value;
    }

    @Override
    public StringTag clone() {
        return new StringTag(this.getName(), this.value);
    }
}

