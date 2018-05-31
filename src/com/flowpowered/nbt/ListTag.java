/*
 * Decompiled with CFR 0_129.
 */
package com.flowpowered.nbt;

import com.flowpowered.nbt.Tag;
import com.flowpowered.nbt.TagType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ListTag<T extends Tag<?>>
extends Tag<List<T>> {
    private final Class<T> type;
    private final List<T> value;

    public ListTag(String name, Class<T> type, List<T> value) {
        super(TagType.TAG_LIST, name);
        this.type = type;
        this.value = Collections.unmodifiableList(value);
    }

    public Class<T> getElementType() {
        return this.type;
    }

    @Override
    public List<T> getValue() {
        return this.value;
    }

    public String toString() {
        String name = this.getName();
        String append = "";
        if (name != null && !name.equals("")) {
            append = "(\"" + this.getName() + "\")";
        }
        StringBuilder bldr = new StringBuilder();
        bldr.append("TAG_List").append(append).append(": ").append(this.value.size()).append(" entries of type ").append(TagType.getByTagClass(this.type).getTypeName()).append("\r\n{\r\n");
        for (Tag t : this.value) {
            bldr.append("   ").append(t.toString().replaceAll("\r\n", "\r\n   ")).append("\r\n");
        }
        bldr.append("}");
        return bldr.toString();
    }

    @Override
    public ListTag<T> clone() {
        ArrayList<Object> newList = new ArrayList<Object>();
        for (Tag v : this.value) {
            newList.add(v.clone());
        }
        return new ListTag<T>(this.getName(), this.type, newList);
    }
}

