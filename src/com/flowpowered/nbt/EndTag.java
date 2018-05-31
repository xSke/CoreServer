/*
 * Decompiled with CFR 0_129.
 */
package com.flowpowered.nbt;

import com.flowpowered.nbt.Tag;
import com.flowpowered.nbt.TagType;

public final class EndTag
extends Tag<Object> {
    public EndTag() {
        super(TagType.TAG_END);
    }

    @Override
    public Object getValue() {
        return null;
    }

    public String toString() {
        return "TAG_End";
    }

    @Override
    public EndTag clone() {
        return new EndTag();
    }
}

