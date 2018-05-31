/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz.impl;

import java.util.Arrays;
import mikera.indexz.Index;
import mikera.matrixx.AMatrix;
import mikera.vectorz.AVector;
import mikera.vectorz.impl.ADenseArrayVector;
import mikera.vectorz.impl.ASparseVector;
import mikera.vectorz.impl.GrowableIndexedVector;
import mikera.vectorz.impl.SparseIndexedVector;
import mikera.vectorz.util.DoubleArrays;
import mikera.vectorz.util.IntArrays;
import mikera.vectorz.util.VectorzException;

public abstract class ASparseIndexedVector
extends ASparseVector {
    private static final long serialVersionUID = -8106136233328863653L;

    public ASparseIndexedVector(int length) {
        super(length);
    }

    abstract double[] internalData();

    abstract Index internalIndex();

    int[] internalIndexArray() {
        return this.internalIndex().data;
    }

    @Override
    public boolean includesIndex(int i) {
        return this.internalIndex().indexPosition(i) >= 0;
    }

    @Override
    public boolean isZero() {
        return DoubleArrays.isZero(this.internalData());
    }

    @Override
    public boolean isRangeZero(int start, int length) {
        int end = start + length;
        Index index = this.internalIndex();
        double[] data = this.internalData();
        int si = index.seekPosition(start);
        int di = index.seekPosition(end);
        for (int i = si; i < di; ++i) {
            if (data[i] == 0.0) continue;
            return false;
        }
        return true;
    }

    @Override
    public double elementSum() {
        return DoubleArrays.elementSum(this.internalData());
    }

    @Override
    public void multiply(double factor) {
        double[] data = this.internalData();
        for (int i = 0; i < data.length; ++i) {
            this.unsafeSet(i, this.unsafeGet(i) * factor);
        }
    }

    @Override
    public double magnitudeSquared() {
        return DoubleArrays.elementSquaredSum(this.internalData());
    }

    @Override
    public long nonZeroCount() {
        return DoubleArrays.nonZeroCount(this.internalData());
    }

    @Override
    public double dotProduct(double[] data, int offset) {
        double result = 0.0;
        double[] tdata = this.internalData();
        int[] ixs = this.internalIndex().data;
        for (int j = 0; j < tdata.length; ++j) {
            result += tdata[j] * data[offset + ixs[j]];
        }
        return result;
    }

    @Override
    public AVector innerProduct(AMatrix a) {
        int cc = a.columnCount();
        GrowableIndexedVector dest = GrowableIndexedVector.createLength(cc);
        for (int i = 0; i < cc; ++i) {
            double v = this.dotProduct(a.getColumn(i));
            if (v == 0.0) continue;
            dest.append(i, v);
        }
        return dest.toSparseIndexedVector();
    }

    @Override
    public final double dotProduct(AVector v) {
        if (v instanceof ADenseArrayVector) {
            return this.dotProduct((ADenseArrayVector)v);
        }
        if (v instanceof ASparseVector) {
            return this.dotProduct((ASparseVector)v);
        }
        double result = 0.0;
        double[] data = this.internalData();
        int[] ixs = this.internalIndexArray();
        for (int j = 0; j < data.length; ++j) {
            result += data[j] * v.unsafeGet(ixs[j]);
        }
        return result;
    }

    public final double dotProduct(ASparseVector v) {
        double result = 0.0;
        double[] data = this.internalData();
        int[] ixs = this.internalIndexArray();
        AVector vvalues = v.nonSparseValues();
        double[] vdata = vvalues.asDoubleArray();
        if (vdata == null) {
            vdata = vvalues.toDoubleArray();
        }
        int[] vixs = v.nonSparseIndex().data;
        if (data.length == 0) {
            return 0.0;
        }
        if (vdata.length == 0) {
            return 0.0;
        }
        int ti = 0;
        int vi = 0;
        while (ti < data.length && vi < vdata.length) {
            int tv = ixs[ti];
            int vv = vixs[vi];
            if (tv == vv) {
                result += data[ti] * vdata[vi];
                ++ti;
                ++vi;
                continue;
            }
            if (tv < vv) {
                ++ti;
                continue;
            }
            ++vi;
        }
        return result;
    }

    @Override
    public int[] nonZeroIndices() {
        int n = (int)this.nonZeroCount();
        if (n == 0) {
            return new int[0];
        }
        double[] data = this.internalData();
        Index index = this.internalIndex();
        int[] ret = new int[n];
        int di = 0;
        for (int i = 0; i < data.length; ++i) {
            if (data[i] == 0.0) continue;
            ret[di++] = index.get(i);
        }
        if (di != n) {
            throw new VectorzException("Invalid non-zero index count. Maybe concurrent modification of vector?");
        }
        return ret;
    }

    @Override
    public void addToArray(int offset, double[] array, int arrayOffset, int length) {
        assert (offset >= 0 && offset + length <= this.length);
        double[] data = this.internalData();
        Index index = this.internalIndex();
        int start = index.seekPosition(offset);
        int[] ixs = index.data;
        int dataLength = data.length;
        for (int j = start; j < dataLength; ++j) {
            int di = ixs[j] - offset;
            if (di >= length) {
                return;
            }
            double[] arrd = array;
            int n = arrayOffset + di;
            arrd[n] = arrd[n] + data[j];
        }
    }

    @Override
    public void addToArray(double[] dest, int offset) {
        double[] data = this.internalData();
        int[] ixs = this.internalIndexArray();
        int dataLength = data.length;
        for (int i = 0; i < dataLength; ++i) {
            double[] arrd = dest;
            int n = offset + ixs[i];
            arrd[n] = arrd[n] + data[i];
        }
    }

    @Override
    public void addToArray(double[] dest, int offset, int stride) {
        double[] data = this.internalData();
        int[] ixs = this.internalIndexArray();
        for (int i = 0; i < data.length; ++i) {
            double[] arrd = dest;
            int n = offset + ixs[i] * stride;
            arrd[n] = arrd[n] + data[i];
        }
    }

    @Override
    public void addMultipleToArray(double factor, int offset, double[] array, int arrayOffset, int length) {
        int start;
        int aOffset = arrayOffset - offset;
        double[] data = this.internalData();
        Index index = this.internalIndex();
        int[] ixs = index.data;
        for (int i = start = index.seekPosition((int)offset); i < data.length; ++i) {
            int di = ixs[i];
            if (di >= offset + length) {
                return;
            }
            double[] arrd = array;
            int n = di + aOffset;
            arrd[n] = arrd[n] + factor * data[i];
        }
    }

    @Override
    public void addProductToArray(double factor, int offset, AVector other, int otherOffset, double[] array, int arrayOffset, int length) {
        if (other instanceof ADenseArrayVector) {
            this.addProductToArray(factor, offset, (ADenseArrayVector)other, otherOffset, array, arrayOffset, length);
            return;
        }
        assert (offset >= 0);
        assert (offset + length <= this.length());
        double[] data = this.internalData();
        Index index = this.internalIndex();
        int[] ixs = index.data;
        int dataLength = data.length;
        for (int j = index.seekPosition((int)offset); j < dataLength; ++j) {
            int i = ixs[j] - offset;
            if (i >= length) {
                return;
            }
            double[] arrd = array;
            int n = i + arrayOffset;
            arrd[n] = arrd[n] + factor * data[j] * other.get(i + otherOffset);
        }
    }

    @Override
    public void addProductToArray(double factor, int offset, ADenseArrayVector other, int otherOffset, double[] array, int arrayOffset, int length) {
        assert (offset >= 0);
        assert (offset + length <= this.length());
        double[] otherArray = other.getArray();
        otherOffset += other.getArrayOffset();
        double[] data = this.internalData();
        Index index = this.internalIndex();
        int[] ixs = index.data;
        int dataLength = data.length;
        for (int j = index.seekPosition((int)offset); j < dataLength; ++j) {
            int i = ixs[j] - offset;
            if (i >= length) {
                return;
            }
            double[] arrd = array;
            int n = i + arrayOffset;
            arrd[n] = arrd[n] + factor * data[j] * otherArray[i + otherOffset];
        }
    }

    @Override
    public boolean equalsArray(double[] ds, int offset) {
        double[] data = this.internalData();
        int[] ixs = this.internalIndexArray();
        int n = data.length;
        if (n == 0) {
            return DoubleArrays.isZero(ds, offset, this.length);
        }
        int i = 0;
        for (int di = 0; di < n; ++di) {
            int t = ixs[di];
            while (i < t) {
                if (ds[offset + i] != 0.0) {
                    return false;
                }
                ++i;
            }
            if (ds[offset + t] != data[di]) {
                return false;
            }
            ++i;
        }
        return DoubleArrays.isZero(ds, offset + i, this.length - i);
    }

    @Override
    public boolean equals(AVector v) {
        int len = this.length();
        if (v.length() != len) {
            return false;
        }
        if (v instanceof ADenseArrayVector) {
            ADenseArrayVector vv = (ADenseArrayVector)v;
            return this.equalsArray(vv.getArray(), vv.getArrayOffset());
        }
        double[] data = this.internalData();
        int[] ixs = this.internalIndexArray();
        int n = ixs.length;
        int start = 0;
        for (int i = 0; i < n; ++i) {
            int pos = ixs[i];
            if (!v.isRangeZero(start, pos - start)) {
                return false;
            }
            if (v.unsafeGet(pos) != data[i]) {
                return false;
            }
            start = pos + 1;
        }
        return v.isRangeZero(start, len - start);
    }

    public final SparseIndexedVector cloneIncludingIndices(int[] ixs) {
        Index index = this.internalIndex();
        int[] nixs = IntArrays.mergeSorted(index.data, ixs);
        double[] data = this.internalData();
        int nl = nixs.length;
        double[] ndata = new double[nl];
        int si = 0;
        for (int i = 0; i < nl; ++i) {
            int z = index.data[si];
            if (z != nixs[i]) continue;
            ndata[i] = data[si];
            if (++si >= data.length) break;
        }
        return SparseIndexedVector.wrap(this.length, nixs, ndata);
    }

    @Override
    public final void getElements(double[] array, int offset) {
        Arrays.fill(array, offset, offset + this.length, 0.0);
        this.copySparseValuesTo(array, offset);
    }

    @Override
    public final void copyTo(AVector v, int offset) {
        if (v instanceof ADenseArrayVector) {
            ADenseArrayVector av = (ADenseArrayVector)v;
            this.getElements(av.getArray(), av.getArrayOffset() + offset);
        }
        v.fillRange(offset, this.length, 0.0);
        double[] data = this.internalData();
        int[] ixs = this.internalIndexArray();
        for (int i = 0; i < data.length; ++i) {
            v.unsafeSet(offset + ixs[i], data[i]);
        }
    }

    protected final void copySparseValuesTo(double[] array, int offset) {
        Index index = this.internalIndex();
        int[] ixs = index.data;
        double[] data = this.internalData();
        for (int i = 0; i < data.length; ++i) {
            int di = ixs[i];
            array[offset + di] = data[i];
        }
    }
}

