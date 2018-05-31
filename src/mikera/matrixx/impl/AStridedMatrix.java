/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.impl;

import java.util.Iterator;
import mikera.arrayz.INDArray;
import mikera.arrayz.impl.IStridedArray;
import mikera.matrixx.AMatrix;
import mikera.matrixx.Matrixx;
import mikera.matrixx.impl.AArrayMatrix;
import mikera.matrixx.impl.StridedElementIterator;
import mikera.matrixx.impl.StridedMatrix;
import mikera.vectorz.AVector;
import mikera.vectorz.Op;
import mikera.vectorz.Vectorz;
import mikera.vectorz.impl.AStridedVector;
import mikera.vectorz.util.ErrorMessages;

public abstract class AStridedMatrix
extends AArrayMatrix
implements IStridedArray {
    private static final long serialVersionUID = -8908577438753599161L;

    protected AStridedMatrix(double[] data, int rows, int cols) {
        super(data, rows, cols);
    }

    @Override
    public abstract int getArrayOffset();

    public abstract int rowStride();

    public abstract int columnStride();

    @Override
    public AStridedMatrix subMatrix(int rowStart, int rowCount, int colStart, int colCount) {
        if (rowStart < 0 || rowStart >= this.rows || colStart < 0 || colStart >= this.cols) {
            throw new IndexOutOfBoundsException(ErrorMessages.position(rowStart, colStart));
        }
        if (rowStart + rowCount > this.rows || colStart + colCount > this.cols) {
            throw new IndexOutOfBoundsException(ErrorMessages.position(rowStart + rowCount, colStart + colCount));
        }
        int rowStride = this.rowStride();
        int colStride = this.columnStride();
        int offset = this.getArrayOffset();
        return StridedMatrix.wrap(this.data, rowCount, colCount, offset + rowStart * rowStride + colStart * colStride, rowStride, colStride);
    }

    @Override
    public AStridedVector getRowView(int i) {
        return Vectorz.wrapStrided(this.data, this.getArrayOffset() + i * this.rowStride(), this.cols, this.columnStride());
    }

    @Override
    public double diagonalProduct() {
        int n = Math.min(this.rowCount(), this.columnCount());
        int offset = this.getArrayOffset();
        int st = this.rowStride() + this.columnStride();
        double[] data = this.getArray();
        double result = 1.0;
        for (int i = 0; i < n; ++i) {
            result *= data[offset];
            offset += st;
        }
        return result;
    }

    @Override
    public double trace() {
        int n = Math.min(this.rowCount(), this.columnCount());
        int offset = this.getArrayOffset();
        int st = this.rowStride() + this.columnStride();
        double[] data = this.getArray();
        double result = 0.0;
        for (int i = 0; i < n; ++i) {
            result += data[offset];
            offset += st;
        }
        return result;
    }

    @Override
    public AStridedVector getColumnView(int i) {
        return Vectorz.wrapStrided(this.data, this.getArrayOffset() + i * this.columnStride(), this.rows, this.rowStride());
    }

    @Override
    public AStridedVector getBand(int i) {
        int cs = this.columnStride();
        int rs = this.rowStride();
        if (i > this.cols || i < - this.rows) {
            throw new IndexOutOfBoundsException(ErrorMessages.invalidBand(this, i));
        }
        return Vectorz.wrapStrided(this.data, this.getArrayOffset() + this.bandStartColumn(i) * cs + this.bandStartRow(i) * rs, this.bandLength(i), rs + cs);
    }

    @Override
    public void add(AVector v) {
        this.checkColumnCount(v.length());
        int offset = this.getArrayOffset();
        int colStride = this.columnStride();
        int rowStride = this.rowStride();
        for (int i = 0; i < this.rows; ++i) {
            v.addToArray(this.data, offset + i * rowStride, colStride);
        }
    }

    @Override
    public void addToArray(double[] dest, int destOffset) {
        int offset = this.getArrayOffset();
        int colStride = this.columnStride();
        int rowStride = this.rowStride();
        for (int i = 0; i < this.rows; ++i) {
            int ro = offset + i * rowStride;
            for (int j = 0; j < this.cols; ++j) {
                double[] arrd = dest;
                int n = destOffset++;
                arrd[n] = arrd[n] + this.data[ro + j * colStride];
            }
        }
    }

    @Override
    public void applyOp(Op op) {
        int offset = this.getArrayOffset();
        int colStride = this.columnStride();
        int rowStride = this.rowStride();
        for (int i = 0; i < this.rows; ++i) {
            int ro = offset + i * rowStride;
            for (int j = 0; j < this.cols; ++j) {
                int ix = ro + j * colStride;
                this.data[ix] = op.apply(this.data[ix]);
            }
        }
    }

    @Override
    public void add(AMatrix m) {
        this.checkSameShape(m);
        int offset = this.getArrayOffset();
        int colStride = this.columnStride();
        int rowStride = this.rowStride();
        for (int i = 0; i < this.rows; ++i) {
            m.getRow(i).addToArray(this.data, offset + i * rowStride, colStride);
        }
    }

    @Override
    public abstract void copyRowTo(int var1, double[] var2, int var3);

    @Override
    public abstract void copyColumnTo(int var1, double[] var2, int var3);

    @Override
    public int[] getStrides() {
        return new int[]{this.rowStride(), this.columnStride()};
    }

    @Override
    public int getStride(int dimension) {
        switch (dimension) {
            case 0: {
                return this.rowStride();
            }
            case 1: {
                return this.columnStride();
            }
        }
        throw new IllegalArgumentException(ErrorMessages.invalidDimension(this, dimension));
    }

    @Override
    public Iterator<Double> elementIterator() {
        return new StridedElementIterator(this);
    }

    @Override
    public AMatrix getTranspose() {
        return this.getTransposeView();
    }

    @Override
    public AMatrix getTransposeView() {
        return Matrixx.wrapStrided(this.getArray(), this.columnCount(), this.rowCount(), this.getArrayOffset(), this.columnStride(), this.rowStride());
    }

    @Override
    public boolean isPackedArray() {
        return this.getArrayOffset() == 0 && this.columnStride() == 1 && this.rowStride() == this.columnCount() && (long)this.getArray().length == this.elementCount();
    }

    @Override
    public double[] asDoubleArray() {
        if (this.isPackedArray()) {
            return this.getArray();
        }
        return null;
    }

    @Override
    public boolean isZero() {
        if (this.rowStride() > this.columnStride()) {
            int rc = this.rowCount();
            for (int i = 0; i < rc; ++i) {
                if (this.getRow(i).isZero()) continue;
                return false;
            }
        } else {
            int cc = this.columnCount();
            for (int i = 0; i < cc; ++i) {
                if (this.getColumn(i).isZero()) continue;
                return false;
            }
        }
        return true;
    }
}

