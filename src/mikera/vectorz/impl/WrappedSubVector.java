/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz.impl;

import java.util.Iterator;
import mikera.arrayz.INDArray;
import mikera.vectorz.AVector;
import mikera.vectorz.impl.ASizedVector;
import mikera.vectorz.impl.Vector0;
import mikera.vectorz.impl.VectorIterator;

public final class WrappedSubVector
extends ASizedVector {
    private static final long serialVersionUID = 2323553136938665228L;
    private final AVector wrapped;
    private final int offset;

    private WrappedSubVector(AVector source, int offset, int length) {
        super(length);
        if (source instanceof WrappedSubVector) {
            WrappedSubVector v = (WrappedSubVector)source;
            this.wrapped = v.wrapped;
            this.offset = offset + v.offset;
        } else {
            this.wrapped = source;
            this.offset = offset;
        }
    }

    public static WrappedSubVector wrap(AVector source, int offset, int length) {
        return new WrappedSubVector(source, offset, length);
    }

    @Override
    public Iterator<Double> iterator() {
        return new VectorIterator(this.wrapped, this.offset, this.length);
    }

    @Override
    public boolean isFullyMutable() {
        return this.wrapped.isFullyMutable();
    }

    @Override
    public boolean isElementConstrained() {
        return this.wrapped.isElementConstrained();
    }

    @Override
    public boolean isView() {
        return true;
    }

    @Override
    public boolean isZero() {
        return this.wrapped.isRangeZero(this.offset, this.length);
    }

    @Override
    public boolean isRangeZero(int start, int length) {
        return this.wrapped.isRangeZero(this.offset + start, length);
    }

    @Override
    public double get(int i) {
        this.checkIndex(i);
        return this.wrapped.unsafeGet(i + this.offset);
    }

    @Override
    public void set(int i, double value) {
        this.checkIndex(i);
        this.wrapped.unsafeSet(i + this.offset, value);
    }

    @Override
    public double unsafeGet(int i) {
        return this.wrapped.unsafeGet(i + this.offset);
    }

    @Override
    public void unsafeSet(int i, double value) {
        this.wrapped.unsafeSet(i + this.offset, value);
    }

    @Override
    public void add(AVector src, int offset) {
        this.wrapped.add(this.offset, src, offset, this.length);
    }

    @Override
    public void addToArray(int offset, double[] array, int arrayOffset, int length) {
        this.wrapped.addToArray(this.offset + offset, array, arrayOffset, length);
    }

    @Override
    public AVector subVector(int offset, int length) {
        int len = this.checkRange(offset, length);
        if (length == 0) {
            return Vector0.INSTANCE;
        }
        if (len == length) {
            return this;
        }
        return this.wrapped.subVector(this.offset + offset, length);
    }

    @Override
    public void copyTo(int offset, AVector dest, int destOffset, int length) {
        this.wrapped.copyTo(this.offset + offset, dest, destOffset, length);
    }

    @Override
    public void copyTo(int offset, double[] dest, int destOffset, int length) {
        this.wrapped.copyTo(this.offset + offset, dest, destOffset, length);
    }

    @Override
    public void copyTo(int offset, double[] dest, int destOffset, int length, int stride) {
        this.wrapped.copyTo(this.offset + offset, dest, destOffset, length, stride);
    }

    @Override
    public AVector tryEfficientJoin(AVector a) {
        if (a instanceof WrappedSubVector) {
            return this.tryEfficientJoin((WrappedSubVector)a);
        }
        return null;
    }

    private AVector tryEfficientJoin(WrappedSubVector a) {
        if (a.wrapped == this.wrapped && a.offset == this.offset + this.length) {
            int newLength = this.length + a.length;
            if (this.offset == 0 && newLength == this.wrapped.length()) {
                return this.wrapped;
            }
            return new WrappedSubVector(this.wrapped, this.offset, newLength);
        }
        return null;
    }

    @Override
    public WrappedSubVector exactClone() {
        return new WrappedSubVector(this.wrapped.exactClone(), this.offset, this.length);
    }

    @Override
    public void addAt(int i, double v) {
        this.wrapped.addAt(this.offset + i, v);
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

