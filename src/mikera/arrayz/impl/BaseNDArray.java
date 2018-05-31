/*
 * Decompiled with CFR 0_129.
 */
package mikera.arrayz.impl;

import mikera.arrayz.INDArray;
import mikera.arrayz.impl.BaseShapedArray;
import mikera.arrayz.impl.IStridedArray;
import mikera.vectorz.util.ErrorMessages;
import mikera.vectorz.util.IntArrays;

public abstract class BaseNDArray
extends BaseShapedArray
implements IStridedArray {
    private static final long serialVersionUID = -4221161437647016169L;
    protected final int dimensions;
    protected int offset;
    protected final double[] data;
    protected final int[] stride;

    protected BaseNDArray(double[] data, int dimensions, int offset, int[] shape, int[] stride) {
        super(shape);
        this.data = data;
        this.offset = offset;
        this.stride = stride;
        this.dimensions = dimensions;
    }

    @Override
    public int dimensionality() {
        return this.dimensions;
    }

    @Override
    public final int getStride(int dim) {
        return this.stride[dim];
    }

    @Override
    public final int getShape(int dim) {
        return this.shape[dim];
    }

    @Override
    public long[] getLongShape() {
        long[] sh = new long[this.dimensions];
        IntArrays.copyIntsToLongs(this.shape, sh);
        return sh;
    }

    public /* varargs */ int getIndex(int ... indexes) {
        int ix = this.offset;
        for (int i = 0; i < this.dimensions; ++i) {
            ix += indexes[i] * this.getStride(i);
        }
        return ix;
    }

    @Override
    public /* varargs */ double get(int ... indexes) {
        int ix = this.offset;
        for (int i = 0; i < this.dimensions; ++i) {
            ix += indexes[i] * this.getStride(i);
        }
        return this.data[ix];
    }

    @Override
    public double get() {
        if (this.dimensions == 0) {
            return this.data[this.offset];
        }
        throw new UnsupportedOperationException(ErrorMessages.invalidIndex((INDArray)this, new int[0]));
    }

    @Override
    public double get(int x) {
        if (this.dimensions == 1) {
            return this.data[this.offset + x * this.getStride(0)];
        }
        throw new UnsupportedOperationException(ErrorMessages.invalidIndex((INDArray)this, (long)x));
    }

    @Override
    public double get(int x, int y) {
        if (this.dimensions == 2) {
            return this.data[this.offset + x * this.getStride(0) + y * this.getStride(1)];
        }
        throw new UnsupportedOperationException(ErrorMessages.invalidIndex((INDArray)this, x, y));
    }

    @Override
    public boolean isPackedArray() {
        if (this.offset != 0) {
            return false;
        }
        int st = 1;
        for (int i = this.dimensions - 1; i >= 0; --i) {
            if (this.getStride(i) != st) {
                return false;
            }
            int d = this.shape[i];
            st *= d;
        }
        return st == this.data.length;
    }

    @Override
    public long elementCount() {
        return IntArrays.arrayProduct(this.shape);
    }

    @Override
    public int getArrayOffset() {
        return this.offset;
    }

    @Override
    public int[] getStrides() {
        return this.stride;
    }

    @Override
    protected final void checkDimension(int dimension) {
        if (dimension < 0 || dimension >= this.dimensions) {
            throw new IndexOutOfBoundsException(ErrorMessages.invalidDimension(this, dimension));
        }
    }
}

