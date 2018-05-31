/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.event;

import com.azureusnation.coreserver.block.BlockFace;
import com.azureusnation.coreserver.event.Event;
import com.azureusnation.coreserver.item.ItemStack;
import mikera.vectorz.Vector3;

public class PlayerPlaceBlockEvent
extends Event {
    ItemStack itemStack;
    Vector3 position;
    BlockFace face;
    private Vector3 cursorPosition;

    public PlayerPlaceBlockEvent(ItemStack itemStack, Vector3 position, BlockFace blockFace, Vector3 cursorPosition) {
        this.itemStack = itemStack;
        this.position = position;
        this.face = blockFace;
        this.cursorPosition = cursorPosition;
    }

    public ItemStack getItemStack() {
        return this.itemStack;
    }

    public Vector3 getPosition() {
        return this.position;
    }

    public Vector3 getCursorPosition() {
        return this.cursorPosition;
    }

    public BlockFace getFace() {
        return this.face;
    }
}

