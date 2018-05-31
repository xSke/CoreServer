/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.impl;

import java.util.Arrays;
import mikera.arrayz.INDArray;
import mikera.matrixx.AMatrix;
import mikera.matrixx.impl.ARectangularMatrix;
import mikera.matrixx.impl.IFastRows;
import mikera.vectorz.AVector;
import mikera.vectorz.util.ErrorMessages;

public final class BroadcastVectorMatrix
extends ARectangularMatrix
implements IFastRows {
    private static final long serialVersionUID = 8586152718389477791L;
    private final AVector vector;

    private BroadcastVectorMatrix(AVector v, int rows) {
        super(rows, v.length());
        this.vector = v;
    }

    @Override
    public boolean isFullyMutable() {
        return false;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    public static BroadcastVectorMatrix wrap(AVector v, int rows) {
        return new BroadcastVectorMatrix(v, rows);
    }

    @Override
    public AVector getRowView(int row) {
        this.checkRow(row);
        return this.vector;
    }

    @Override
    public final void copyColumnTo(int col, double[] dest, int destOffset) {
        double v = this.vector.get(col);
        Arrays.fill(dest, destOffset, destOffset + this.rows, v);
    }

    @Override
    public final void copyRowTo(int row, double[] dest, int destOffset) {
        this.vector.getElements(dest, destOffset);
    }

    @Override
    public AMatrix subMatrix(int rowStart, int rows, int colStart, int cols) {
        return BroadcastVectorMatrix.wrap(this.vector.subVector(colStart, cols), rows);
    }

    @Override
    public AMatrix exactClone() {
        return BroadcastVectorMatrix.wrap(this.vector.exactClone(), this.rows);
    }

    @Override
    public double get(int row, int column) {
        this.checkRow(row);
        return this.vector.get(column);
    }

    @Override
    public double unsafeGet(int row, int column) {
        return this.vector.unsafeGet(column);
    }

    @Override
    public void set(int row, int column, double value) {
        throw new UnsupportedOperationException(ErrorMessages.immutable(this));
    }

    @Override
    public boolean isZero() {
        return this.vector.isZero();
    }
}

