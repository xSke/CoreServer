/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz.impl;

import mikera.vectorz.AVector;

public abstract class BaseDerivedVector
extends AVector {
    private static final long serialVersionUID = -9039112666567131812L;
    protected final AVector source;

    protected BaseDerivedVector(AVector source) {
        this.source = source;
    }

    @Override
    public int length() {
        return this.source.length();
    }

    @Override
    public double get(int i) {
        return this.source.get(i);
    }

    @Override
    public double unsafeGet(int i) {
        return this.source.unsafeGet(i);
    }

    @Override
    public double dotProduct(double[] data, int offset) {
        return this.source.dotProduct(data, offset);
    }

    @Override
    public void set(int i, double value) {
        this.source.set(i, value);
    }

    @Override
    public void unsafeSet(int i, double value) {
        this.source.unsafeSet(i, value);
    }

    @Override
    public void getElements(double[] dest, int offset) {
        this.source.getElements(dest, offset);
    }

    @Override
    public boolean isView() {
        return true;
    }

    @Override
    public boolean isFullyMutable() {
        return this.source.isFullyMutable();
    }

    @Override
    public boolean isMutable() {
        return this.source.isMutable();
    }

    @Override
    public boolean equals(AVector v) {
        return this.source.equals(v);
    }

    @Override
    public boolean equalsArray(double[] data, int offset) {
        return this.source.equalsArray(data, offset);
    }
}

