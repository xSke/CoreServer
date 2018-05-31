/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.impl;

import mikera.arrayz.INDArray;
import mikera.matrixx.AMatrix;
import mikera.vectorz.AVector;
import mikera.vectorz.impl.AMatrixViewVector;

public final class MatrixRowView
extends AMatrixViewVector {
    private final int row;

    public MatrixRowView(AMatrix aMatrix, int row) {
        super(aMatrix, aMatrix.columnCount());
        this.row = row;
    }

    @Override
    public double get(int i) {
        return this.source.get(this.row, i);
    }

    @Override
    public double unsafeGet(int i) {
        return this.source.unsafeGet(this.row, i);
    }

    @Override
    public void set(int i, double value) {
        this.source.set(this.row, i, value);
    }

    @Override
    public void unsafeSet(int i, double value) {
        this.source.unsafeSet(this.row, i, value);
    }

    @Override
    public boolean isFullyMutable() {
        return this.source.isFullyMutable();
    }

    @Override
    public MatrixRowView exactClone() {
        return new MatrixRowView(this.source.exactClone(), this.row);
    }

    @Override
    public void getElements(double[] data, int offset) {
        this.source.copyRowTo(this.row, data, offset);
    }

    @Override
    protected int calcRow(int i) {
        return this.row;
    }

    @Override
    protected int calcCol(int i) {
        return i;
    }

    @Override
    public AVector clone() {
        return this.source.getRowClone(this.row);
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
            if (v.unsafeGet(i) == this.unsafeGet(i)) continue;
            return false;
        }
        return true;
    }

    @Override
    public double dotProduct(double[] data, int offset) {
        double result = 0.0;
        for (int i = 0; i < this.length; ++i) {
            result += data[offset + i] * this.unsafeGet(i);
        }
        return result;
    }
}

