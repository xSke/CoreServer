/*
 * Decompiled with CFR 0_129.
 */
package com.flowpowered.nbt.holder;

import com.flowpowered.nbt.ListTag;
import com.flowpowered.nbt.Tag;
import com.flowpowered.nbt.holder.Field;
import com.flowpowered.nbt.holder.FieldUtils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ListField<T>
implements Field<List<T>> {
    private final Field<T> backingField;

    public ListField(Field<T> field) {
        this.backingField = field;
    }

    @Override
    public List<T> getValue(Tag<?> tag) throws IllegalArgumentException {
        ListTag listTag = FieldUtils.checkTagCast(tag, ListTag.class);
        ArrayList<T> result = new ArrayList<T>();
        Iterator iterator = listTag.getValue().iterator();
        while (iterator.hasNext()) {
            Tag element = (Tag)iterator.next();
            result.add(this.backingField.getValue(element));
        }
        return result;
    }

    @Override
    public Tag<?> getValue(String name, List<T> value) {
        ArrayList tags = new ArrayList();
        Class tagClazz = Tag.class;
        for (T element : value) {
            Tag tag = this.backingField.getValue("", element);
            tagClazz = tag.getClass();
            tags.add(tag);
        }
        return new ListTag<Tag>(name, tagClazz, tags);
    }
}

