/*
 * Decompiled with CFR 0_129.
 */
package com.azureusnation.coreserver.util;

import mikera.vectorz.Vector3;

public class Cuboid {
    private Vector3 a;
    private Vector3 b;

    public Cuboid(Vector3 a, Vector3 b) {
        this.a = new Vector3(Math.min(a.x, b.x), Math.min(a.y, b.y), Math.min(a.z, b.z));
        this.b = new Vector3(Math.max(a.x, b.x), Math.max(a.y, b.y), Math.max(a.z, b.z));
    }

    public Vector3 getA() {
        return this.a;
    }

    public Vector3 getB() {
        return this.b;
    }

    public void fixCorners() {
        Vector3 tmpA = this.a.clone();
        Vector3 tmpB = this.b.clone();
        this.a.setValues(Math.min(tmpA.x, tmpB.x), Math.min(tmpA.y, tmpB.y), Math.min(tmpA.z, tmpB.z));
        this.b.setValues(Math.max(tmpA.x, tmpB.x), Math.max(tmpA.y, tmpB.y), Math.max(tmpA.z, tmpB.z));
    }

    public boolean contains(Vector3 point) {
        return point.x >= this.a.x && point.x <= this.b.x && point.y >= this.a.y && point.y <= this.b.y && point.z >= this.a.z && point.z <= this.b.z;
    }
}

