/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.impl;

import mikera.arrayz.INDArray;
import mikera.matrixx.AMatrix;
import mikera.vectorz.AVector;
import mikera.vectorz.impl.AMatrixViewVector;

public final class MatrixColumnView
extends AMatrixViewVector {
    private final int column;

    public MatrixColumnView(AMatrix aMatrix, int column) {
        super(aMatrix, aMatrix.rowCount());
        this.column = column;
    }

    @Override
    public double get(int i) {
        return this.source.get(i, this.column);
    }

    @Override
    public double unsafeGet(int i) {
        return this.source.unsafeGet(i, this.column);
    }

    @Override
    public void set(int i, double value) {
        this.source.set(i, this.column, value);
    }

    @Override
    public void unsafeSet(int i, double value) {
        this.source.unsafeSet(i, this.column, value);
    }

    @Override
    public boolean isFullyMutable() {
        return this.source.isFullyMutable();
    }

    @Override
    public MatrixColumnView exactClone() {
        return new MatrixColumnView(this.source.exactClone(), this.column);
    }

    @Override
    public void getElements(double[] data, int offset) {
        this.source.copyColumnTo(this.column, data, offset);
    }

    @Override
    protected int calcRow(int i) {
        return i;
    }

    @Override
    protected int calcCol(int i) {
        return this.column;
    }

    @Override
    public double dotProduct(double[] data, int offset) {
        double result = 0.0;
        for (int i = 0; i < this.length; ++i) {
            result += data[offset + i] * this.unsafeGet(i);
        }
        return result;
    }

    @Override
    public AVector clone() {
        return this.source.getColumnClone(this.column);
    }
}

