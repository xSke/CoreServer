/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz;

import java.nio.DoubleBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import mikera.arrayz.INDArray;
import mikera.indexz.AIndex;
import mikera.indexz.Index;
import mikera.randomz.Hash;
import mikera.vectorz.AScalar;
import mikera.vectorz.AVector;
import mikera.vectorz.Op;
import mikera.vectorz.Scalar;
import mikera.vectorz.Tools;
import mikera.vectorz.impl.ADenseArrayVector;
import mikera.vectorz.impl.ASizedVector;
import mikera.vectorz.impl.AStridedVector;
import mikera.vectorz.util.DoubleArrays;
import mikera.vectorz.util.ErrorMessages;
import mikera.vectorz.util.VectorzException;

public final class Vector
extends ADenseArrayVector {
    private static final long serialVersionUID = 6283741614665875877L;
    public static final Vector EMPTY = Vector.wrap(DoubleArrays.EMPTY);

    private /* varargs */ Vector(double ... values) {
        super(values.length, values);
    }

    private /* varargs */ Vector(Object ... values) {
        super(values.length, new double[values.length]);
        for (int i = 0; i < this.length; ++i) {
            this.data[i] = Tools.toDouble(values[i]);
        }
    }

    private Vector(int length) {
        this(new double[length]);
    }

    public Vector(AVector source) {
        this(source.toDoubleArray());
    }

    public static Vector wrap(double[] source) {
        return new Vector(source);
    }

    public static Vector create(double[] data) {
        if (data.length == 0) {
            return EMPTY;
        }
        return Vector.wrap((double[])data.clone());
    }

    public static Vector create(ArrayList<Double> al) {
        int n = al.size();
        Vector v = Vector.createLength(n);
        for (int i = 0; i < n; ++i) {
            v.unsafeSet(i, al.get(i));
        }
        return v;
    }

    public static Vector create(List<Double> al) {
        int n = al.size();
        Vector v = Vector.createLength(n);
        for (int i = 0; i < n; ++i) {
            v.unsafeSet(i, al.get(i));
        }
        return v;
    }

    public static Vector create(double[] data, int start, int length) {
        return Vector.wrap(DoubleArrays.copyOf(data, start, length));
    }

    public static Vector createFromVector(AVector source, int length) {
        Vector v = Vector.createLength(length);
        int n = Math.min(length, source.length());
        source.copyTo(0, v.data, 0, n);
        return v;
    }

    public static /* varargs */ Vector of(double ... values) {
        return Vector.create(values);
    }

    public static Vector createLength(int length) {
        if (length < 1) {
            if (length < 0) {
                throw new IllegalArgumentException(ErrorMessages.illegalSize(length));
            }
            return EMPTY;
        }
        return new Vector(length);
    }

    public static Vector create(AVector a) {
        return new Vector(a.toDoubleArray());
    }

    public static Vector create(INDArray a) {
        return new Vector(a.toDoubleArray());
    }

    public static Vector create(AIndex a) {
        int n = a.length();
        Vector v = Vector.createLength(n);
        for (int i = 0; i < n; ++i) {
            v.unsafeSet(i, a.get(i));
        }
        return v;
    }

    @Override
    public double get(int i) {
        return this.data[i];
    }

    @Override
    public double unsafeGet(int i) {
        return this.data[i];
    }

    @Override
    public void set(int i, double value) {
        this.data[i] = value;
    }

    @Override
    public void unsafeSet(int i, double value) {
        this.data[i] = value;
    }

    @Override
    public void setRange(int offset, double[] data, int dataOffset, int length) {
        System.arraycopy(data, dataOffset, this.data, offset, length);
    }

    @Override
    public void set(AVector a) {
        if (a instanceof Vector) {
            if (a == this) {
                return;
            }
            Vector v = (Vector)a;
            System.arraycopy(v.data, 0, this.data, 0, this.data.length);
        } else {
            super.set(a);
        }
    }

    @Override
    public void getElements(double[] dest, int offset) {
        System.arraycopy(this.data, 0, dest, offset, this.data.length);
    }

    @Override
    public void getElements(double[] data, int offset, int[] indices) {
        int n = indices.length;
        for (int i = 0; i < n; ++i) {
            data[offset + i] = data[indices[i]];
        }
    }

    @Override
    public int getArrayOffset() {
        return 0;
    }

    @Override
    public void applyOp(Op op) {
        op.applyTo(this.data, 0, this.data.length);
    }

    @Override
    public void fill(double value) {
        Arrays.fill(this.data, value);
    }

    @Override
    public void clamp(double min, double max) {
        DoubleArrays.clamp(this.data, min, max);
    }

    @Override
    public void square() {
        DoubleArrays.square(this.data);
    }

    @Override
    public void tanh() {
        DoubleArrays.tanh(this.data);
    }

    @Override
    public void logistic() {
        DoubleArrays.logistic(this.data);
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
    public long nonZeroCount() {
        return DoubleArrays.nonZeroCount(this.data);
    }

    @Override
    public void signum() {
        DoubleArrays.signum(this.data);
    }

    @Override
    public void abs() {
        DoubleArrays.abs(this.data);
    }

    @Override
    public void add(ADenseArrayVector src, int srcOffset) {
        int length = this.length();
        assert (srcOffset >= 0);
        assert (srcOffset + length <= src.length());
        double[] vdata = src.getArray();
        int voffset = src.getArrayOffset() + srcOffset;
        for (int i = 0; i < length; ++i) {
            double[] arrd = this.data;
            int n = i;
            arrd[n] = arrd[n] + vdata[voffset + i];
        }
    }

    @Override
    public void addMultiple(ADenseArrayVector v, double factor) {
        int length = this.checkSameLength(v);
        v.addMultipleToArray(factor, 0, this.data, 0, length);
    }

    @Override
    public void add(AVector v) {
        this.checkSameLength(v);
        v.addToArray(this.data, 0);
    }

    @Override
    public void add(double[] srcData, int srcOffset) {
        int length = this.length();
        DoubleArrays.add(srcData, srcOffset, this.data, 0, length);
    }

    @Override
    public void scaleAdd(double factor, double constant) {
        int length = this.length();
        for (int i = 0; i < length; ++i) {
            this.data[i] = factor * this.data[i] + constant;
        }
    }

    @Override
    public void add(double constant) {
        DoubleArrays.add(this.data, 0, this.data.length, constant);
    }

    @Override
    public void addProduct(AVector a, AVector b) {
        if (a instanceof Vector && b instanceof Vector) {
            this.addProduct((Vector)a, (Vector)b);
            return;
        }
        super.addProduct(a, b);
    }

    public void addProduct(Vector a, Vector b) {
        int length = this.checkSameLength(a, b);
        for (int i = 0; i < length; ++i) {
            double[] arrd = this.data;
            int n = i;
            arrd[n] = arrd[n] + a.data[i] * b.data[i];
        }
    }

    public void addProduct(Vector a, Vector b, double factor) {
        int length = this.checkSameLength(a, b);
        for (int i = 0; i < length; ++i) {
            double[] arrd = this.data;
            int n = i;
            arrd[n] = arrd[n] + a.data[i] * b.data[i] * factor;
        }
    }

    @Override
    public void addAt(int i, double v) {
        double[] arrd = this.data;
        int n = i;
        arrd[n] = arrd[n] + v;
    }

    @Override
    public void sub(AVector v) {
        if (v instanceof ADenseArrayVector) {
            this.sub((ADenseArrayVector)v);
            return;
        }
        int length = this.checkSameLength(v);
        for (int i = 0; i < length; ++i) {
            double[] arrd = this.data;
            int n = i;
            arrd[n] = arrd[n] - v.unsafeGet(i);
        }
    }

    @Override
    public double dotProduct(AVector v, Index ix) {
        if (v instanceof Vector) {
            return this.dotProduct((Vector)v, ix);
        }
        int vl = v.length();
        assert (v.length() == ix.length());
        double result = 0.0;
        int[] idata = ix.getData();
        for (int i = 0; i < vl; ++i) {
            result += this.data[idata[i]] * v.unsafeGet(i);
        }
        return result;
    }

    public double dotProduct(Vector v, Index ix) {
        int vl = v.length();
        assert (v.length() == ix.length());
        double result = 0.0;
        int[] idata = ix.getData();
        for (int i = 0; i < vl; ++i) {
            result += this.data[idata[i]] * v.data[i];
        }
        return result;
    }

    @Override
    public double dotProduct(double[] data, int offset) {
        int len = this.length();
        double result = 0.0;
        for (int i = 0; i < len; ++i) {
            result += this.data[i] * data[offset + i];
        }
        return result;
    }

    @Override
    public double dotProduct(AVector v) {
        this.checkSameLength(v);
        return v.dotProduct(this.data, 0);
    }

    @Override
    public Vector innerProduct(double a) {
        int n = this.length;
        double[] result = new double[n];
        for (int i = 0; i < this.length; ++i) {
            result[i] = this.data[i] * a;
        }
        return Vector.wrap(result);
    }

    @Override
    public Scalar innerProduct(AVector v) {
        return Scalar.create(this.dotProduct(v));
    }

    @Override
    public Scalar innerProduct(Vector v) {
        return Scalar.create(this.dotProduct(v));
    }

    @Override
    public double dotProduct(Vector v) {
        int len = this.checkSameLength(v);
        double result = 0.0;
        for (int i = 0; i < len; ++i) {
            result += this.data[i] * v.data[i];
        }
        return result;
    }

    public double distanceSquared(Vector v) {
        int len = this.length();
        double total = 0.0;
        for (int i = 0; i < len; ++i) {
            double d = this.data[i] - v.data[i];
            total += d * d;
        }
        return total;
    }

    public double distance(Vector v) {
        return Math.sqrt(this.distanceSquared(v));
    }

    @Override
    public double distance(AVector v) {
        if (v instanceof Vector) {
            return this.distance((Vector)v);
        }
        return super.distance(v);
    }

    public void sub(ADenseArrayVector v) {
        this.sub(v, 0);
    }

    public void sub(ADenseArrayVector src, int srcOffset) {
        int length = this.length();
        assert (length == src.length());
        double[] srcData = src.getArray();
        int voffset = src.getArrayOffset() + srcOffset;
        for (int i = 0; i < length; ++i) {
            double[] arrd = this.data;
            int n = i;
            arrd[n] = arrd[n] - srcData[voffset + i];
        }
    }

    @Override
    public void addMultiple(AVector v, double factor) {
        if (v instanceof ADenseArrayVector) {
            this.addMultiple((ADenseArrayVector)v, factor);
            return;
        }
        v.addMultipleToArray(factor, 0, this.data, 0, this.length());
    }

    @Override
    public void addWeighted(AVector v, double factor) {
        if (v instanceof ADenseArrayVector) {
            this.addWeighted((ADenseArrayVector)v, factor);
            return;
        }
        int length = this.checkSameLength(v);
        for (int i = 0; i < length; ++i) {
            this.data[i] = this.data[i] * (1.0 - factor) + v.unsafeGet(i) * factor;
        }
    }

    public void addWeighted(ADenseArrayVector v, double factor) {
        int length = this.length();
        assert (length == v.length());
        double[] arr = v.getArray();
        int offset = v.getArrayOffset();
        for (int i = 0; i < length; ++i) {
            this.data[i] = this.data[i] * (1.0 - factor) + arr[i + offset] * factor;
        }
    }

    @Override
    public void addMultiple(Vector source, Index index, double factor) {
        int len = source.length();
        if (index.length() != len) {
            throw new VectorzException(ErrorMessages.incompatibleShapes(index, source));
        }
        for (int i = 0; i < len; ++i) {
            int j = index.data[i];
            double[] arrd = this.data;
            int n = j;
            arrd[n] = arrd[n] + source.data[i] * factor;
        }
    }

    @Override
    public void addMultiple(Index destToSource, Vector source, double factor) {
        int len = this.length();
        if (destToSource.length() != len) {
            throw new VectorzException("Index length must match this vector");
        }
        int i = 0;
        while (i < len) {
            int j = destToSource.data[i];
            double[] arrd = this.data;
            int n = i++;
            arrd[n] = arrd[n] + source.data[j] * factor;
        }
    }

    @Override
    public void multiply(double factor) {
        DoubleArrays.multiply(this.data, factor);
    }

    @Override
    public void multiply(AVector v) {
        if (v instanceof Vector) {
            this.multiply((Vector)v);
            return;
        }
        this.checkSameLength(v);
        v.multiplyTo(this.data, 0);
    }

    public void multiply(Vector v) {
        this.checkSameLength(v);
        DoubleArrays.multiply(this.data, v.data);
    }

    @Override
    public void divide(AVector v) {
        if (v instanceof Vector) {
            this.divide((Vector)v);
            return;
        }
        this.checkSameLength(v);
        v.divideTo(this.data, 0);
    }

    public void divide(Vector v) {
        int len = this.checkSameLength(v);
        for (int i = 0; i < len; ++i) {
            this.data[i] = this.data[i] / v.data[i];
        }
    }

    @Override
    public void divide(double factor) {
        this.multiply(1.0 / factor);
    }

    @Override
    public boolean isView() {
        return false;
    }

    @Override
    public Vector clone() {
        return Vector.wrap(DoubleArrays.copyOf(this.data));
    }

    @Override
    public int hashCode() {
        int hashCode = 1;
        int len = this.length();
        for (int i = 0; i < len; ++i) {
            hashCode = 31 * hashCode + Hash.hashCode(this.data[i]);
        }
        return hashCode;
    }

    @Override
    public Vector ensureMutable() {
        return this;
    }

    @Override
    public Vector exactClone() {
        return this.clone();
    }

    @Override
    public boolean isPackedArray() {
        return true;
    }

    @Override
    public void toDoubleBuffer(DoubleBuffer dest) {
        dest.put(this.data);
    }

    @Override
    public Vector toNormal() {
        Vector v = Vector.create(this);
        v.normalise();
        return v;
    }

    @Override
    public Vector toVector() {
        return this;
    }

    @Override
    public double[] asDoubleArray() {
        return this.data;
    }

    @Override
    public Vector dense() {
        return this;
    }

    @Override
    public boolean equals(AVector v) {
        if (v.length() != this.length) {
            return false;
        }
        return v.equalsArray(this.data, 0);
    }

    @Override
    public boolean equals(ADenseArrayVector v) {
        if (this.length != v.length()) {
            return false;
        }
        return v.equalsArray(this.data, 0);
    }

    @Override
    public boolean equalsArray(double[] arr, int offset) {
        return DoubleArrays.equals(this.data, 0, arr, offset, this.length);
    }

    @Override
    public boolean equalsArray(double[] arr) {
        return DoubleArrays.equals(this.data, arr, this.length);
    }

    @Override
    protected int index(int i) {
        return i;
    }
}

