/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz.impl;

import mikera.indexz.Index;
import mikera.vectorz.AVector;
import mikera.vectorz.Vector1;
import mikera.vectorz.impl.ASparseVector;
import mikera.vectorz.util.IntArrays;

public abstract class ASingleElementVector
extends ASparseVector {
    private static final long serialVersionUID = -5246190958486810285L;
    protected final int index;

    protected ASingleElementVector(int index, int length) {
        super(length);
        this.index = index;
    }

    protected abstract double value();

    protected final int index() {
        return this.index;
    }

    @Override
    public double dotProduct(AVector v) {
        return this.value() * v.unsafeGet(this.index());
    }

    @Override
    public double dotProduct(double[] data, int offset) {
        return this.value() * data[offset + this.index()];
    }

    @Override
    public int nonSparseElementCount() {
        return 1;
    }

    @Override
    public AVector nonSparseValues() {
        return Vector1.of(this.value());
    }

    @Override
    public Index nonSparseIndex() {
        return Index.of(this.index);
    }

    @Override
    public boolean equals(AVector v) {
        int len = v.length();
        if (len != this.length) {
            return false;
        }
        if (v.unsafeGet(this.index) != this.value()) {
            return false;
        }
        if (!v.isRangeZero(0, this.index)) {
            return false;
        }
        if (!v.isRangeZero(this.index + 1, len - (this.index + 1))) {
            return false;
        }
        return true;
    }

    @Override
    public int[] nonZeroIndices() {
        if (this.value() == 0.0) {
            return IntArrays.EMPTY_INT_ARRAY;
        }
        return new int[]{this.index};
    }
}

