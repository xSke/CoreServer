/*
 * Decompiled with CFR 0_129.
 */
package mikera.arrayz.impl;

import java.nio.DoubleBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import mikera.arrayz.Array;
import mikera.arrayz.Arrayz;
import mikera.arrayz.INDArray;
import mikera.arrayz.ISparse;
import mikera.arrayz.impl.IDense;
import mikera.arrayz.impl.IDenseArray;
import mikera.arrayz.impl.ImmutableArray;
import mikera.arrayz.impl.JoinedArray;
import mikera.arrayz.impl.SliceArray;
import mikera.arrayz.impl.SliceElementIterator;
import mikera.arrayz.impl.SliceIterator;
import mikera.indexz.AIndex;
import mikera.indexz.Index;
import mikera.matrixx.AMatrix;
import mikera.matrixx.Matrix;
import mikera.matrixx.Matrixx;
import mikera.matrixx.impl.SparseRowMatrix;
import mikera.util.Maths;
import mikera.vectorz.AScalar;
import mikera.vectorz.AVector;
import mikera.vectorz.IOperator;
import mikera.vectorz.Op;
import mikera.vectorz.Ops;
import mikera.vectorz.Scalar;
import mikera.vectorz.Tools;
import mikera.vectorz.Vector;
import mikera.vectorz.Vectorz;
import mikera.vectorz.impl.SingleDoubleIterator;
import mikera.vectorz.util.ErrorMessages;
import mikera.vectorz.util.IntArrays;
import mikera.vectorz.util.LongArrays;

public abstract class AbstractArray<T>
implements INDArray,
Iterable<T> {
    private static final long serialVersionUID = -958234961396539071L;

    @Override
    public abstract double get();

    @Override
    public abstract double get(int var1);

    @Override
    public double get(long x) {
        if (x >= (long)this.getShape(0)) {
            throw new IndexOutOfBoundsException("Index: " + x);
        }
        return this.get((int)x);
    }

    @Override
    public abstract double get(int var1, int var2);

    @Override
    public double get(long x, long y) {
        if (x >= (long)this.getShape(0)) {
            throw new IndexOutOfBoundsException("Index [0]: " + x);
        }
        if (y >= (long)this.getShape(1)) {
            throw new IndexOutOfBoundsException("Index [1]: " + y);
        }
        return this.get((int)x, (int)y);
    }

    @Override
    public double get(long[] xs) {
        int n = xs.length;
        int[] ixs = new int[n];
        for (int i = 0; i < n; ++i) {
            long ix = xs[i];
            if (ix >= (long)this.getShape(i)) {
                throw new IndexOutOfBoundsException("Index [" + i + "]: " + ix);
            }
            ixs[i] = (int)ix;
        }
        return this.get(ixs);
    }

    @Override
    public double get(AIndex ix) {
        return this.get(ix.toArray());
    }

    @Override
    public double get(Index ix) {
        return this.get(ix.getData());
    }

    @Override
    public int getShape(int dim) {
        return this.getShape()[dim];
    }

    @Override
    public int[] getShapeClone() {
        int n = this.dimensionality();
        int[] sh = new int[n];
        for (int i = 0; i < n; ++i) {
            sh[i] = this.getShape(i);
        }
        return sh;
    }

    @Override
    public long[] getLongShape() {
        return LongArrays.copyOf(this.getShape());
    }

    @Override
    public boolean epsilonEquals(INDArray a) {
        return this.epsilonEquals(a, 1.0E-7);
    }

    @Override
    public boolean epsilonEquals(INDArray a, double epsilon) {
        if (this.dimensionality() == 0) {
            double d = this.get() - a.get();
            return Math.abs(d) <= epsilon;
        }
        int sc = this.sliceCount();
        if (a.sliceCount() != sc) {
            return false;
        }
        for (int i = 0; i < sc; ++i) {
            INDArray s = this.slice(i);
            if (s.epsilonEquals(a.slice(i), epsilon)) continue;
            return false;
        }
        return true;
    }

    @Override
    public boolean isBoolean() {
        if (this.dimensionality() == 0) {
            return Tools.isBoolean(this.get());
        }
        int sc = this.sliceCount();
        for (int i = 0; i < sc; ++i) {
            INDArray s = this.slice(i);
            if (s.isBoolean()) continue;
            return false;
        }
        return true;
    }

    @Override
    public boolean isSparse() {
        return this instanceof ISparse;
    }

    @Override
    public boolean isDense() {
        return this instanceof IDense;
    }

    @Override
    public boolean isMutable() {
        int n = this.sliceCount();
        for (int i = 0; i < n; ++i) {
            if (!this.slice(i).isMutable()) continue;
            return true;
        }
        return false;
    }

    @Override
    public boolean isFullyMutable() {
        int n = this.sliceCount();
        for (int i = 0; i < n; ++i) {
            if (this.slice(i).isFullyMutable()) continue;
            return false;
        }
        return true;
    }

    @Override
    public void applyOp(Op op) {
        int n = this.sliceCount();
        for (int i = 0; i < n; ++i) {
            this.slice(i).applyOp(op);
        }
    }

    @Override
    public void applyOp(IOperator op) {
        int n = this.sliceCount();
        for (int i = 0; i < n; ++i) {
            this.slice(i).applyOp(op);
        }
    }

    @Override
    public void multiply(double d) {
        int n = this.sliceCount();
        for (int i = 0; i < n; ++i) {
            this.slice(i).multiply(d);
        }
    }

    @Override
    public INDArray multiplyCopy(double d) {
        INDArray r = this.clone();
        r.multiply(d);
        return r;
    }

    @Override
    public INDArray applyOpCopy(Op op) {
        INDArray r = this.clone();
        r.applyOp(op);
        return r;
    }

    @Override
    public boolean isElementConstrained() {
        int n = this.sliceCount();
        for (int i = 0; i < n; ++i) {
            if (!this.slice(i).isElementConstrained()) continue;
            return true;
        }
        return false;
    }

    @Override
    public boolean isSameShape(INDArray a) {
        int dims = this.dimensionality();
        if (dims != a.dimensionality()) {
            return false;
        }
        for (int i = 0; i < dims; ++i) {
            if (this.getShape(i) == a.getShape(i)) continue;
            return false;
        }
        return true;
    }

    @Override
    public AVector asVector() {
        if (this instanceof IDenseArray) {
            IDenseArray a = (IDenseArray)((Object)this);
            return Vectorz.wrap(a.getArray(), a.getArrayOffset(), (int)this.elementCount());
        }
        int n = this.sliceCount();
        AVector result = this.slice(0).asVector();
        for (int i = 1; i < n; ++i) {
            result = result.join(this.slice(i).asVector());
        }
        return result;
    }

    @Override
    public void setElements(double[] values, int offset) {
        this.setElements(0, values, offset, (int)this.elementCount());
    }

    @Override
    public void setElements(int pos, double[] values, int offset, int length) {
        int s2;
        if (length == 0) {
            return;
        }
        int ss = (int)this.slice(0).elementCount();
        int s1 = pos / ss;
        if (s1 == (s2 = (pos + length - 1) / ss)) {
            this.slice(s1).setElements(pos - s1 * ss, values, offset, length);
            return;
        }
        int si = offset;
        int l1 = (s1 + 1) * ss - pos;
        if (l1 > 0) {
            this.slice(s1).setElements(pos - s1 * ss, values, si, l1);
            si += l1;
        }
        for (int i = s1 + 1; i < s2; ++i) {
            this.slice(i).setElements(values, si);
            si += ss;
        }
        int l2 = pos + length - s2 * ss;
        if (l2 > 0) {
            this.slice(s2).setElements(0, values, si, l2);
        }
    }

    @Override
    public boolean isZero() {
        if (this.dimensionality() == 0) {
            return this.get() == 0.0;
        }
        int sc = this.sliceCount();
        for (int i = 0; i < sc; ++i) {
            INDArray s = this.slice(i);
            if (s.isZero()) continue;
            return false;
        }
        return true;
    }

    @Override
    public INDArray ensureMutable() {
        if (this.isFullyMutable() && !this.isView()) {
            return this;
        }
        return this.clone();
    }

    @Override
    public void fill(double value) {
        if (this.dimensionality() == 0) {
            this.set(value);
        } else {
            int sc = this.sliceCount();
            for (int i = 0; i < sc; ++i) {
                INDArray s = this.slice(i);
                s.fill(value);
            }
        }
    }

    @Override
    public INDArray innerProduct(double a) {
        INDArray result = this.clone();
        result.scale(a);
        return result;
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
        int sc = this.sliceCount();
        ArrayList<INDArray> sips = new ArrayList<INDArray>(sc);
        for (int i = 0; i < sc; ++i) {
            sips.add(this.slice(i).innerProduct(a));
        }
        return SliceArray.create(sips);
    }

    @Override
    public INDArray innerProduct(AScalar s) {
        return this.innerProduct(s.get());
    }

    @Override
    public INDArray innerProduct(AVector a) {
        return this.innerProduct((INDArray)a);
    }

    @Override
    public INDArray outerProduct(INDArray a) {
        ArrayList<INDArray> al = new ArrayList<INDArray>(this.sliceCount());
        for (T s : this) {
            if (s instanceof INDArray) {
                al.add(((INDArray)s).outerProduct(a));
                continue;
            }
            double x = Tools.toDouble(s);
            INDArray sa = a.clone();
            sa.scale(x);
            al.add(sa);
        }
        return Arrayz.create(al);
    }

    @Override
    public INDArray getTranspose() {
        return this.getTransposeCopy();
    }

    @Override
    public INDArray getTransposeView() {
        throw new UnsupportedOperationException();
    }

    @Override
    public INDArray getTransposeCopy() {
        Array nd = Array.create(this);
        return nd.getTransposeView();
    }

    @Override
    public final void scale(double d) {
        this.multiply(d);
    }

    @Override
    public void scaleAdd(double factor, double constant) {
        this.multiply(factor);
        this.add(constant);
    }

    @Override
    public void set(double value) {
        this.set(new int[0], value);
    }

    @Override
    public void set(int x, double value) {
        this.set(new int[]{x}, value);
    }

    @Override
    public void set(int x, int y, double value) {
        this.set(new int[]{x, y}, value);
    }

    @Override
    public void set(INDArray a) {
        int tdims = this.dimensionality();
        int adims = a.dimensionality();
        if (adims < tdims) {
            int sc = this.sliceCount();
            for (int i = 0; i < sc; ++i) {
                INDArray s = this.slice(i);
                s.set(a);
            }
        } else if (adims == tdims) {
            if (tdims == 0) {
                this.set(a.get());
                return;
            }
            int sc = this.sliceCount();
            for (int i = 0; i < sc; ++i) {
                INDArray s = this.slice(i);
                s.set(a.slice(i));
            }
        } else {
            throw new IllegalArgumentException(ErrorMessages.incompatibleShapes(this, a));
        }
    }

    @Override
    public void clamp(double min, double max) {
        if (this.dimensionality() == 0) {
            this.set(Maths.bound(this.get(), min, max));
            return;
        }
        int len = this.sliceCount();
        for (int i = 0; i < len; ++i) {
            this.slice(i).clamp(min, max);
        }
    }

    @Override
    public void set(Object o) {
        if (o instanceof INDArray) {
            this.set((INDArray)o);
            return;
        }
        if (o instanceof Number) {
            this.set(((Number)o).doubleValue());
            return;
        }
        if (o instanceof Iterable) {
            int i = 0;
            for (T ob : (Iterable)o) {
                this.slice(i).set(ob);
            }
            return;
        }
        if (o instanceof double[]) {
            this.setElements((double[])o);
            return;
        }
        throw new UnsupportedOperationException("Can't set to value for " + o.getClass().toString());
    }

    @Override
    public /* varargs */ void setElements(double ... values) {
        int vl = values.length;
        if ((long)vl != this.elementCount()) {
            throw new IllegalArgumentException("Wrong array length: " + vl);
        }
        this.setElements(0, values, 0, vl);
    }

    @Override
    public void square() {
        this.applyOp(Ops.SQUARE);
    }

    @Override
    public INDArray squareCopy() {
        INDArray r = this.clone();
        r.square();
        return r;
    }

    @Override
    public INDArray absCopy() {
        INDArray r = this.clone();
        r.abs();
        return r;
    }

    @Override
    public INDArray reciprocalCopy() {
        INDArray r = this.clone();
        r.reciprocal();
        return r;
    }

    @Override
    public INDArray signumCopy() {
        INDArray r = this.clone();
        r.signum();
        return r;
    }

    @Override
    public Iterator<T> iterator() {
        return new SliceIterator<T>(this);
    }

    @Override
    public Iterator<Double> elementIterator() {
        if (this.dimensionality() == 0) {
            return new SingleDoubleIterator(this.get());
        }
        return new SliceElementIterator(this);
    }

    public boolean equals(Object o) {
        if (!(o instanceof INDArray)) {
            return false;
        }
        return this.equals((INDArray)o);
    }

    @Override
    public boolean equalsArray(double[] data) {
        if ((long)data.length != this.elementCount()) {
            return false;
        }
        return this.equalsArray(data, 0);
    }

    public int hashCode() {
        return this.asVector().hashCode();
    }

    public String toString() {
        if (this.elementCount() > 10000L) {
            Index shape = Index.create(this.getShape());
            return "Large array with shape: " + shape.toString();
        }
        return this.toStringFull();
    }

    public String toStringFull() {
        if (this.dimensionality() == 0) {
            return Double.toString(this.get());
        }
        StringBuilder sb = new StringBuilder();
        int length = this.sliceCount();
        sb.append('[');
        if (length > 0) {
            sb.append(this.slice(0).toString());
            for (int i = 1; i < length; ++i) {
                sb.append(',');
                sb.append(this.slice(i).toString());
            }
        }
        sb.append(']');
        return sb.toString();
    }

    @Override
    public INDArray clone() {
        return Arrayz.create(this);
    }

    @Override
    public INDArray copy() {
        if (!this.isMutable()) {
            return this;
        }
        return this.clone();
    }

    @Override
    public INDArray scaleCopy(double d) {
        INDArray r = this.clone();
        r.scale(d);
        return r;
    }

    @Override
    public INDArray negateCopy() {
        INDArray r = this.clone();
        r.negate();
        return r;
    }

    @Override
    public boolean equals(INDArray a) {
        int dims = this.dimensionality();
        if (a.dimensionality() != dims) {
            return false;
        }
        if (dims == 0) {
            return this.get() == a.get();
        }
        if (dims == 1) {
            return this.equals(a.asVector());
        }
        int sc = this.sliceCount();
        for (int i = 0; i < sc; ++i) {
            if (this.slice(i).equals(a.slice(i))) continue;
            return false;
        }
        return true;
    }

    public boolean equals(AVector a) {
        if (this.dimensionality() != 1) {
            return false;
        }
        int sc = this.sliceCount();
        if (a.length() != sc) {
            return false;
        }
        for (int i = 0; i < sc; ++i) {
            if (this.get(i) == a.unsafeGet(i)) continue;
            return false;
        }
        return true;
    }

    @Override
    public boolean equalsArray(double[] data, int offset) {
        int dims = this.dimensionality();
        if (dims == 0) {
            return data[offset] == this.get();
        }
        if (dims == 1) {
            return this.asVector().equalsArray(data, offset);
        }
        int sc = this.sliceCount();
        int skip = (int)this.slice(0).elementCount();
        for (int i = 0; i < sc; ++i) {
            if (this.slice(i).equalsArray(data, offset + i * skip)) continue;
            return false;
        }
        return true;
    }

    @Override
    public void add(INDArray a) {
        int dims = this.dimensionality();
        if (dims == 0) {
            this.add(a.get());
            return;
        }
        int adims = a.dimensionality();
        int n = this.sliceCount();
        int na = a.sliceCount();
        if (dims == adims) {
            if (n != na) {
                throw new IllegalArgumentException(ErrorMessages.incompatibleShapes(this, a));
            }
            for (int i = 0; i < n; ++i) {
                this.slice(i).add(a.slice(i));
            }
        } else if (adims < dims) {
            for (int i = 0; i < n; ++i) {
                this.slice(i).add(a);
            }
        } else {
            throw new IllegalArgumentException(ErrorMessages.incompatibleShapes(this, a));
        }
    }

    @Override
    public void add(double a) {
        int dims = this.dimensionality();
        if (dims == 0) {
            this.set(a + this.get());
        } else {
            int n = this.sliceCount();
            for (int i = 0; i < n; ++i) {
                this.slice(i).add(a);
            }
        }
    }

    @Override
    public void addAt(int i, double v) {
        int ss = (int)(this.elementCount() / (long)this.sliceCount());
        int s = i / ss;
        this.slice(s).addAt(i - s * ss, v);
    }

    @Override
    public INDArray addCopy(INDArray a) {
        INDArray r = this.broadcastCloneLike(a);
        r.add(a);
        return r;
    }

    @Override
    public INDArray subCopy(INDArray a) {
        INDArray r = this.broadcastCloneLike(a);
        r.sub(a);
        return r;
    }

    @Override
    public INDArray multiplyCopy(INDArray a) {
        INDArray r = this.broadcastCloneLike(a);
        r.multiply(a);
        return r;
    }

    @Override
    public INDArray divideCopy(INDArray a) {
        INDArray r = this.broadcastCloneLike(a);
        r.divide(a);
        return r;
    }

    @Override
    public void addToArray(double[] data, int offset) {
        int dims = this.dimensionality();
        if (dims == 0) {
            double[] arrd = data;
            int n = offset;
            arrd[n] = arrd[n] + this.get();
        } else {
            int n = this.sliceCount();
            INDArray s0 = this.slice(0);
            int ec = (int)s0.elementCount();
            s0.addToArray(data, offset);
            for (int i = 1; i < n; ++i) {
                this.slice(i).addToArray(data, offset + i * ec);
            }
        }
    }

    @Override
    public void pow(double exponent) {
        int dims = this.dimensionality();
        if (dims == 0) {
            this.set(Math.pow(this.get(), exponent));
        } else {
            int n = this.sliceCount();
            for (int i = 0; i < n; ++i) {
                this.slice(i).pow(exponent);
            }
        }
    }

    @Override
    public void sub(double a) {
        this.add(- a);
    }

    @Override
    public void multiply(INDArray a) {
        int dims = this.dimensionality();
        if (dims == 0) {
            this.set(this.get() * a.get());
            return;
        }
        int adims = a.dimensionality();
        if (adims == 0) {
            this.multiply(a.get());
            return;
        }
        int n = this.sliceCount();
        int na = a.sliceCount();
        if (dims == adims) {
            if (n != na) {
                throw new IllegalArgumentException(ErrorMessages.incompatibleShapes(this, a));
            }
            for (int i = 0; i < n; ++i) {
                this.slice(i).multiply(a.slice(i));
            }
        } else if (adims < dims) {
            for (int i = 0; i < n; ++i) {
                this.slice(i).multiply(a);
            }
        } else {
            throw new IllegalArgumentException(ErrorMessages.incompatibleShapes(this, a));
        }
    }

    @Override
    public void divide(INDArray a) {
        int dims = this.dimensionality();
        if (dims == 0) {
            this.set(this.get() / a.get());
            return;
        }
        int adims = a.dimensionality();
        if (adims == 0) {
            this.scale(1.0 / a.get());
            return;
        }
        int n = this.sliceCount();
        int na = a.sliceCount();
        if (dims == adims) {
            if (n != na) {
                throw new IllegalArgumentException(ErrorMessages.incompatibleShapes(this, a));
            }
            for (int i = 0; i < n; ++i) {
                this.slice(i).divide(a.slice(i));
            }
        } else if (adims < dims) {
            for (int i = 0; i < n; ++i) {
                this.slice(i).divide(a);
            }
        } else {
            throw new IllegalArgumentException(ErrorMessages.incompatibleShapes(this, a));
        }
    }

    @Override
    public void divide(double factor) {
        this.multiply(1.0 / factor);
    }

    @Override
    public long nonZeroCount() {
        if (this.dimensionality() == 0) {
            return this.get() == 0.0 ? 0L : 1L;
        }
        long result = 0L;
        int n = this.sliceCount();
        for (int i = 0; i < n; ++i) {
            result += this.slice(i).nonZeroCount();
        }
        return result;
    }

    public double density() {
        return (double)this.nonZeroCount() / (double)this.elementCount();
    }

    @Override
    public double elementSum() {
        if (this.dimensionality() == 0) {
            return this.get();
        }
        double result = 0.0;
        int n = this.sliceCount();
        for (int i = 0; i < n; ++i) {
            result += this.slice(i).elementSum();
        }
        return result;
    }

    @Override
    public double elementProduct() {
        if (this.dimensionality() == 0) {
            return this.get();
        }
        double result = 1.0;
        int n = this.sliceCount();
        for (int i = 0; i < n; ++i) {
            if ((result *= this.slice(i).elementProduct()) != 0.0) continue;
            return 0.0;
        }
        return result;
    }

    @Override
    public double elementMax() {
        if (this.dimensionality() == 0) {
            return this.get();
        }
        double result = this.slice(0).elementMax();
        int n = this.sliceCount();
        for (int i = 1; i < n; ++i) {
            double v = this.slice(i).elementMax();
            if (v <= result) continue;
            result = v;
        }
        return result;
    }

    @Override
    public boolean elementsEqual(double value) {
        if (this.dimensionality() == 0) {
            return this.get() == value;
        }
        int n = this.sliceCount();
        for (int i = 0; i < n; ++i) {
            if (this.slice(i).elementsEqual(value)) continue;
            return false;
        }
        return true;
    }

    @Override
    public double elementMin() {
        if (this.dimensionality() == 0) {
            return this.get();
        }
        double result = this.slice(0).elementMin();
        int n = this.sliceCount();
        for (int i = 1; i < n; ++i) {
            double v = this.slice(i).elementMin();
            if (v >= result) continue;
            result = v;
        }
        return result;
    }

    @Override
    public double elementSquaredSum() {
        if (this.dimensionality() == 0) {
            double value = this.get();
            return value * value;
        }
        double result = 0.0;
        int n = this.sliceCount();
        for (int i = 0; i < n; ++i) {
            result += this.slice(i).elementSquaredSum();
        }
        return result;
    }

    @Override
    public void sub(INDArray a) {
        int dims = this.dimensionality();
        if (dims == 0) {
            this.sub(a.get());
            return;
        }
        int n = this.sliceCount();
        int na = a.sliceCount();
        int adims = a.dimensionality();
        if (dims == adims) {
            if (n != na) {
                throw new IllegalArgumentException(ErrorMessages.incompatibleShapes(this, a));
            }
            for (int i = 0; i < n; ++i) {
                this.slice(i).sub(a.slice(i));
            }
        } else if (adims < dims) {
            for (int i = 0; i < n; ++i) {
                this.slice(i).sub(a);
            }
        } else {
            throw new IllegalArgumentException(ErrorMessages.incompatibleShapes(this, a));
        }
    }

    @Override
    public void negate() {
        this.multiply(-1.0);
    }

    @Override
    public void reciprocal() {
        if (this.dimensionality() == 0) {
            this.set(1.0 / this.get());
        } else {
            int sc = this.sliceCount();
            for (int i = 0; i < sc; ++i) {
                this.slice(i).reciprocal();
            }
        }
    }

    @Override
    public void abs() {
        if (this.dimensionality() == 0) {
            this.set(Math.abs(this.get()));
        } else {
            int sc = this.sliceCount();
            for (int i = 0; i < sc; ++i) {
                this.slice(i).abs();
            }
        }
    }

    @Override
    public void sqrt() {
        if (this.dimensionality() == 0) {
            this.set(Math.sqrt(this.get()));
        } else {
            int sc = this.sliceCount();
            for (int i = 0; i < sc; ++i) {
                this.slice(i).sqrt();
            }
        }
    }

    @Override
    public void log() {
        if (this.dimensionality() == 0) {
            this.set(Math.log(this.get()));
        } else {
            int sc = this.sliceCount();
            for (int i = 0; i < sc; ++i) {
                this.slice(i).log();
            }
        }
    }

    @Override
    public void exp() {
        if (this.dimensionality() == 0) {
            this.set(Math.exp(this.get()));
        } else {
            int sc = this.sliceCount();
            for (int i = 0; i < sc; ++i) {
                this.slice(i).exp();
            }
        }
    }

    @Override
    public void signum() {
        if (this.dimensionality() == 0) {
            this.set(Math.signum(this.get()));
        } else {
            int sc = this.sliceCount();
            for (int i = 0; i < sc; ++i) {
                this.slice(i).signum();
            }
        }
    }

    @Override
    public /* varargs */ INDArray reshape(int ... targetShape) {
        return Arrayz.createFromVector(this.asVector(), targetShape);
    }

    @Override
    public INDArray reorder(int dim, int[] order) {
        int n = order.length;
        if (n == 0) {
            int[] shape = this.getShapeClone();
            shape[0] = 0;
            return Arrayz.createZeroArray(shape);
        }
        int dims = this.dimensionality();
        if (dim < 0 || dim >= dims) {
            throw new IndexOutOfBoundsException(ErrorMessages.invalidDimension(this, dim));
        }
        ArrayList<INDArray> newSlices = new ArrayList<INDArray>(n);
        for (int si : order) {
            newSlices.add(this.slice(dim, si));
        }
        int[] shp = this.getShapeClone();
        shp[dim] = n;
        if (dims == 2 && dim == 0) {
            return SparseRowMatrix.create(newSlices, shp[0], shp[1]);
        }
        if (dim == 0) {
            return SliceArray.create(newSlices, shp);
        }
        Array a = Array.newArray(shp);
        for (int di = 0; di < n; ++di) {
            a.slice(dim, di).set(newSlices.get(di));
        }
        return a;
    }

    @Override
    public INDArray reorder(int[] order) {
        return this.reorder(0, order);
    }

    @Override
    public List<?> getSlices(int dimension) {
        int l = this.getShape(dimension);
        ArrayList<INDArray> al = new ArrayList<INDArray>(l);
        for (int i = 0; i < l; ++i) {
            al.add(this.slice(dimension, i));
        }
        return al;
    }

    @Override
    public List<?> getSlices() {
        int n = this.sliceCount();
        ArrayList<INDArray> al = new ArrayList<INDArray>(n);
        for (int i = 0; i < n; ++i) {
            al.add(this.slice(i));
        }
        return al;
    }

    @Override
    public List<INDArray> getSliceViews() {
        int n = this.sliceCount();
        ArrayList<INDArray> al = new ArrayList<INDArray>(n);
        for (int i = 0; i < n; ++i) {
            al.add(this.slice(i));
        }
        return al;
    }

    @Override
    public int componentCount() {
        return 0;
    }

    @Override
    public INDArray getComponent(int k) {
        throw new UnsupportedOperationException("Component based access not supported for class " + this.getClass().getCanonicalName());
    }

    @Override
    public INDArray[] getComponents() {
        int cc = this.componentCount();
        INDArray[] result = new INDArray[cc];
        for (int i = 0; i < cc; ++i) {
            result[i] = this.getComponent(i);
        }
        return result;
    }

    @Override
    public INDArray withComponents(INDArray[] cs) {
        throw new UnsupportedOperationException("Component re-wrapping not supported for class " + this.getClass().getCanonicalName());
    }

    @Override
    public INDArray subArray(int[] offsets, int[] shape) {
        int n = this.dimensionality();
        if (offsets.length != n) {
            throw new IllegalArgumentException(ErrorMessages.invalidIndex((INDArray)this, offsets));
        }
        if (shape.length != n) {
            throw new IllegalArgumentException(ErrorMessages.invalidIndex((INDArray)this, offsets));
        }
        int[] thisShape = this.getShape();
        if (IntArrays.equals(shape, thisShape)) {
            if (IntArrays.isZero(offsets)) {
                return this;
            }
            throw new IllegalArgumentException("Invalid subArray offsets");
        }
        int nslices = shape[0];
        ArrayList<INDArray> al = new ArrayList<INDArray>(nslices);
        int endIndex = offsets[0] + nslices;
        int[] zzoffsets = IntArrays.removeIndex(offsets, 0);
        int[] zzshape = IntArrays.removeIndex(shape, 0);
        for (int i = offsets[0]; i < endIndex; ++i) {
            al.add(this.slice(i).subArray(zzoffsets, zzshape));
        }
        return SliceArray.create(al);
    }

    @Override
    public INDArray join(INDArray a, int dimension) {
        return JoinedArray.join(this, a, dimension);
    }

    @Override
    public INDArray join(INDArray a) {
        return JoinedArray.join(this, a, 0);
    }

    @Override
    public INDArray rotateView(int dimension, int shift) {
        int dlen = this.getShape(dimension);
        if (dlen == 0) {
            return this;
        }
        int n = this.dimensionality();
        if ((shift = Maths.mod(shift, dlen)) == 0) {
            return this;
        }
        int[] off = new int[n];
        int[] shp = this.getShapeClone();
        shp[dimension] = shift;
        INDArray right = this.subArray(off, shp);
        shp[dimension] = dlen - shift;
        off[dimension] = shift;
        INDArray left = this.subArray(off, shp);
        return left.join(right, dimension);
    }

    @Override
    public Vector toVector() {
        int n = (int)this.elementCount();
        double[] data = new double[n];
        this.getElements(data, 0);
        return Vector.wrap(data);
    }

    @Override
    public Array toArray() {
        return Array.create(this);
    }

    @Override
    public List<Double> asElementList() {
        return this.asVector().asElementList();
    }

    @Override
    public final double[] getElements() {
        return this.toDoubleArray();
    }

    @Override
    public void getElements(double[] dest, int offset) {
        if (this.dimensionality() == 0) {
            dest[offset] = this.get();
            return;
        }
        int sc = this.sliceCount();
        for (int i = 0; i < sc; ++i) {
            INDArray s = this.slice(i);
            s.getElements(dest, offset);
            offset = (int)((long)offset + s.elementCount());
        }
    }

    @Override
    public void getElements(Object[] dest, int offset) {
        if (this.dimensionality() == 0) {
            dest[offset] = this.get();
            return;
        }
        int sc = this.sliceCount();
        for (int i = 0; i < sc; ++i) {
            INDArray s = this.slice(i);
            s.getElements(dest, offset);
            offset = (int)((long)offset + s.elementCount());
        }
    }

    @Override
    public void copyTo(double[] arr) {
        this.getElements(arr, 0);
    }

    @Override
    public void toDoubleBuffer(DoubleBuffer dest) {
        int sc = this.sliceCount();
        for (int i = 0; i < sc; ++i) {
            INDArray s = this.slice(i);
            s.toDoubleBuffer(dest);
        }
    }

    @Override
    public double[] toDoubleArray() {
        double[] result = Array.createStorage(this.getShape());
        if (this.isSparse()) {
            this.addToArray(result, 0);
        } else {
            this.getElements(result, 0);
        }
        return result;
    }

    @Override
    public double[] asDoubleArray() {
        return null;
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
    public Object sliceValue(int i) {
        if (this.dimensionality() == 1) {
            return this.get(i);
        }
        return this.slice(i);
    }

    @Override
    public /* varargs */ INDArray broadcast(int ... targetShape) {
        int tdims = targetShape.length;
        int dims = this.dimensionality();
        if (tdims < dims) {
            throw new IllegalArgumentException(ErrorMessages.incompatibleBroadcast((INDArray)this, targetShape));
        }
        if (dims == tdims) {
            if (IntArrays.equals(targetShape, this.getShape())) {
                return this;
            }
            throw new IllegalArgumentException(ErrorMessages.incompatibleBroadcast((INDArray)this, targetShape));
        }
        int n = targetShape[0];
        INDArray s = this.broadcast(Arrays.copyOfRange(targetShape, 1, tdims));
        return SliceArray.repeat(s, n);
    }

    @Override
    public INDArray immutable() {
        if (!this.isMutable()) {
            return this;
        }
        return ImmutableArray.create(this);
    }

    @Override
    public INDArray mutable() {
        if (this.isFullyMutable() && !this.isView()) {
            return this;
        }
        return this.clone();
    }

    @Override
    public final INDArray mutableClone() {
        return this.clone();
    }

    @Override
    public INDArray sparse() {
        if (this instanceof ISparse) {
            return this;
        }
        int dims = this.dimensionality();
        switch (dims) {
            case 0: {
                return this;
            }
            case 1: {
                return Vectorz.createSparse(this.asVector());
            }
            case 2: {
                return Matrixx.createSparse(this.getSliceViews());
            }
        }
        int n = this.sliceCount();
        List<INDArray> sls = this.getSliceViews();
        for (int i = 0; i < n; ++i) {
            sls.set(i, sls.get(i).sparse());
        }
        return SliceArray.create(sls);
    }

    @Override
    public INDArray sparseClone() {
        int dims = this.dimensionality();
        switch (dims) {
            case 0: {
                return Scalar.create(this.get());
            }
            case 1: {
                return Vectorz.createSparseMutable(this.asVector());
            }
            case 2: {
                return Matrixx.createSparseRows(this);
            }
        }
        int n = this.sliceCount();
        List<INDArray> sls = this.getSliceViews();
        for (int i = 0; i < n; ++i) {
            sls.set(i, sls.get(i).sparseClone());
        }
        return SliceArray.create(sls);
    }

    @Override
    public INDArray dense() {
        if (this instanceof IDense) {
            return this;
        }
        return this.denseClone();
    }

    @Override
    public INDArray denseClone() {
        int dims = this.dimensionality();
        switch (dims) {
            case 0: {
                return Scalar.create(this.get());
            }
            case 1: {
                return Vector.create(this);
            }
            case 2: {
                return Matrix.create(this);
            }
        }
        return Array.create(this);
    }

    @Override
    public INDArray broadcastLike(INDArray target) {
        return this.broadcast(target.getShape());
    }

    @Override
    public AMatrix broadcastLike(AMatrix target) {
        return Matrixx.toMatrix(this.broadcast(target.getShape()));
    }

    @Override
    public AVector broadcastLike(AVector target) {
        return Vectorz.toVector(this.broadcast(target.getShape()));
    }

    @Override
    public INDArray broadcastCloneLike(INDArray target) {
        int dims = this.dimensionality();
        int targetDims = target.dimensionality();
        INDArray r = this;
        if (dims < targetDims) {
            r = r.broadcastLike(target);
        }
        return r.clone();
    }

    @Override
    public INDArray broadcastCopyLike(INDArray target) {
        if (this.isMutable()) {
            return this.broadcastCloneLike(target);
        }
        return this.broadcastLike(target);
    }

    @Override
    public void validate() {
    }

    protected void checkDimension(int dimension) {
        if (dimension < 0 || dimension >= this.dimensionality()) {
            throw new IndexOutOfBoundsException(ErrorMessages.invalidDimension(this, dimension));
        }
    }

    @Override
    public boolean hasUncountable() {
        if (this.dimensionality() == 0) {
            return Vectorz.isUncountable(this.get());
        }
        int sc = this.sliceCount();
        for (int i = 0; i < sc; ++i) {
            INDArray s = this.slice(i);
            if (!s.hasUncountable()) continue;
            return true;
        }
        return false;
    }

    @Override
    public double elementPowSum(double p) {
        if (this.dimensionality() == 0) {
            double value = this.get();
            return Math.pow(value, p);
        }
        double result = 0.0;
        int n = this.sliceCount();
        for (int i = 0; i < n; ++i) {
            result += this.slice(i).elementPowSum(p);
        }
        return result;
    }

    @Override
    public double elementAbsPowSum(double p) {
        if (this.dimensionality() == 0) {
            double value = Math.abs(this.get());
            return Math.pow(value, p);
        }
        double result = 0.0;
        int n = this.sliceCount();
        for (int i = 0; i < n; ++i) {
            result += this.slice(i).elementAbsPowSum(p);
        }
        return result;
    }
}

