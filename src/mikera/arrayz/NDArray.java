/*
 * Decompiled with CFR 0_129.
 */
package mikera.arrayz;

import java.nio.DoubleBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import mikera.arrayz.Array;
import mikera.arrayz.Arrayz;
import mikera.arrayz.INDArray;
import mikera.arrayz.impl.BaseNDArray;
import mikera.arrayz.impl.IStridedArray;
import mikera.arrayz.impl.ImmutableArray;
import mikera.matrixx.Matrix;
import mikera.vectorz.AVector;
import mikera.vectorz.IOperator;
import mikera.vectorz.Op;
import mikera.vectorz.Vector;
import mikera.vectorz.Vectorz;
import mikera.vectorz.impl.ArrayIndexScalar;
import mikera.vectorz.impl.ArraySubVector;
import mikera.vectorz.impl.SingleDoubleIterator;
import mikera.vectorz.impl.Vector0;
import mikera.vectorz.util.ErrorMessages;
import mikera.vectorz.util.IntArrays;
import mikera.vectorz.util.VectorzException;

public final class NDArray
extends BaseNDArray {
    private static final long serialVersionUID = -262272579159731240L;

    /* varargs */ NDArray(int ... shape) {
        super(new double[(int)IntArrays.arrayProduct(shape)], shape.length, 0, shape, IntArrays.calcStrides(shape));
    }

    NDArray(double[] data, int offset, int[] shape, int[] stride) {
        this(data, shape.length, offset, shape, stride);
    }

    NDArray(double[] data, int dimensions, int offset, int[] shape, int[] stride) {
        super(data, shape.length, offset, shape, stride);
    }

    public static NDArray wrap(double[] data, int[] shape) {
        int dims = shape.length;
        return new NDArray(data, dims, 0, shape, IntArrays.calcStrides(shape));
    }

    public static NDArray wrap(Vector v) {
        return NDArray.wrap(v.getArray(), v.getShape());
    }

    public static NDArray wrap(Matrix m) {
        return NDArray.wrap(m.data, m.getShape());
    }

    public static NDArray wrap(IStridedArray a) {
        return new NDArray(a.getArray(), a.getArrayOffset(), a.getShape(), a.getStrides());
    }

    public static NDArray wrap(INDArray a) {
        if (!(a instanceof IStridedArray)) {
            throw new IllegalArgumentException(a.getClass() + " is not a strided array!");
        }
        return NDArray.wrap((IStridedArray)a);
    }

    public static /* varargs */ NDArray newArray(int ... shape) {
        return new NDArray(shape);
    }

    @Override
    public void set(double value) {
        if (this.dimensions == 0) {
            this.data[this.offset] = value;
        } else if (this.dimensions == 1) {
            int n = this.sliceCount();
            int st = this.getStride(0);
            for (int i = 0; i < n; ++i) {
                this.data[this.offset + i * st] = value;
            }
        } else {
            for (INDArray s : this.getSlices()) {
                s.set(value);
            }
        }
    }

    @Override
    public void set(int x, double value) {
        if (this.dimensions != 1) {
            throw new UnsupportedOperationException(ErrorMessages.invalidIndex((INDArray)this, (long)x));
        }
        this.data[this.offset + x * this.getStride((int)0)] = value;
    }

    @Override
    public void set(int x, int y, double value) {
        if (this.dimensions != 2) {
            throw new UnsupportedOperationException(ErrorMessages.invalidIndex((INDArray)this, x, y));
        }
        this.data[this.offset + x * this.getStride((int)0) + y * this.getStride((int)1)] = value;
    }

    @Override
    public void set(int[] indexes, double value) {
        int ix = this.offset;
        if (indexes.length != this.dimensions) {
            throw new IllegalArgumentException(ErrorMessages.invalidIndex((INDArray)this, indexes));
        }
        for (int i = 0; i < this.dimensions; ++i) {
            ix += indexes[i] * this.getStride(i);
        }
        this.data[ix] = value;
    }

    @Override
    public INDArray getTranspose() {
        return this.getTransposeView();
    }

    @Override
    public INDArray getTransposeView() {
        return Arrayz.wrapStrided(this.data, this.offset, IntArrays.reverse(this.shape), IntArrays.reverse(this.stride));
    }

    @Override
    public AVector asVector() {
        if (this.dimensions == 0) {
            return ArraySubVector.wrap(this.data, this.offset, 1);
        }
        if (this.dimensions == 1) {
            return Vectorz.wrapStrided(this.data, this.offset, this.getShape(0), this.getStride(0));
        }
        AVector v = Vector0.INSTANCE;
        int n = this.sliceCount();
        for (int i = 0; i < n; ++i) {
            v = v.join(this.slice(i).asVector());
        }
        return v;
    }

    @Override
    public /* varargs */ INDArray reshape(int ... dimensions) {
        return super.reshape(dimensions);
    }

    @Override
    public /* varargs */ INDArray broadcast(int ... dimensions) {
        return super.broadcast(dimensions);
    }

    @Override
    public INDArray slice(int majorSlice) {
        if (this.dimensions == 0) {
            throw new IllegalArgumentException("Can't slice a 0-d NDArray");
        }
        if (this.dimensions == 1) {
            return new ArrayIndexScalar(this.data, this.offset + majorSlice * this.getStride(0));
        }
        if (this.dimensions == 2) {
            int st = this.stride[1];
            return Vectorz.wrapStrided(this.data, this.offset + majorSlice * this.getStride(0), this.getShape(1), st);
        }
        return Arrayz.wrapStrided(this.data, this.offset + majorSlice * this.getStride(0), Arrays.copyOfRange(this.shape, 1, this.dimensions), Arrays.copyOfRange(this.stride, 1, this.dimensions));
    }

    @Override
    public INDArray slice(int dimension, int index) {
        this.checkDimension(dimension);
        if (dimension == 0) {
            return this.slice(index);
        }
        if (this.dimensions == 2) {
            return Vectorz.wrapStrided(this.data, this.offset + index * this.getStride(1), this.getShape(0), this.getStride(0));
        }
        return Arrayz.wrapStrided(this.data, this.offset + index * this.stride[dimension], IntArrays.removeIndex(this.shape, index), IntArrays.removeIndex(this.stride, index));
    }

    @Override
    public NDArray subArray(int[] offsets, int[] shape) {
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
        return new NDArray(this.data, this.offset + IntArrays.dotProduct(offsets, this.stride), IntArrays.copyOf(shape), this.stride);
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public boolean isFullyMutable() {
        return true;
    }

    @Override
    public boolean isElementConstrained() {
        return false;
    }

    @Override
    public boolean isView() {
        return !this.isPackedArray();
    }

    @Override
    public void applyOp(Op op) {
        if (this.dimensions == 0) {
            this.data[this.offset] = op.apply(this.data[this.offset]);
        } else if (this.dimensions == 1) {
            int len = this.sliceCount();
            int st = this.getStride(0);
            if (st == 1) {
                op.applyTo(this.data, this.offset, len);
            } else {
                for (int i = 0; i < len; ++i) {
                    this.data[this.offset + i * st] = op.apply(this.data[this.offset + i * st]);
                }
            }
        } else {
            int n = this.sliceCount();
            for (int i = 0; i < n; ++i) {
                this.slice(i).applyOp(op);
            }
        }
    }

    @Override
    public void applyOp(IOperator op) {
        this.applyOp((Op)op);
    }

    public boolean equals(NDArray a) {
        if (this.dimensions != a.dimensions) {
            return false;
        }
        if (this.dimensions == 0) {
            return this.get() == a.get();
        }
        int sc = this.sliceCount();
        if (a.sliceCount() != sc) {
            return false;
        }
        for (int i = 0; i < sc; ++i) {
            if (this.slice(i).equals(a.slice(i))) continue;
            return false;
        }
        return true;
    }

    @Override
    public boolean equals(INDArray a) {
        if (a instanceof NDArray) {
            return this.equals((NDArray)a);
        }
        if (this.dimensions != a.dimensionality()) {
            return false;
        }
        if (this.dimensions == 0) {
            return this.get() == a.get();
        }
        int sc = this.sliceCount();
        if (a.sliceCount() != sc) {
            return false;
        }
        for (int i = 0; i < sc; ++i) {
            if (this.slice(i).equals(a.slice(i))) continue;
            return false;
        }
        return true;
    }

    @Override
    public NDArray exactClone() {
        NDArray c = new NDArray((double[])this.data.clone(), this.offset, (int[])this.shape.clone(), (int[])this.stride.clone());
        return c;
    }

    @Override
    public INDArray clone() {
        return Array.create(this);
    }

    @Override
    public void multiply(double d) {
        if (this.dimensions == 0) {
            double[] arrd = this.data;
            int n = this.offset;
            arrd[n] = arrd[n] * d;
        } else if (this.dimensions == 1) {
            int n = this.sliceCount();
            for (int i = 0; i < n; ++i) {
                double[] arrd = this.data;
                int n2 = this.offset + i * this.getStride(0);
                arrd[n2] = arrd[n2] * d;
            }
        } else {
            int n = this.sliceCount();
            for (int i = 0; i < n; ++i) {
                this.slice(i).scale(d);
            }
        }
    }

    @Override
    public void setElements(int pos, double[] values, int offset, int length) {
        if (length == 0) {
            return;
        }
        if (this.dimensions == 0) {
            if (length != 1) {
                throw new IllegalArgumentException("Must have one element!");
            }
            if (pos != 0) {
                throw new IllegalArgumentException("Element index out of bounds: " + pos);
            }
            this.data[this.offset] = values[offset];
        } else if (this.dimensions == 1) {
            this.asVector().setElements(pos, values, offset, length);
        } else {
            super.setElements(pos, values, offset, length);
        }
    }

    @Override
    public void toDoubleBuffer(DoubleBuffer dest) {
        if (this.dimensions == 0) {
            dest.put(this.data[this.offset]);
        } else if (this.isPackedArray()) {
            dest.put(this.data, 0, this.data.length);
        } else {
            int sc = this.sliceCount();
            for (int i = 0; i < sc; ++i) {
                INDArray s = this.slice(i);
                s.toDoubleBuffer(dest);
            }
        }
    }

    @Override
    public double[] asDoubleArray() {
        return this.isPackedArray() ? this.data : null;
    }

    @Override
    public List<INDArray> getSlices() {
        if (this.dimensions == 0) {
            throw new IllegalArgumentException(ErrorMessages.noSlices(this));
        }
        int n = this.getShape(0);
        ArrayList<INDArray> al = new ArrayList<INDArray>(n);
        for (int i = 0; i < n; ++i) {
            al.add(this.slice(i));
        }
        return al;
    }

    @Override
    public Iterator<Double> elementIterator() {
        if (this.dimensionality() == 0) {
            return new SingleDoubleIterator(this.data[this.offset]);
        }
        return super.elementIterator();
    }

    @Override
    public void validate() {
        if (this.dimensions > this.shape.length) {
            throw new VectorzException("Insufficient shape data");
        }
        if (this.dimensions > this.stride.length) {
            throw new VectorzException("Insufficient stride data");
        }
        if (this.offset < 0 || this.offset >= this.data.length) {
            throw new VectorzException("Offset out of bounds");
        }
        int[] endIndex = IntArrays.decrementAll(this.shape);
        int endOffset = this.offset + IntArrays.dotProduct(endIndex, this.stride);
        if (endOffset < 0 || endOffset > this.data.length) {
            throw new VectorzException("End offset out of bounds");
        }
        super.validate();
    }

    @Override
    public INDArray immutable() {
        return ImmutableArray.create(this);
    }

    @Override
    public double[] getArray() {
        return this.data;
    }

    public static INDArray wrapStrided(double[] data, int offset, int[] shape, int[] strides) {
        return new NDArray(data, offset, shape, strides);
    }
}

