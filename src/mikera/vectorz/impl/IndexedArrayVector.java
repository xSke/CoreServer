/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz.impl;

import java.util.Arrays;
import mikera.arrayz.INDArray;
import mikera.vectorz.AScalar;
import mikera.vectorz.AVector;
import mikera.vectorz.impl.ArrayIndexScalar;
import mikera.vectorz.impl.BaseIndexedVector;
import mikera.vectorz.impl.Vector0;
import mikera.vectorz.util.IntArrays;

public final class IndexedArrayVector
extends BaseIndexedVector {
    private static final long serialVersionUID = 3220750778637547658L;
    private final double[] data;

    private IndexedArrayVector(double[] source, int[] indexes) {
        super(indexes);
        this.data = source;
    }

    public static IndexedArrayVector wrap(double[] data, int[] indexes) {
        return new IndexedArrayVector(data, indexes);
    }

    @Override
    public double get(int i) {
        return this.data[this.indexes[i]];
    }

    @Override
    public double unsafeGet(int i) {
        return this.data[this.indexes[i]];
    }

    @Override
    public /* varargs */ IndexedArrayVector selectView(int ... inds) {
        int[] ci = IntArrays.select(this.indexes, inds);
        return new IndexedArrayVector(this.data, ci);
    }

    @Override
    public void set(int i, double value) {
        this.data[this.indexes[i]] = value;
    }

    @Override
    public void unsafeSet(int i, double value) {
        this.data[this.indexes[i]] = value;
    }

    @Override
    public void addAt(int i, double value) {
        double[] arrd = this.data;
        int n = this.indexes[i];
        arrd[n] = arrd[n] + value;
    }

    @Override
    public AVector subVector(int offset, int length) {
        int len = this.checkRange(offset, length);
        if (length == 0) {
            return Vector0.INSTANCE;
        }
        if (length == len) {
            return this;
        }
        int end = offset + length;
        int[] newIndexes = Arrays.copyOfRange(this.indexes, offset, end);
        return IndexedArrayVector.wrap(this.data, newIndexes);
    }

    @Override
    public ArrayIndexScalar slice(int i) {
        return ArrayIndexScalar.wrap(this.data, this.indexes[i]);
    }

    @Override
    public void getElements(double[] dest, int offset) {
        for (int i = 0; i < this.length; ++i) {
            dest[offset + i] = this.data[this.indexes[i]];
        }
    }

    @Override
    public void addToArray(double[] dest, int offset) {
        for (int i = 0; i < this.length; ++i) {
            double[] arrd = dest;
            int n = offset + i;
            arrd[n] = arrd[n] + this.data[this.indexes[i]];
        }
    }

    @Override
    public void addMultipleToArray(double factor, int offset, double[] dest, int destOffset, int length) {
        for (int i = 0; i < length; ++i) {
            double[] arrd = dest;
            int n = destOffset + i;
            arrd[n] = arrd[n] + this.data[this.indexes[offset + i]] * factor;
        }
    }

    @Override
    public double dotProduct(double[] data, int offset) {
        double result = 0.0;
        for (int i = 0; i < this.length; ++i) {
            result += this.unsafeGet(i) * data[offset + i];
        }
        return result;
    }

    @Override
    public IndexedArrayVector exactClone() {
        return IndexedArrayVector.wrap((double[])this.data.clone(), (int[])this.indexes.clone());
    }
}

