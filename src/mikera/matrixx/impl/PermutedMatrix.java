/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.impl;

import mikera.arrayz.INDArray;
import mikera.indexz.Index;
import mikera.indexz.Indexz;
import mikera.matrixx.AMatrix;
import mikera.matrixx.impl.ADelegatedMatrix;
import mikera.vectorz.AVector;
import mikera.vectorz.IOperator;
import mikera.vectorz.Op;
import mikera.vectorz.impl.IndexedSubVector;
import mikera.vectorz.util.VectorzException;

public class PermutedMatrix
extends ADelegatedMatrix {
    private static final long serialVersionUID = -4237549899714650293L;
    private final Index rowPermutations;
    private final Index columnPermutations;

    public PermutedMatrix(AMatrix source, Index rowPermutations) {
        this(source, rowPermutations, Indexz.createSequence(source.columnCount()));
    }

    public PermutedMatrix(AMatrix source, Index rowPermutations, Index columnPermutations) {
        super(rowPermutations.length(), columnPermutations.length(), source);
        if (source instanceof PermutedMatrix) {
            PermutedMatrix pm = (PermutedMatrix)source;
            Index rp = pm.rowPermutations.clone();
            rp.permute(rowPermutations);
            rowPermutations = rp;
            Index cp = pm.columnPermutations.clone();
            rp.permute(columnPermutations);
            columnPermutations = cp;
            source = pm.source;
        }
        if (source.rowCount() != rowPermutations.length()) {
            throw new VectorzException("Incorrect row permutation count: " + rowPermutations.length());
        }
        if (source.columnCount() != columnPermutations.length()) {
            throw new VectorzException("Incorrect column permutation count: " + columnPermutations.length());
        }
        this.rowPermutations = rowPermutations;
        this.columnPermutations = columnPermutations;
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
    public boolean isZero() {
        return this.source.isZero();
    }

    @Override
    public double get(int row, int column) {
        int sourceRow = this.rowPermutations.get(row);
        int sourceColumn = this.columnPermutations.get(column);
        return this.source.get(sourceRow, sourceColumn);
    }

    @Override
    public void set(int row, int column, double value) {
        int sourceRow = this.rowPermutations.get(row);
        int sourceColumn = this.columnPermutations.get(column);
        this.source.set(sourceRow, sourceColumn, value);
    }

    @Override
    public double unsafeGet(int row, int column) {
        int sourceRow = this.rowPermutations.get(row);
        int sourceColumn = this.columnPermutations.get(column);
        return this.source.unsafeGet(sourceRow, sourceColumn);
    }

    @Override
    public void unsafeSet(int row, int column, double value) {
        int sourceRow = this.rowPermutations.get(row);
        int sourceColumn = this.columnPermutations.get(column);
        this.source.unsafeSet(sourceRow, sourceColumn, value);
    }

    @Override
    public AVector getRowView(int row) {
        return IndexedSubVector.wrap(this.source.getRowView(this.rowPermutations.get(row)), this.columnPermutations.getData());
    }

    @Override
    public AVector getColumnView(int column) {
        return IndexedSubVector.wrap(this.source.getColumnView(this.columnPermutations.get(column)), this.rowPermutations.getData());
    }

    @Override
    public PermutedMatrix exactClone() {
        return new PermutedMatrix(this.source.exactClone(), this.rowPermutations.clone(), this.columnPermutations.clone());
    }
}

