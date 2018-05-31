/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.marker;

import com.azureusnation.coreserver.marker.Marker;
import mikera.vectorz.Vector3;

public class PointMarker
extends Marker {
    private Vector3 position;

    public PointMarker(int id, Vector3 position) {
        super(id);
        this.position = position;
    }

    public Vector3 getPosition() {
        return this.position;
    }
}

