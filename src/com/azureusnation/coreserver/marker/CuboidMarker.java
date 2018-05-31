/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.marker;

import com.azureusnation.coreserver.marker.Marker;
import com.azureusnation.coreserver.util.Cuboid;

public class CuboidMarker
extends Marker {
    private Cuboid cuboid;

    public CuboidMarker(int id, Cuboid cuboid) {
        super(id);
        this.cuboid = cuboid;
    }

    public Cuboid getCuboid() {
        return this.cuboid;
    }
}

