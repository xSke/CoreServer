/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz.impl;

import mikera.arrayz.INDArray;
import mikera.indexz.Index;
import mikera.matrixx.AMatrix;
import mikera.matrixx.impl.AVectorMatrix;
import mikera.vectorz.AVector;
import mikera.vectorz.Op;
import mikera.vectorz.Vector;
import mikera.vectorz.impl.ASparseIndexedVector;
import mikera.vectorz.impl.ASparseVector;
import mikera.vectorz.impl.ImmutableVector;
import mikera.vectorz.impl.SparseIndexedVector;
import mikera.vectorz.impl.Vector0;
import mikera.vectorz.impl.ZeroVector;
import mikera.vectorz.util.DoubleArrays;
import mikera.vectorz.util.ErrorMessages;
import mikera.vectorz.util.VectorzException;

public class SparseImmutableVector
extends ASparseIndexedVector {
    private static final long serialVersionUID = 750093598603613879L;
    private final Index index;
    private final double[] data;
    private final int dataLength;

    private SparseImmutableVector(int length, Index index) {
        this(length, index, new double[index.length()]);
    }

    private SparseImmutableVector(int length, Index index, double[] data) {
        super(length);
        this.index = index;
        this.data = data;
        this.dataLength = data.length;
    }

    private SparseImmutableVector(int length, Index index, AVector data) {
        this(length, index, data.toDoubleArray());
    }

    public static SparseImmutableVector wrap(int length, Index index, double[] data) {
        assert (index.length() == data.length);
        assert (index.isDistinctSorted());
        return new SparseImmutableVector(length, index, data);
    }

    @Override
    double[] internalData() {
        return this.data;
    }

    @Override
    Index internalIndex() {
        return this.index;
    }

    public static AVector create(int length, Index index, double[] data) {
        int dataLength = data.length;
        if (!index.isDistinctSorted()) {
            throw new IllegalArgumentException("Index must be sorted and distinct");
        }
        if (index.length() != dataLength) {
            throw new IllegalArgumentException("Length of index: mismatch woth data");
        }
        if (dataLength == 0) {
            return ZeroVector.create(length);
        }
        if (dataLength == length) {
            return ImmutableVector.create(data);
        }
        return new SparseImmutableVector(length, index.clone(), DoubleArrays.copyOf(data));
    }

    public static AVector create(int length, Index index, AVector data) {
        int dataLength = data.length();
        if (!index.isDistinctSorted()) {
            throw new IllegalArgumentException("Index must be sorted and distinct");
        }
        if (index.length() != dataLength) {
            throw new IllegalArgumentException("Length of index: mismatch woth data");
        }
        if (dataLength == 0) {
            return ZeroVector.create(length);
        }
        if (dataLength == length) {
            return ImmutableVector.create(data);
        }
        return SparseImmutableVector.wrap(length, index.clone(), data.toDoubleArray());
    }

    public static AVector create(AVector source) {
        int length = source.length();
        if (length == 0) {
            return Vector0.INSTANCE;
        }
        int dataLength = (int)source.nonZeroCount();
        if (dataLength == length) {
            return ImmutableVector.create(source);
        }
        if (dataLength == 0) {
            return ZeroVector.create(length);
        }
        int[] indexes = new int[dataLength];
        double[] vals = new double[dataLength];
        int pos = 0;
        for (int i = 0; i < length; ++i) {
            double v = source.unsafeGet(i);
            if (v == 0.0) continue;
            indexes[pos] = i;
            vals[pos] = v;
            ++pos;
        }
        return SparseImmutableVector.wrap(length, Index.wrap(indexes), vals);
    }

    public static AVector create(ASparseVector source) {
        int length = source.length();
        if (length == 0) {
            return Vector0.INSTANCE;
        }
        int[] indexes = source.nonZeroIndices();
        double[] vals = source.nonZeroValues();
        return SparseImmutableVector.wrap(length, Index.wrap(indexes), vals);
    }

    public static AVector createFromRow(AMatrix m, int row) {
        if (m instanceof AVectorMatrix) {
            return SparseImmutableVector.create(m.getRow(row));
        }
        return SparseImmutableVector.create(m.getRow(row));
    }

    @Override
    public int nonSparseElementCount() {
        return this.dataLength;
    }

    @Override
    public boolean isZero() {
        return false;
    }

    @Override
    public double maxAbsElement() {
        double result = this.data[0];
        for (int i = 1; i < this.dataLength; ++i) {
            double d = Math.abs(this.data[i]);
            if (d <= result) continue;
            result = d;
        }
        return result;
    }

    @Override
    public int maxElementIndex() {
        int ind;
        double result = this.data[0];
        int di = 0;
        for (int i = 1; i < this.dataLength; ++i) {
            double d = this.data[i];
            if (d <= result) continue;
            result = d;
            di = i;
        }
        if (result < 0.0 && (ind = this.index.findMissing()) >= 0) {
            return ind;
        }
        return this.index.get(di);
    }

    @Override
    public int maxAbsElementIndex() {
        double result = Math.abs(this.data[0]);
        int di = 0;
        for (int i = 1; i < this.dataLength; ++i) {
            double d = Math.abs(this.data[i]);
            if (d <= result) continue;
            result = d;
            di = i;
        }
        return this.index.get(di);
    }

    @Override
    public int minElementIndex() {
        int ind;
        double result = this.data[0];
        int di = 0;
        for (int i = 1; i < this.dataLength; ++i) {
            double d = this.data[i];
            if (d >= result) continue;
            result = d;
            di = i;
        }
        if (result > 0.0 && (ind = this.index.findMissing()) >= 0) {
            return ind;
        }
        return this.index.get(di);
    }

    @Override
    public void negate() {
        throw new UnsupportedOperationException(ErrorMessages.immutable(this));
    }

    @Override
    public void applyOp(Op op) {
        throw new UnsupportedOperationException(ErrorMessages.immutable(this));
    }

    @Override
    public void abs() {
        throw new UnsupportedOperationException(ErrorMessages.immutable(this));
    }

    @Override
    public double get(int i) {
        this.checkIndex(i);
        return this.unsafeGet(i);
    }

    @Override
    public double unsafeGet(int i) {
        int ip = this.index.indexPosition(i);
        if (ip < 0) {
            return 0.0;
        }
        return this.data[ip];
    }

    @Override
    public boolean isFullyMutable() {
        return false;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public long nonZeroCount() {
        return this.dataLength;
    }

    @Override
    public int[] nonZeroIndices() {
        return (int[])this.index.data.clone();
    }

    @Override
    public void add(ASparseVector v) {
        throw new UnsupportedOperationException(ErrorMessages.immutable(this));
    }

    @Override
    public void set(AVector v) {
        throw new UnsupportedOperationException(ErrorMessages.immutable(this));
    }

    @Override
    public void set(int i, double value) {
        throw new UnsupportedOperationException(ErrorMessages.immutable(this));
    }

    @Override
    public void addAt(int i, double value) {
        throw new UnsupportedOperationException(ErrorMessages.immutable(this));
    }

    @Override
    public Vector nonSparseValues() {
        return Vector.wrap(this.data);
    }

    @Override
    public double[] nonZeroValues() {
        return DoubleArrays.copyOf(this.data);
    }

    @Override
    public Index nonSparseIndex() {
        return this.index;
    }

    @Override
    public boolean includesIndex(int i) {
        return this.index.indexPosition(i) >= 0;
    }

    @Override
    public Vector dense() {
        Vector v = Vector.createLength(this.length);
        this.addToArray(v.data, 0);
        return v;
    }

    @Override
    public SparseIndexedVector mutable() {
        return SparseIndexedVector.create(this.length, this.index, this.data);
    }

    @Override
    public SparseIndexedVector clone() {
        return SparseIndexedVector.create(this.length, this.index, this.data);
    }

    @Override
    public SparseIndexedVector sparseClone() {
        return SparseIndexedVector.create(this.length, this.index, this.data);
    }

    @Override
    public SparseImmutableVector exactClone() {
        return new SparseImmutableVector(this.length, this.index.clone(), (double[])this.data.clone());
    }

    @Override
    public void validate() {
        if (this.data.length == 0) {
            throw new VectorzException("SparseImmutableVector must have some non-zero values");
        }
        if (this.index.length() != this.data.length) {
            throw new VectorzException("Inconsistent data and index!");
        }
        if (!this.index.isDistinctSorted()) {
            throw new VectorzException("Invalid index: " + this.index);
        }
        for (int i = 0; i < this.data.length; ++i) {
            if (this.data[i] != 0.0) continue;
            throw new VectorzException("Should be no zero values in data array!");
        }
        super.validate();
    }
}

