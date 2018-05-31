/*
 * Decompiled with CFR 0_129.
 */
package com.flowpowered.nbt.holder;

import com.flowpowered.nbt.CompoundMap;
import com.flowpowered.nbt.CompoundTag;
import com.flowpowered.nbt.Tag;
import com.flowpowered.nbt.holder.FieldValue;
import com.flowpowered.nbt.stream.NBTInputStream;
import com.flowpowered.nbt.stream.NBTOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class FieldHolder {
    private final List<FieldValue<?>> fields = new ArrayList();

    protected /* varargs */ FieldHolder(FieldValue<?> ... fields) {
        this.addFields(fields);
    }

    protected /* varargs */ void addFields(FieldValue<?> ... fields) {
        Collections.addAll(this.fields, fields);
    }

    public CompoundMap save() {
        CompoundMap map = new CompoundMap();
        for (FieldValue field : this.fields) {
            field.save(map);
        }
        return map;
    }

    public void load(CompoundTag tag) {
        for (FieldValue field : this.fields) {
            field.load(tag);
        }
    }

    public void save(File file, boolean compressed) throws IOException {
        this.save(new FileOutputStream(file), compressed);
    }

    public void save(OutputStream stream, boolean compressed) throws IOException {
        NBTOutputStream os = new NBTOutputStream(stream, compressed);
        os.writeTag(new CompoundTag("", this.save()));
    }

    public void load(File file, boolean compressed) throws IOException {
        this.load(new FileInputStream(file), compressed);
    }

    public void load(InputStream stream, boolean compressed) throws IOException {
        NBTInputStream is = new NBTInputStream(stream, compressed);
        Tag tag = is.readTag();
        if (!(tag instanceof CompoundTag)) {
            throw new IllegalArgumentException("Expected CompoundTag, got " + tag.getClass());
        }
        CompoundTag compound = (CompoundTag)tag;
        this.load(compound);
    }
}

