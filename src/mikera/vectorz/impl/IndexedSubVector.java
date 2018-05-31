/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz.impl;

import mikera.arrayz.INDArray;
import mikera.vectorz.AVector;
import mikera.vectorz.impl.BaseIndexedVector;
import mikera.vectorz.impl.Vector0;
import mikera.vectorz.util.ErrorMessages;
import mikera.vectorz.util.IntArrays;
import mikera.vectorz.util.VectorzException;

public final class IndexedSubVector
extends BaseIndexedVector {
    private static final long serialVersionUID = -1411109918028367417L;
    private final AVector source;

    private IndexedSubVector(AVector source, int[] indexes) {
        super(indexes);
        this.source = source;
    }

    public static IndexedSubVector wrap(AVector source, int[] indexes) {
        return new IndexedSubVector(source, indexes);
    }

    @Override
    public void addToArray(double[] dest, int offset) {
        for (int i = 0; i < this.length; ++i) {
            double[] arrd = dest;
            int n = offset + i;
            arrd[n] = arrd[n] + this.source.unsafeGet(this.indexes[i]);
        }
    }

    @Override
    public void getElements(double[] dest, int offset) {
        for (int i = 0; i < this.length; ++i) {
            dest[offset + i] = this.source.unsafeGet(this.indexes[i]);
        }
    }

    @Override
    public /* varargs */ AVector selectView(int ... inds) {
        int[] ci = IntArrays.select(this.indexes, inds);
        return new IndexedSubVector(this.source, ci);
    }

    @Override
    public double get(int i) {
        return this.source.unsafeGet(this.indexes[i]);
    }

    @Override
    public void set(int i, double value) {
        this.source.unsafeSet(this.indexes[i], value);
    }

    @Override
    public double unsafeGet(int i) {
        return this.source.unsafeGet(this.indexes[i]);
    }

    @Override
    public void unsafeSet(int i, double value) {
        this.source.unsafeSet(this.indexes[i], value);
    }

    @Override
    public AVector subVector(int offset, int length) {
        if (offset < 0 || offset + length > this.length) {
            throw new IndexOutOfBoundsException(ErrorMessages.invalidRange(this, offset, length));
        }
        if (length == 0) {
            return Vector0.INSTANCE;
        }
        if (length == this.length) {
            return this;
        }
        int[] newIndexes = new int[length];
        for (int i = 0; i < length; ++i) {
            newIndexes[i] = this.indexes[offset + i];
        }
        return IndexedSubVector.wrap(this.source, newIndexes);
    }

    @Override
    public IndexedSubVector exactClone() {
        return IndexedSubVector.wrap(this.source.exactClone(), (int[])this.indexes.clone());
    }

    @Override
    public void validate() {
        super.validate();
        int slen = this.source.length();
        for (int i = 0; i < this.length; ++i) {
            if (this.indexes[i] >= 0 && this.indexes[i] < slen) continue;
            throw new VectorzException("Indexes out of range");
        }
    }

    @Override
    public void addAt(int i, double v) {
        this.source.addAt(this.indexes[i], v);
    }

    @Override
    public double dotProduct(double[] data, int offset) {
        double result = 0.0;
        for (int i = 0; i < this.length; ++i) {
            result += data[offset + i] * this.unsafeGet(i);
        }
        return result;
    }
}

