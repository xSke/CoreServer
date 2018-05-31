/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.impl;

import mikera.arrayz.INDArray;
import mikera.matrixx.AMatrix;
import mikera.matrixx.impl.ADiagonalMatrix;
import mikera.vectorz.AVector;
import mikera.vectorz.impl.ADenseArrayVector;
import mikera.vectorz.impl.RepeatedElementVector;
import mikera.vectorz.util.ErrorMessages;

public class ScalarMatrix
extends ADiagonalMatrix {
    private static final long serialVersionUID = 3777724453035425881L;
    private final double scale;

    public ScalarMatrix(int dimensions, double scale) {
        super(dimensions);
        this.scale = scale;
        if (dimensions < 1) {
            throw new IllegalArgumentException("ScalarMatrix must have one or more dimensions");
        }
    }

    @Override
    public long nonZeroCount() {
        return this.scale == 0.0 ? 0L : (long)this.dimensions;
    }

    @Override
    public double elementSum() {
        return this.scale * (double)this.dimensions;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public AMatrix innerProduct(AMatrix m) {
        if (this.dimensions != m.rowCount()) {
            throw new IllegalArgumentException(ErrorMessages.incompatibleShapes(this, m));
        }
        return m.innerProduct(this.scale);
    }

    @Override
    public double get(int row, int column) {
        if (row < 0 || row >= this.dimensions || column < 0 || column >= this.dimensions) {
            throw new IndexOutOfBoundsException(ErrorMessages.invalidIndex((INDArray)this, row, column));
        }
        return row == column ? this.scale : 0.0;
    }

    @Override
    public double unsafeGet(int row, int column) {
        return row == column ? this.scale : 0.0;
    }

    @Override
    public RepeatedElementVector getLeadingDiagonal() {
        return RepeatedElementVector.create(this.dimensions, this.scale);
    }

    public static ScalarMatrix create(int dimensions, double scale) {
        return new ScalarMatrix(dimensions, scale);
    }

    @Override
    public double trace() {
        return this.scale * (double)this.dimensions;
    }

    @Override
    public double diagonalProduct() {
        return Math.pow(this.scale, this.dimensions);
    }

    @Override
    public AMatrix multiplyCopy(double d) {
        return ScalarMatrix.create(this.dimensions, d * this.scale);
    }

    @Override
    public void transformInPlace(AVector v) {
        v.multiply(this.scale);
    }

    @Override
    public void transformInPlace(ADenseArrayVector v) {
        v.multiply(this.scale);
    }

    @Override
    public ScalarMatrix exactClone() {
        return new ScalarMatrix(this.dimensions, this.scale);
    }
}

