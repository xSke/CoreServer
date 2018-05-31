/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.impl;

import mikera.arrayz.INDArray;
import mikera.matrixx.AMatrix;
import mikera.vectorz.util.ErrorMessages;

public abstract class ARectangularMatrix
extends AMatrix {
    private static final long serialVersionUID = 6429003789294676974L;
    protected final int rows;
    protected final int cols;

    protected ARectangularMatrix(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
    }

    @Override
    public final int rowCount() {
        return this.rows;
    }

    @Override
    public final int columnCount() {
        return this.cols;
    }

    @Override
    public final int[] getShape() {
        return new int[]{this.rows, this.cols};
    }

    @Override
    public final int[] getShapeClone() {
        return new int[]{this.rows, this.cols};
    }

    @Override
    public int bandLength(int band) {
        return ARectangularMatrix.bandLength(this.rows, this.cols, band);
    }

    @Override
    public final int getShape(int dim) {
        if (dim == 0) {
            return this.rows;
        }
        if (dim == 1) {
            return this.cols;
        }
        throw new IndexOutOfBoundsException(ErrorMessages.invalidDimension(this, dim));
    }

    @Override
    public final boolean isSameShape(INDArray m) {
        return m.dimensionality() == 2 && this.rows == m.getShape(0) && this.cols == m.getShape(1);
    }

    @Override
    public int checkSquare() {
        int rc = this.rows;
        if (rc != this.cols) {
            throw new UnsupportedOperationException(ErrorMessages.nonSquareMatrix(this));
        }
        return rc;
    }

    @Override
    protected void checkSameShape(AMatrix m) {
        int rc = this.rowCount();
        int cc = this.columnCount();
        if (rc != m.rowCount() || cc != m.columnCount()) {
            throw new IndexOutOfBoundsException(ErrorMessages.mismatch(this, m));
        }
    }

    protected int checkColumn(int column) {
        int cc = this.columnCount();
        if (column < 0 || column >= cc) {
            throw new IndexOutOfBoundsException(ErrorMessages.invalidSlice(this, 1, column));
        }
        return cc;
    }

    protected int checkRow(int row) {
        int rc = this.rowCount();
        if (row < 0 || row >= rc) {
            throw new IndexOutOfBoundsException(ErrorMessages.invalidSlice(this, 0, row));
        }
        return rc;
    }

    @Override
    protected void checkSameShape(ARectangularMatrix m) {
        int rc = this.rowCount();
        int cc = this.columnCount();
        if (rc != m.rowCount() || cc != m.columnCount()) {
            throw new IndexOutOfBoundsException(ErrorMessages.mismatch(this, m));
        }
    }

    @Override
    protected final void checkIndex(int i, int j) {
        if (i < 0 || i >= this.rows || j < 0 || j >= this.cols) {
            throw new IndexOutOfBoundsException(ErrorMessages.invalidIndex((INDArray)this, i, j));
        }
    }

    @Override
    public final boolean isSameShape(AMatrix m) {
        return this.rows == m.rowCount() && this.cols == m.columnCount();
    }

    public final boolean isSameShape(ARectangularMatrix m) {
        return this.rows == m.rows && this.cols == m.cols;
    }

    @Override
    public final long elementCount() {
        return (long)this.rows * (long)this.cols;
    }
}

