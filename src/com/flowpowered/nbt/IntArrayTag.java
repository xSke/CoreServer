/*
 * Decompiled with CFR 0_129.
 */
package com.flowpowered.nbt;

import com.flowpowered.nbt.Tag;
import com.flowpowered.nbt.TagType;
import java.util.Arrays;

public class IntArrayTag
extends Tag<int[]> {
    private final int[] value;

    public IntArrayTag(String name, int[] value) {
        super(TagType.TAG_INT_ARRAY, name);
        this.value = value;
    }

    @Override
    public int[] getValue() {
        return this.value;
    }

    public String toString() {
        StringBuilder hex = new StringBuilder();
        for (int s : this.value) {
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
        return "TAG_Int_Array" + append + ": " + hex.toString();
    }

    @Override
    public IntArrayTag clone() {
        int[] clonedArray = this.cloneArray(this.value);
        return new IntArrayTag(this.getName(), clonedArray);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof IntArrayTag)) {
            return false;
        }
        IntArrayTag tag = (IntArrayTag)other;
        return Arrays.equals(this.value, tag.value) && this.getName().equals(tag.getName());
    }

    private int[] cloneArray(int[] intArray) {
        if (intArray == null) {
            return null;
        }
        int length = intArray.length;
        byte[] newArray = new byte[length];
        System.arraycopy(intArray, 0, newArray, 0, length);
        return intArray;
    }
}

