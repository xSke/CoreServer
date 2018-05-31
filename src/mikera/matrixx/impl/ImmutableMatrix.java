/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.impl;

import java.nio.DoubleBuffer;
import mikera.arrayz.INDArray;
import mikera.arrayz.impl.IDenseArray;
import mikera.matrixx.AMatrix;
import mikera.matrixx.Matrix;
import mikera.matrixx.impl.ARectangularMatrix;
import mikera.matrixx.impl.IFastRows;
import mikera.vectorz.AVector;
import mikera.vectorz.Vector;
import mikera.vectorz.impl.ImmutableVector;
import mikera.vectorz.util.DoubleArrays;
import mikera.vectorz.util.ErrorMessages;

public final class ImmutableMatrix
extends ARectangularMatrix
implements IDenseArray,
IFastRows {
    private static final long serialVersionUID = 2848013010449128820L;
    private final double[] data;

    private ImmutableMatrix(int rows, int cols, double[] data) {
        super(rows, cols);
        this.data = data;
    }

    public ImmutableMatrix(AMatrix m) {
        super(m.rowCount(), m.columnCount());
        this.data = m.toDoubleArray();
    }

    public static ImmutableMatrix wrap(Matrix source) {
        double[] data = source.data;
        return new ImmutableMatrix(source.rowCount(), source.columnCount(), data);
    }

    public static ImmutableMatrix wrap(int rows, int cols, double[] data) {
        return new ImmutableMatrix(rows, cols, data);
    }

    private Matrix asMatrix() {
        return Matrix.wrap(this.rows, this.cols, this.data);
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public boolean isFullyMutable() {
        return false;
    }

    @Override
    public boolean isView() {
        return false;
    }

    @Override
    public long nonZeroCount() {
        return DoubleArrays.nonZeroCount(this.data);
    }

    @Override
    public boolean isBoolean() {
        return DoubleArrays.isBoolean(this.data, 0, this.data.length);
    }

    @Override
    public boolean isZero() {
        return DoubleArrays.isZero(this.data, 0, this.data.length);
    }

    @Override
    public double get(int i, int j) {
        this.checkIndex(i, j);
        return this.unsafeGet(i, j);
    }

    @Override
    public ImmutableVector getRowView(int row) {
        this.checkRow(row);
        return ImmutableVector.wrap(this.data, row * this.cols, this.cols);
    }

    @Override
    public double unsafeGet(int i, int j) {
        return this.data[i * this.cols + j];
    }

    @Override
    public void set(int row, int column, double value) {
        throw new UnsupportedOperationException(ErrorMessages.immutable(this));
    }

    @Override
    public final void copyRowTo(int row, double[] dest, int destOffset) {
        int srcOffset = row * this.cols;
        System.arraycopy(this.data, srcOffset, dest, destOffset, this.cols);
    }

    @Override
    public Vector innerProduct(AVector a) {
        return this.asMatrix().transform(a);
    }

    @Override
    public Vector transform(AVector a) {
        return this.asMatrix().transform(a);
    }

    @Override
    public void transform(Vector source, Vector dest) {
        this.asMatrix().transform(source, dest);
    }

    @Override
    public final void copyColumnTo(int col, double[] dest, int destOffset) {
        int colOffset = col;
        for (int i = 0; i < this.rows; ++i) {
            dest[destOffset + i] = this.data[colOffset + i * this.cols];
        }
    }

    @Override
    public double elementSum() {
        return DoubleArrays.elementSum(this.data);
    }

    @Override
    public double elementSquaredSum() {
        return DoubleArrays.elementSquaredSum(this.data);
    }

    @Override
    public Vector toVector() {
        return Vector.create(this.data);
    }

    @Override
    public Matrix toMatrix() {
        return Matrix.wrap(this.rows, this.cols, DoubleArrays.copyOf(this.data));
    }

    @Override
    public Matrix toMatrixTranspose() {
        Matrix m = Matrix.create(this.cols, this.rows);
        for (int j = 0; j < this.cols; ++j) {
            this.copyColumnTo(j, m.data, this.rows * j);
        }
        return m;
    }

    @Override
    public void toDoubleBuffer(DoubleBuffer dest) {
        dest.put(this.data);
    }

    @Override
    public void getElements(double[] dest, int offset) {
        System.arraycopy(this.data, 0, dest, offset, this.data.length);
    }

    @Override
    public void addToArray(double[] data, int offset) {
        DoubleArrays.add(this.data, 0, data, offset, this.rows * this.cols);
    }

    @Override
    public Matrix clone() {
        return Matrix.create(this);
    }

    @Override
    public AMatrix exactClone() {
        return new ImmutableMatrix(this);
    }

    @Override
    public boolean equals(AMatrix a) {
        if (!this.isSameShape(a)) {
            return false;
        }
        return a.equalsArray(this.data, 0);
    }

    @Override
    public boolean equalsArray(double[] data, int offset) {
        return DoubleArrays.equals(this.data, 0, data, offset, this.rows * this.cols);
    }

    public double[] getInternalData() {
        return this.data;
    }

    public static ImmutableMatrix create(AMatrix a) {
        int rows = a.rowCount();
        int cols = a.columnCount();
        return ImmutableMatrix.wrap(rows, cols, a.getElements());
    }

    @Override
    public double[] getArray() {
        return this.data;
    }

    @Override
    public int getArrayOffset() {
        return 0;
    }
}

