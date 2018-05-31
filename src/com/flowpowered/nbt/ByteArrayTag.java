/*
 * Decompiled with CFR 0_129.
 */
package com.flowpowered.nbt;

import com.flowpowered.nbt.Tag;
import com.flowpowered.nbt.TagType;
import java.util.Arrays;

public final class ByteArrayTag
extends Tag<byte[]> {
    private final byte[] value;

    public ByteArrayTag(String name, byte[] value) {
        super(TagType.TAG_BYTE_ARRAY, name);
        this.value = value;
    }

    @Override
    public byte[] getValue() {
        return this.value;
    }

    public String toString() {
        StringBuilder hex = new StringBuilder();
        for (byte b : this.value) {
            String hexDigits = Integer.toHexString(b).toUpperCase();
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
        return "TAG_Byte_Array" + append + ": " + hex.toString();
    }

    @Override
    public ByteArrayTag clone() {
        byte[] clonedArray = this.cloneArray(this.value);
        return new ByteArrayTag(this.getName(), clonedArray);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ByteArrayTag)) {
            return false;
        }
        ByteArrayTag tag = (ByteArrayTag)other;
        return Arrays.equals(this.value, tag.value) && this.getName().equals(tag.getName());
    }

    private byte[] cloneArray(byte[] byteArray) {
        if (byteArray == null) {
            return null;
        }
        int length = byteArray.length;
        byte[] newArray = new byte[length];
        System.arraycopy(byteArray, 0, newArray, 0, length);
        return newArray;
    }
}

