/*
 * Decompiled with CFR 0_129.
 */
package mikera.arrayz.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import mikera.arrayz.Array;
import mikera.arrayz.Arrayz;
import mikera.arrayz.INDArray;
import mikera.arrayz.impl.BaseShapedArray;
import mikera.matrixx.Matrix;
import mikera.vectorz.AVector;
import mikera.vectorz.IOperator;
import mikera.vectorz.Op;
import mikera.vectorz.Vector;
import mikera.vectorz.impl.Vector0;
import mikera.vectorz.util.IntArrays;
import mikera.vectorz.util.VectorzException;

public final class SliceArray<T extends INDArray>
extends BaseShapedArray {
    private static final long serialVersionUID = -2343678749417219155L;
    private final long[] longShape;
    private final T[] slices;

    private SliceArray(int[] shape, T[] slices) {
        super(shape);
        this.slices = slices;
        int dims = shape.length;
        this.longShape = new long[dims];
        for (int i = 0; i < dims; ++i) {
            this.longShape[i] = shape[i];
        }
    }

    public static <T extends INDArray> SliceArray<T> create(INDArray a) {
        return new SliceArray(a.getShape(), a.toSliceArray());
    }

    public static /* varargs */ <T extends INDArray> SliceArray<T> of(T ... slices) {
        return new SliceArray(IntArrays.consArray(slices.length, slices[0].getShape()), (INDArray[])slices.clone());
    }

    public static SliceArray<INDArray> repeat(INDArray s, int n) {
        ArrayList<INDArray> al = new ArrayList<INDArray>(n);
        for (int i = 0; i < n; ++i) {
            al.add(s);
        }
        return SliceArray.create(al);
    }

    public static SliceArray<INDArray> create(List<INDArray> slices) {
        int slen = slices.size();
        if (slen == 0) {
            throw new IllegalArgumentException("Empty list of slices provided to SliceArray");
        }
        INDArray[] arr = new INDArray[slen];
        return new SliceArray(IntArrays.consArray(slen, slices.get(0).getShape()), slices.toArray(arr));
    }

    public static SliceArray<INDArray> create(List<INDArray> slices, int[] shape) {
        int slen = slices.size();
        INDArray[] arr = new INDArray[slen];
        return new SliceArray(shape, slices.toArray(arr));
    }

    @Override
    public int dimensionality() {
        return this.shape.length;
    }

    @Override
    public long[] getLongShape() {
        return this.longShape;
    }

    @Override
    public /* varargs */ double get(int ... indexes) {
        int d = indexes.length;
        T slice = this.slices[indexes[0]];
        switch (d) {
            case 0: {
                throw new VectorzException("Can't do 0D get on SliceArray!");
            }
            case 1: {
                return slice.get();
            }
            case 2: {
                return slice.get(indexes[1]);
            }
            case 3: {
                return slice.get(indexes[1], indexes[2]);
            }
        }
        return slice.get(Arrays.copyOfRange(indexes, 1, d));
    }

    @Override
    public void set(double value) {
        for (T s : this.slices) {
            s.set(value);
        }
    }

    @Override
    public void fill(double value) {
        for (T s : this.slices) {
            s.fill(value);
        }
    }

    @Override
    public void set(int[] indexes, double value) {
        int d = indexes.length;
        if (d == 0) {
            this.set(value);
        }
        T slice = this.slices[indexes[0]];
        switch (d) {
            case 0: {
                throw new VectorzException("Can't do 0D set on SliceArray!");
            }
            case 1: {
                slice.set(value);
                return;
            }
            case 2: {
                slice.set(indexes[1], value);
                return;
            }
            case 3: {
                slice.set(indexes[1], indexes[2], value);
                return;
            }
        }
        slice.set(Arrays.copyOfRange(indexes, 1, d), value);
    }

    @Override
    public AVector asVector() {
        AVector v = Vector0.INSTANCE;
        for (T a : this.slices) {
            v = v.join(a.asVector());
        }
        return v;
    }

    @Override
    public /* varargs */ INDArray reshape(int ... dimensions) {
        return Arrayz.createFromVector(this.asVector(), dimensions);
    }

    public T slice(int majorSlice) {
        return this.slices[majorSlice];
    }

    @Override
    public int componentCount() {
        return this.sliceCount();
    }

    public T getComponent(int k) {
        return this.slices[k];
    }

    @Override
    public INDArray slice(int dimension, int index) {
        this.checkDimension(dimension);
        if (dimension == 0) {
            return this.slice(index);
        }
        ArrayList<INDArray> al = new ArrayList<INDArray>(this.sliceCount());
        for (INDArray s : this) {
            al.add(s.slice(dimension - 1, index));
        }
        return SliceArray.create(al);
    }

    @Override
    public long elementCount() {
        return IntArrays.arrayProduct(this.shape);
    }

    @Override
    public INDArray innerProduct(INDArray a) {
        int dims = this.dimensionality();
        switch (dims) {
            case 0: {
                a = a.clone();
                a.scale(this.get());
                return a;
            }
            case 1: {
                return this.toVector().innerProduct(a);
            }
            case 2: {
                return Matrix.create(this).innerProduct(a);
            }
        }
        int n = this.sliceCount();
        ArrayList<INDArray> al = new ArrayList<INDArray>(n);
        for (INDArray s : this) {
            al.add(s.innerProduct(a));
        }
        return Arrayz.create(al);
    }

    @Override
    public INDArray outerProduct(INDArray a) {
        int n = this.sliceCount();
        ArrayList<INDArray> al = new ArrayList<INDArray>(n);
        for (INDArray s : this) {
            al.add(s.outerProduct(a));
        }
        return Arrayz.create(al);
    }

    @Override
    public boolean isMutable() {
        for (T a : this.slices) {
            if (!a.isMutable()) continue;
            return true;
        }
        return false;
    }

    @Override
    public boolean isFullyMutable() {
        for (T a : this.slices) {
            if (a.isFullyMutable()) continue;
            return false;
        }
        return true;
    }

    @Override
    public boolean isZero() {
        for (T a : this.slices) {
            if (a.isZero()) continue;
            return false;
        }
        return true;
    }

    @Override
    public boolean isBoolean() {
        for (T a : this.slices) {
            if (a.isBoolean()) continue;
            return false;
        }
        return true;
    }

    @Override
    public boolean isElementConstrained() {
        for (T a : this.slices) {
            if (!a.isElementConstrained()) continue;
            return true;
        }
        return false;
    }

    @Override
    public boolean isView() {
        return true;
    }

    @Override
    public void applyOp(Op op) {
        for (T a : this.slices) {
            a.applyOp(op);
        }
    }

    @Override
    public void applyOp(IOperator op) {
        for (T a : this.slices) {
            a.applyOp(op);
        }
    }

    @Override
    public void multiply(double d) {
        for (T a : this.slices) {
            a.scale(d);
        }
    }

    @Override
    public boolean equals(INDArray a) {
        if (!Arrays.equals(a.getShape(), this.getShape())) {
            return false;
        }
        for (int i = 0; i < this.slices.length; ++i) {
            if (this.slices[i].equals(a.slice(i))) continue;
            return false;
        }
        return true;
    }

    @Override
    public SliceArray<T> exactClone() {
        INDArray[] newSlices = (INDArray[])this.slices.clone();
        for (int i = 0; i < this.slices.length; ++i) {
            newSlices[i] = newSlices[i].exactClone();
        }
        return new SliceArray(this.shape, newSlices);
    }

    @Override
    public List<?> getSlices() {
        int n = this.sliceCount();
        ArrayList<Serializable> al = new ArrayList<Serializable>(n);
        if (this.dimensionality() == 1) {
            for (int i = 0; i < n; ++i) {
                al.add((Double)this.slices[i].get());
            }
            return al;
        }
        for (INDArray sl : this) {
            al.add(sl);
        }
        return al;
    }

    @Override
    public INDArray[] toSliceArray() {
        int n = this.sliceCount();
        INDArray[] al = new INDArray[n];
        for (int i = 0; i < n; ++i) {
            al[i] = this.slice(i);
        }
        return al;
    }

    @Override
    public INDArray[] getComponents() {
        return this.toSliceArray();
    }

    @Override
    public double[] toDoubleArray() {
        double[] result = Array.createStorage(this.getShape());
        int skip = (int)this.slice(0).elementCount();
        for (int i = 0; i < this.slices.length; ++i) {
            T s = this.slices[i];
            if (s.isSparse()) {
                s.addToArray(result, skip * i);
                continue;
            }
            s.getElements(result, skip * i);
        }
        return result;
    }

    @Override
    public boolean equalsArray(double[] values, int offset) {
        int skip = (int)this.slice(0).elementCount();
        int di = offset;
        for (int i = 0; i < this.slices.length; ++i) {
            if (!this.slices[i].equalsArray(values, di)) {
                return false;
            }
            di += skip;
        }
        return true;
    }

    @Override
    public void validate() {
        if (this.shape.length != this.longShape.length) {
            throw new VectorzException("Shape mismatch");
        }
        long ec = 0L;
        for (int i = 0; i < this.slices.length; ++i) {
            T s = this.slices[i];
            ec += s.elementCount();
            this.slices[i].validate();
            int[] ss = s.getShape();
            for (int j = 0; j < ss.length; ++j) {
                if (this.getShape(j + 1) == ss[j]) continue;
                throw new VectorzException("Slice shape mismatch");
            }
        }
        if (ec != this.elementCount()) {
            throw new VectorzException("Element count mismatch");
        }
        super.validate();
    }

    @Override
    public double get() {
        throw new IllegalArgumentException("0d get not supported on " + this.getClass());
    }

    @Override
    public double get(int x) {
        return this.slices[x].get();
    }

    @Override
    public double get(int x, int y) {
        return this.slices[x].get(y);
    }
}

