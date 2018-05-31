/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz.impl;

import mikera.arrayz.INDArray;
import mikera.arrayz.impl.IDenseArray;
import mikera.arrayz.impl.IStridedArray;
import mikera.vectorz.AScalar;
import mikera.vectorz.AVector;
import mikera.vectorz.Scalar;
import mikera.vectorz.impl.ArraySubVector;
import mikera.vectorz.impl.ImmutableScalar;
import mikera.vectorz.util.IntArrays;
import mikera.vectorz.util.VectorzException;

public class ArrayIndexScalar
extends AScalar
implements IStridedArray,
IDenseArray {
    private static final long serialVersionUID = 5928615452582152522L;
    final double[] array;
    final int index;

    public ArrayIndexScalar(double[] array, int index) {
        this.array = array;
        this.index = index;
    }

    public static ArrayIndexScalar wrap(double[] array, int index) {
        return new ArrayIndexScalar(array, index);
    }

    @Override
    public double get() {
        return this.array[this.index];
    }

    @Override
    public void set(double value) {
        this.array[this.index] = value;
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
    public Scalar clone() {
        return Scalar.create(this.array[this.index]);
    }

    @Override
    public ArrayIndexScalar exactClone() {
        return new ArrayIndexScalar((double[])this.array.clone(), this.index);
    }

    @Override
    public void validate() {
        if (this.index < 0 || this.index >= this.array.length) {
            throw new VectorzException("Index out of bounds");
        }
        super.validate();
    }

    @Override
    public double[] getArray() {
        return this.array;
    }

    @Override
    public int getArrayOffset() {
        return this.index;
    }

    @Override
    public int[] getStrides() {
        return IntArrays.EMPTY_INT_ARRAY;
    }

    @Override
    public int getStride(int dimension) {
        throw new IndexOutOfBoundsException("Can't access strides for a scalar");
    }

    @Override
    public boolean isPackedArray() {
        return this.index == 0 && this.array.length == 1;
    }

    @Override
    public double[] asDoubleArray() {
        return this.isPackedArray() ? this.array : null;
    }

    @Override
    public ArrayIndexScalar mutable() {
        return this;
    }

    @Override
    public ArraySubVector asVector() {
        return ArraySubVector.wrap(this.array, this.index, 1);
    }

    @Override
    public ImmutableScalar immutable() {
        return ImmutableScalar.create(this.get());
    }
}

