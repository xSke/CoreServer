/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.impl;

import java.util.Arrays;
import java.util.Iterator;
import mikera.arrayz.INDArray;
import mikera.arrayz.ISparse;
import mikera.matrixx.AMatrix;
import mikera.matrixx.Matrix;
import mikera.matrixx.Matrixx;
import mikera.matrixx.impl.ARectangularMatrix;
import mikera.matrixx.impl.IFastColumns;
import mikera.matrixx.impl.IFastRows;
import mikera.randomz.Hash;
import mikera.vectorz.AVector;
import mikera.vectorz.Vector;
import mikera.vectorz.Vectorz;
import mikera.vectorz.impl.RepeatedElementIterator;
import mikera.vectorz.util.DoubleArrays;
import mikera.vectorz.util.ErrorMessages;

public final class ZeroMatrix
extends ARectangularMatrix
implements IFastRows,
IFastColumns,
ISparse {
    private static final long serialVersionUID = 875833013123277805L;

    @Override
    public boolean isFullyMutable() {
        return false;
    }

    private ZeroMatrix(int rows, int columns) {
        super(rows, columns);
    }

    public static ZeroMatrix create(int rows, int columns) {
        return new ZeroMatrix(rows, columns);
    }

    public static ZeroMatrix createSameShape(AMatrix a) {
        return new ZeroMatrix(a.rowCount(), a.columnCount());
    }

    @Override
    public boolean isSparse() {
        return true;
    }

    @Override
    public boolean isSquare() {
        return this.cols == this.rows;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public boolean isSymmetric() {
        return this.isSquare();
    }

    @Override
    public boolean isDiagonal() {
        return this.isSquare();
    }

    @Override
    public boolean isUpperTriangular() {
        return true;
    }

    @Override
    public boolean isLowerTriangular() {
        return true;
    }

    @Override
    public boolean isBoolean() {
        return true;
    }

    @Override
    public int upperBandwidthLimit() {
        return 0;
    }

    @Override
    public int lowerBandwidthLimit() {
        return 0;
    }

    @Override
    public void multiply(double factor) {
    }

    @Override
    public AVector getRowView(int row) {
        return Vectorz.createZeroVector(this.cols);
    }

    @Override
    public AVector getColumnView(int col) {
        return Vectorz.createZeroVector(this.rows);
    }

    @Override
    public void copyRowTo(int row, double[] dest, int destOffset) {
        Arrays.fill(dest, destOffset, destOffset + this.columnCount(), 0.0);
    }

    @Override
    public void copyColumnTo(int col, double[] dest, int destOffset) {
        Arrays.fill(dest, destOffset, destOffset + this.rowCount(), 0.0);
    }

    @Override
    public void addToArray(double[] dest, int offset) {
    }

    @Override
    public AMatrix addCopy(AMatrix a) {
        if (!this.isSameShape(a)) {
            throw new IllegalArgumentException(ErrorMessages.incompatibleShapes(this, a));
        }
        return a.copy();
    }

    @Override
    public void getElements(double[] dest, int destOffset) {
        Arrays.fill(dest, destOffset, destOffset + this.rowCount() * this.columnCount(), 0.0);
    }

    @Override
    public double determinant() {
        if (this.isSquare()) {
            throw new UnsupportedOperationException(ErrorMessages.squareMatrixRequired(this));
        }
        return 0.0;
    }

    @Override
    public int rank() {
        return 0;
    }

    @Override
    public double trace() {
        return 0.0;
    }

    @Override
    public double diagonalProduct() {
        int n = Math.min(this.rowCount(), this.columnCount());
        return n > 0 ? 0.0 : 1.0;
    }

    @Override
    public double calculateElement(int i, AVector v) {
        assert (i >= 0);
        assert (i < this.rows);
        return 0.0;
    }

    @Override
    public double get(int i, int j) {
        this.checkIndex(i, j);
        return 0.0;
    }

    @Override
    public void set(int row, int column, double value) {
        throw new UnsupportedOperationException(ErrorMessages.immutable(this));
    }

    @Override
    public double unsafeGet(int row, int column) {
        return 0.0;
    }

    @Override
    public void unsafeSet(int row, int column, double value) {
        throw new UnsupportedOperationException(ErrorMessages.immutable(this));
    }

    @Override
    public AMatrix clone() {
        return Matrixx.newMatrix(this.rows, this.cols);
    }

    @Override
    public boolean isZero() {
        return true;
    }

    @Override
    public double elementSum() {
        return 0.0;
    }

    @Override
    public double elementMax() {
        if (this.rows <= 0 || this.cols <= 0) {
            throw new IllegalArgumentException(ErrorMessages.noElements(this));
        }
        return 0.0;
    }

    @Override
    public double elementMin() {
        if (this.rows <= 0 || this.cols <= 0) {
            throw new IllegalArgumentException(ErrorMessages.noElements(this));
        }
        return 0.0;
    }

    @Override
    public long nonZeroCount() {
        return 0L;
    }

    @Override
    public int hashCode() {
        return Hash.zeroVectorHash(this.cols * this.rows);
    }

    @Override
    public void transform(AVector input, AVector output) {
        assert (output.length() == this.rows);
        output.fill(0.0);
    }

    @Override
    public void transform(Vector input, Vector output) {
        assert (output.length() == this.rows);
        output.fill(0.0);
    }

    @Override
    public boolean isInvertible() {
        return false;
    }

    @Override
    public AVector asVector() {
        return Vectorz.createZeroVector(this.cols * this.rows);
    }

    @Override
    public AMatrix innerProduct(AMatrix m) {
        if (this.columnCount() != m.rowCount()) {
            throw new IllegalArgumentException(ErrorMessages.incompatibleShapes(this, m));
        }
        return ZeroMatrix.create(this.rows, m.columnCount());
    }

    @Override
    public ZeroMatrix multiplyCopy(double a) {
        return this;
    }

    @Override
    public void elementMul(AMatrix m) {
    }

    @Override
    public boolean equals(AMatrix m) {
        if (!this.isSameShape(m)) {
            return false;
        }
        return m.isZero();
    }

    @Override
    public boolean equalsArray(double[] data, int offset) {
        return DoubleArrays.isZero(data, offset, this.rows * this.cols);
    }

    @Override
    public ZeroMatrix getTranspose() {
        if (this.cols == this.rows) {
            return this;
        }
        return ZeroMatrix.create(this.cols, this.rows);
    }

    @Override
    public ZeroMatrix getTransposeView() {
        if (this.cols == this.rows) {
            return this;
        }
        return ZeroMatrix.create(this.cols, this.rows);
    }

    @Override
    public Matrix toMatrix() {
        return Matrix.create(this.rows, this.cols);
    }

    @Override
    public double[] toDoubleArray() {
        return new double[this.rows * this.cols];
    }

    @Override
    public AMatrix sparseClone() {
        return Matrixx.createSparse(this.rows, this.cols);
    }

    @Override
    public Matrix toMatrixTranspose() {
        return Matrix.create(this.cols, this.rows);
    }

    @Override
    public AVector getLeadingDiagonal() {
        return Vectorz.createZeroVector(Math.min(this.rows, this.cols));
    }

    @Override
    public AVector getBand(int band) {
        return Vectorz.createZeroVector(this.bandLength(band));
    }

    @Override
    public AMatrix subMatrix(int rowStart, int rows, int colStart, int cols) {
        return ZeroMatrix.create(rows, cols);
    }

    @Override
    public Iterator<Double> elementIterator() {
        return new RepeatedElementIterator(this.cols * this.rows, 0.0);
    }

    @Override
    public ZeroMatrix exactClone() {
        return new ZeroMatrix(this.rows, this.cols);
    }

    @Override
    public boolean hasUncountable() {
        return false;
    }

    @Override
    public double elementPowSum(double p) {
        return 0.0;
    }

    @Override
    public double elementAbsPowSum(double p) {
        return this.elementPowSum(p);
    }
}

