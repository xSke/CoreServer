/*
 * Decompiled with CFR 0_129.
 */
package com.flowpowered.nbt;

import com.flowpowered.nbt.Tag;
import com.flowpowered.nbt.TagType;

public final class DoubleTag
extends Tag<Double> {
    private final double value;

    public DoubleTag(String name, double value) {
        super(TagType.TAG_DOUBLE, name);
        this.value = value;
    }

    @Override
    public Double getValue() {
        return this.value;
    }

    public String toString() {
        String name = this.getName();
        String append = "";
        if (name != null && !name.equals("")) {
            append = "(\"" + this.getName() + "\")";
        }
        return "TAG_Double" + append + ": " + this.value;
    }

    @Override
    public DoubleTag clone() {
        return new DoubleTag(this.getName(), this.value);
    }
}

