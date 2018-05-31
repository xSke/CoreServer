/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.impl;

import mikera.arrayz.INDArray;
import mikera.matrixx.AMatrix;
import mikera.matrixx.Matrix;
import mikera.matrixx.impl.ARectangularMatrix;
import mikera.matrixx.impl.IFastColumns;
import mikera.matrixx.impl.IFastRows;
import mikera.matrixx.impl.RowMatrix;
import mikera.vectorz.AVector;
import mikera.vectorz.Op;
import mikera.vectorz.Vector;
import mikera.vectorz.util.ErrorMessages;

public class ColumnMatrix
extends ARectangularMatrix
implements IFastColumns,
IFastRows {
    private static final long serialVersionUID = -6040718921619985258L;
    private final AVector vector;

    public ColumnMatrix(AVector v) {
        super(v.length(), 1);
        this.vector = v;
    }

    public static ColumnMatrix wrap(AVector v) {
        return new ColumnMatrix(v);
    }

    @Override
    public boolean isFullyMutable() {
        return this.vector.isFullyMutable();
    }

    @Override
    public boolean isMutable() {
        return this.vector.isMutable();
    }

    @Override
    public boolean isZero() {
        return this.vector.isZero();
    }

    @Override
    public void copyColumnTo(int col, double[] dest, int destOffset) {
        if (col != 0) {
            throw new IndexOutOfBoundsException(ErrorMessages.invalidSlice(this, 1, col));
        }
        this.vector.getElements(dest, destOffset);
    }

    @Override
    public void copyRowTo(int row, double[] dest, int destOffset) {
        dest[destOffset] = this.vector.get(row);
    }

    @Override
    public void getElements(double[] data, int offset) {
        this.vector.getElements(data, offset);
    }

    @Override
    public void applyOp(Op op) {
        this.vector.applyOp(op);
    }

    @Override
    public void multiply(double factor) {
        this.vector.scale(factor);
    }

    @Override
    public double elementSum() {
        return this.vector.elementSum();
    }

    @Override
    public double elementMin() {
        return this.vector.elementMin();
    }

    @Override
    public double elementMax() {
        return this.vector.elementMax();
    }

    @Override
    public long nonZeroCount() {
        return this.vector.nonZeroCount();
    }

    @Override
    public double get(int row, int column) {
        if (column != 0) {
            throw new IndexOutOfBoundsException(ErrorMessages.position(row, column));
        }
        return this.vector.get(row);
    }

    @Override
    public void set(int row, int column, double value) {
        if (column != 0) {
            throw new IndexOutOfBoundsException(ErrorMessages.position(row, column));
        }
        this.vector.set(row, value);
    }

    @Override
    public double unsafeGet(int row, int column) {
        return this.vector.unsafeGet(row);
    }

    @Override
    public void unsafeSet(int row, int column, double value) {
        this.vector.unsafeSet(row, value);
    }

    @Override
    public void addAt(int i, int j, double d) {
        assert (j == 0);
        this.vector.addAt(i, d);
    }

    @Override
    public void addToArray(double[] data, int offset) {
        this.vector.addToArray(data, offset);
    }

    @Override
    public RowMatrix getTranspose() {
        return new RowMatrix(this.vector);
    }

    @Override
    public RowMatrix getTransposeView() {
        return new RowMatrix(this.vector);
    }

    @Override
    public AVector getColumn(int i) {
        if (i != 0) {
            throw new IndexOutOfBoundsException(ErrorMessages.invalidSlice(this, 1, i));
        }
        return this.vector;
    }

    @Override
    public AVector getRowView(int i) {
        return this.vector.subVector(i, 1);
    }

    @Override
    public Vector toVector() {
        return this.vector.toVector();
    }

    @Override
    public AVector asVector() {
        return this.vector;
    }

    @Override
    public ColumnMatrix exactClone() {
        return new ColumnMatrix(this.vector.exactClone());
    }

    @Override
    public boolean equalsArray(double[] data, int offset) {
        return this.vector.equalsArray(data, offset);
    }

    @Override
    public Matrix transposeInnerProduct(Matrix s) {
        Vector v = this.vector.innerProduct(s).toVector();
        return Matrix.wrap(1, s.columnCount(), v.asDoubleArray());
    }

    @Override
    public boolean hasUncountable() {
        return this.vector.hasUncountable();
    }

    @Override
    public double elementPowSum(double p) {
        return this.vector.elementPowSum(p);
    }

    @Override
    public double elementAbsPowSum(double p) {
        return this.vector.elementAbsPowSum(p);
    }
}

