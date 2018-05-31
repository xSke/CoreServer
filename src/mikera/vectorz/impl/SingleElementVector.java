/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz.impl;

import mikera.arrayz.INDArray;
import mikera.indexz.Index;
import mikera.matrixx.AMatrix;
import mikera.vectorz.AScalar;
import mikera.vectorz.AVector;
import mikera.vectorz.Scalar;
import mikera.vectorz.Vector1;
import mikera.vectorz.impl.ASingleElementVector;
import mikera.vectorz.impl.ASparseVector;
import mikera.vectorz.impl.ImmutableScalar;
import mikera.vectorz.impl.SparseIndexedVector;
import mikera.vectorz.impl.Vector0;
import mikera.vectorz.impl.VectorIndexScalar;
import mikera.vectorz.impl.ZeroVector;
import mikera.vectorz.util.ErrorMessages;
import mikera.vectorz.util.IntArrays;

public final class SingleElementVector
extends ASingleElementVector {
    final double value;

    public SingleElementVector(int componentIndex, int dimensions) {
        this(componentIndex, dimensions, 0.0);
    }

    public SingleElementVector(int componentIndex, int dimensions, double value) {
        super(componentIndex, dimensions);
        if (dimensions <= 0) {
            throw new IllegalArgumentException("SingleElementVEctor must have >= 1 dimensions");
        }
        if (componentIndex < 0 || componentIndex >= dimensions) {
            throw new IllegalArgumentException("Invalid non-zero component index: " + componentIndex);
        }
        this.value = value;
    }

    public static SingleElementVector create(double val, int i, int len) {
        return new SingleElementVector(i, len, val);
    }

    @Override
    public boolean isZero() {
        return this.value == 0.0;
    }

    @Override
    public boolean isRangeZero(int start, int length) {
        if (this.value == 0.0) {
            return true;
        }
        return start > this.index || start + length <= this.index;
    }

    @Override
    public double magnitude() {
        return this.value;
    }

    @Override
    public double elementSum() {
        return this.value;
    }

    @Override
    public double elementProduct() {
        return this.length > 1 ? 0.0 : this.value;
    }

    @Override
    public double elementMax() {
        return this.length > 1 ? Math.max(0.0, this.value) : this.value;
    }

    @Override
    public double elementMin() {
        return this.length > 1 ? Math.min(0.0, this.value) : this.value;
    }

    @Override
    public double magnitudeSquared() {
        return this.value * this.value;
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
    public boolean isElementConstrained() {
        return true;
    }

    @Override
    public double density() {
        return 1.0 / (double)this.length();
    }

    @Override
    public double get(int i) {
        if (i < 0 || i >= this.length) {
            throw new IndexOutOfBoundsException();
        }
        return i == this.index ? this.value : 0.0;
    }

    @Override
    public double unsafeGet(int i) {
        return i == this.index ? this.value : 0.0;
    }

    @Override
    public void set(int i, double value) {
        throw new UnsupportedOperationException(ErrorMessages.immutable(this));
    }

    @Override
    public void addToArray(int offset, double[] array, int arrayOffset, int length) {
        if (this.index < offset) {
            return;
        }
        if (this.index >= offset + length) {
            return;
        }
        double[] arrd = array;
        int n = arrayOffset - offset + this.index;
        arrd[n] = arrd[n] + this.value;
    }

    @Override
    public void addToArray(double[] array, int offset, int stride) {
        double[] arrd = array;
        int n = offset + this.index * stride;
        arrd[n] = arrd[n] + this.value;
    }

    @Override
    public void addMultipleToArray(double factor, int offset, double[] array, int arrayOffset, int length) {
        if (this.index < offset) {
            return;
        }
        if (this.index >= offset + length) {
            return;
        }
        double[] arrd = array;
        int n = arrayOffset - offset + this.index;
        arrd[n] = arrd[n] + this.value * factor;
    }

    @Override
    public final AScalar slice(int i) {
        if (i == this.index) {
            return VectorIndexScalar.wrap(this, i);
        }
        if (i < 0 || i >= this.length) {
            throw new IndexOutOfBoundsException(ErrorMessages.invalidIndex((INDArray)this, (long)i));
        }
        return ImmutableScalar.ZERO;
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
        if (offset > this.index || offset + length <= this.index) {
            return ZeroVector.create(length);
        }
        return SingleElementVector.create(this.value, this.index - offset, length);
    }

    @Override
    public AVector tryEfficientJoin(AVector a) {
        if (a instanceof ZeroVector) {
            return SingleElementVector.create(this.value, this.index, this.length + a.length());
        }
        return null;
    }

    @Override
    public AVector innerProduct(double d) {
        return SingleElementVector.create(this.value * d, this.index, this.length);
    }

    @Override
    public AScalar innerProduct(AVector v) {
        this.checkSameLength(v);
        return Scalar.create(this.value * v.unsafeGet(this.index));
    }

    @Override
    public AVector innerProduct(AMatrix a) {
        return a.getRow(this.index).multiplyCopy(this.value);
    }

    @Override
    public SingleElementVector exactClone() {
        return new SingleElementVector(this.index, this.length, this.value);
    }

    @Override
    public SparseIndexedVector sparseClone() {
        return SparseIndexedVector.create(this.length, Index.of(this.index), new double[]{this.value});
    }

    @Override
    public boolean equalsArray(double[] data, int offset) {
        int i;
        if (data[offset + this.index] != this.value) {
            return false;
        }
        for (i = 0; i < this.index; ++i) {
            if (data[offset + i] == 0.0) continue;
            return false;
        }
        for (i = this.index + 1; i < this.length; ++i) {
            if (data[offset + i] == 0.0) continue;
            return false;
        }
        return true;
    }

    @Override
    public int nonSparseElementCount() {
        return 1;
    }

    @Override
    public AVector nonSparseValues() {
        return Vector1.of(this.value);
    }

    @Override
    public Index nonSparseIndex() {
        return Index.of(this.index);
    }

    @Override
    public int[] nonZeroIndices() {
        if (this.value == 0.0) {
            return IntArrays.EMPTY_INT_ARRAY;
        }
        return new int[]{this.index};
    }

    @Override
    public boolean includesIndex(int i) {
        return i == this.index;
    }

    @Override
    public void add(ASparseVector v) {
        throw new UnsupportedOperationException(ErrorMessages.immutable(this));
    }

    @Override
    public boolean hasUncountable() {
        return Double.isNaN(this.value) || Double.isInfinite(this.value);
    }

    @Override
    public double elementPowSum(double p) {
        return Math.pow(this.value, p);
    }

    @Override
    public double elementAbsPowSum(double p) {
        return Math.pow(Math.abs(this.value), p);
    }

    @Override
    public double dotProduct(double[] data, int offset) {
        return this.value * data[offset + this.index];
    }

    @Override
    protected double value() {
        return this.value;
    }
}

