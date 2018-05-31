/*
 * Decompiled with CFR 0_129.
 */
package com.flowpowered.nbt;

import com.flowpowered.nbt.CompoundMap;
import com.flowpowered.nbt.Tag;
import com.flowpowered.nbt.TagType;
import java.util.Collection;

public class CompoundTag
extends Tag<CompoundMap> {
    private final CompoundMap value;

    public CompoundTag(String name, CompoundMap value) {
        super(TagType.TAG_COMPOUND, name);
        this.value = value;
    }

    @Override
    public CompoundMap getValue() {
        return this.value;
    }

    public String toString() {
        String name = this.getName();
        String append = "";
        if (name != null && !name.equals("")) {
            append = "(\"" + this.getName() + "\")";
        }
        StringBuilder bldr = new StringBuilder();
        bldr.append("TAG_Compound").append(append).append(": ").append(this.value.size()).append(" entries\r\n{\r\n");
        for (Tag entry : this.value.values()) {
            bldr.append("   ").append(entry.toString().replaceAll("\r\n", "\r\n   ")).append("\r\n");
        }
        bldr.append("}");
        return bldr.toString();
    }

    @Override
    public CompoundTag clone() {
        CompoundMap map = new CompoundMap(this.value);
        return new CompoundTag(this.getName(), map);
    }
}

