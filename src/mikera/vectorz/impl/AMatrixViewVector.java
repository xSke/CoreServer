/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz.impl;

import mikera.arrayz.INDArray;
import mikera.matrixx.AMatrix;
import mikera.vectorz.AScalar;
import mikera.vectorz.AVector;
import mikera.vectorz.impl.ASizedVector;
import mikera.vectorz.impl.MatrixIndexScalar;

public abstract class AMatrixViewVector
extends ASizedVector {
    protected AMatrix source;

    protected AMatrixViewVector(AMatrix source, int length) {
        super(length);
        this.source = source;
    }

    protected abstract int calcRow(int var1);

    protected abstract int calcCol(int var1);

    @Override
    public void set(int i, double value) {
        this.checkIndex(i);
        this.source.unsafeSet(this.calcRow(i), this.calcCol(i), value);
    }

    @Override
    public void addAt(int i, double v) {
        int r = this.calcRow(i);
        int c = this.calcCol(i);
        this.source.unsafeSet(r, c, this.source.unsafeGet(r, c) + v);
    }

    @Override
    public void unsafeSet(int i, double value) {
        this.source.unsafeSet(this.calcRow(i), this.calcCol(i), value);
    }

    @Override
    public double get(int i) {
        this.checkIndex(i);
        return this.source.unsafeGet(this.calcRow(i), this.calcCol(i));
    }

    @Override
    public double unsafeGet(int i) {
        return this.source.unsafeGet(this.calcRow(i), this.calcCol(i));
    }

    @Override
    public void getElements(double[] data, int offset) {
        for (int i = 0; i < this.length; ++i) {
            data[offset + i] = this.source.unsafeGet(this.calcRow(i), this.calcCol(i));
        }
    }

    @Override
    public MatrixIndexScalar slice(int i) {
        return MatrixIndexScalar.wrap(this.source, this.calcRow(i), this.calcCol(i));
    }

    @Override
    public boolean equals(AVector v) {
        if (v == this) {
            return true;
        }
        if (v.length() != this.length) {
            return false;
        }
        for (int i = 0; i < this.length; ++i) {
            if (v.unsafeGet(i) == this.source.unsafeGet(this.calcRow(i), this.calcCol(i))) continue;
            return false;
        }
        return true;
    }

    @Override
    public boolean equalsArray(double[] data, int offset) {
        for (int i = 0; i < this.length; ++i) {
            if (this.unsafeGet(i) == data[offset++]) continue;
            return false;
        }
        return true;
    }
}

