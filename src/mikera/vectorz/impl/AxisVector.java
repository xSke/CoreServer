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
import mikera.vectorz.Vector;
import mikera.vectorz.Vector2;
import mikera.vectorz.Vector3;
import mikera.vectorz.impl.ASingleElementVector;
import mikera.vectorz.impl.ASizedVector;
import mikera.vectorz.impl.ASparseVector;
import mikera.vectorz.impl.ImmutableScalar;
import mikera.vectorz.impl.ImmutableVector;
import mikera.vectorz.impl.SingleElementVector;
import mikera.vectorz.impl.SparseIndexedVector;
import mikera.vectorz.impl.Vector0;
import mikera.vectorz.impl.ZeroVector;
import mikera.vectorz.util.ErrorMessages;
import mikera.vectorz.util.VectorzException;

public final class AxisVector
extends ASingleElementVector {
    private static final long serialVersionUID = 6767495113060894804L;
    private static final ImmutableVector NON_SPARSE = ImmutableVector.of(1.0);

    private AxisVector(int axisIndex, int length) {
        super(axisIndex, length);
    }

    public static AxisVector create(int axisIndex, int dimensions) {
        if (axisIndex < 0 || axisIndex >= dimensions) {
            throw new IllegalArgumentException("Axis out of range");
        }
        return new AxisVector(axisIndex, dimensions);
    }

    public int axis() {
        return this.index;
    }

    @Override
    public double magnitude() {
        return 1.0;
    }

    @Override
    public double magnitudeSquared() {
        return 1.0;
    }

    @Override
    public double normalise() {
        return 1.0;
    }

    @Override
    public AVector normaliseCopy() {
        return this;
    }

    @Override
    public void square() {
    }

    @Override
    public AVector squareCopy() {
        return this;
    }

    @Override
    public void abs() {
    }

    @Override
    public AxisVector absCopy() {
        return this;
    }

    @Override
    public void sqrt() {
    }

    @Override
    public AxisVector sqrtCopy() {
        return this;
    }

    @Override
    public void signum() {
    }

    @Override
    public AxisVector signumCopy() {
        return this;
    }

    @Override
    public double elementSum() {
        return 1.0;
    }

    @Override
    public double elementProduct() {
        return this.length > 1 ? 0.0 : 1.0;
    }

    @Override
    public double elementMax() {
        return 1.0;
    }

    @Override
    public double elementMin() {
        return this.length > 1 ? 0.0 : 1.0;
    }

    @Override
    public int maxElementIndex() {
        return this.index;
    }

    @Override
    public double maxAbsElement() {
        return 1.0;
    }

    @Override
    public int maxAbsElementIndex() {
        return this.index;
    }

    @Override
    public int minElementIndex() {
        if (this.length == 1) {
            return 0;
        }
        return this.index == 0 ? 1 : 0;
    }

    @Override
    public long nonZeroCount() {
        return 1L;
    }

    @Override
    public boolean isZero() {
        return false;
    }

    @Override
    public boolean isRangeZero(int start, int length) {
        return start > this.index || start + length <= this.index;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public boolean isBoolean() {
        return true;
    }

    @Override
    public boolean isUnitLengthVector() {
        return true;
    }

    @Override
    public double dotProduct(AVector v) {
        return v.unsafeGet(this.axis());
    }

    @Override
    public double dotProduct(double[] data, int offset) {
        return data[offset + this.axis()];
    }

    @Override
    public double dotProduct(Vector v) {
        assert (this.length == v.length());
        return v.data[this.axis()];
    }

    public double dotProduct(Vector3 v) {
        switch (this.axis()) {
            case 0: {
                return v.x;
            }
            case 1: {
                return v.y;
            }
        }
        return v.z;
    }

    public double dotProduct(Vector2 v) {
        switch (this.axis()) {
            case 0: {
                return v.x;
            }
        }
        return v.y;
    }

    @Override
    public AVector innerProduct(double d) {
        return SingleElementVector.create(d, this.index, this.length);
    }

    @Override
    public Scalar innerProduct(Vector v) {
        this.checkSameLength(v);
        return Scalar.create(v.unsafeGet(this.index));
    }

    @Override
    public Scalar innerProduct(AVector v) {
        this.checkSameLength(v);
        return Scalar.create(v.unsafeGet(this.index));
    }

    @Override
    public AVector innerProduct(AMatrix m) {
        this.checkLength(m.rowCount());
        return m.getRow(this.index).copy();
    }

    @Override
    public double get(int i) {
        this.checkIndex(i);
        return i == this.axis() ? 1.0 : 0.0;
    }

    @Override
    public double unsafeGet(int i) {
        return i == this.axis() ? 1.0 : 0.0;
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
        arrd[n] = arrd[n] + 1.0;
    }

    @Override
    public void addToArray(double[] array, int offset, int stride) {
        double[] arrd = array;
        int n = offset + this.index * stride;
        arrd[n] = arrd[n] + 1.0;
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
        arrd[n] = arrd[n] + factor;
    }

    @Override
    public final ImmutableScalar slice(int i) {
        this.checkIndex(i);
        if (i == this.axis()) {
            return ImmutableScalar.ONE;
        }
        return ImmutableScalar.ZERO;
    }

    @Override
    public Vector toNormal() {
        return this.toVector();
    }

    @Override
    public double[] toDoubleArray() {
        double[] data = new double[this.length];
        data[this.index] = 1.0;
        return data;
    }

    @Override
    public Vector toVector() {
        return Vector.wrap(this.toDoubleArray());
    }

    @Override
    public AVector subVector(int start, int length) {
        int len = this.checkRange(start, length);
        if (length == len) {
            return this;
        }
        if (length == 0) {
            return Vector0.INSTANCE;
        }
        int end = start + length;
        if (start <= this.axis() && end > this.axis()) {
            return AxisVector.create(this.axis() - start, length);
        }
        return ZeroVector.create(length);
    }

    @Override
    public double density() {
        return 1.0 / (double)this.length;
    }

    @Override
    public int nonSparseElementCount() {
        return 1;
    }

    @Override
    public AVector nonSparseValues() {
        return NON_SPARSE;
    }

    @Override
    public Index nonSparseIndex() {
        return Index.of(this.axis());
    }

    @Override
    public int[] nonZeroIndices() {
        return new int[]{this.index};
    }

    @Override
    public double[] nonZeroValues() {
        return new double[]{1.0};
    }

    @Override
    public void add(ASparseVector v) {
        throw new UnsupportedOperationException(ErrorMessages.immutable(this));
    }

    @Override
    public AVector addCopy(AVector v) {
        this.checkSameLength(v);
        AVector r = v.clone();
        r.addAt(this.index, 1.0);
        return r;
    }

    @Override
    public AVector subCopy(AVector v) {
        this.checkSameLength(v);
        AVector r = v.negateCopy().mutable();
        r.addAt(this.index, 1.0);
        return r;
    }

    @Override
    public boolean includesIndex(int i) {
        return i == this.axis();
    }

    @Override
    public void set(int i, double value) {
        throw new UnsupportedOperationException(ErrorMessages.immutable(this));
    }

    @Override
    public boolean equalsArray(double[] data, int offset) {
        int i;
        if (data[offset + this.index] != 1.0) {
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
    public boolean equals(AVector v) {
        int len = v.length();
        if (len != this.length) {
            return false;
        }
        if (v.unsafeGet(this.index) != 1.0) {
            return false;
        }
        if (!v.isRangeZero(0, this.index)) {
            return false;
        }
        if (!v.isRangeZero(this.index + 1, this.length - this.index - 1)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean elementsEqual(double value) {
        return value == 1.0 && this.length == 1;
    }

    @Override
    public AxisVector exactClone() {
        return this;
    }

    @Override
    public SparseIndexedVector sparseClone() {
        return SparseIndexedVector.create(this.length, Index.of(this.index), new double[]{1.0});
    }

    @Override
    public void validate() {
        if (this.length <= 0) {
            throw new VectorzException("Axis vector length is too small: " + this.length);
        }
        if (this.axis() < 0 || this.axis() >= this.length) {
            throw new VectorzException("Axis index out of bounds");
        }
        super.validate();
    }

    @Override
    public boolean hasUncountable() {
        return false;
    }

    @Override
    public double elementPowSum(double p) {
        return 1.0;
    }

    @Override
    public double elementAbsPowSum(double p) {
        return this.elementPowSum(p);
    }

    @Override
    protected double value() {
        return 1.0;
    }
}

