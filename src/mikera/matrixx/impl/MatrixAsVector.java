/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.impl;

import java.util.List;
import mikera.arrayz.INDArray;
import mikera.matrixx.AMatrix;
import mikera.matrixx.impl.IFastRows;
import mikera.vectorz.AVector;
import mikera.vectorz.Op;
import mikera.vectorz.impl.AMatrixViewVector;

public class MatrixAsVector
extends AMatrixViewVector {
    protected final int rows;
    protected final int columns;

    public MatrixAsVector(AMatrix a) {
        super(a, a.rowCount() * a.columnCount());
        this.rows = a.rowCount();
        this.columns = a.columnCount();
    }

    @Override
    public double get(int i) {
        this.checkIndex(i);
        return this.source.unsafeGet(i / this.columns, i % this.columns);
    }

    @Override
    public void set(int i, double value) {
        this.checkIndex(i);
        this.source.unsafeSet(i / this.columns, i % this.columns, value);
    }

    @Override
    public double unsafeGet(int i) {
        return this.source.unsafeGet(i / this.columns, i % this.columns);
    }

    @Override
    public void unsafeSet(int i, double value) {
        this.source.unsafeSet(i / this.columns, i % this.columns, value);
    }

    @Override
    public AVector exactClone() {
        return new MatrixAsVector(this.source.exactClone());
    }

    @Override
    public boolean isFullyMutable() {
        return this.source.isFullyMutable();
    }

    @Override
    public boolean isMutable() {
        return this.source.isMutable();
    }

    @Override
    public boolean isView() {
        return true;
    }

    @Override
    public void set(double value) {
        this.source.set(value);
    }

    @Override
    public boolean isSparse() {
        return this.source.isSparse();
    }

    @Override
    public boolean isZero() {
        return this.source.isZero();
    }

    @Override
    public void getElements(double[] data, int offset) {
        this.source.getElements(data, offset);
    }

    @Override
    public List<Double> asElementList() {
        return this.source.asElementList();
    }

    @Override
    public void addToArray(double[] data, int offset) {
        this.source.addToArray(data, offset);
    }

    @Override
    public void clamp(double min, double max) {
        this.source.clamp(min, max);
    }

    @Override
    public boolean equalsArray(double[] data, int offset) {
        return this.source.equalsArray(data, offset);
    }

    @Override
    public double elementSum() {
        return this.source.elementSum();
    }

    @Override
    public double magnitudeSquared() {
        return this.source.elementSquaredSum();
    }

    @Override
    public void applyOp(Op op) {
        this.source.applyOp(op);
    }

    @Override
    public void abs() {
        this.source.abs();
    }

    @Override
    public void signum() {
        this.source.signum();
    }

    @Override
    public void negate() {
        this.source.negate();
    }

    @Override
    public void exp() {
        this.source.exp();
    }

    @Override
    public void log() {
        this.source.log();
    }

    @Override
    public void square() {
        this.source.square();
    }

    @Override
    public void sqrt() {
        this.source.sqrt();
    }

    @Override
    public void pow(double exponent) {
        this.source.pow(exponent);
    }

    @Override
    protected int calcRow(int i) {
        return i / this.columns;
    }

    @Override
    protected int calcCol(int i) {
        return i % this.columns;
    }

    @Override
    public AVector subVector(int start, int length) {
        int endRow;
        int startRow = this.calcRow(start);
        if (startRow == (endRow = this.calcRow(start + length - 1))) {
            return this.source.getRowView(startRow).subVector(start - startRow * this.columns, length);
        }
        if (startRow == endRow - 1 && this.source instanceof IFastRows) {
            int split = endRow * this.columns;
            return this.source.getRowView(startRow).subVector(start - startRow * this.columns, split - start).join(this.source.getRowView(endRow).subVector(0, start + length - split));
        }
        return super.subVector(start, length);
    }

    @Override
    public double dotProduct(double[] data, int offset) {
        double result = 0.0;
        for (int i = 0; i < this.rows; ++i) {
            result += this.source.getRow(i).dotProduct(data, offset + i * this.columns);
        }
        return result;
    }
}

