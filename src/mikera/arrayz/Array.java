/*
 * Decompiled with CFR 0_129.
 */
package mikera.arrayz;

import java.nio.DoubleBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import mikera.arrayz.INDArray;
import mikera.arrayz.NDArray;
import mikera.arrayz.impl.BaseShapedArray;
import mikera.arrayz.impl.IDenseArray;
import mikera.arrayz.impl.IStridedArray;
import mikera.arrayz.impl.ImmutableArray;
import mikera.indexz.Index;
import mikera.matrixx.Matrix;
import mikera.vectorz.AVector;
import mikera.vectorz.IOperator;
import mikera.vectorz.Op;
import mikera.vectorz.Scalar;
import mikera.vectorz.Tools;
import mikera.vectorz.Vector;
import mikera.vectorz.Vectorz;
import mikera.vectorz.impl.ArrayIndexScalar;
import mikera.vectorz.impl.StridedElementIterator;
import mikera.vectorz.util.DoubleArrays;
import mikera.vectorz.util.ErrorMessages;
import mikera.vectorz.util.IntArrays;
import mikera.vectorz.util.VectorzException;

public final class Array
extends BaseShapedArray
implements IStridedArray,
IDenseArray {
    private static final long serialVersionUID = -8636720562647069034L;
    private final int dimensions;
    private final int[] strides;
    private final double[] data;

    private Array(int dims, int[] shape, int[] strides) {
        super(shape);
        this.dimensions = dims;
        this.strides = strides;
        int n = (int)IntArrays.arrayProduct(shape);
        this.data = new double[n];
    }

    private Array(int[] shape, double[] data) {
        this(shape.length, shape, IntArrays.calcStrides(shape), data);
    }

    private Array(int dims, int[] shape, double[] data) {
        this(dims, shape, IntArrays.calcStrides(shape), data);
    }

    public static /* varargs */ INDArray wrap(double[] data, int ... shape) {
        long ec = IntArrays.arrayProduct(shape);
        if ((long)data.length != ec) {
            throw new IllegalArgumentException("Data array does not have correct number of elements, expected: " + ec);
        }
        return new Array(shape.length, shape, data);
    }

    private Array(int dims, int[] shape, int[] strides, double[] data) {
        super(shape);
        this.dimensions = dims;
        this.strides = strides;
        this.data = data;
    }

    public static Array wrap(Vector v) {
        return new Array(v.getShape(), v.getArray());
    }

    public static Array wrap(Matrix m) {
        return new Array(m.getShape(), m.getArray());
    }

    public static /* varargs */ Array newArray(int ... shape) {
        return new Array(shape.length, shape, Array.createStorage(shape));
    }

    public static Array create(INDArray a) {
        int[] shape = a.getShape();
        return new Array(a.dimensionality(), shape, a.toDoubleArray());
    }

    public static /* varargs */ double[] createStorage(int ... shape) {
        long ec = 1L;
        for (int i = 0; i < shape.length; ++i) {
            int si = shape[i];
            if (ec * (long)si != (long)((int)ec * si)) {
                throw new IllegalArgumentException(ErrorMessages.tooManyElements(shape));
            }
            ec *= (long)shape[i];
        }
        int n = (int)ec;
        if (ec != (long)n) {
            throw new IllegalArgumentException(ErrorMessages.tooManyElements(shape));
        }
        return new double[n];
    }

    @Override
    public int dimensionality() {
        return this.dimensions;
    }

    @Override
    protected final void checkDimension(int dimension) {
        if (dimension < 0 || dimension >= this.dimensions) {
            throw new IndexOutOfBoundsException(ErrorMessages.invalidDimension(this, dimension));
        }
    }

    @Override
    public long[] getLongShape() {
        long[] lshape = new long[this.dimensions];
        IntArrays.copyIntsToLongs(this.shape, lshape);
        return lshape;
    }

    @Override
    public int getStride(int dim) {
        return this.strides[dim];
    }

    public /* varargs */ int getIndex(int ... indexes) {
        int ix = 0;
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
        this.data[this.getIndex((int[])indexes)] = value;
    }

    @Override
    public Vector asVector() {
        return Vector.wrap(this.data);
    }

    @Override
    public Vector toVector() {
        return Vector.create(this.data);
    }

    @Override
    public INDArray slice(int majorSlice) {
        return this.slice(0, majorSlice);
    }

    @Override
    public INDArray slice(int dimension, int index) {
        this.checkDimension(dimension);
        if (this.dimensions == 1) {
            return ArrayIndexScalar.wrap(this.data, index);
        }
        if (this.dimensions == 2) {
            if (dimension == 0) {
                return Vectorz.wrap(this.data, index * this.shape[1], this.shape[1]);
            }
            return Vectorz.wrapStrided(this.data, index, this.shape[0], this.strides[0]);
        }
        int offset = index * this.getStride(dimension);
        return new NDArray(this.data, offset, IntArrays.removeIndex(this.shape, dimension), IntArrays.removeIndex(this.strides, dimension));
    }

    @Override
    public INDArray getTranspose() {
        return this.getTransposeView();
    }

    @Override
    public INDArray getTransposeView() {
        return NDArray.wrapStrided(this.data, 0, IntArrays.reverse(this.shape), IntArrays.reverse(this.strides));
    }

    @Override
    public INDArray subArray(int[] offsets, int[] shape) {
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
        int[] strides = IntArrays.calcStrides(this.shape);
        return new NDArray(this.data, IntArrays.dotProduct(offsets, strides), IntArrays.copyOf(shape), strides);
    }

    @Override
    public long elementCount() {
        return this.data.length;
    }

    @Override
    public double elementSum() {
        return DoubleArrays.elementSum(this.data);
    }

    @Override
    public double elementMax() {
        return DoubleArrays.elementMax(this.data);
    }

    @Override
    public double elementMin() {
        return DoubleArrays.elementMin(this.data);
    }

    @Override
    public double elementSquaredSum() {
        return DoubleArrays.elementSquaredSum(this.data);
    }

    @Override
    public void abs() {
        DoubleArrays.abs(this.data);
    }

    @Override
    public void signum() {
        DoubleArrays.signum(this.data);
    }

    @Override
    public void square() {
        DoubleArrays.square(this.data);
    }

    @Override
    public void exp() {
        DoubleArrays.exp(this.data);
    }

    @Override
    public void log() {
        DoubleArrays.log(this.data);
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
        return false;
    }

    @Override
    public void applyOp(Op op) {
        op.applyTo(this.data);
    }

    @Override
    public void applyOp(IOperator op) {
        if (op instanceof Op) {
            ((Op)op).applyTo(this.data);
        } else {
            for (int i = 0; i < this.data.length; ++i) {
                this.data[i] = op.apply(this.data[i]);
            }
        }
    }

    @Override
    public boolean equals(INDArray a) {
        if (a instanceof Array) {
            return this.equals((Array)a);
        }
        if (!this.isSameShape(a)) {
            return false;
        }
        return a.equalsArray(this.data, 0);
    }

    public boolean equals(Array a) {
        if (a.dimensions != this.dimensions) {
            return false;
        }
        if (!IntArrays.equals(this.shape, a.shape)) {
            return false;
        }
        return DoubleArrays.equals(this.data, a.data);
    }

    @Override
    public Array exactClone() {
        return new Array(this.dimensions, this.shape, this.strides, (double[])this.data.clone());
    }

    @Override
    public void setElements(int pos, double[] values, int offset, int length) {
        System.arraycopy(values, offset, this.data, pos, length);
    }

    @Override
    public void getElements(double[] values, int offset) {
        System.arraycopy(this.data, 0, values, offset, this.data.length);
    }

    @Override
    public Iterator<Double> elementIterator() {
        return new StridedElementIterator(this.data, 0, (int)this.elementCount(), 1);
    }

    @Override
    public void multiply(double factor) {
        DoubleArrays.multiply(this.data, 0, this.data.length, factor);
    }

    @Override
    public List<?> getSlices() {
        if (this.dimensions == 1) {
            int n = this.sliceCount();
            ArrayList<Double> al = new ArrayList<Double>(n);
            for (int i = 0; i < n; ++i) {
                al.add(this.get(i));
            }
            return al;
        }
        return super.getSliceViews();
    }

    @Override
    public void toDoubleBuffer(DoubleBuffer dest) {
        dest.put(this.data);
    }

    @Override
    public double[] toDoubleArray() {
        return DoubleArrays.copyOf(this.data);
    }

    @Override
    public double[] asDoubleArray() {
        return this.data;
    }

    @Override
    public INDArray clone() {
        switch (this.dimensions) {
            case 0: {
                return Scalar.create(this.data[0]);
            }
            case 1: {
                return Vector.create(this.data);
            }
            case 2: {
                return Matrix.wrap(this.shape[0], this.shape[1], DoubleArrays.copyOf(this.data));
            }
        }
        return Array.wrap(DoubleArrays.copyOf(this.data), this.shape);
    }

    @Override
    public void validate() {
        super.validate();
        if (this.dimensions != this.shape.length) {
            throw new VectorzException("Inconsistent dimensionality");
        }
        if (this.dimensions > 0 && this.strides[this.dimensions - 1] != 1) {
            throw new VectorzException("Last stride should be 1");
        }
        if ((long)this.data.length != IntArrays.arrayProduct(this.shape)) {
            throw new VectorzException("Inconsistent shape");
        }
        if (!IntArrays.equals(this.strides, IntArrays.calcStrides(this.shape))) {
            throw new VectorzException("Inconsistent strides");
        }
    }

    public static /* varargs */ Array createFromVector(AVector a, int ... shape) {
        Array m = Array.newArray(shape);
        int n = (int)Math.min(m.elementCount(), (long)a.length());
        a.copyTo(0, m.data, 0, n);
        return m;
    }

    @Override
    public double[] getArray() {
        return this.data;
    }

    @Override
    public int getArrayOffset() {
        return 0;
    }

    @Override
    public int[] getStrides() {
        return this.strides;
    }

    @Override
    public boolean isPackedArray() {
        return true;
    }

    @Override
    public boolean isZero() {
        return DoubleArrays.isZero(this.data);
    }

    @Override
    public INDArray immutable() {
        return ImmutableArray.wrap(DoubleArrays.copyOf(this.data), this.shape);
    }

    @Override
    public double get() {
        if (this.dimensions == 0) {
            return this.data[0];
        }
        throw new IllegalArgumentException("O-d get not supported on Array of shape: " + Index.of(this.getShape()).toString());
    }

    @Override
    public double get(int x) {
        if (this.dimensions == 1) {
            return this.data[x];
        }
        throw new IllegalArgumentException("1-d get not supported on Array of shape: " + Index.of(this.getShape()).toString());
    }

    @Override
    public double get(int x, int y) {
        if (this.dimensions == 2) {
            return this.data[x * this.strides[0] + y];
        }
        throw new IllegalArgumentException("2-d get not supported on Array of shape: " + Index.of(this.getShape()).toString());
    }

    @Override
    public boolean equalsArray(double[] data, int offset) {
        return DoubleArrays.equals(this.data, 0, data, offset, Tools.toInt(this.elementCount()));
    }
}

