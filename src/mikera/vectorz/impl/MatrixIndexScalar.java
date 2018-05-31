/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz.impl;

import mikera.arrayz.INDArray;
import mikera.matrixx.AMatrix;
import mikera.vectorz.AScalar;
import mikera.vectorz.Scalar;
import mikera.vectorz.impl.ImmutableScalar;
import mikera.vectorz.util.ErrorMessages;
import mikera.vectorz.util.VectorzException;

public class MatrixIndexScalar
extends AScalar {
    private static final long serialVersionUID = -4023138233113585392L;
    final AMatrix matrix;
    final int row;
    final int col;

    private MatrixIndexScalar(AMatrix matrix, int row, int col) {
        this.matrix = matrix;
        this.row = row;
        this.col = col;
    }

    public static MatrixIndexScalar wrap(AMatrix matrix, int row, int col) {
        MatrixIndexScalar m = new MatrixIndexScalar(matrix, row, col);
        if (!m.isValidIndex()) {
            throw new VectorzException(ErrorMessages.invalidIndex((INDArray)matrix, row, col));
        }
        return m;
    }

    private boolean isValidIndex() {
        return this.row >= 0 && this.row < this.matrix.rowCount() && this.col >= 0 && this.col < this.matrix.columnCount();
    }

    @Override
    public double get() {
        return this.matrix.unsafeGet(this.row, this.col);
    }

    @Override
    public void set(double value) {
        this.matrix.unsafeSet(this.row, this.col, value);
    }

    @Override
    public boolean isMutable() {
        return this.matrix.isMutable();
    }

    @Override
    public boolean isFullyMutable() {
        return this.matrix.isFullyMutable();
    }

    @Override
    public boolean isView() {
        return true;
    }

    @Override
    public Scalar clone() {
        return new Scalar(this.get());
    }

    @Override
    public MatrixIndexScalar exactClone() {
        return new MatrixIndexScalar(this.matrix.clone(), this.row, this.col);
    }

    @Override
    public AScalar mutable() {
        if (this.matrix.isFullyMutable()) {
            return this;
        }
        return Scalar.create(this.get());
    }

    @Override
    public AScalar immutable() {
        return ImmutableScalar.create(this.get());
    }

    @Override
    public void validate() {
        if (!this.isValidIndex()) {
            throw new VectorzException(ErrorMessages.invalidIndex((INDArray)this.matrix, this.row, this.col));
        }
        super.validate();
    }
}

