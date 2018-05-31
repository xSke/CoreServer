/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.impl;

import mikera.arrayz.INDArray;
import mikera.arrayz.ISparse;
import mikera.indexz.Index;
import mikera.matrixx.AMatrix;
import mikera.matrixx.impl.ABooleanMatrix;
import mikera.matrixx.impl.IFastRows;
import mikera.vectorz.AVector;
import mikera.vectorz.Vector;
import mikera.vectorz.impl.AxisVector;
import mikera.vectorz.util.ErrorMessages;
import mikera.vectorz.util.VectorzException;

public final class SubsetMatrix
extends ABooleanMatrix
implements ISparse,
IFastRows {
    private static final long serialVersionUID = 4937375232646236833L;
    private int inputDims;
    private Index components;

    private SubsetMatrix(int inputDimensions, Index components) {
        this.inputDims = inputDimensions;
        this.components = components;
    }

    public static SubsetMatrix create(Index components, int inputDimensions) {
        SubsetMatrix sm = new SubsetMatrix(inputDimensions, components);
        if (!sm.components.allInRange(0, sm.inputDims)) {
            throw new IllegalArgumentException("SubsetMatrix with input dimensionality " + sm.inputDims + " not valid for component indexes: " + sm.components);
        }
        return sm;
    }

    @Override
    public void transform(AVector source, AVector dest) {
        dest.set(source, this.components);
    }

    @Override
    public double elementSum() {
        return this.rowCount();
    }

    @Override
    public long nonZeroCount() {
        return this.rowCount();
    }

    @Override
    public int rowCount() {
        return this.components.length();
    }

    @Override
    public int columnCount() {
        return this.inputDims;
    }

    @Override
    public double density() {
        return 1.0 / (double)this.inputDims;
    }

    @Override
    public AxisVector getRowView(int i) {
        return AxisVector.create(this.components.get(i), this.inputDims);
    }

    @Override
    public double calculateElement(int i, AVector inputVector) {
        return inputVector.unsafeGet(this.components.get(i));
    }

    @Override
    public double calculateElement(int i, Vector inputVector) {
        return inputVector.unsafeGet(this.components.get(i));
    }

    @Override
    public double get(int row, int column) {
        if (column < 0 || column >= this.inputDims) {
            throw new IndexOutOfBoundsException(ErrorMessages.invalidIndex((INDArray)this, row, column));
        }
        return column == this.components.get(row) ? 1.0 : 0.0;
    }

    @Override
    public double unsafeGet(int row, int column) {
        return column == this.components.get(row) ? 1.0 : 0.0;
    }

    @Override
    public void set(int row, int column, double value) {
        throw new UnsupportedOperationException(ErrorMessages.notFullyMutable(this, row, column));
    }

    @Override
    public SubsetMatrix exactClone() {
        return SubsetMatrix.create(this.components.clone(), this.inputDims);
    }

    @Override
    public void validate() {
        int rc = this.rowCount();
        int cc = this.columnCount();
        for (int i = 0; i < rc; ++i) {
            int s = this.components.get(i);
            if (s >= 0 && s < cc) continue;
            throw new VectorzException("Component out of range at row " + i);
        }
        super.validate();
    }

    @Override
    public boolean isZero() {
        return false;
    }
}

