/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.impl;

import java.nio.Buffer;
import java.nio.DoubleBuffer;
import mikera.arrayz.INDArray;
import mikera.indexz.Index;
import mikera.matrixx.AMatrix;
import mikera.matrixx.impl.ARectangularMatrix;
import mikera.vectorz.AVector;
import mikera.vectorz.impl.BufferVector;

public class BufferMatrix
extends ARectangularMatrix {
    private static final long serialVersionUID = 2933979132279936135L;
    final DoubleBuffer buffer;

    protected BufferMatrix(int rows, int cols) {
        this(DoubleBuffer.allocate(rows * cols), rows, cols);
    }

    protected BufferMatrix(DoubleBuffer buf, int rows, int cols) {
        super(rows, cols);
        this.buffer = buf;
    }

    public static BufferMatrix wrap(double[] source, int rows, int cols) {
        if (source.length != rows * cols) {
            throw new IllegalArgumentException("Wrong array size for matrix of shape " + Index.of(rows, cols));
        }
        return new BufferMatrix(DoubleBuffer.wrap(source), rows, cols);
    }

    public static BufferMatrix wrap(DoubleBuffer source, int rows, int cols) {
        return new BufferMatrix(source, rows, cols);
    }

    public static AMatrix create(AMatrix m) {
        return BufferMatrix.wrap(m.toDoubleArray(), m.rowCount(), m.columnCount());
    }

    @Override
    public double get(int i, int j) {
        this.checkColumn(j);
        return this.buffer.get(i * this.cols + j);
    }

    @Override
    public void set(int i, int j, double value) {
        this.checkColumn(j);
        this.buffer.put(i * this.cols + j, value);
    }

    @Override
    public double unsafeGet(int i, int j) {
        return this.buffer.get(i * this.cols + j);
    }

    @Override
    public void unsafeSet(int i, int j, double value) {
        this.buffer.put(i * this.cols + j, value);
    }

    @Override
    public BufferVector getRowView(int i) {
        int cols = this.cols;
        int t = i * cols;
        this.buffer.position(t);
        this.buffer.limit(t + cols);
        DoubleBuffer subBuffer = this.buffer.slice();
        this.buffer.clear();
        return BufferVector.wrap(subBuffer, cols);
    }

    @Override
    public BufferVector asVector() {
        return BufferVector.wrap(this.buffer.duplicate(), this.rows * this.cols);
    }

    @Override
    public boolean isFullyMutable() {
        return true;
    }

    @Override
    public BufferMatrix clone() {
        return this.exactClone();
    }

    @Override
    public BufferMatrix exactClone() {
        int ec = this.buffer.capacity();
        double[] newArray = new double[ec];
        this.buffer.get(newArray);
        this.buffer.clear();
        return BufferMatrix.wrap(newArray, this.rows, this.cols);
    }

    @Override
    public boolean isZero() {
        return this.asVector().isZero();
    }
}

