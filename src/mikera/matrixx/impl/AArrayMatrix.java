/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.impl;

import mikera.arrayz.INDArray;
import mikera.matrixx.AMatrix;
import mikera.matrixx.Matrix;
import mikera.matrixx.impl.ARectangularMatrix;

public abstract class AArrayMatrix
extends ARectangularMatrix {
    private static final long serialVersionUID = 7423448070352281717L;
    public final double[] data;

    protected AArrayMatrix(double[] data, int rows, int cols) {
        super(rows, cols);
        this.data = data;
    }

    public double[] getArray() {
        return this.data;
    }

    @Override
    public double get(int i, int j) {
        this.checkIndex(i, j);
        return this.data[this.index(i, j)];
    }

    @Override
    public void set(int i, int j, double value) {
        this.checkIndex(i, j);
        this.data[this.index((int)i, (int)j)] = value;
    }

    @Override
    public double unsafeGet(int i, int j) {
        return this.data[this.index(i, j)];
    }

    @Override
    public void unsafeSet(int i, int j, double value) {
        this.data[this.index((int)i, (int)j)] = value;
    }

    public abstract boolean isPackedArray();

    @Override
    public Matrix getTransposeCopy() {
        return this.toMatrixTranspose();
    }

    protected abstract int index(int var1, int var2);
}

