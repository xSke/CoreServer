/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.impl;

import java.util.List;
import mikera.arrayz.INDArray;
import mikera.matrixx.AMatrix;
import mikera.matrixx.Matrix;
import mikera.matrixx.impl.ADelegatedMatrix;
import mikera.matrixx.impl.IFastColumns;
import mikera.matrixx.impl.IFastRows;
import mikera.matrixx.impl.SparseColumnMatrix;
import mikera.matrixx.impl.SparseRowMatrix;
import mikera.vectorz.AVector;
import mikera.vectorz.IOperator;
import mikera.vectorz.Op;

public class TransposedMatrix
extends ADelegatedMatrix {
    private static final long serialVersionUID = 4350297037540121584L;

    private TransposedMatrix(AMatrix source) {
        super(source.columnCount(), source.rowCount(), source);
    }

    public static AMatrix wrap(AMatrix m) {
        if (m instanceof TransposedMatrix) {
            return ((TransposedMatrix)m).source;
        }
        return new TransposedMatrix(m);
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
    public double get(int row, int column) {
        return this.source.get(column, row);
    }

    @Override
    public void set(int row, int column, double value) {
        this.source.set(column, row, value);
    }

    @Override
    public double unsafeGet(int row, int column) {
        return this.source.unsafeGet(column, row);
    }

    @Override
    public void unsafeSet(int row, int column, double value) {
        this.source.unsafeSet(column, row, value);
    }

    @Override
    public AVector getRow(int row) {
        return this.source.getColumn(row);
    }

    @Override
    public AVector getColumn(int column) {
        return this.source.getRow(column);
    }

    @Override
    public AVector getRowClone(int row) {
        return this.source.getColumnClone(row);
    }

    @Override
    public AVector getColumnClone(int column) {
        return this.source.getRowClone(column);
    }

    @Override
    public AVector getRowView(int row) {
        return this.source.getColumnView(row);
    }

    @Override
    public AVector getColumnView(int column) {
        return this.source.getRowView(column);
    }

    @Override
    public int sliceCount() {
        return this.source.columnCount();
    }

    @Override
    public double trace() {
        return this.source.trace();
    }

    @Override
    public double diagonalProduct() {
        return this.source.diagonalProduct();
    }

    @Override
    public Matrix toMatrixTranspose() {
        return this.source.toMatrix();
    }

    @Override
    public Matrix toMatrix() {
        return this.source.toMatrixTranspose();
    }

    @Override
    public void copyRowTo(int row, double[] dest, int destOffset) {
        this.source.copyColumnTo(row, dest, destOffset);
    }

    @Override
    public void copyColumnTo(int col, double[] dest, int destOffset) {
        this.source.copyRowTo(col, dest, destOffset);
    }

    @Override
    public void getElements(double[] dest, int destOffset) {
        int rc = this.rowCount();
        int cc = this.columnCount();
        for (int i = 0; i < rc; ++i) {
            this.source.copyColumnTo(i, dest, destOffset + i * cc);
        }
    }

    @Override
    public double determinant() {
        return this.source.determinant();
    }

    @Override
    public boolean isSymmetric() {
        return this.source.isSymmetric();
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
    public boolean isUpperTriangular() {
        return this.source.isLowerTriangular();
    }

    @Override
    public int lowerBandwidthLimit() {
        return this.source.upperBandwidthLimit();
    }

    @Override
    public int lowerBandwidth() {
        return this.source.upperBandwidth();
    }

    @Override
    public int upperBandwidthLimit() {
        return this.source.lowerBandwidthLimit();
    }

    @Override
    public int upperBandwidth() {
        return this.source.lowerBandwidth();
    }

    @Override
    public AVector getBand(int i) {
        return this.source.getBand(- i);
    }

    @Override
    public boolean isLowerTriangular() {
        return this.source.isUpperTriangular();
    }

    @Override
    public AMatrix getTranspose() {
        return this.source;
    }

    @Override
    public AMatrix getTransposeView() {
        return this.source;
    }

    @Override
    public AMatrix getTransposeCopy() {
        return this.source.copy();
    }

    @Override
    public AMatrix transposeInnerProduct(AMatrix s) {
        return this.source.innerProduct(s);
    }

    @Override
    public AMatrix transposeInnerProduct(Matrix s) {
        return this.source.innerProduct(s);
    }

    @Override
    public AMatrix innerProduct(AMatrix s) {
        return this.source.transposeInnerProduct(s);
    }

    @Override
    public AMatrix innerProduct(Matrix s) {
        return this.source.transposeInnerProduct(s);
    }

    @Override
    public AMatrix sparseClone() {
        if (this.source instanceof IFastColumns) {
            return SparseRowMatrix.create(this.source.getColumns());
        }
        if (this.source instanceof IFastRows) {
            return SparseColumnMatrix.create(this.source.getRows());
        }
        return SparseRowMatrix.create(this.source.getColumns());
    }

    @Override
    public TransposedMatrix exactClone() {
        return new TransposedMatrix(this.source.exactClone());
    }

    @Override
    public boolean equals(AMatrix m) {
        return m.equalsTranspose(this.source);
    }
}

