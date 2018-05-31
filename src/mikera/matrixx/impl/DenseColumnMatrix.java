/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.impl;

import java.util.Arrays;
import mikera.arrayz.INDArray;
import mikera.arrayz.impl.IDense;
import mikera.matrixx.AMatrix;
import mikera.matrixx.Matrix;
import mikera.matrixx.impl.AStridedMatrix;
import mikera.matrixx.impl.IFastColumns;
import mikera.vectorz.AVector;
import mikera.vectorz.Op;
import mikera.vectorz.impl.AStridedVector;
import mikera.vectorz.impl.ArraySubVector;
import mikera.vectorz.util.DoubleArrays;

public class DenseColumnMatrix
extends AStridedMatrix
implements IFastColumns,
IDense {
    private static final long serialVersionUID = 5459617932072332096L;

    private DenseColumnMatrix(int rowCount, int columnCount, double[] data) {
        super(data, rowCount, columnCount);
    }

    private DenseColumnMatrix(int rowCount, int columnCount) {
        this(rowCount, columnCount, Matrix.createStorage(rowCount, columnCount));
    }

    public static DenseColumnMatrix wrap(int rows, int cols, double[] data) {
        return new DenseColumnMatrix(rows, cols, data);
    }

    @Override
    public int getArrayOffset() {
        return 0;
    }

    @Override
    public int rowStride() {
        return 1;
    }

    @Override
    public int columnStride() {
        return this.rows;
    }

    @Override
    public ArraySubVector getColumnView(int j) {
        return ArraySubVector.wrap(this.data, j * this.rows, this.rows);
    }

    @Override
    public void copyRowTo(int i, double[] dest, int destOffset) {
        for (int j = 0; j < this.cols; ++j) {
            dest[destOffset + j] = this.data[i + j * this.rows];
        }
    }

    @Override
    public void copyColumnTo(int j, double[] dest, int destOffset) {
        System.arraycopy(this.data, j * this.rows, dest, destOffset, this.rows);
    }

    @Override
    public void setRow(int i, AVector row) {
        int cc = this.checkColumnCount(row.length());
        int j = 0;
        while (j < cc) {
            this.data[this.index((int)i, (int)j)] = row.unsafeGet(i);
            ++i;
        }
    }

    @Override
    public void setColumn(int j, AVector col) {
        int rc = this.checkRowCount(col.length());
        col.getElements(this.data, j * rc);
    }

    @Override
    public void addMultiple(AMatrix m, double factor) {
        this.checkRowCount(m.rowCount());
        int cc = this.checkColumnCount(m.columnCount());
        for (int i = 0; i < cc; ++i) {
            this.getColumnView(i).addMultiple(m.getColumn(i), factor);
        }
    }

    @Override
    protected int index(int i, int j) {
        return i + j * this.rows;
    }

    @Override
    public double get(int i, int j) {
        this.checkRow(i);
        return this.data[j * this.rows + i];
    }

    @Override
    public void set(int i, int j, double value) {
        this.checkRow(i);
        this.data[j * this.rows + i] = value;
    }

    @Override
    public void unsafeSet(int i, int j, double value) {
        this.data[j * this.rows + i] = value;
    }

    @Override
    public double unsafeGet(int i, int j) {
        return this.data[j * this.rows + i];
    }

    @Override
    public void addAt(int i, int j, double d) {
        double[] arrd = this.data;
        int n = j * this.rows + i;
        arrd[n] = arrd[n] + d;
    }

    @Override
    public boolean isFullyMutable() {
        return true;
    }

    @Override
    public boolean isPackedArray() {
        return this.cols <= 1;
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
    public double elementSum() {
        return DoubleArrays.elementSum(this.data);
    }

    @Override
    public double elementSquaredSum() {
        return DoubleArrays.elementSquaredSum(this.data);
    }

    @Override
    public double elementMax() {
        return DoubleArrays.elementMax(this.data);
    }

    @Override
    public double elementMin() {
        return DoubleArrays.elementMin(this.data);
    }

    @Override
    public void abs() {
        DoubleArrays.abs(this.data);
    }

    @Override
    public void signum() {
        DoubleArrays.signum(this.data);
    }

    @Override
    public void square() {
        DoubleArrays.square(this.data);
    }

    @Override
    public void exp() {
        DoubleArrays.exp(this.data);
    }

    @Override
    public void log() {
        DoubleArrays.log(this.data);
    }

    @Override
    public void applyOp(Op op) {
        op.applyTo(this.data);
    }

    @Override
    public long nonZeroCount() {
        return DoubleArrays.nonZeroCount(this.data);
    }

    @Override
    public void add(double d) {
        DoubleArrays.add(this.data, d);
    }

    @Override
    public void multiply(double factor) {
        DoubleArrays.multiply(this.data, factor);
    }

    @Override
    public void set(double value) {
        Arrays.fill(this.data, value);
    }

    @Override
    public void reciprocal() {
        DoubleArrays.reciprocal(this.data, 0, this.data.length);
    }

    @Override
    public void clamp(double min, double max) {
        DoubleArrays.clamp(this.data, 0, this.data.length, min, max);
    }

    @Override
    public Matrix getTranspose() {
        return this.getTransposeView();
    }

    @Override
    public Matrix getTransposeView() {
        return Matrix.wrap(this.cols, this.rows, this.data);
    }

    @Override
    public DenseColumnMatrix exactClone() {
        return new DenseColumnMatrix(this.rows, this.cols, (double[])this.data.clone());
    }

    @Override
    public DenseColumnMatrix dense() {
        return this;
    }

    @Override
    public DenseColumnMatrix copy() {
        return this.exactClone();
    }

    @Override
    public DenseColumnMatrix clone() {
        return this.exactClone();
    }

    @Override
    public Matrix toMatrixTranspose() {
        return Matrix.wrap(this.cols, this.rows, this.data);
    }
}

