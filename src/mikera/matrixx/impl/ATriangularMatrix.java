/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.impl;

import mikera.matrixx.impl.AArrayMatrix;
import mikera.vectorz.AVector;
import mikera.vectorz.util.DoubleArrays;
import mikera.vectorz.util.ErrorMessages;

public abstract class ATriangularMatrix
extends AArrayMatrix {
    private static final long serialVersionUID = -5557895922040729998L;

    protected ATriangularMatrix(double[] data, int rows, int cols) {
        super(data, rows, cols);
    }

    @Override
    public boolean isFullyMutable() {
        return false;
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public boolean isPackedArray() {
        return false;
    }

    @Override
    public abstract AVector getBand(int var1);

    @Override
    public double determinant() {
        if (this.rows != this.cols) {
            throw new IllegalArgumentException(ErrorMessages.nonSquareMatrix(this));
        }
        return this.diagonalProduct();
    }

    @Override
    public boolean isZero() {
        return DoubleArrays.isZero(this.data);
    }
}

