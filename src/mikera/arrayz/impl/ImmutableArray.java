/*
 * Decompiled with CFR 0_129.
 */
package mikera.arrayz.impl;

import java.nio.DoubleBuffer;
import java.util.Arrays;
import mikera.arrayz.Arrayz;
import mikera.arrayz.INDArray;
import mikera.arrayz.impl.BaseNDArray;
import mikera.arrayz.impl.IDense;
import mikera.vectorz.AVector;
import mikera.vectorz.Tools;
import mikera.vectorz.impl.ImmutableScalar;
import mikera.vectorz.impl.ImmutableVector;
import mikera.vectorz.util.DoubleArrays;
import mikera.vectorz.util.ErrorMessages;
import mikera.vectorz.util.IntArrays;

public class ImmutableArray
extends BaseNDArray
implements IDense {
    private static final long serialVersionUID = 2078025371733533775L;

    private ImmutableArray(int dims, int[] shape, int[] strides) {
        this(new double[(int)IntArrays.arrayProduct(shape)], shape.length, 0, shape, strides);
    }

    private ImmutableArray(double[] data, int dimensions, int offset, int[] shape, int[] stride) {
        super(data, dimensions, offset, shape, stride);
    }

    private ImmutableArray(int[] shape, double[] data) {
        this(shape.length, shape, IntArrays.calcStrides(shape), data);
    }

    private ImmutableArray(int dims, int[] shape, double[] data) {
        this(dims, shape, IntArrays.calcStrides(shape), data);
    }

    public static INDArray wrap(double[] data, int[] shape) {
        long ec = IntArrays.arrayProduct(shape);
        if ((long)data.length != ec) {
            throw new IllegalArgumentException("Data array does not have correct number of elements, expected: " + ec);
        }
        return new ImmutableArray(shape.length, shape, data);
    }

    private ImmutableArray(int dims, int[] shape, int[] strides, double[] data) {
        this(data, dims, 0, shape, strides);
    }

    @Override
    public int dimensionality() {
        return this.dimensions;
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
    public boolean isElementConstrained() {
        return true;
    }

    @Override
    public int[] getShape() {
        return this.shape;
    }

    @Override
    public int[] getShapeClone() {
        return (int[])this.shape.clone();
    }

    @Override
    public long[] getLongShape() {
        long[] lshape = new long[this.dimensions];
        IntArrays.copyIntsToLongs(this.shape, lshape);
        return lshape;
    }

    @Override
    public /* varargs */ int getIndex(int ... indexes) {
        int ix = this.offset;
        for (int i = 0; i < this.dimensions; ++i) {
            ix += indexes[i] * this.getStride(i);
        }
        return ix;
    }

    @Override
    public /* varargs */ double get(int ... indexes) {
        return this.data[this.getIndex(indexes)];
    }

    @Override
    public void set(int[] indexes, double value) {
        throw new UnsupportedOperationException(ErrorMessages.immutable(this));
    }

    @Override
    public INDArray slice(int majorSlice) {
        if (this.dimensions == 0) {
            throw new IllegalArgumentException("Can't slice a 0-d NDArray");
        }
        if (this.dimensions == 1) {
            return ImmutableScalar.create(this.get(majorSlice));
        }
        if (this.dimensions == 2 && this.stride[1] == 1) {
            return ImmutableVector.wrap(this.data, this.offset + majorSlice * this.getStride(0), this.shape[1]);
        }
        return new ImmutableArray(this.data, this.dimensions - 1, this.offset + majorSlice * this.getStride(0), Arrays.copyOfRange(this.shape, 1, this.dimensions), Arrays.copyOfRange(this.stride, 1, this.dimensions));
    }

    @Override
    public INDArray slice(int dimension, int index) {
        this.checkDimension(dimension);
        if (dimension == 0) {
            return this.slice(index);
        }
        return new ImmutableArray(this.data, this.dimensions - 1, this.offset + index * this.stride[dimension], IntArrays.removeIndex(this.shape, index), IntArrays.removeIndex(this.stride, index));
    }

    @Override
    public int sliceCount() {
        return this.shape[0];
    }

    @Override
    public ImmutableArray subArray(int[] offsets, int[] shape) {
        int n = this.dimensions;
        if (offsets.length != n) {
            throw new IllegalArgumentException(ErrorMessages.invalidIndex((INDArray)this, offsets));
        }
        if (shape.length != n) {
            throw new IllegalArgumentException(ErrorMessages.invalidIndex((INDArray)this, offsets));
        }
        if (IntArrays.equals(shape, this.shape)) {
            if (IntArrays.isZero(offsets)) {
                return this;
            }
            throw new IllegalArgumentException("Invalid subArray offsets");
        }
        return new ImmutableArray(this.data, n, this.offset + IntArrays.dotProduct(offsets, this.stride), IntArrays.copyOf(shape), this.stride);
    }

    @Override
    public long elementCount() {
        return IntArrays.arrayProduct(this.shape);
    }

    @Override
    public boolean isView() {
        return false;
    }

    @Override
    public AVector asVector() {
        if (this.dimensions > 0) {
            return super.asVector();
        }
        return ImmutableVector.wrap(new double[]{this.data[this.offset]});
    }

    @Override
    public INDArray exactClone() {
        return new ImmutableArray((double[])this.data.clone(), this.dimensions, this.offset, (int[])this.shape.clone(), (int[])this.stride.clone());
    }

    @Override
    public INDArray sparseClone() {
        return Arrayz.createSparse(this);
    }

    @Override
    public void toDoubleBuffer(DoubleBuffer dest) {
        if (this.dimensions > 0) {
            super.toDoubleBuffer(dest);
        } else {
            dest.put(this.data[this.offset]);
        }
    }

    @Override
    public boolean equalsArray(double[] data, int offset) {
        return DoubleArrays.equals(this.data, this.offset, data, offset, Tools.toInt(this.elementCount()));
    }

    public static INDArray create(INDArray a) {
        int[] shape = a.getShape();
        int n = (int)IntArrays.arrayProduct(shape);
        double[] newData = new double[n];
        a.getElements(newData, 0);
        return ImmutableArray.wrap(newData, shape);
    }

    @Override
    public double[] getArray() {
        throw new UnsupportedOperationException("Array access not supported by ImmutableArray");
    }
}

