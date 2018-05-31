/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz.impl;

import java.nio.DoubleBuffer;
import java.util.Iterator;
import mikera.arrayz.INDArray;
import mikera.arrayz.impl.IDense;
import mikera.randomz.Hash;
import mikera.vectorz.AScalar;
import mikera.vectorz.AVector;
import mikera.vectorz.Vector;
import mikera.vectorz.impl.AArrayVector;
import mikera.vectorz.impl.ImmutableScalar;
import mikera.vectorz.impl.SparseImmutableVector;
import mikera.vectorz.impl.StridedElementIterator;
import mikera.vectorz.impl.Vector0;
import mikera.vectorz.util.DoubleArrays;
import mikera.vectorz.util.ErrorMessages;
import mikera.vectorz.util.VectorzException;

public class ImmutableVector
extends AArrayVector
implements IDense {
    private static final long serialVersionUID = -3679147880242779555L;
    public final int offset;

    private ImmutableVector(double[] data) {
        this(data, 0, data.length);
    }

    public static /* varargs */ ImmutableVector of(double ... data) {
        return ImmutableVector.wrap((double[])data.clone());
    }

    private ImmutableVector(double[] data, int offset, int length) {
        super(length, data);
        this.offset = offset;
    }

    public static ImmutableVector create(double[] data) {
        return ImmutableVector.wrap(DoubleArrays.copyOf(data));
    }

    public static ImmutableVector create(AVector v) {
        int length = v.length();
        double[] data = new double[length];
        v.getElements(data, 0);
        return new ImmutableVector(data, 0, length);
    }

    public static ImmutableVector wrap(double[] data) {
        return new ImmutableVector(data, 0, data.length);
    }

    public static ImmutableVector wrap(double[] data, int offset, int length) {
        if (offset < 0 || length < 0 || offset + length > data.length) {
            throw new IndexOutOfBoundsException();
        }
        return new ImmutableVector(data, offset, length);
    }

    public static ImmutableVector wrap(Vector source) {
        double[] data = source.data;
        return new ImmutableVector(data, 0, data.length);
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
    public boolean isView() {
        return false;
    }

    @Override
    public boolean isZero() {
        return DoubleArrays.isZero(this.data, this.offset, this.length);
    }

    @Override
    public boolean isRangeZero(int start, int length) {
        return DoubleArrays.isZero(this.data, this.offset + start, length);
    }

    @Override
    public final ImmutableScalar slice(int i) {
        return ImmutableScalar.create(this.get(i));
    }

    @Override
    public AVector subVector(int start, int length) {
        int len = this.checkRange(start, length);
        if (length == 0) {
            return Vector0.INSTANCE;
        }
        if (length == len) {
            return this;
        }
        return new ImmutableVector(this.data, this.offset + start, length);
    }

    @Override
    public void toDoubleBuffer(DoubleBuffer dest) {
        dest.put(this.data, this.offset, this.length());
    }

    @Override
    public double[] toDoubleArray() {
        return DoubleArrays.copyOf(this.data, this.offset, this.length);
    }

    @Override
    public void getElements(double[] data, int offset) {
        System.arraycopy(this.data, this.offset, data, offset, this.length());
    }

    @Override
    public void copyTo(int offset, double[] dest, int destOffset, int length) {
        System.arraycopy(this.data, this.offset + offset, dest, destOffset, length);
    }

    @Override
    public void multiplyTo(double[] data, int offset) {
        DoubleArrays.arraymultiply(this.data, this.offset, data, offset, this.length());
    }

    @Override
    public void addToArray(double[] array, int offset) {
        this.addToArray(0, array, offset, this.length());
    }

    @Override
    public void addToArray(int offset, double[] array, int arrayOffset, int length) {
        DoubleArrays.add(this.data, offset + this.offset, array, arrayOffset, length);
    }

    @Override
    public void addMultipleToArray(double factor, int offset, double[] array, int arrayOffset, int length) {
        int dataOffset = this.offset + offset;
        DoubleArrays.addMultiple(array, arrayOffset, this.data, dataOffset, length, factor);
    }

    @Override
    public void divideTo(double[] data, int offset) {
        DoubleArrays.arraydivide(this.data, this.offset, data, offset, this.length());
    }

    @Override
    public double dotProduct(double[] data, int offset) {
        return DoubleArrays.dotProduct(this.data, this.offset, data, offset, this.length());
    }

    @Override
    public double dotProduct(AVector v) {
        this.checkSameLength(v);
        return v.dotProduct(this.data, this.offset);
    }

    @Override
    public double magnitudeSquared() {
        return DoubleArrays.elementSquaredSum(this.data, this.offset, this.length);
    }

    @Override
    public double get(int i) {
        this.checkIndex(i);
        return this.data[this.offset + i];
    }

    @Override
    public void set(int i, double value) {
        throw new UnsupportedOperationException(ErrorMessages.immutable(this));
    }

    @Override
    public double unsafeGet(int i) {
        return this.data[this.offset + i];
    }

    @Override
    public void unsafeSet(int i, double value) {
        throw new UnsupportedOperationException(ErrorMessages.immutable(this));
    }

    @Override
    public void addAt(int i, double v) {
        throw new UnsupportedOperationException(ErrorMessages.immutable(this));
    }

    @Override
    public Iterator<Double> iterator() {
        return new StridedElementIterator(this.data, this.offset, this.length, 1);
    }

    @Override
    public int hashCode() {
        int hashCode = 1;
        for (int i = 0; i < this.length; ++i) {
            hashCode = 31 * hashCode + Hash.hashCode(this.data[this.offset + i]);
        }
        return hashCode;
    }

    @Override
    public boolean equals(AVector v) {
        if (v.length() != this.length) {
            return false;
        }
        return v.equalsArray(this.data, this.offset);
    }

    @Override
    public boolean equalsArray(double[] data, int offset) {
        return DoubleArrays.equals(data, offset, this.data, this.offset, this.length());
    }

    @Override
    public Vector clone() {
        return Vector.wrap(this.toDoubleArray());
    }

    @Override
    public AVector sparse() {
        return SparseImmutableVector.create(this);
    }

    @Override
    public AVector exactClone() {
        return new ImmutableVector(this.data, this.offset, this.length);
    }

    @Override
    public AVector immutable() {
        return this;
    }

    @Override
    public void validate() {
        if (this.offset < 0 || this.offset + this.length > this.data.length || this.length < 0) {
            throw new VectorzException("ImmutableVector data out of bounds");
        }
        super.validate();
    }

    @Override
    protected int index(int i) {
        return this.offset + i;
    }
}

