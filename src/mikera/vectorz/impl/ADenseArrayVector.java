/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz.impl;

import java.nio.DoubleBuffer;
import java.util.Arrays;
import mikera.arrayz.INDArray;
import mikera.arrayz.impl.IDenseArray;
import mikera.vectorz.AScalar;
import mikera.vectorz.AVector;
import mikera.vectorz.Op;
import mikera.vectorz.Vector;
import mikera.vectorz.Vectorz;
import mikera.vectorz.impl.ASizedVector;
import mikera.vectorz.impl.AStridedVector;
import mikera.vectorz.impl.ArrayIndexScalar;
import mikera.vectorz.impl.ArraySubVector;
import mikera.vectorz.impl.IndexedArrayVector;
import mikera.vectorz.impl.JoinedArrayVector;
import mikera.vectorz.impl.Vector0;
import mikera.vectorz.util.DoubleArrays;
import mikera.vectorz.util.IntArrays;
import mikera.vectorz.util.VectorzException;

public abstract class ADenseArrayVector
extends AStridedVector
implements IDenseArray {
    protected ADenseArrayVector(int length, double[] data) {
        super(length, data);
    }

    @Override
    public final int getStride() {
        return 1;
    }

    @Override
    public AVector subVector(int offset, int length) {
        int len = this.checkRange(offset, length);
        if (length == 0) {
            return Vector0.INSTANCE;
        }
        if (length == len) {
            return this;
        }
        return new ArraySubVector(this, offset, length);
    }

    @Override
    public ArrayIndexScalar slice(int position) {
        this.checkIndex(position);
        return new ArrayIndexScalar(this.getArray(), this.getArrayOffset() + position);
    }

    @Override
    public /* varargs */ AVector selectView(int ... inds) {
        inds = (int[])inds.clone();
        IntArrays.add(inds, this.getArrayOffset());
        return IndexedArrayVector.wrap(this.getArray(), inds);
    }

    @Override
    public boolean isPackedArray() {
        return this.getArrayOffset() == 0 && this.length() == this.getArray().length;
    }

    @Override
    public boolean isView() {
        return true;
    }

    @Override
    public boolean isZero() {
        return DoubleArrays.isZero(this.getArray(), this.getArrayOffset(), this.length());
    }

    @Override
    public boolean isRangeZero(int start, int length) {
        return DoubleArrays.isZero(this.getArray(), this.getArrayOffset() + start, length);
    }

    @Override
    public void toDoubleBuffer(DoubleBuffer dest) {
        dest.put(this.getArray(), this.getArrayOffset(), this.length());
    }

    @Override
    public void getElements(double[] data, int offset) {
        System.arraycopy(this.getArray(), this.getArrayOffset(), data, offset, this.length());
    }

    @Override
    public ADenseArrayVector dense() {
        return this;
    }

    @Override
    public void fillRange(int offset, int length, double value) {
        assert (offset >= 0 && length >= 0 && offset + length <= this.length());
        double[] arr = this.getArray();
        int off = this.getArrayOffset();
        Arrays.fill(arr, off + offset, off + offset + length, value);
    }

    @Override
    public void set(AVector a) {
        this.checkSameLength(a);
        a.getElements(this.getArray(), this.getArrayOffset());
    }

    @Override
    public void set(AVector a, int offset) {
        assert (offset >= 0);
        assert (offset + this.length() <= a.length());
        a.copyTo(offset, this, 0, this.length());
    }

    @Override
    public void setRange(int offset, double[] data, int dataOffset, int length) {
        System.arraycopy(data, dataOffset, this.getArray(), this.getArrayOffset() + offset, length);
    }

    @Override
    public void setElements(int pos, double[] values, int offset, int length) {
        this.checkRange(pos, length);
        System.arraycopy(values, offset, this.getArray(), this.getArrayOffset() + pos, length);
    }

    @Override
    public abstract double get(int var1);

    @Override
    public abstract void set(int var1, double var2);

    @Override
    public abstract double unsafeGet(int var1);

    @Override
    public abstract void unsafeSet(int var1, double var2);

    @Override
    public void add(AVector src) {
        src.addToArray(0, this.getArray(), this.getArrayOffset(), this.length());
    }

    @Override
    public void add(AVector src, int srcOffset) {
        src.addToArray(srcOffset, this.getArray(), this.getArrayOffset(), this.length);
    }

    @Override
    public void add(int offset, AVector src) {
        src.addToArray(0, this.getArray(), this.getArrayOffset() + offset, this.length);
    }

    public void add(int offset, ADenseArrayVector src) {
        int length = src.length();
        DoubleArrays.add(src.getArray(), src.getArrayOffset(), this.getArray(), offset + this.getArrayOffset(), length);
    }

    public void add(int offset, ADenseArrayVector src, int srcOffset, int length) {
        DoubleArrays.add(src.getArray(), src.getArrayOffset() + srcOffset, this.getArray(), offset + this.getArrayOffset(), length);
    }

    @Override
    public void addMultiple(AVector v, double factor) {
        int length = this.checkSameLength(v);
        v.addMultipleToArray(factor, 0, this.getArray(), this.getArrayOffset(), length);
    }

    @Override
    public void scaleAdd(double factor, double constant) {
        DoubleArrays.scaleAdd(this.getArray(), this.getArrayOffset(), this.length(), factor, constant);
    }

    @Override
    public void add(double constant) {
        DoubleArrays.add(this.getArray(), this.getArrayOffset(), this.length(), constant);
    }

    @Override
    public void addProduct(AVector a, int aOffset, AVector b, int bOffset, double factor) {
        int length = this.length();
        double[] array = this.getArray();
        int offset = this.getArrayOffset();
        a.addProductToArray(factor, aOffset, b, bOffset, array, offset, length);
    }

    @Override
    public void addToArray(int offset, double[] destData, int destOffset, int length) {
        double[] data = this.getArray();
        int dataOffset = this.getArrayOffset() + offset;
        DoubleArrays.add(data, dataOffset, destData, destOffset, length);
    }

    @Override
    public void addToArray(double[] dest, int offset, int stride) {
        double[] data = this.getArray();
        int dataOffset = this.getArrayOffset();
        for (int i = 0; i < this.length; ++i) {
            double[] arrd = dest;
            int n = offset + i * stride;
            arrd[n] = arrd[n] + data[dataOffset + i];
        }
    }

    @Override
    public void addProduct(AVector a, AVector b) {
        int len = this.length();
        assert (len == a.length());
        assert (len == b.length());
        double[] array = this.getArray();
        int offset = this.getArrayOffset();
        if (b instanceof ADenseArrayVector) {
            a.addProductToArray(1.0, 0, (ADenseArrayVector)b, 0, array, offset, len);
        } else {
            a.addProductToArray(1.0, 0, b, 0, array, offset, len);
        }
    }

    @Override
    public void addProduct(AVector a, AVector b, double factor) {
        if (factor == 0.0) {
            return;
        }
        int len = this.length();
        assert (len == a.length());
        assert (len == b.length());
        double[] array = this.getArray();
        int offset = this.getArrayOffset();
        if (b instanceof ADenseArrayVector) {
            a.addProductToArray(factor, 0, (ADenseArrayVector)b, 0, array, offset, len);
        } else {
            a.addProductToArray(factor, 0, b, 0, array, offset, len);
        }
    }

    @Override
    public void addMultipleToArray(double factor, int offset, double[] array, int arrayOffset, int length) {
        if (factor == 0.0) {
            return;
        }
        double[] data = this.getArray();
        int dataOffset = this.getArrayOffset() + offset;
        for (int i = 0; i < length; ++i) {
            double[] arrd = array;
            int n = i + arrayOffset;
            arrd[n] = arrd[n] + factor * data[i + dataOffset];
        }
    }

    @Override
    public void addProductToArray(double factor, int offset, AVector other, int otherOffset, double[] array, int arrayOffset, int length) {
        if (factor == 0.0) {
            return;
        }
        if (other instanceof ADenseArrayVector) {
            this.addProductToArray(factor, offset, (ADenseArrayVector)other, otherOffset, array, arrayOffset, length);
            return;
        }
        assert (offset >= 0);
        assert (offset + length <= this.length());
        double[] thisArray = this.getArray();
        offset += this.getArrayOffset();
        for (int i = 0; i < length; ++i) {
            double[] arrd = array;
            int n = i + arrayOffset;
            arrd[n] = arrd[n] + factor * thisArray[i + offset] * other.unsafeGet(i + otherOffset);
        }
    }

    @Override
    public void addProductToArray(double factor, int offset, ADenseArrayVector other, int otherOffset, double[] array, int arrayOffset, int length) {
        if (factor == 0.0) {
            return;
        }
        assert (offset >= 0);
        assert (offset + length <= this.length());
        double[] otherArray = other.getArray();
        otherOffset += other.getArrayOffset();
        double[] thisArray = this.getArray();
        offset += this.getArrayOffset();
        for (int i = 0; i < length; ++i) {
            double[] arrd = array;
            int n = i + arrayOffset;
            arrd[n] = arrd[n] + factor * thisArray[i + offset] * otherArray[i + otherOffset];
        }
    }

    public void add(ADenseArrayVector src, int srcOffset) {
        src.checkRange(srcOffset, this.length);
        double[] vdata = src.getArray();
        double[] data = this.getArray();
        int offset = this.getArrayOffset();
        int voffset = src.getArrayOffset() + srcOffset;
        for (int i = 0; i < this.length; ++i) {
            double[] arrd = data;
            int n = offset + i;
            arrd[n] = arrd[n] + vdata[voffset + i];
        }
    }

    @Override
    public void add(double[] data, int offset) {
        DoubleArrays.add(data, offset, this.getArray(), this.getArrayOffset(), this.length);
    }

    @Override
    public void addAt(int i, double v) {
        assert (i >= 0 && i < this.length());
        double[] data = this.getArray();
        int offset = this.getArrayOffset();
        double[] arrd = data;
        int n = i + offset;
        arrd[n] = arrd[n] + v;
    }

    @Override
    public double dotProduct(double[] data, int offset) {
        return DoubleArrays.dotProduct(this.getArray(), this.getArrayOffset(), data, offset, this.length());
    }

    @Override
    public double dotProduct(AVector v) {
        int length = this.checkSameLength(v);
        if (v instanceof ADenseArrayVector) {
            ADenseArrayVector vv = (ADenseArrayVector)v;
            return DoubleArrays.dotProduct(this.getArray(), this.getArrayOffset(), vv.getArray(), vv.getArrayOffset(), length);
        }
        return v.dotProduct(this.getArray(), this.getArrayOffset());
    }

    @Override
    public void abs() {
        DoubleArrays.abs(this.getArray(), this.getArrayOffset(), this.length());
    }

    @Override
    public void log() {
        DoubleArrays.log(this.getArray(), this.getArrayOffset(), this.length());
    }

    @Override
    public void exp() {
        DoubleArrays.exp(this.getArray(), this.getArrayOffset(), this.length());
    }

    @Override
    public void applyOp(Op op) {
        op.applyTo(this.getArray(), this.getArrayOffset(), this.length());
    }

    @Override
    public ADenseArrayVector applyOpCopy(Op op) {
        int len = this.length();
        Vector v = Vector.createLength(len);
        op.applyTo(v.getArray(), v.getArrayOffset(), len);
        return v;
    }

    @Override
    public double elementSum() {
        return DoubleArrays.elementSum(this.getArray(), this.getArrayOffset(), this.length());
    }

    @Override
    public double elementPowSum(double exponent) {
        return DoubleArrays.elementPowSum(this.getArray(), this.getArrayOffset(), this.length(), exponent);
    }

    @Override
    public double elementAbsPowSum(double exponent) {
        return DoubleArrays.elementAbsPowSum(this.getArray(), this.getArrayOffset(), this.length(), exponent);
    }

    @Override
    public double elementProduct() {
        return DoubleArrays.elementProduct(this.getArray(), this.getArrayOffset(), this.length());
    }

    @Override
    public double elementMax() {
        return DoubleArrays.elementMax(this.getArray(), this.getArrayOffset(), this.length());
    }

    @Override
    public double elementMin() {
        return DoubleArrays.elementMin(this.getArray(), this.getArrayOffset(), this.length());
    }

    @Override
    public double maxAbsElement() {
        return DoubleArrays.elementMaxAbs(this.getArray(), this.getArrayOffset(), this.length());
    }

    @Override
    public int minElementIndex() {
        return DoubleArrays.elementMinIndex(this.getArray(), this.getArrayOffset(), this.length());
    }

    @Override
    public int maxElementIndex() {
        return DoubleArrays.elementMaxIndex(this.getArray(), this.getArrayOffset(), this.length());
    }

    @Override
    public int maxAbsElementIndex() {
        return DoubleArrays.elementMaxAbsIndex(this.getArray(), this.getArrayOffset(), this.length());
    }

    @Override
    public long nonZeroCount() {
        return DoubleArrays.nonZeroCount(this.getArray(), this.getArrayOffset(), this.length());
    }

    @Override
    public int[] nonZeroIndices() {
        return DoubleArrays.nonZeroIndices(this.getArray(), this.getArrayOffset(), this.length());
    }

    @Override
    public void square() {
        DoubleArrays.square(this.getArray(), this.getArrayOffset(), this.length());
    }

    @Override
    public Vector squareCopy() {
        double[] ds = this.toDoubleArray();
        DoubleArrays.square(ds);
        return Vector.wrap(ds);
    }

    @Override
    public void sqrt() {
        DoubleArrays.sqrt(this.getArray(), this.getArrayOffset(), this.length());
    }

    @Override
    public void signum() {
        DoubleArrays.signum(this.getArray(), this.getArrayOffset(), this.length());
    }

    @Override
    public void multiply(AVector v) {
        v.multiplyTo(this.getArray(), this.getArrayOffset());
    }

    @Override
    public void multiply(double[] src, int srcOffset) {
        int len = this.length();
        double[] cdata = this.getArray();
        int coffset = this.getArrayOffset();
        for (int i = 0; i < len; ++i) {
            double[] arrd = cdata;
            int n = i + coffset;
            arrd[n] = arrd[n] * src[i + srcOffset];
        }
    }

    @Override
    public void multiplyTo(double[] dest, int destOffset) {
        DoubleArrays.arraymultiply(this.getArray(), this.getArrayOffset(), dest, destOffset, this.length());
    }

    @Override
    public void divide(AVector v) {
        v.divideTo(this.getArray(), this.getArrayOffset());
    }

    @Override
    public void divide(double[] data, int offset) {
        DoubleArrays.arraydivide(data, offset, this.getArray(), this.getArrayOffset(), this.length());
    }

    @Override
    public void divideTo(double[] data, int offset) {
        DoubleArrays.arraydivide(this.getArray(), this.getArrayOffset(), data, offset, this.length());
    }

    @Override
    public void copyTo(int start, AVector dest, int destOffset, int length) {
        dest.setElements(destOffset, this.getArray(), this.getArrayOffset() + start, length);
    }

    public void copyTo(int offset, ADenseArrayVector dest, int destOffset, int length) {
        double[] src = this.getArray();
        int off = this.getArrayOffset();
        double[] dst = dest.getArray();
        System.arraycopy(src, off + offset, dst, dest.getArrayOffset() + destOffset, length);
    }

    @Override
    public void copyTo(int offset, double[] dest, int destOffset, int length, int stride) {
        for (int i = 0; i < length; ++i) {
            dest[destOffset + i * stride] = this.data[i + offset];
        }
    }

    @Override
    public void copyTo(int offset, double[] dest, int destOffset, int length) {
        double[] src = this.getArray();
        int off = this.getArrayOffset();
        System.arraycopy(src, off + offset, dest, destOffset, length);
    }

    public void addMultiple(ADenseArrayVector v, double factor) {
        int length = this.checkSameLength(v);
        v.addMultipleToArray(factor, 0, this.getArray(), this.getArrayOffset(), length);
    }

    @Override
    public void addMultiple(int offset, AVector src, int srcOffset, int length, double factor) {
        this.checkRange(offset, length);
        src.checkRange(srcOffset, length);
        if (factor == 0.0) {
            return;
        }
        int tOffset = offset + this.getArrayOffset();
        src.addMultipleToArray(factor, srcOffset, this.getArray(), tOffset, length);
    }

    @Override
    public double magnitudeSquared() {
        return DoubleArrays.elementSquaredSum(this.data, this.getArrayOffset(), this.length);
    }

    @Override
    public double magnitude() {
        return Math.sqrt(this.magnitudeSquared());
    }

    @Override
    public void fill(double value) {
        int offset = this.getArrayOffset();
        Arrays.fill(this.getArray(), offset, offset + this.length, value);
    }

    @Override
    public void pow(double exponent) {
        int len = this.length();
        double[] data = this.getArray();
        int offset = this.getArrayOffset();
        DoubleArrays.pow(data, offset, len, exponent);
    }

    @Override
    public void reciprocal() {
        DoubleArrays.reciprocal(this.getArray(), this.getArrayOffset(), this.length());
    }

    @Override
    public void clamp(double min, double max) {
        DoubleArrays.clamp(this.getArray(), this.getArrayOffset(), this.length(), min, max);
    }

    @Override
    public void multiply(double factor) {
        DoubleArrays.multiply(this.getArray(), this.getArrayOffset(), this.length(), factor);
    }

    @Override
    public AVector tryEfficientJoin(AVector v) {
        if (v instanceof ADenseArrayVector) {
            return this.join((ADenseArrayVector)v);
        }
        if (v instanceof JoinedArrayVector) {
            return this.join((JoinedArrayVector)v);
        }
        return null;
    }

    public AVector join(ADenseArrayVector v) {
        if (v.getArray() == this.getArray() && this.getArrayOffset() + this.length == v.getArrayOffset()) {
            return Vectorz.wrap(this.getArray(), this.getArrayOffset(), this.length + v.length());
        }
        return JoinedArrayVector.joinVectors(this, v);
    }

    public JoinedArrayVector join(JoinedArrayVector v) {
        return JoinedArrayVector.wrap(this).join(v);
    }

    @Override
    public boolean equals(INDArray v) {
        if (v.dimensionality() != 1) {
            return false;
        }
        int len = this.length();
        if (len != v.getShape(0)) {
            return false;
        }
        return v.equalsArray(this.getArray(), this.getArrayOffset());
    }

    @Override
    public boolean equals(AVector v) {
        if (v == this) {
            return true;
        }
        int len = this.length();
        if (v.length() != len) {
            return false;
        }
        return v.equalsArray(this.getArray(), this.getArrayOffset());
    }

    @Override
    public boolean equalsArray(double[] data, int offset) {
        return DoubleArrays.equals(data, offset, this.getArray(), this.getArrayOffset(), this.length);
    }

    @Override
    public boolean equalsArray(double[] data) {
        if (this.length() != data.length) {
            return false;
        }
        return this.equalsArray(data, 0);
    }

    @Override
    public boolean elementsEqual(double value) {
        int length = this.length();
        double[] data = this.getArray();
        int offset = this.getArrayOffset();
        return DoubleArrays.elementsEqual(data, offset, length, value);
    }

    @Override
    public void validate() {
        int length = this.length();
        double[] data = this.getArray();
        int offset = this.getArrayOffset();
        if (offset < 0 || offset + length > data.length) {
            throw new VectorzException("ArrayVector out of bounds");
        }
        super.validate();
    }
}

