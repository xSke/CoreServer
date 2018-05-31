/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz.impl;

import java.util.Iterator;
import mikera.arrayz.Arrayz;
import mikera.arrayz.INDArray;
import mikera.arrayz.impl.IStridedArray;
import mikera.matrixx.AMatrix;
import mikera.matrixx.Matrixx;
import mikera.matrixx.impl.StridedMatrix;
import mikera.vectorz.AVector;
import mikera.vectorz.Vector;
import mikera.vectorz.impl.AArrayVector;
import mikera.vectorz.impl.IndexedArrayVector;
import mikera.vectorz.impl.StridedElementIterator;
import mikera.vectorz.util.DoubleArrays;
import mikera.vectorz.util.ErrorMessages;

public abstract class AStridedVector
extends AArrayVector
implements IStridedArray {
    private static final long serialVersionUID = -7239429584755803950L;

    protected AStridedVector(int length, double[] data) {
        super(length, data);
    }

    @Override
    public double[] getArray() {
        return this.data;
    }

    @Override
    public abstract int getArrayOffset();

    public abstract int getStride();

    @Override
    public AStridedVector ensureMutable() {
        return this.clone();
    }

    @Override
    public boolean isRangeZero(int start, int length) {
        int stride = this.getStride();
        int offset = this.getArrayOffset() + start * stride;
        return DoubleArrays.isZero(this.data, offset, length, stride);
    }

    @Override
    public double dotProduct(double[] data, int offset) {
        double[] array = this.getArray();
        int thisOffset = this.getArrayOffset();
        int stride = this.getStride();
        int length = this.length();
        double result = 0.0;
        for (int i = 0; i < length; ++i) {
            result += array[i * stride + thisOffset] * data[i + offset];
        }
        return result;
    }

    @Override
    public double elementSum() {
        int len = this.length();
        double[] array = this.getArray();
        int offset = this.getArrayOffset();
        int stride = this.getStride();
        double result = 0.0;
        for (int i = 0; i < len; ++i) {
            result += array[offset + i * stride];
        }
        return result;
    }

    @Override
    public double elementProduct() {
        int len = this.length();
        double[] array = this.getArray();
        int offset = this.getArrayOffset();
        int stride = this.getStride();
        double result = 1.0;
        for (int i = 0; i < len; ++i) {
            result *= array[offset + i * stride];
        }
        return result;
    }

    @Override
    public double elementMax() {
        int len = this.length();
        double[] array = this.getArray();
        int offset = this.getArrayOffset();
        int stride = this.getStride();
        double max = -1.7976931348623157E308;
        for (int i = 0; i < len; ++i) {
            double d = array[offset + i * stride];
            if (d <= max) continue;
            max = d;
        }
        return max;
    }

    @Override
    public double elementMin() {
        int len = this.length();
        double[] array = this.getArray();
        int offset = this.getArrayOffset();
        int stride = this.getStride();
        double min = Double.MAX_VALUE;
        for (int i = 0; i < len; ++i) {
            double d = array[offset + i * stride];
            if (d >= min) continue;
            min = d;
        }
        return min;
    }

    @Override
    public /* varargs */ INDArray broadcast(int ... shape) {
        int dims = shape.length;
        if (dims == 0) {
            throw new IllegalArgumentException(ErrorMessages.incompatibleBroadcast((INDArray)this, shape));
        }
        if (dims == 1) {
            if (shape[0] != this.length()) {
                throw new IllegalArgumentException(ErrorMessages.incompatibleBroadcast((INDArray)this, shape));
            }
            return this;
        }
        if (dims == 2) {
            int rc = shape[0];
            int cc = shape[1];
            if (cc != this.length()) {
                throw new IllegalArgumentException(ErrorMessages.incompatibleBroadcast((INDArray)this, shape));
            }
            return Matrixx.wrapStrided(this.getArray(), rc, cc, this.getArrayOffset(), 0, this.getStride());
        }
        if (shape[dims - 1] != this.length()) {
            throw new IllegalArgumentException(ErrorMessages.incompatibleBroadcast((INDArray)this, shape));
        }
        int[] newStrides = new int[dims];
        newStrides[dims - 1] = this.getStride();
        return Arrayz.wrapStrided(this.getArray(), this.getArrayOffset(), shape, newStrides);
    }

    @Override
    public AMatrix broadcastLike(AMatrix target) {
        if (this.length() == target.columnCount()) {
            return StridedMatrix.wrap(this.getArray(), target.rowCount(), this.length(), this.getArrayOffset(), 0, this.getStride());
        }
        throw new IllegalArgumentException(ErrorMessages.incompatibleBroadcast((INDArray)this, target));
    }

    @Override
    public /* varargs */ AVector selectView(int ... inds) {
        int n = inds.length;
        int[] ix = new int[n];
        int off = this.getArrayOffset();
        int stride = this.getStride();
        for (int i = 0; i < n; ++i) {
            ix[i] = off + stride * inds[i];
        }
        return IndexedArrayVector.wrap(this.getArray(), ix);
    }

    @Override
    public AStridedVector clone() {
        return Vector.create(this);
    }

    @Override
    public void set(AVector v) {
        int length = this.checkSameLength(v);
        int stride = this.getStride();
        v.copyTo(0, this.getArray(), this.getArrayOffset(), length, stride);
    }

    @Override
    public void setElements(double[] values, int offset) {
        double[] data = this.getArray();
        int stride = this.getStride();
        int off = this.getArrayOffset();
        for (int i = 0; i < this.length; ++i) {
            data[off + i * stride] = values[offset + i];
        }
    }

    @Override
    public void setElements(int pos, double[] values, int offset, int length) {
        double[] data = this.getArray();
        int stride = this.getStride();
        int off = this.getArrayOffset() + pos * stride;
        for (int i = 0; i < length; ++i) {
            data[off + i * stride] = values[offset + i];
        }
    }

    @Override
    public void add(double[] data, int offset) {
        int stride = this.getStride();
        double[] tdata = this.getArray();
        int toffset = this.getArrayOffset();
        int length = this.length();
        for (int i = 0; i < length; ++i) {
            double[] arrd = tdata;
            int n = toffset + i * stride;
            arrd[n] = arrd[n] + data[offset + i];
        }
    }

    @Override
    public void add(int offset, AVector a) {
        int stride = this.getStride();
        a.addToArray(this.getArray(), this.getArrayOffset() + offset * stride, stride);
    }

    @Override
    public void add(int offset, AVector a, int aOffset, int length) {
        double[] tdata = this.getArray();
        int stride = this.getStride();
        int toffset = this.getArrayOffset() + offset * stride;
        a.subVector(aOffset, length).addToArray(tdata, toffset, stride);
    }

    @Override
    public void addAt(int i, double v) {
        double[] data;
        int ix = this.checkIndex(i);
        double[] arrd = data = this.getArray();
        int n = ix;
        arrd[n] = arrd[n] + v;
    }

    @Override
    public void addToArray(int offset, double[] destData, int destOffset, int length) {
        int stride = this.getStride();
        double[] tdata = this.getArray();
        int toffset = this.getArrayOffset() + offset * stride;
        for (int i = 0; i < length; ++i) {
            double[] arrd = destData;
            int n = destOffset + i;
            arrd[n] = arrd[n] + tdata[toffset + i * stride];
        }
    }

    @Override
    public void addToArray(double[] dest, int destOffset, int destStride) {
        int stride = this.getStride();
        double[] tdata = this.getArray();
        int toffset = this.getArrayOffset();
        for (int i = 0; i < this.length; ++i) {
            double[] arrd = dest;
            int n = destOffset + i * destStride;
            arrd[n] = arrd[n] + tdata[toffset + i * stride];
        }
    }

    @Override
    public void copyTo(int offset, double[] dest, int destOffset, int length, int stride) {
        int thisStride = this.getStride();
        int thisOffset = this.getArrayOffset();
        for (int i = offset; i < length; ++i) {
            dest[destOffset + i * stride] = this.data[thisOffset + i * thisStride];
        }
    }

    @Override
    public void clamp(double min, double max) {
        int len = this.length();
        int stride = this.getStride();
        double[] data = this.getArray();
        int offset = this.getArrayOffset();
        for (int i = 0; i < len; ++i) {
            int ix = offset + i * stride;
            double v = data[ix];
            if (v < min) {
                data[ix] = min;
                continue;
            }
            if (v <= max) continue;
            data[ix] = max;
        }
    }

    @Override
    public double[] asDoubleArray() {
        if (this.isPackedArray()) {
            return this.getArray();
        }
        return null;
    }

    @Override
    public boolean isPackedArray() {
        return this.getStride() == 1 && this.getArrayOffset() == 0 && this.getArray().length == this.length();
    }

    @Override
    public int[] getStrides() {
        return new int[]{this.getStride()};
    }

    @Override
    public Iterator<Double> iterator() {
        return new StridedElementIterator(this.getArray(), this.getArrayOffset(), this.length(), this.getStride());
    }

    @Override
    public int getStride(int dimension) {
        switch (dimension) {
            case 0: {
                return this.getStride();
            }
        }
        throw new IllegalArgumentException(ErrorMessages.invalidDimension(this, dimension));
    }

    @Override
    public void fill(double value) {
        int stride = this.getStride();
        double[] array = this.getArray();
        int di = this.getArrayOffset();
        for (int i = 0; i < this.length; ++i) {
            array[di] = value;
            di += stride;
        }
    }

    @Override
    public boolean equalsArray(double[] data, int offset) {
        int stride = this.getStride();
        double[] array = this.getArray();
        int di = this.getArrayOffset();
        for (int i = 0; i < this.length; ++i) {
            if (data[offset + i] != array[di]) {
                return false;
            }
            di += stride;
        }
        return true;
    }
}

