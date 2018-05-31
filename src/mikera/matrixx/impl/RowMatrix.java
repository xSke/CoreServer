/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.impl;

import mikera.arrayz.INDArray;
import mikera.matrixx.AMatrix;
import mikera.matrixx.Matrix;
import mikera.matrixx.impl.ARectangularMatrix;
import mikera.matrixx.impl.ColumnMatrix;
import mikera.matrixx.impl.IFastColumns;
import mikera.matrixx.impl.IFastRows;
import mikera.vectorz.AVector;
import mikera.vectorz.Op;
import mikera.vectorz.Vector;
import mikera.vectorz.util.DoubleArrays;
import mikera.vectorz.util.ErrorMessages;

public class RowMatrix
extends ARectangularMatrix
implements IFastColumns,
IFastRows {
    private static final long serialVersionUID = 2636365975400418264L;
    private final AVector vector;

    public RowMatrix(AVector v) {
        super(1, v.length());
        this.vector = v;
    }

    public static RowMatrix wrap(AVector v) {
        return new RowMatrix(v);
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
    public Vector toVector() {
        return this.vector.toVector();
    }

    @Override
    public AVector asVector() {
        return this.vector;
    }

    @Override
    public void multiply(double factor) {
        this.vector.scale(factor);
    }

    @Override
    public void applyOp(Op op) {
        this.vector.applyOp(op);
    }

    @Override
    public double elementSum() {
        return this.vector.elementSum();
    }

    @Override
    public double elementSquaredSum() {
        return this.vector.elementSquaredSum();
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
        if (row != 0) {
            throw new IndexOutOfBoundsException(ErrorMessages.invalidIndex((INDArray)this, row, column));
        }
        return this.vector.get(column);
    }

    @Override
    public void set(int row, int column, double value) {
        if (row != 0) {
            throw new IndexOutOfBoundsException(ErrorMessages.invalidIndex((INDArray)this, row, column));
        }
        this.vector.set(column, value);
    }

    @Override
    public double unsafeGet(int row, int column) {
        return this.vector.unsafeGet(column);
    }

    @Override
    public void unsafeSet(int row, int column, double value) {
        this.vector.unsafeSet(column, value);
    }

    @Override
    public AVector getRowView(int i) {
        if (i != 0) {
            throw new IndexOutOfBoundsException(ErrorMessages.invalidSlice(this, i));
        }
        return this.vector;
    }

    @Override
    public AVector getColumnView(int i) {
        return this.vector.subVector(i, 1);
    }

    @Override
    public void addAt(int i, int j, double d) {
        assert (i == 0);
        this.vector.addAt(j, d);
    }

    @Override
    public void addToArray(double[] data, int offset) {
        this.vector.addToArray(data, offset);
    }

    @Override
    public ColumnMatrix getTranspose() {
        return new ColumnMatrix(this.vector);
    }

    @Override
    public ColumnMatrix getTransposeView() {
        return new ColumnMatrix(this.vector);
    }

    @Override
    public RowMatrix exactClone() {
        return new RowMatrix(this.vector.exactClone());
    }

    @Override
    public boolean equalsArray(double[] data, int offset) {
        return this.vector.equalsArray(data, offset);
    }

    @Override
    public void copyRowTo(int row, double[] dest, int destOffset) {
        if (row != 0) {
            throw new IndexOutOfBoundsException(ErrorMessages.invalidSlice(this, row));
        }
        this.vector.getElements(dest, destOffset);
    }

    @Override
    public void copyColumnTo(int col, double[] dest, int destOffset) {
        dest[destOffset] = this.vector.get(col);
    }

    @Override
    public void getElements(double[] data, int offset) {
        this.vector.getElements(data, offset);
    }

    @Override
    public Matrix transposeInnerProduct(Matrix s) {
        if (s.rowCount() != 1) {
            throw new IllegalArgumentException(ErrorMessages.incompatibleShapes(this, s));
        }
        int rc = this.columnCount();
        int cc = s.columnCount();
        Matrix m = Matrix.create(rc, cc);
        for (int i = 0; i < rc; ++i) {
            double ti = this.vector.unsafeGet(i);
            DoubleArrays.addMultiple(m.data, i * cc, s.data, 0, cc, ti);
        }
        return m;
    }
}

