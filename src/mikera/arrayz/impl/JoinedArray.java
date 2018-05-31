/*
 * Decompiled with CFR 0_129.
 */
package mikera.arrayz.impl;

import mikera.arrayz.INDArray;
import mikera.arrayz.impl.BaseShapedArray;
import mikera.vectorz.util.ErrorMessages;

public class JoinedArray
extends BaseShapedArray {
    private static final long serialVersionUID = 4929988077055768422L;
    final INDArray left;
    final INDArray right;
    final int dimension;
    final int split;

    private JoinedArray(INDArray left, INDArray right, int dim) {
        super(left.getShapeClone());
        this.left = left;
        this.right = right;
        this.dimension = dim;
        this.split = this.shape[this.dimension];
        int[] arrn = this.shape;
        int n = this.dimension;
        arrn[n] = arrn[n] + right.getShape(this.dimension);
    }

    public static JoinedArray join(INDArray a, INDArray b, int dim) {
        int n = a.dimensionality();
        if (b.dimensionality() != n) {
            throw new IllegalArgumentException(ErrorMessages.incompatibleShapes(a, b));
        }
        for (int i = 0; i < n; ++i) {
            if (i == dim || a.getShape(i) == b.getShape(i)) continue;
            throw new IllegalArgumentException(ErrorMessages.incompatibleShapes(a, b));
        }
        return new JoinedArray(a, b, dim);
    }

    @Override
    public /* varargs */ double get(int ... indexes) {
        if (indexes.length != this.dimensionality()) {
            throw new IllegalArgumentException(ErrorMessages.invalidIndex((INDArray)this, indexes));
        }
        int di = indexes[this.dimension];
        if (di < this.split) {
            return this.left.get(indexes);
        }
        int[] arrn = indexes = (int[])indexes.clone();
        int n = this.dimension;
        arrn[n] = arrn[n] - this.split;
        return this.right.get(indexes);
    }

    @Override
    public void set(int[] indexes, double value) {
        if (indexes.length != this.dimensionality()) {
            throw new IllegalArgumentException(ErrorMessages.invalidIndex((INDArray)this, indexes));
        }
        int di = indexes[this.dimension];
        if (di < this.split) {
            this.left.set(indexes, value);
        } else {
            int[] arrn = indexes = (int[])indexes.clone();
            int n = this.dimension;
            arrn[n] = arrn[n] - this.split;
            this.right.set(indexes, value);
        }
    }

    @Override
    public INDArray slice(int majorSlice) {
        if (this.dimension == 0) {
            return majorSlice < this.split ? this.left.slice(majorSlice) : this.right.slice(majorSlice - this.split);
        }
        return new JoinedArray(this.left.slice(majorSlice), this.right.slice(majorSlice), this.dimension - 1);
    }

    @Override
    public int componentCount() {
        return 2;
    }

    @Override
    public INDArray getComponent(int k) {
        switch (k) {
            case 0: {
                return this.left;
            }
            case 1: {
                return this.right;
            }
        }
        throw new IndexOutOfBoundsException(ErrorMessages.invalidComponent(this, k));
    }

    @Override
    public INDArray slice(int dimension, int index) {
        if (this.dimension == dimension) {
            return index < this.split ? this.left.slice(dimension, index) : this.right.slice(dimension, index - this.split);
        }
        if (dimension == 0) {
            return this.slice(index);
        }
        int nd = dimension < this.dimension ? dimension : dimension - 1;
        return this.left.slice(dimension, index).join(this.right.slice(dimension, index), nd);
    }

    @Override
    public boolean isView() {
        return true;
    }

    @Override
    public INDArray exactClone() {
        return new JoinedArray(this.left.exactClone(), this.right.exactClone(), this.dimension);
    }

    @Override
    public void validate() {
        if (this.left.getShape(this.dimension) + this.right.getShape(this.dimension) != this.shape[this.dimension]) {
            throw new Error("Inconsistent shape along split dimension");
        }
        super.validate();
    }

    @Override
    public double get() {
        throw new IllegalArgumentException("0d get not supported on " + this.getClass());
    }

    @Override
    public double get(int x) {
        if (x < 0 || x >= this.sliceCount()) {
            throw new IndexOutOfBoundsException(ErrorMessages.invalidIndex((INDArray)this, (long)x));
        }
        if (x < this.split) {
            return this.left.get(x);
        }
        return this.right.get(x - this.split);
    }

    @Override
    public double get(int x, int y) {
        if (this.dimension == 0) {
            if (x < 0 || x >= this.sliceCount()) {
                throw new IndexOutOfBoundsException(ErrorMessages.invalidIndex((INDArray)this, x, y));
            }
            if (x < this.split) {
                return this.left.get(x, y);
            }
            return this.right.get(x - this.split, y);
        }
        if (y < 0 || y >= this.sliceCount()) {
            throw new IndexOutOfBoundsException(ErrorMessages.invalidIndex((INDArray)this, x, y));
        }
        if (y < this.split) {
            return this.left.get(x, y);
        }
        return this.right.get(x, y - this.split);
    }

    @Override
    public boolean equalsArray(double[] data, int offset) {
        return this.left.equalsArray(data, offset) && this.right.equalsArray(data, (int)((long)offset + this.left.elementCount()));
    }
}

