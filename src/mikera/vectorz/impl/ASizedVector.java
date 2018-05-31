/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz.impl;

import mikera.arrayz.INDArray;
import mikera.vectorz.AVector;
import mikera.vectorz.util.ErrorMessages;

public abstract class ASizedVector
extends AVector {
    protected final int length;

    protected ASizedVector(int length) {
        this.length = length;
    }

    @Override
    public final int length() {
        return this.length;
    }

    @Override
    public final long elementCount() {
        return this.length();
    }

    @Override
    public final int sliceCount() {
        return this.length;
    }

    @Override
    public double get(long i) {
        if (i < 0L || i >= (long)this.length) {
            throw new IndexOutOfBoundsException(ErrorMessages.invalidIndex((INDArray)this, i));
        }
        return this.unsafeGet((int)i);
    }

    @Override
    public final int[] getShape() {
        return new int[]{this.length};
    }

    @Override
    public boolean isSameShape(AVector a) {
        return this.length == a.length();
    }

    @Override
    protected final int checkSameLength(AVector v) {
        int len = this.length;
        if (len != v.length()) {
            throw new IllegalArgumentException(ErrorMessages.incompatibleShapes(this, (INDArray)v));
        }
        return len;
    }

    @Override
    public final int checkRange(int offset, int length) {
        int len = this.length;
        int end = offset + length;
        if (offset < 0 || end > len) {
            throw new IndexOutOfBoundsException(ErrorMessages.invalidRange(this, offset, length));
        }
        return len;
    }

    @Override
    public final int checkIndex(int i) {
        int len = this.length;
        if (i < 0 || i >= len) {
            throw new IndexOutOfBoundsException(ErrorMessages.invalidIndex((INDArray)this, (long)i));
        }
        return len;
    }

    @Override
    public final int checkLength(int length) {
        if (this.length != length) {
            throw new IllegalArgumentException("Vector length mismatch, expected length = " + length + ", but got length = " + this.length);
        }
        return length;
    }

    @Override
    protected final int checkSameLength(ASizedVector v) {
        int len = this.length;
        if (len != v.length) {
            throw new IllegalArgumentException(ErrorMessages.incompatibleShapes(this, (INDArray)v));
        }
        return len;
    }

    @Override
    public final int[] getShapeClone() {
        return new int[]{this.length};
    }

    @Override
    public final int getShape(int dim) {
        if (dim == 0) {
            return this.length;
        }
        throw new IndexOutOfBoundsException(ErrorMessages.invalidDimension(this, dim));
    }

    @Override
    public boolean equalsArray(double[] data) {
        if (this.length != data.length) {
            return false;
        }
        return this.equalsArray(data, 0);
    }
}

