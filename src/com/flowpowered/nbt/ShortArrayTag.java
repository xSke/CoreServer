/*
 * Decompiled with CFR 0_129.
 */
package com.flowpowered.nbt;

import com.flowpowered.nbt.Tag;
import com.flowpowered.nbt.TagType;
import java.util.Arrays;

public class ShortArrayTag
extends Tag<short[]> {
    private final short[] value;

    public ShortArrayTag(String name, short[] value) {
        super(TagType.TAG_SHORT_ARRAY, name);
        this.value = value;
    }

    @Override
    public short[] getValue() {
        return this.value;
    }

    public String toString() {
        StringBuilder hex = new StringBuilder();
        for (short s : this.value) {
            String hexDigits = Integer.toHexString(s).toUpperCase();
            if (hexDigits.length() == 1) {
                hex.append("0");
            }
            hex.append(hexDigits).append(" ");
        }
        String name = this.getName();
        String append = "";
        if (name != null && !name.equals("")) {
            append = "(\"" + this.getName() + "\")";
        }
        return "TAG_Short_Array" + append + ": " + hex.toString();
    }

    @Override
    public ShortArrayTag clone() {
        short[] clonedArray = this.cloneArray(this.value);
        return new ShortArrayTag(this.getName(), clonedArray);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ShortArrayTag)) {
            return false;
        }
        ShortArrayTag tag = (ShortArrayTag)other;
        return Arrays.equals(this.value, tag.value) && this.getName().equals(tag.getName());
    }

    private short[] cloneArray(short[] shortArray) {
        if (shortArray == null) {
            return null;
        }
        int length = shortArray.length;
        short[] newArray = new short[length];
        System.arraycopy(shortArray, 0, newArray, 0, length);
        return shortArray;
    }
}

