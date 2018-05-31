/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.impl;

import mikera.arrayz.INDArray;
import mikera.matrixx.AMatrix;
import mikera.matrixx.impl.AVectorMatrix;
import mikera.transformz.marker.ISpecialisedTransform;
import mikera.vectorz.AVector;
import mikera.vectorz.Vector3;

public final class VectorMatrixM3
extends AVectorMatrix<Vector3>
implements ISpecialisedTransform {
    private static final long serialVersionUID = -8148184725377519520L;
    private Vector3[] rowData;

    public VectorMatrixM3(int rowCount) {
        super(rowCount, 3);
        this.rowData = new Vector3[rowCount];
        for (int i = 0; i < rowCount; ++i) {
            this.rowData[i] = new Vector3();
        }
    }

    private VectorMatrixM3(Vector3[] rows) {
        super(rows.length, 3);
        this.rowData = rows;
    }

    @Override
    public void multiply(double factor) {
        for (Vector3 vector : this.rowData) {
            vector.scale(factor);
        }
    }

    @Override
    public void replaceRow(int i, AVector row) {
        this.replaceRow(i, (Vector3)row);
    }

    public void replaceRow(int i, Vector3 row) {
        this.rowData[i] = row;
    }

    @Override
    public double get(int row, int column) {
        return this.rowData[row].get(column);
    }

    @Override
    public void set(int row, int column, double value) {
        this.rowData[row].set(column, value);
    }

    @Override
    public double unsafeGet(int row, int column) {
        return this.rowData[row].unsafeGet(column);
    }

    @Override
    public void unsafeSet(int row, int column, double value) {
        this.rowData[row].unsafeSet(column, value);
    }

    @Override
    public Vector3 getRowView(int row) {
        return this.rowData[row];
    }

    @Override
    public void transform(AVector source, AVector dest) {
        if (source instanceof Vector3) {
            this.transform((Vector3)source, dest);
            return;
        }
        super.transform(source, dest);
    }

    public void transform(Vector3 source, AVector dest) {
        for (int i = 0; i < this.rows; ++i) {
            dest.set(i, this.getRow(i).dotProduct(source));
        }
    }

    @Override
    public double calculateElement(int i, AVector inputVector) {
        assert (i < this.rows);
        Vector3 row = this.rowData[i];
        return row.dotProduct(inputVector);
    }

    @Override
    public boolean isSquare() {
        return this.rows == 3;
    }

    @Override
    public VectorMatrixM3 clone() {
        VectorMatrixM3 m = new VectorMatrixM3((Vector3[])this.rowData.clone());
        for (int i = 0; i < this.rows; ++i) {
            m.rowData[i] = m.rowData[i].clone();
        }
        return m;
    }

    @Override
    public VectorMatrixM3 exactClone() {
        return this.clone();
    }
}

