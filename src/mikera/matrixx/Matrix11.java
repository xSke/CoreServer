/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx;

import mikera.arrayz.INDArray;
import mikera.matrixx.AMatrix;
import mikera.matrixx.impl.APrimitiveMatrix;
import mikera.vectorz.AVector;
import mikera.vectorz.Vector1;
import mikera.vectorz.util.ErrorMessages;

public final class Matrix11
extends APrimitiveMatrix {
    private static final long serialVersionUID = -1961422159148368299L;
    private double value;

    public Matrix11() {
        this(0.0);
    }

    public Matrix11(double value) {
        this.value = value;
    }

    public Matrix11(AMatrix m) {
        this.value = m.unsafeGet(0, 0);
    }

    @Override
    public int rowCount() {
        return 1;
    }

    @Override
    public int columnCount() {
        return 1;
    }

    @Override
    public int checkSquare() {
        return 1;
    }

    @Override
    public double determinant() {
        return this.value;
    }

    @Override
    public double elementSum() {
        return this.value;
    }

    @Override
    public double elementMax() {
        return this.value;
    }

    @Override
    public double elementMin() {
        return this.value;
    }

    @Override
    public double elementSquaredSum() {
        return this.value * this.value;
    }

    @Override
    public long nonZeroCount() {
        return this.value == 0.0 ? 0L : 1L;
    }

    @Override
    public long elementCount() {
        return 1L;
    }

    @Override
    public boolean isDiagonal() {
        return true;
    }

    @Override
    public boolean isZero() {
        return this.value == 0.0;
    }

    @Override
    public boolean isIdentity() {
        return this.value == 1.0;
    }

    @Override
    public Matrix11 inverse() {
        if (this.value == 0.0) {
            return null;
        }
        return new Matrix11(1.0 / this.value);
    }

    @Override
    public double trace() {
        return this.value;
    }

    @Override
    public double get(int row, int column) {
        if (row != 0 || column != 0) {
            throw new IndexOutOfBoundsException(ErrorMessages.invalidIndex((INDArray)this, row, column));
        }
        return this.value;
    }

    @Override
    public void set(int row, int column, double value) {
        if (row != 0 || column != 0) {
            throw new IndexOutOfBoundsException(ErrorMessages.invalidIndex((INDArray)this, row, column));
        }
        this.value = value;
    }

    @Override
    public double unsafeGet(int row, int column) {
        return this.value;
    }

    @Override
    public void unsafeSet(int row, int column, double value) {
        this.value = value;
    }

    @Override
    public void addAt(int i, int j, double value) {
        this.value += value;
    }

    @Override
    public void multiply(double factor) {
        this.value *= factor;
    }

    @Override
    public Vector1 getRowClone(int row) {
        switch (row) {
            case 0: {
                return Vector1.of(this.value);
            }
        }
        throw new IndexOutOfBoundsException(ErrorMessages.invalidSlice(this, row));
    }

    @Override
    public Vector1 getColumnClone(int column) {
        switch (column) {
            case 0: {
                return Vector1.of(this.value);
            }
        }
        throw new IndexOutOfBoundsException(ErrorMessages.invalidSlice(this, column));
    }

    @Override
    public void getElements(double[] data, int offset) {
        data[offset] = this.value;
    }

    @Override
    public void copyRowTo(int row, double[] dest, int destOffset) {
        dest[destOffset] = this.value;
    }

    @Override
    public void copyColumnTo(int col, double[] dest, int destOffset) {
        dest[destOffset] = this.value;
    }

    @Override
    public AMatrix exactClone() {
        return new Matrix11(this.value);
    }

    @Override
    public boolean equalsArray(double[] data, int offset) {
        return data[offset] == this.value;
    }

    @Override
    public double[] toDoubleArray() {
        return new double[]{this.value};
    }
}

