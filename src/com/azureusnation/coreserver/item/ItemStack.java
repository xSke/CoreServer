/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.item;

import com.flowpowered.nbt.CompoundTag;

public class ItemStack {
    private short id;
    private int size;
    private int damage;
    private CompoundTag nbt;

    public ItemStack(short id) {
        this(id, 1, 0, null);
    }

    public ItemStack(short id, int size, int damage, CompoundTag nbt) {
        this.id = id;
        this.size = size;
        this.damage = damage;
        this.nbt = nbt;
    }

    public int getDamage() {
        return this.damage;
    }

    public int getSize() {
        return this.size;
    }

    public CompoundTag getNbt() {
        return this.nbt;
    }

    public short getId() {
        return this.id;
    }
}

