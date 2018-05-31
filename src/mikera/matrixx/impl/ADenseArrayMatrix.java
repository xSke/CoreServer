/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.impl;

import mikera.arrayz.INDArray;
import mikera.arrayz.impl.IDenseArray;
import mikera.matrixx.AMatrix;
import mikera.matrixx.impl.ARectangularMatrix;
import mikera.matrixx.impl.AStridedMatrix;
import mikera.matrixx.impl.IFastRows;
import mikera.vectorz.AVector;
import mikera.vectorz.Tools;
import mikera.vectorz.Vector;
import mikera.vectorz.Vectorz;
import mikera.vectorz.impl.ADenseArrayVector;
import mikera.vectorz.impl.AStridedVector;
import mikera.vectorz.util.DoubleArrays;
import mikera.vectorz.util.ErrorMessages;

public abstract class ADenseArrayMatrix
extends AStridedMatrix
implements IFastRows,
IDenseArray {
    private static final long serialVersionUID = -2144964424833585026L;

    protected ADenseArrayMatrix(double[] data, int rows, int cols) {
        super(data, rows, cols);
    }

    @Override
    public abstract int getArrayOffset();

    @Override
    public boolean isPackedArray() {
        return this.getArrayOffset() == 0 && this.data.length == this.rows * this.cols;
    }

    @Override
    public boolean isZero() {
        return DoubleArrays.isZero(this.data, this.getArrayOffset(), this.rows * this.cols);
    }

    @Override
    public boolean isUpperTriangular() {
        int rc = this.rowCount();
        int cc = this.columnCount();
        int offset = this.getArrayOffset();
        for (int i = 1; i < rc; ++i) {
            if (DoubleArrays.isZero(this.data, offset + i * cc, Math.min(cc, i))) continue;
            return false;
        }
        return true;
    }

    @Override
    public boolean isLowerTriangular() {
        int offset = this.getArrayOffset();
        int cc = this.columnCount();
        int testRows = Math.min(cc, this.rowCount());
        for (int i = 0; i < testRows; ++i) {
            if (!DoubleArrays.isZero(this.data, offset + i + 1, cc - i - 1)) {
                return false;
            }
            offset += cc;
        }
        return true;
    }

    @Override
    public int rowStride() {
        return this.cols;
    }

    @Override
    public int columnStride() {
        return 1;
    }

    @Override
    public double unsafeGet(int i, int j) {
        return this.data[this.index(i, j)];
    }

    @Override
    public void set(AVector v) {
        int rc = this.rowCount();
        if (v.length() != this.cols) {
            throw new IllegalArgumentException(ErrorMessages.incompatibleShapes(this, (INDArray)v));
        }
        double[] data = this.getArray();
        int offset = this.getArrayOffset();
        for (int i = 0; i < rc; ++i) {
            v.getElements(data, offset + i * this.cols);
        }
    }

    @Override
    public void set(AMatrix m) {
        if (!this.isSameShape(m)) {
            throw new IllegalArgumentException(ErrorMessages.incompatibleShapes(this, m));
        }
        double[] data = this.getArray();
        int offset = this.getArrayOffset();
        m.getElements(data, offset);
    }

    @Override
    public void setElements(double[] values, int offset) {
        double[] data = this.getArray();
        int di = this.getArrayOffset();
        System.arraycopy(values, offset, data, di, Tools.toInt(this.elementCount()));
    }

    @Override
    public void setElements(int pos, double[] values, int offset, int length) {
        double[] data = this.getArray();
        int di = this.getArrayOffset() + pos;
        System.arraycopy(values, offset, data, di, length);
    }

    @Override
    public ADenseArrayVector getRowView(int i) {
        return Vectorz.wrap(this.data, this.getArrayOffset() + i * this.cols, this.cols);
    }

    @Override
    public AStridedVector getColumnView(int i) {
        return Vectorz.wrapStrided(this.data, this.getArrayOffset() + i, this.rows, this.cols);
    }

    @Override
    public double elementSum() {
        return DoubleArrays.elementSum(this.data, this.getArrayOffset(), this.rows * this.cols);
    }

    @Override
    public double elementSquaredSum() {
        return DoubleArrays.elementSquaredSum(this.data, this.getArrayOffset(), this.rows * this.cols);
    }

    @Override
    public double elementMax() {
        return DoubleArrays.elementMax(this.data, this.getArrayOffset(), this.rows * this.cols);
    }

    @Override
    public double elementMin() {
        return DoubleArrays.elementMin(this.data, this.getArrayOffset(), this.rows * this.cols);
    }

    @Override
    public void copyRowTo(int row, double[] dest, int destOffset) {
        System.arraycopy(this.data, this.getArrayOffset() + row * this.cols, dest, destOffset, this.cols);
    }

    @Override
    public void unsafeSet(int i, int j, double value) {
        this.data[this.index((int)i, (int)j)] = value;
    }

    @Override
    protected int index(int row, int col) {
        return this.getArrayOffset() + row * this.cols + col;
    }

    @Override
    public void transform(AVector source, AVector dest) {
        if (source instanceof Vector && dest instanceof Vector) {
            this.transform((Vector)source, (Vector)dest);
            return;
        }
        if (this.rows != dest.length()) {
            throw new IllegalArgumentException(ErrorMessages.wrongDestLength(dest));
        }
        if (this.cols != source.length()) {
            throw new IllegalArgumentException(ErrorMessages.wrongSourceLength(source));
        }
        double[] data = this.getArray();
        int offset = this.getArrayOffset();
        for (int i = 0; i < this.rows; ++i) {
            dest.unsafeSet(i, source.dotProduct(data, offset + i * this.cols));
        }
    }

    @Override
    public void add(AVector v) {
        int rc = this.rowCount();
        int cc = this.columnCount();
        if (cc != v.length()) {
            throw new IllegalArgumentException(ErrorMessages.mismatch(this, v));
        }
        double[] data = this.getArray();
        int offset = this.getArrayOffset();
        for (int i = 0; i < rc; ++i) {
            v.addToArray(data, offset + i * cc);
        }
    }

    @Override
    public void add(AMatrix a) {
        if (!this.isSameShape(a)) {
            throw new IllegalArgumentException(ErrorMessages.incompatibleShapes(this, a));
        }
        a.addToArray(this.getArray(), this.getArrayOffset());
    }

    public void add(ADenseArrayMatrix a, ADenseArrayMatrix b) {
        this.checkSameShape(a);
        this.checkSameShape(b);
        DoubleArrays.add2(this.getArray(), this.getArrayOffset(), a.getArray(), a.getArrayOffset(), b.getArray(), b.getArrayOffset(), Tools.toInt(this.elementCount()));
    }

    @Override
    public void addToArray(double[] data, int offset) {
        DoubleArrays.add(this.getArray(), this.getArrayOffset(), data, offset, this.rows * this.cols);
    }

    @Override
    public boolean equals(AMatrix a) {
        if (!this.isSameShape(a)) {
            return false;
        }
        return a.equalsArray(this.getArray(), this.getArrayOffset());
    }

    @Override
    public boolean equals(INDArray a) {
        if (!this.isSameShape(a)) {
            return false;
        }
        return a.equalsArray(this.getArray(), this.getArrayOffset());
    }

    @Override
    public boolean equalsArray(double[] data, int offset) {
        return DoubleArrays.equals(this.getArray(), this.getArrayOffset(), data, offset, this.rows * this.cols);
    }

    @Override
    public boolean equals(ADenseArrayMatrix m) {
        if (!this.isSameShape(m)) {
            return false;
        }
        return DoubleArrays.equals(this.getArray(), this.getArrayOffset(), m.getArray(), m.getArrayOffset(), this.rows * this.cols);
    }
}

