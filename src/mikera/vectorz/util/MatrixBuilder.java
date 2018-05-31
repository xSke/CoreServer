/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz.util;

import mikera.arrayz.INDArray;
import mikera.matrixx.AMatrix;
import mikera.vectorz.AVector;
import mikera.vectorz.Vectorz;
import mikera.vectorz.util.ErrorMessages;

public class MatrixBuilder
extends AMatrix {
    private static final long serialVersionUID = -5875133722867126330L;
    private AVector[] data = new AVector[4];
    int length = 0;

    private void ensureSize(int newSize) {
        if (newSize > this.data.length) {
            AVector[] nd = new AVector[java.lang.Math.min(newSize, this.data.length * 2)];
            System.arraycopy(this.data, 0, nd, 0, this.length);
            this.data = nd;
        }
    }

    public void append(Iterable<Object> d) {
        this.ensureSize(this.length + 1);
        this.data[this.length++] = Vectorz.create(d);
    }

    public void append(AVector v) {
        this.ensureSize(this.length + 1);
        this.data[this.length++] = Vectorz.create(v);
    }

    public void append(double[] ds) {
        this.ensureSize(this.length + 1);
        this.data[this.length++] = Vectorz.create(ds);
    }

    public void appendRow(AVector row) {
        this.append(row);
    }

    @Override
    public void replaceRow(int i, AVector row) {
        if (i < 0 || i >= this.length) {
            throw new IndexOutOfBoundsException(ErrorMessages.invalidSlice(this, i));
        }
        this.data[i] = row;
    }

    @Override
    public AVector getRowView(int row) {
        if (row < 0 || row >= this.length) {
            throw new IndexOutOfBoundsException(ErrorMessages.invalidSlice(this, row));
        }
        return this.data[row];
    }

    @Override
    public int rowCount() {
        return this.length;
    }

    @Override
    public int columnCount() {
        return this.data[0].length();
    }

    @Override
    public AMatrix exactClone() {
        MatrixBuilder mb = new MatrixBuilder();
        for (int i = 0; i < this.length; ++i) {
            mb.append(this.data[i].exactClone());
        }
        return mb;
    }

    @Override
    public double get(int row, int column) {
        this.checkIndex(row, column);
        return this.data[row].get(column);
    }

    @Override
    public void set(int row, int column, double value) {
        this.checkIndex(row, column);
        this.data[row].set(column, value);
    }

    @Override
    public boolean isFullyMutable() {
        for (int i = 0; i < this.rowCount(); ++i) {
            if (this.data[i].isFullyMutable()) continue;
            return false;
        }
        return true;
    }
}

