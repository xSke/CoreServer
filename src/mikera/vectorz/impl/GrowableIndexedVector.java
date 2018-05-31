/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz.impl;

import mikera.arrayz.INDArray;
import mikera.indexz.GrowableIndex;
import mikera.indexz.Index;
import mikera.vectorz.AVector;
import mikera.vectorz.GrowableVector;
import mikera.vectorz.impl.ASparseVector;
import mikera.vectorz.impl.SparseIndexedVector;
import mikera.vectorz.util.VectorzException;

public class GrowableIndexedVector
extends ASparseVector {
    private static final long serialVersionUID = 441979517032171392L;
    private final GrowableIndex index;
    private final GrowableVector data;

    private GrowableIndexedVector(int length, GrowableIndex index, GrowableVector data) {
        super(length);
        this.index = index;
        this.data = data;
    }

    private GrowableIndexedVector(int length) {
        super(length);
        this.index = new GrowableIndex();
        this.data = new GrowableVector();
    }

    public static GrowableIndexedVector createLength(int len) {
        return new GrowableIndexedVector(len);
    }

    @Override
    public double get(int i) {
        this.checkIndex(i);
        int ix = this.index.indexPosition(i);
        if (ix < 0) {
            return 0.0;
        }
        return this.data.get(ix);
    }

    public void append(int i, double value) {
        this.index.checkedAppend(i);
        this.data.append(value);
    }

    @Override
    public void set(int i, double value) {
        int ix = this.index.indexPosition(i);
        if (ix < 0) {
            throw new UnsupportedOperationException("Can't set at index: " + i);
        }
        this.data.unsafeSet(ix, value);
    }

    @Override
    public boolean isFullyMutable() {
        return false;
    }

    @Override
    public double dotProduct(double[] data, int offset) {
        return this.toSparseIndexedVector().dotProduct(data, offset);
    }

    public SparseIndexedVector toSparseIndexedVector() {
        return SparseIndexedVector.create(this.length, Index.create(this.index), this.data.toDoubleArray());
    }

    @Override
    public AVector exactClone() {
        return new GrowableIndexedVector(this.length, this.index.exactClone(), this.data.exactClone());
    }

    @Override
    public SparseIndexedVector sparseClone() {
        return this.toSparseIndexedVector();
    }

    @Override
    public int nonSparseElementCount() {
        return this.index.length();
    }

    @Override
    public AVector nonSparseValues() {
        return this.data;
    }

    @Override
    public Index nonSparseIndex() {
        return Index.create(this.index);
    }

    @Override
    public boolean includesIndex(int i) {
        return this.index.indexPosition(i) >= 0;
    }

    @Override
    public void add(ASparseVector v) {
        Index ix = v.nonSparseIndex();
        AVector vs = v.nonSparseValues();
        int n = ix.length();
        for (int i = 0; i < n; ++i) {
            this.addAt(ix.get(i), vs.get(i));
        }
    }

    @Override
    public void validate() {
        if (this.index.length() != this.data.length()) {
            throw new VectorzException("Mismatched index and data length!");
        }
    }
}

