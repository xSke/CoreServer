/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.impl;

import mikera.arrayz.INDArray;
import mikera.matrixx.AMatrix;
import mikera.matrixx.Matrix;
import mikera.matrixx.Matrixx;
import mikera.matrixx.impl.ADenseArrayMatrix;
import mikera.matrixx.impl.AStridedMatrix;
import mikera.vectorz.AVector;
import mikera.vectorz.Op;
import mikera.vectorz.Vector;
import mikera.vectorz.Vectorz;
import mikera.vectorz.impl.AStridedVector;
import mikera.vectorz.util.ErrorMessages;
import mikera.vectorz.util.VectorzException;

public final class StridedMatrix
extends AStridedMatrix {
    private static final long serialVersionUID = -7928115802247422177L;
    private final int rowStride;
    private final int colStride;
    private final int offset;

    private StridedMatrix(double[] data, int rowCount, int columnCount, int offset, int rowStride, int columnStride) {
        super(data, rowCount, columnCount);
        this.offset = offset;
        this.rowStride = rowStride;
        this.colStride = columnStride;
    }

    public static StridedMatrix create(int rowCount, int columnCount) {
        double[] data = new double[rowCount * columnCount];
        return new StridedMatrix(data, rowCount, columnCount, 0, columnCount, 1);
    }

    @Override
    public boolean isFullyMutable() {
        return true;
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public AStridedVector getRowView(int i) {
        return Vectorz.wrapStrided(this.data, this.offset + i * this.rowStride, this.cols, this.colStride);
    }

    @Override
    public AStridedVector getColumnView(int i) {
        return Vectorz.wrapStrided(this.data, this.offset + i * this.colStride, this.rows, this.rowStride);
    }

    @Override
    public void copyRowTo(int row, double[] dest, int destOffset) {
        int rowOffset = this.offset + row * this.rowStride;
        for (int i = 0; i < this.cols; ++i) {
            dest[destOffset + i] = this.data[rowOffset + i * this.colStride];
        }
    }

    @Override
    public void copyColumnTo(int col, double[] dest, int destOffset) {
        int colOffset = this.offset + col * this.colStride;
        for (int i = 0; i < this.rows; ++i) {
            dest[destOffset + i] = this.data[colOffset + i * this.rowStride];
        }
    }

    @Override
    public int rowStride() {
        return this.rowStride;
    }

    @Override
    public int columnStride() {
        return this.colStride;
    }

    @Override
    public int getArrayOffset() {
        return this.offset;
    }

    @Override
    public boolean isPackedArray() {
        return this.offset == 0 && this.colStride == 1 && this.rowStride == this.cols && this.data.length == this.rows * this.cols;
    }

    @Override
    public AStridedMatrix subMatrix(int rowStart, int rowCount, int colStart, int colCount) {
        if (rowStart < 0 || rowStart >= this.rows || colStart < 0 || colStart >= this.cols) {
            throw new IndexOutOfBoundsException(ErrorMessages.position(rowStart, colStart));
        }
        if (rowStart + rowCount > this.rows || colStart + colCount > this.cols) {
            throw new IndexOutOfBoundsException(ErrorMessages.position(rowStart + rowCount, colStart + colCount));
        }
        return new StridedMatrix(this.data, rowCount, colCount, this.offset + rowStart * this.rowStride + colStart * this.colStride, this.rowStride, this.colStride);
    }

    @Override
    public void applyOp(Op op) {
        int rc = this.rowCount();
        int cc = this.columnCount();
        int o = this.offset;
        for (int row = 0; row < rc; ++row) {
            int ro = o + row * this.rowStride();
            for (int col = 0; col < cc; ++col) {
                int index = ro + col * this.colStride;
                double v = this.data[index];
                this.data[index] = op.apply(v);
            }
        }
    }

    @Override
    public void getElements(double[] dest, int destOffset) {
        int rc = this.rowCount();
        int cc = this.columnCount();
        for (int row = 0; row < rc; ++row) {
            this.copyRowTo(row, dest, destOffset + row * cc);
        }
    }

    @Override
    public AMatrix getTranspose() {
        return Matrixx.wrapStrided(this.data, this.cols, this.rows, this.offset, this.colStride, this.rowStride);
    }

    @Override
    public AMatrix getTransposeView() {
        return Matrixx.wrapStrided(this.data, this.cols, this.rows, this.offset, this.colStride, this.rowStride);
    }

    @Override
    public double get(int i, int j) {
        this.checkIndex(i, j);
        return this.data[this.index(i, j)];
    }

    @Override
    public double unsafeGet(int i, int j) {
        return this.data[this.index(i, j)];
    }

    @Override
    public AVector asVector() {
        if (this.isPackedArray()) {
            return Vector.wrap(this.data);
        }
        if (this.cols == 1) {
            return Vectorz.wrapStrided(this.data, this.offset, this.rows, this.rowStride);
        }
        if (this.rows == 1) {
            return Vectorz.wrapStrided(this.data, this.offset, this.cols, this.colStride);
        }
        return super.asVector();
    }

    @Override
    public void set(int i, int j, double value) {
        this.checkIndex(i, j);
        this.data[this.index((int)i, (int)j)] = value;
    }

    @Override
    public void unsafeSet(int i, int j, double value) {
        this.data[this.index((int)i, (int)j)] = value;
    }

    @Override
    public AMatrix exactClone() {
        return new StridedMatrix((double[])this.data.clone(), this.rows, this.cols, this.offset, this.rowStride, this.colStride);
    }

    public static StridedMatrix create(AMatrix m) {
        StridedMatrix sm = StridedMatrix.create(m.rowCount(), m.columnCount());
        sm.set(m);
        return sm;
    }

    public static StridedMatrix wrap(Matrix m) {
        return new StridedMatrix(m.data, m.rowCount(), m.columnCount(), 0, m.columnCount(), 1);
    }

    public static StridedMatrix wrap(double[] data, int rows, int columns, int offset, int rowStride, int columnStride) {
        return new StridedMatrix(data, rows, columns, offset, rowStride, columnStride);
    }

    @Override
    public void validate() {
        super.validate();
        if (!this.equals(this.exactClone())) {
            throw new VectorzException("Thing not equal to itself");
        }
        if (this.offset < 0) {
            throw new VectorzException("Negative offset! [" + this.offset + "]");
        }
        if (this.index(this.rows - 1, this.cols - 1) >= this.data.length) {
            throw new VectorzException("Negative offset! [" + this.offset + "]");
        }
    }

    @Override
    protected final int index(int row, int col) {
        return this.offset + row * this.rowStride + col * this.colStride;
    }

    @Override
    public Matrix clone() {
        return Matrix.create(this);
    }

    @Override
    public boolean equals(AMatrix a) {
        if (a == this) {
            return true;
        }
        if (a instanceof ADenseArrayMatrix) {
            return this.equals((ADenseArrayMatrix)a);
        }
        if (!this.isSameShape(a)) {
            return false;
        }
        for (int i = 0; i < this.rows; ++i) {
            for (int j = 0; j < this.cols; ++j) {
                if (this.data[this.index(i, j)] == a.unsafeGet(i, j)) continue;
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean equalsArray(double[] data, int offset) {
        for (int i = 0; i < this.rows; ++i) {
            int si = this.offset + i * this.rowStride;
            for (int j = 0; j < this.cols; ++j) {
                if (this.data[si] != data[offset++]) {
                    return false;
                }
                si += this.colStride;
            }
        }
        return true;
    }
}

