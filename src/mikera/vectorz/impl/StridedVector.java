/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz.impl;

import mikera.arrayz.INDArray;
import mikera.vectorz.AVector;
import mikera.vectorz.impl.ADenseArrayVector;
import mikera.vectorz.impl.AStridedVector;
import mikera.vectorz.impl.ArraySubVector;
import mikera.vectorz.impl.Vector0;
import mikera.vectorz.util.VectorzException;

public final class StridedVector
extends AStridedVector {
    private static final long serialVersionUID = 5807998427323932401L;
    private final int offset;
    private final int stride;

    private StridedVector(double[] data, int offset, int length, int stride) {
        int lastOffset;
        super(length, data);
        if (offset < 0) {
            throw new IndexOutOfBoundsException();
        }
        if (length > 0 && ((lastOffset = offset + (length - 1) * stride) >= data.length || lastOffset < 0)) {
            throw new IndexOutOfBoundsException("StridedVector ends outside array");
        }
        this.offset = offset;
        this.stride = stride;
    }

    public static StridedVector wrapStrided(double[] data, int offset, int length, int stride) {
        return new StridedVector(data, offset, length, stride);
    }

    public static StridedVector wrap(double[] data, int offset, int length, int stride) {
        return StridedVector.wrapStrided(data, offset, length, stride);
    }

    @Override
    public boolean isView() {
        return true;
    }

    @Override
    public boolean isFullyMutable() {
        return true;
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public double dotProduct(AVector v) {
        int length = this.checkLength(v.length());
        if (v instanceof ADenseArrayVector) {
            ADenseArrayVector av = (ADenseArrayVector)v;
            return this.dotProduct(av.getArray(), av.getArrayOffset());
        }
        double result = 0.0;
        for (int i = 0; i < length; ++i) {
            result += this.data[this.offset + i * this.stride] * v.unsafeGet(i);
        }
        return result;
    }

    @Override
    public double dotProduct(double[] ds, int off) {
        double result = 0.0;
        for (int i = 0; i < this.length; ++i) {
            result += this.data[this.offset + i * this.stride] * ds[i + off];
        }
        return result;
    }

    @Override
    public void set(AVector v) {
        int length = this.checkSameLength(v);
        v.copyTo(0, this.data, this.offset, length, this.stride);
    }

    @Override
    public void add(AVector v) {
        if (v instanceof AStridedVector) {
            this.add((AStridedVector)v);
            return;
        }
        super.add(v);
    }

    public void add(AStridedVector v) {
        int length = this.checkLength(v.length());
        double[] vdata = v.getArray();
        int voffset = v.getArrayOffset();
        int vstride = v.getStride();
        for (int i = 0; i < length; ++i) {
            double[] arrd = this.data;
            int n = this.offset + i * this.stride;
            arrd[n] = arrd[n] + vdata[voffset + i * vstride];
        }
    }

    @Override
    public int getStride() {
        return this.stride;
    }

    @Override
    public int getArrayOffset() {
        return this.offset;
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
        if (length == 1) {
            return ArraySubVector.wrap(this.data, this.offset + start * this.stride, 1);
        }
        return StridedVector.wrapStrided(this.data, this.offset + start * this.stride, length, this.stride);
    }

    @Override
    public double get(int i) {
        this.checkIndex(i);
        return this.data[this.offset + i * this.stride];
    }

    @Override
    public void set(int i, double value) {
        this.checkIndex(i);
        this.data[this.offset + i * this.stride] = value;
    }

    @Override
    public double unsafeGet(int i) {
        return this.data[this.offset + i * this.stride];
    }

    @Override
    public void unsafeSet(int i, double value) {
        this.data[this.offset + i * this.stride] = value;
    }

    @Override
    public void addAt(int i, double value) {
        double[] arrd = this.data;
        int n = this.offset + i * this.stride;
        arrd[n] = arrd[n] + value;
    }

    @Override
    public void getElements(double[] dest, int destOffset) {
        for (int i = 0; i < this.length; ++i) {
            dest[destOffset + i] = this.data[this.offset + i * this.stride];
        }
    }

    @Override
    public StridedVector exactClone() {
        double[] data = (double[])this.data.clone();
        return StridedVector.wrapStrided(data, this.offset, this.length, this.stride);
    }

    @Override
    public void validate() {
        if (this.length > 0) {
            if (this.offset < 0 || this.offset >= this.data.length) {
                throw new VectorzException("offset out of bounds: " + this.offset);
            }
            int lastIndex = this.offset + this.stride * (this.length - 1);
            if (lastIndex < 0 || lastIndex >= this.data.length) {
                throw new VectorzException("lastIndex out of bounds: " + lastIndex);
            }
        }
        super.validate();
    }

    @Override
    protected int index(int i) {
        return this.offset + i * this.stride;
    }
}

