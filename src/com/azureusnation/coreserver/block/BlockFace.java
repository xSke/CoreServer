/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.block;

import mikera.vectorz.Vector3;

public enum BlockFace {
    BOTTOM(new Vector3(0.0, -1.0, 0.0)),
    TOP(new Vector3(0.0, 1.0, 0.0)),
    NORTH(new Vector3(0.0, 0.0, -1.0)),
    SOUTH(new Vector3(0.0, 0.0, 1.0)),
    WEST(new Vector3(-1.0, 0.0, 0.0)),
    EAST(new Vector3(1.0, 0.0, 0.0));
    
    private Vector3 offset;

    private BlockFace(Vector3 offset) {
        this.offset = offset;
    }

    public Vector3 getOffset() {
        return this.offset;
    }
}

