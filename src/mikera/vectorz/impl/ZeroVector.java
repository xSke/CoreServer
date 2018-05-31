/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz.impl;

import java.io.ObjectStreamException;
import java.util.Arrays;
import java.util.Iterator;
import mikera.arrayz.INDArray;
import mikera.indexz.Index;
import mikera.matrixx.AMatrix;
import mikera.randomz.Hash;
import mikera.vectorz.AScalar;
import mikera.vectorz.AVector;
import mikera.vectorz.Scalar;
import mikera.vectorz.Vector;
import mikera.vectorz.Vectorz;
import mikera.vectorz.impl.ASizedVector;
import mikera.vectorz.impl.ASparseVector;
import mikera.vectorz.impl.AxisVector;
import mikera.vectorz.impl.ImmutableScalar;
import mikera.vectorz.impl.RepeatedElementIterator;
import mikera.vectorz.impl.RepeatedElementVector;
import mikera.vectorz.impl.SingleElementVector;
import mikera.vectorz.impl.Vector0;
import mikera.vectorz.util.Constants;
import mikera.vectorz.util.DoubleArrays;
import mikera.vectorz.util.ErrorMessages;
import mikera.vectorz.util.IntArrays;

public final class ZeroVector
extends ASparseVector {
    private static final long serialVersionUID = -7928191943246067239L;
    private static final int ZERO_VECTOR_CACHE_SIZE = 30;
    private static final ZeroVector[] ZERO_VECTORS = new ZeroVector[30];
    private static ZeroVector last = new ZeroVector(30);

    private ZeroVector(int dimensions) {
        super(dimensions);
    }

    public static ZeroVector create(int dimensions) {
        return ZeroVector.createCached(dimensions);
    }

    public static ZeroVector createNew(int dimensions) {
        if (dimensions <= 0) {
            throw new IllegalArgumentException("Can't create length " + dimensions + " ZeroVector. Use Vector0 instead");
        }
        return new ZeroVector(dimensions);
    }

    public static ZeroVector createCached(int dimensions) {
        if (dimensions <= 0) {
            throw new IllegalArgumentException("Can't create length " + dimensions + " ZeroVector. Use Vector0 instead");
        }
        ZeroVector zv = ZeroVector.tryCreate(dimensions);
        if (zv != null) {
            return zv;
        }
        last = zv = new ZeroVector(dimensions);
        return zv;
    }

    public static ZeroVector create(INDArray array) {
        int n = Vectorz.safeLongToInt(array.elementCount());
        return ZeroVector.create(n);
    }

    private static ZeroVector tryCreate(int dimensions) {
        if (dimensions < 30) {
            return ZERO_VECTORS[dimensions];
        }
        if (dimensions == ZeroVector.last.length) {
            return last;
        }
        return null;
    }

    @Override
    public double dotProduct(AVector v) {
        this.checkSameLength(v);
        return 0.0;
    }

    @Override
    public double dotProduct(double[] data, int offset) {
        return 0.0;
    }

    @Override
    public AVector innerProduct(AMatrix m) {
        this.checkLength(m.rowCount());
        return ZeroVector.create(m.columnCount());
    }

    @Override
    public ImmutableScalar innerProduct(AVector a) {
        this.checkSameLength(a);
        return ImmutableScalar.ZERO;
    }

    @Override
    public Scalar innerProduct(Vector v) {
        this.checkSameLength(v);
        return Scalar.create(0.0);
    }

    @Override
    public ZeroVector innerProduct(double a) {
        return this;
    }

    @Override
    public double get(int i) {
        this.checkIndex(i);
        return 0.0;
    }

    @Override
    public void set(int i, double value) {
        if (0.0 != value) {
            throw new UnsupportedOperationException(ErrorMessages.immutable(this));
        }
    }

    @Override
    public double unsafeGet(int i) {
        return 0.0;
    }

    @Override
    public void unsafeSet(int i, double value) {
        if (0.0 != value) {
            throw new UnsupportedOperationException(ErrorMessages.immutable(this));
        }
    }

    @Override
    public void add(ASparseVector v) {
        throw new UnsupportedOperationException(ErrorMessages.immutable(this));
    }

    @Override
    public AVector addCopy(AVector a) {
        this.checkSameLength(a);
        return a.copy();
    }

    @Override
    public AVector subCopy(AVector a) {
        this.checkSameLength(a);
        return a.negateCopy();
    }

    @Override
    public RepeatedElementVector reciprocalCopy() {
        return RepeatedElementVector.create(this.length, Double.POSITIVE_INFINITY);
    }

    @Override
    public ZeroVector absCopy() {
        return this;
    }

    @Override
    public void multiply(AVector a) {
        this.checkSameLength(a);
    }

    @Override
    public void multiply(double factor) {
        throw new UnsupportedOperationException(ErrorMessages.immutable(this));
    }

    @Override
    public ZeroVector multiplyCopy(AVector a) {
        this.checkSameLength(a);
        return this;
    }

    @Override
    public AVector multiplyCopy(double factor) {
        return Vectorz.createRepeatedElement(this.length, factor * 0.0);
    }

    @Override
    public AVector normaliseCopy() {
        return this;
    }

    @Override
    public ZeroVector divideCopy(AVector a) {
        this.checkSameLength(a);
        return this;
    }

    @Override
    public double magnitudeSquared() {
        return 0.0;
    }

    @Override
    public double magnitude() {
        return 0.0;
    }

    @Override
    public double elementSum() {
        return 0.0;
    }

    @Override
    public double elementProduct() {
        return 0.0;
    }

    @Override
    public double elementMax() {
        return 0.0;
    }

    @Override
    public double elementMin() {
        return 0.0;
    }

    @Override
    public int maxElementIndex() {
        return 0;
    }

    @Override
    public double maxAbsElement() {
        return 0.0;
    }

    @Override
    public int maxAbsElementIndex() {
        return 0;
    }

    @Override
    public int minElementIndex() {
        return 0;
    }

    @Override
    public long nonZeroCount() {
        return 0L;
    }

    @Override
    public double[] nonZeroValues() {
        return DoubleArrays.EMPTY;
    }

    @Override
    public boolean isZero() {
        return true;
    }

    @Override
    public boolean isRangeZero(int start, int length) {
        return true;
    }

    @Override
    public boolean isBoolean() {
        return true;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public boolean isFullyMutable() {
        return false;
    }

    @Override
    public boolean isUnitLengthVector() {
        return false;
    }

    @Override
    public void addToArray(int offset, double[] array, int arrayOffset, int length) {
    }

    @Override
    public void copyTo(int offset, double[] dest, int destOffset, int length, int stride) {
        for (int i = 0; i < length; ++i) {
            dest[destOffset + i * stride] = 0.0;
        }
    }

    @Override
    public void addToArray(double[] dest, int offset, int stride) {
    }

    @Override
    public void addMultipleToArray(double factor, int offset, double[] array, int arrayOffset, int length) {
    }

    @Override
    public final ImmutableScalar slice(int i) {
        this.checkIndex(i);
        return ImmutableScalar.ZERO;
    }

    @Override
    public Iterator<Double> iterator() {
        return new RepeatedElementIterator(this.length, Constants.ZERO_DOUBLE);
    }

    @Override
    public double density() {
        return 0.0;
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
        return ZeroVector.create(length);
    }

    public ZeroVector join(ZeroVector a) {
        return ZeroVector.create(this.length + a.length);
    }

    @Override
    public AVector tryEfficientJoin(AVector a) {
        if (a instanceof ZeroVector) {
            return this.join((ZeroVector)a);
        }
        if (a instanceof AxisVector) {
            AxisVector av = (AxisVector)a;
            return AxisVector.create(av.axis() + this.length, av.length() + this.length);
        }
        if (a instanceof SingleElementVector) {
            SingleElementVector sev = (SingleElementVector)a;
            return SingleElementVector.create(sev.value, this.length + sev.index, sev.length + this.length);
        }
        return null;
    }

    @Override
    public AVector reorder(int[] order) {
        int n = order.length;
        if (n == this.length) {
            return this;
        }
        for (int i : order) {
            this.checkIndex(i);
        }
        return ZeroVector.createNew(n);
    }

    @Override
    public AVector reorder(int dim, int[] order) {
        this.checkDimension(dim);
        return this.reorder(order);
    }

    private Object readResolve() throws ObjectStreamException {
        ZeroVector zv = ZeroVector.tryCreate(this.length);
        if (zv != null) {
            return zv;
        }
        return this;
    }

    @Override
    public int nonSparseElementCount() {
        return 0;
    }

    @Override
    public AVector nonSparseValues() {
        return Vector0.INSTANCE;
    }

    @Override
    public Index nonSparseIndex() {
        return Index.EMPTY;
    }

    @Override
    public int[] nonZeroIndices() {
        return IntArrays.EMPTY_INT_ARRAY;
    }

    @Override
    public AVector squareCopy() {
        return this;
    }

    @Override
    public boolean includesIndex(int i) {
        return false;
    }

    @Override
    public int hashCode() {
        return Hash.zeroVectorHash(this.length);
    }

    @Override
    public ZeroVector exactClone() {
        return new ZeroVector(this.length);
    }

    @Override
    public AVector sparseClone() {
        return Vectorz.createSparseMutable(this.length);
    }

    @Override
    public double[] toDoubleArray() {
        return new double[this.length];
    }

    @Override
    public void getElements(double[] dest, int offset) {
        Arrays.fill(dest, offset, offset + this.length(), 0.0);
    }

    @Override
    public void getElements(Object[] dest, int offset) {
        int n = this.length();
        for (int i = 0; i < n; ++i) {
            dest[offset + i] = Constants.ZERO_DOUBLE;
        }
    }

    @Override
    public /* varargs */ AVector selectClone(int ... inds) {
        return Vectorz.newVector(inds.length);
    }

    @Override
    public /* varargs */ AVector selectView(int ... inds) {
        return Vectorz.createZeroVector(inds.length);
    }

    @Override
    public boolean equals(AVector v) {
        if (!this.isSameShape(v)) {
            return false;
        }
        return v.isZero();
    }

    @Override
    public boolean equalsArray(double[] data, int offset) {
        return DoubleArrays.isZero(data, offset, this.length);
    }

    @Override
    public boolean elementsEqual(double value) {
        return value == 0.0;
    }

    @Override
    public boolean hasUncountable() {
        return false;
    }

    @Override
    public double elementPowSum(double p) {
        return 0.0;
    }

    @Override
    public double elementAbsPowSum(double p) {
        return 0.0;
    }

    static {
        for (int i = 1; i < 30; ++i) {
            ZeroVector.ZERO_VECTORS[i] = new ZeroVector(i);
        }
    }
}

