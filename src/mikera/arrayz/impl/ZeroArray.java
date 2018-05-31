/*
 * Decompiled with CFR 0_129.
 */
package mikera.arrayz.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import mikera.arrayz.Arrayz;
import mikera.arrayz.INDArray;
import mikera.arrayz.ISparse;
import mikera.arrayz.impl.BaseShapedArray;
import mikera.arrayz.impl.SliceArray;
import mikera.matrixx.Matrixx;
import mikera.matrixx.impl.ZeroMatrix;
import mikera.vectorz.AVector;
import mikera.vectorz.Vectorz;
import mikera.vectorz.impl.ImmutableScalar;
import mikera.vectorz.impl.ZeroVector;
import mikera.vectorz.util.DoubleArrays;
import mikera.vectorz.util.ErrorMessages;
import mikera.vectorz.util.IntArrays;

public final class ZeroArray
extends BaseShapedArray
implements ISparse {
    private static final long serialVersionUID = 7355257027343666183L;
    private INDArray sliceValue;

    private ZeroArray(int[] shape) {
        super(shape);
        int dims = this.dimensionality();
        switch (dims) {
            case 1: {
                this.sliceValue = ImmutableScalar.ZERO;
                break;
            }
            case 2: {
                this.sliceValue = ZeroVector.create(shape[1]);
                break;
            }
            case 3: {
                this.sliceValue = ZeroMatrix.create(shape[1], shape[2]);
                break;
            }
            default: {
                this.sliceValue = ZeroArray.wrap(IntArrays.removeIndex(shape, 0));
            }
        }
    }

    public static /* varargs */ ZeroArray wrap(int ... shape) {
        return new ZeroArray(shape);
    }

    public static /* varargs */ ZeroArray create(int ... shape) {
        return new ZeroArray((int[])shape.clone());
    }

    @Override
    public long nonZeroCount() {
        return 0L;
    }

    @Override
    public /* varargs */ double get(int ... indexes) {
        if (!IntArrays.validIndex(indexes, this.shape)) {
            throw new IndexOutOfBoundsException(ErrorMessages.invalidIndex((INDArray)this, indexes));
        }
        return 0.0;
    }

    @Override
    public double get() {
        if (this.shape.length != 0) {
            throw new IllegalArgumentException(ErrorMessages.invalidIndex((INDArray)this, new int[0]));
        }
        return 0.0;
    }

    @Override
    public double get(int x) {
        if (this.shape.length != 1) {
            throw new IllegalArgumentException(ErrorMessages.invalidIndex((INDArray)this, new int[0]));
        }
        if (x < 0 || x >= this.shape[0]) {
            throw new IndexOutOfBoundsException(ErrorMessages.invalidIndex((INDArray)this, (long)x));
        }
        return 0.0;
    }

    @Override
    public double get(int x, int y) {
        if (this.shape.length != 2) {
            throw new IllegalArgumentException(ErrorMessages.invalidIndex((INDArray)this, new int[0]));
        }
        if (x < 0 || x >= this.shape[0]) {
            throw new IndexOutOfBoundsException(ErrorMessages.invalidIndex((INDArray)this, x, y));
        }
        if (y < 0 || y >= this.shape[1]) {
            throw new IndexOutOfBoundsException(ErrorMessages.invalidIndex((INDArray)this, x, y));
        }
        return 0.0;
    }

    @Override
    public void set(int[] indexes, double value) {
        throw new UnsupportedOperationException(ErrorMessages.immutable(this));
    }

    @Override
    public INDArray slice(int majorSlice) {
        if (majorSlice < 0 || majorSlice >= this.shape[0]) {
            throw new IndexOutOfBoundsException(ErrorMessages.invalidSlice(this, majorSlice));
        }
        return this.sliceValue;
    }

    @Override
    public INDArray slice(int dimension, int index) {
        if (dimension == 0) {
            return this.slice(index);
        }
        return Arrayz.createZeroArray(IntArrays.removeIndex(this.shape, dimension));
    }

    @Override
    public List<INDArray> getSlices() {
        int sc = this.sliceCount();
        if (sc == 0) {
            return Collections.emptyList();
        }
        ArrayList<INDArray> al = new ArrayList<INDArray>(sc);
        INDArray z = this.slice(0);
        for (int i = 0; i < sc; ++i) {
            al.add(z);
        }
        return al;
    }

    @Override
    public boolean isView() {
        return false;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public boolean isZero() {
        return true;
    }

    @Override
    public ZeroArray getTranspose() {
        return ZeroArray.wrap(IntArrays.reverse(this.shape));
    }

    @Override
    public void addToArray(double[] data, int offset) {
    }

    @Override
    public INDArray addCopy(INDArray a) {
        return a.broadcastCopyLike(this);
    }

    @Override
    public boolean equalsArray(double[] data, int offset) {
        return DoubleArrays.isZero(data, offset, (int)this.elementCount());
    }

    @Override
    public boolean equals(INDArray a) {
        if (!a.isSameShape(this)) {
            return false;
        }
        return a.isZero();
    }

    @Override
    public AVector asVector() {
        return Vectorz.createZeroVector(this.elementCount());
    }

    @Override
    public INDArray clone() {
        return Arrayz.newArray(this.shape);
    }

    @Override
    public INDArray sparseClone() {
        switch (this.dimensionality()) {
            case 0: {
                return ImmutableScalar.ZERO;
            }
            case 1: {
                return Vectorz.createSparseMutable(this.shape[0]);
            }
            case 2: {
                return Matrixx.createSparseRows(this);
            }
        }
        int n = this.sliceCount();
        ArrayList<INDArray> al = new ArrayList<INDArray>(n);
        for (int i = 0; i < n; ++i) {
            al.add(this.slice(i).sparseClone());
        }
        return SliceArray.create(al);
    }

    @Override
    public ZeroArray exactClone() {
        return ZeroArray.create(this.shape);
    }

    @Override
    public boolean hasUncountable() {
        return false;
    }

    @Override
    public double elementPowSum(double p) {
        return 0.0;
    }

    @Override
    public double elementAbsPowSum(double p) {
        return this.elementPowSum(p);
    }
}

