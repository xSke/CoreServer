/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz.impl;

import mikera.arrayz.INDArray;
import mikera.indexz.Index;
import mikera.vectorz.AVector;
import mikera.vectorz.impl.ASizedVector;
import mikera.vectorz.util.ErrorMessages;
import mikera.vectorz.util.IntArrays;
import mikera.vectorz.util.VectorzException;

public class IndexVector
extends ASizedVector {
    private final Index index;

    private IndexVector(Index index) {
        super(index.length());
        this.index = index;
    }

    public static /* varargs */ IndexVector of(int ... values) {
        return new IndexVector(Index.of(values));
    }

    public /* varargs */ IndexVector ofDoubles(double ... values) {
        return new IndexVector(Index.wrap(IntArrays.create(values)));
    }

    public static IndexVector wrap(Index a) {
        return new IndexVector(a);
    }

    @Override
    public double get(int i) {
        return this.index.get(i);
    }

    @Override
    public double unsafeGet(int i) {
        return this.index.unsafeGet(i);
    }

    @Override
    public void set(int i, double value) {
        throw new UnsupportedOperationException(ErrorMessages.immutable(this));
    }

    @Override
    public boolean isElementConstrained() {
        return true;
    }

    @Override
    public boolean isFullyMutable() {
        return false;
    }

    @Override
    public void getElements(double[] data, int offset) {
        IntArrays.copyIntsToDoubles(this.index.data, 0, data, offset, this.length);
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public AVector exactClone() {
        return new IndexVector(this.index.clone());
    }

    @Override
    public void validate() {
        if (this.length != this.index.length()) {
            throw new VectorzException("Incorrect index length!!");
        }
        super.validate();
    }

    @Override
    public double dotProduct(double[] data, int offset) {
        double result = 0.0;
        for (int i = 0; i < this.length; ++i) {
            result += data[offset + i] * (double)this.index.unsafeGet(i);
        }
        return result;
    }
}

