/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz;

import java.nio.DoubleBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import mikera.arrayz.Arrayz;
import mikera.arrayz.INDArray;
import mikera.arrayz.ISparse;
import mikera.arrayz.impl.AbstractArray;
import mikera.arrayz.impl.SliceArray;
import mikera.indexz.Index;
import mikera.matrixx.AMatrix;
import mikera.matrixx.Matrix;
import mikera.matrixx.Matrixx;
import mikera.matrixx.impl.BroadcastVectorMatrix;
import mikera.matrixx.impl.RowMatrix;
import mikera.randomz.Hash;
import mikera.util.Maths;
import mikera.vectorz.AScalar;
import mikera.vectorz.IOperator;
import mikera.vectorz.IVector;
import mikera.vectorz.Op;
import mikera.vectorz.Scalar;
import mikera.vectorz.Tools;
import mikera.vectorz.Vector;
import mikera.vectorz.Vector3;
import mikera.vectorz.Vectorz;
import mikera.vectorz.impl.ADenseArrayVector;
import mikera.vectorz.impl.ASizedVector;
import mikera.vectorz.impl.ASparseVector;
import mikera.vectorz.impl.ImmutableVector;
import mikera.vectorz.impl.IndexedSubVector;
import mikera.vectorz.impl.JoinedVector;
import mikera.vectorz.impl.ListWrapper;
import mikera.vectorz.impl.Vector0;
import mikera.vectorz.impl.VectorIndexScalar;
import mikera.vectorz.impl.VectorIterator;
import mikera.vectorz.impl.WrappedSubVector;
import mikera.vectorz.ops.Logistic;
import mikera.vectorz.util.DoubleArrays;
import mikera.vectorz.util.ErrorMessages;
import mikera.vectorz.util.VectorzException;

public abstract class AVector
extends AbstractArray<Double>
implements IVector,
Comparable<AVector> {
    @Override
    public abstract int length();

    @Override
    public abstract double get(int var1);

    @Override
    public abstract void set(int var1, double var2);

    @Override
    public double get(long i) {
        if (i < 0L || i >= (long)this.length()) {
            throw new IndexOutOfBoundsException(ErrorMessages.invalidIndex((INDArray)this, i));
        }
        return this.unsafeGet((int)i);
    }

    public void set(long i, double value) {
        if (i < 0L || i >= (long)this.length()) {
            throw new IndexOutOfBoundsException(ErrorMessages.invalidIndex((INDArray)this, i));
        }
        this.unsafeSet((int)i, value);
    }

    @Override
    public void set(int[] indexes, double value) {
        if (indexes.length == 1) {
            this.set(indexes[0], value);
        }
        if (indexes.length != 0) {
            throw new UnsupportedOperationException("" + indexes.length + "D set not supported on AVector");
        }
        this.fill(value);
    }

    public void unsafeSet(int i, double value) {
        this.set(i, value);
    }

    public double unsafeGet(int i) {
        return this.get(i);
    }

    @Override
    public final double get(int x, int y) {
        throw new IllegalArgumentException(ErrorMessages.invalidIndex((INDArray)this, x, y));
    }

    @Override
    public final int dimensionality() {
        return 1;
    }

    @Override
    public final /* varargs */ double get(int ... indexes) {
        if (indexes.length != 1) {
            throw new IllegalArgumentException(ErrorMessages.invalidIndex((INDArray)this, indexes));
        }
        return this.get(indexes[0]);
    }

    @Override
    public double get() {
        throw new UnsupportedOperationException("Can't do 0-d get on a vector!");
    }

    @Override
    public AScalar slice(int position) {
        return VectorIndexScalar.wrap(this, position);
    }

    @Override
    public Object sliceValue(int i) {
        return this.get(i);
    }

    @Override
    public AScalar slice(int dimension, int index) {
        this.checkDimension(dimension);
        return this.slice(index);
    }

    @Override
    public int sliceCount() {
        return this.length();
    }

    @Override
    public List<Double> getSlices() {
        return new ListWrapper(this);
    }

    @Override
    public int[] getShape() {
        return new int[]{this.length()};
    }

    @Override
    public int[] getShapeClone() {
        return new int[]{this.length()};
    }

    @Override
    public int getShape(int dim) {
        if (dim == 0) {
            return this.length();
        }
        throw new IndexOutOfBoundsException(ErrorMessages.invalidDimension(this, dim));
    }

    @Override
    public long[] getLongShape() {
        return new long[]{this.length()};
    }

    @Override
    public long elementCount() {
        return this.length();
    }

    @Override
    public long nonZeroCount() {
        int n = this.length();
        long result = 0L;
        for (int i = 0; i < n; ++i) {
            if (this.unsafeGet(i) == 0.0) continue;
            ++result;
        }
        return result;
    }

    public double[] nonZeroValues() {
        int len = this.length();
        int n = (int)this.nonZeroCount();
        if (n == 0) {
            return DoubleArrays.EMPTY;
        }
        double[] vs = new double[n];
        int vi = 0;
        for (int i = 0; i < len; ++i) {
            double d = this.unsafeGet(i);
            if (d == 0.0) continue;
            vs[vi++] = d;
            if (vi < n) continue;
            return vs;
        }
        return vs;
    }

    @Override
    public AVector subArray(int[] offsets, int[] shape) {
        if (offsets.length != 1) {
            throw new IllegalArgumentException(ErrorMessages.invalidIndex((INDArray)this, offsets));
        }
        if (shape.length != 1) {
            throw new IllegalArgumentException(ErrorMessages.invalidIndex((INDArray)this, offsets));
        }
        return this.subVector(offsets[0], shape[0]);
    }

    @Override
    public INDArray rotateView(int dimension, int shift) {
        this.checkDimension(dimension);
        return this.rotateView(shift);
    }

    public INDArray rotateView(int shift) {
        int n = this.length();
        if (n == 0) {
            return this;
        }
        if ((shift = Maths.mod(shift, n)) == 0) {
            return this;
        }
        return this.subVector(shift, n - shift).join(this.subVector(0, shift));
    }

    public AVector subVector(int offset, int length) {
        int len = this.checkRange(offset, length);
        if (length == 0) {
            return Vector0.INSTANCE;
        }
        if (length == len) {
            return this;
        }
        return WrappedSubVector.wrap(this, offset, length);
    }

    @Override
    public AVector join(INDArray b) {
        if (b.dimensionality() != 1) {
            throw new IllegalArgumentException(ErrorMessages.incompatibleShapes(this, b));
        }
        return this.join(b.asVector());
    }

    public AVector join(AVector second) {
        if (second.length() == 0) {
            return this;
        }
        AVector ej = this.tryEfficientJoin(second);
        if (ej != null) {
            return ej;
        }
        return JoinedVector.joinVectors(this, second);
    }

    public AVector tryEfficientJoin(AVector second) {
        return null;
    }

    @Override
    public INDArray join(INDArray a, int dimension) {
        this.checkDimension(dimension);
        if (a instanceof AVector) {
            return this.join((AVector)a);
        }
        if (a.dimensionality() != 1) {
            throw new IllegalArgumentException(ErrorMessages.incompatibleShapes(this, a));
        }
        return this.join(a.asVector());
    }

    @Override
    public int compareTo(AVector a) {
        int len = this.checkSameLength(a);
        for (int i = 0; i < len; ++i) {
            double diff = this.unsafeGet(i) - a.unsafeGet(i);
            if (diff < 0.0) {
                return -1;
            }
            if (diff <= 0.0) continue;
            return 1;
        }
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof AVector) {
            return this.equals((AVector)o);
        }
        if (o instanceof INDArray) {
            return this.equals((INDArray)o);
        }
        return false;
    }

    @Override
    public boolean equals(AVector v) {
        if (v instanceof ADenseArrayVector) {
            return this.equals((ADenseArrayVector)v);
        }
        if (this == v) {
            return true;
        }
        int len = this.length();
        if (len != v.length()) {
            return false;
        }
        for (int i = 0; i < len; ++i) {
            if (this.unsafeGet(i) == v.unsafeGet(i)) continue;
            return false;
        }
        return true;
    }

    public boolean equals(ADenseArrayVector v) {
        if (this.length() != v.length()) {
            return false;
        }
        return this.equalsArray(v.getArray(), v.getArrayOffset());
    }

    @Override
    public boolean equals(INDArray v) {
        if (v instanceof AVector) {
            return this.equals((AVector)v);
        }
        if (v.dimensionality() != 1) {
            return false;
        }
        int len = this.length();
        if (len != v.getShape(0)) {
            return false;
        }
        for (int i = 0; i < len; ++i) {
            if (this.unsafeGet(i) == v.get(i)) continue;
            return false;
        }
        return true;
    }

    public List<Double> toList() {
        int len = this.length();
        ArrayList<Double> al = new ArrayList<Double>(len);
        for (int i = 0; i < len; ++i) {
            al.add(this.unsafeGet(i));
        }
        return al;
    }

    @Override
    public boolean epsilonEquals(INDArray a, double tolerance) {
        if (a instanceof AVector) {
            return this.epsilonEquals((AVector)a, tolerance);
        }
        if (a.dimensionality() != 1) {
            throw new IllegalArgumentException(ErrorMessages.incompatibleShapes(this, a));
        }
        int len = this.length();
        if (len != a.getShape(0)) {
            throw new IllegalArgumentException(ErrorMessages.incompatibleShapes(this, a));
        }
        for (int i = 0; i < len; ++i) {
            if (Tools.epsilonEquals(this.unsafeGet(i), a.get(i), tolerance)) continue;
            return false;
        }
        return true;
    }

    @Override
    public boolean epsilonEquals(INDArray a) {
        return this.epsilonEquals(a, 1.0E-7);
    }

    public boolean epsilonEquals(AVector v) {
        return this.epsilonEquals(v, 1.0E-7);
    }

    @Override
    public boolean epsilonEquals(AVector v, double tolerance) {
        if (this == v) {
            return true;
        }
        int len = this.checkSameLength(v);
        for (int i = 0; i < len; ++i) {
            if (Tools.epsilonEquals(this.unsafeGet(i), v.unsafeGet(i), tolerance)) continue;
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hashCode = 1;
        int len = this.length();
        for (int i = 0; i < len; ++i) {
            hashCode = 31 * hashCode + Hash.hashCode(this.unsafeGet(i));
        }
        return hashCode;
    }

    @Override
    public final void copyTo(double[] arr) {
        this.getElements(arr, 0);
    }

    public final void copyTo(double[] arr, int offset) {
        this.getElements(arr, offset);
    }

    public void copyTo(int offset, double[] dest, int destOffset, int length) {
        for (int i = 0; i < length; ++i) {
            dest[destOffset + i] = this.unsafeGet(i + offset);
        }
    }

    public void copyTo(int offset, double[] dest, int destOffset, int length, int stride) {
        for (int i = 0; i < length; ++i) {
            dest[destOffset + i * stride] = this.unsafeGet(i + offset);
        }
    }

    @Override
    public double[] toDoubleArray() {
        double[] result = new double[this.length()];
        this.getElements(result, 0);
        return result;
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
    public double[] asDoubleArray() {
        return null;
    }

    @Override
    public void toDoubleBuffer(DoubleBuffer dest) {
        int len = this.length();
        for (int i = 0; i < len; ++i) {
            dest.put(this.unsafeGet(i));
        }
    }

    public void copyTo(AVector dest, int destOffset) {
        if (dest instanceof ADenseArrayVector) {
            this.copyTo((ADenseArrayVector)dest, destOffset);
            return;
        }
        int len = this.length();
        if (destOffset + len > dest.length()) {
            throw new IndexOutOfBoundsException();
        }
        for (int i = 0; i < len; ++i) {
            dest.unsafeSet(destOffset + i, this.unsafeGet(i));
        }
    }

    public void copyTo(ADenseArrayVector dest, int destOffset) {
        this.getElements(dest.getArray(), dest.getArrayOffset() + destOffset);
    }

    public void copyTo(int offset, AVector dest, int destOffset, int length) {
        this.checkRange(offset, length);
        dest.checkRange(destOffset, length);
        for (int i = 0; i < length; ++i) {
            dest.unsafeSet(destOffset + i, this.unsafeGet(offset + i));
        }
    }

    @Override
    public void fill(double value) {
        int len = this.length();
        for (int i = 0; i < len; ++i) {
            this.unsafeSet(i, value);
        }
    }

    public void fillRange(int offset, int length, double value) {
        this.subVector(offset, length).fill(value);
    }

    @Override
    public void clamp(double min, double max) {
        int len = this.length();
        for (int i = 0; i < len; ++i) {
            double v = this.unsafeGet(i);
            if (v < min) {
                this.unsafeSet(i, min);
                continue;
            }
            if (v <= max) continue;
            this.unsafeSet(i, max);
        }
    }

    public void clampMax(double max) {
        int len = this.length();
        for (int i = 0; i < len; ++i) {
            double v = this.unsafeGet(i);
            if (v <= max) continue;
            this.unsafeSet(i, max);
        }
    }

    public void clampMin(double min) {
        int len = this.length();
        for (int i = 0; i < len; ++i) {
            double v = this.unsafeGet(i);
            if (v >= min) continue;
            this.unsafeSet(i, min);
        }
    }

    @Override
    public void multiply(double factor) {
        int len = this.length();
        if (factor == 1.0) {
            return;
        }
        for (int i = 0; i < len; ++i) {
            this.unsafeSet(i, this.unsafeGet(i) * factor);
        }
    }

    @Override
    public void multiply(INDArray a) {
        if (a instanceof AVector) {
            this.multiply((AVector)a);
        } else if (a instanceof AScalar) {
            this.multiply(((AScalar)a).get());
        } else {
            int dims = a.dimensionality();
            switch (dims) {
                case 0: {
                    this.multiply(a.get());
                    return;
                }
                case 1: {
                    this.multiply(a.asVector());
                    return;
                }
            }
            throw new VectorzException("Can't multiply vector with array of dimensionality: " + dims);
        }
    }

    public void multiply(AVector v) {
        if (v instanceof ADenseArrayVector) {
            this.multiply((ADenseArrayVector)v);
            return;
        }
        int len = this.checkSameLength(v);
        for (int i = 0; i < len; ++i) {
            this.unsafeSet(i, this.unsafeGet(i) * v.unsafeGet(i));
        }
    }

    public void multiply(ADenseArrayVector v) {
        this.checkSameLength(v);
        this.multiply(v.getArray(), v.getArrayOffset());
    }

    public void multiply(double[] data, int offset) {
        int len = this.length();
        for (int i = 0; i < len; ++i) {
            this.unsafeSet(i, this.unsafeGet(i) * data[i + offset]);
        }
    }

    public void multiplyTo(double[] data, int offset) {
        int len = this.length();
        for (int i = 0; i < len; ++i) {
            double[] arrd = data;
            int n = i + offset;
            arrd[n] = arrd[n] * this.unsafeGet(i);
        }
    }

    @Override
    public void divide(double factor) {
        this.multiply(1.0 / factor);
    }

    @Override
    public void divide(INDArray a) {
        if (a instanceof AVector) {
            this.divide((AVector)a);
        } else {
            super.divide(a);
        }
    }

    public void divide(AVector v) {
        int len = this.checkSameLength(v);
        for (int i = 0; i < len; ++i) {
            this.unsafeSet(i, this.unsafeGet(i) / v.unsafeGet(i));
        }
    }

    public void divide(double[] data, int offset) {
        int len = this.length();
        for (int i = 0; i < len; ++i) {
            this.unsafeSet(i, this.unsafeGet(i) / data[i + offset]);
        }
    }

    public void divideTo(double[] data, int offset) {
        int len = this.length();
        for (int i = 0; i < len; ++i) {
            double[] arrd = data;
            int n = i + offset;
            arrd[n] = arrd[n] / this.unsafeGet(i);
        }
    }

    @Override
    public void abs() {
        int len = this.length();
        for (int i = 0; i < len; ++i) {
            double val = this.unsafeGet(i);
            if (val >= 0.0) continue;
            this.unsafeSet(i, - val);
        }
    }

    @Override
    public AVector absCopy() {
        AVector v = this.clone();
        v.abs();
        return v;
    }

    @Override
    public void log() {
        int len = this.length();
        for (int i = 0; i < len; ++i) {
            double val = this.unsafeGet(i);
            this.unsafeSet(i, Math.log(val));
        }
    }

    @Override
    public void signum() {
        int len = this.length();
        for (int i = 0; i < len; ++i) {
            this.unsafeSet(i, Math.signum(this.unsafeGet(i)));
        }
    }

    @Override
    public void square() {
        int len = this.length();
        for (int i = 0; i < len; ++i) {
            double x = this.unsafeGet(i);
            this.unsafeSet(i, x * x);
        }
    }

    @Override
    public AVector squareCopy() {
        AVector r = this.clone();
        r.square();
        return r;
    }

    @Override
    public AVector sqrtCopy() {
        AVector r = this.clone();
        r.square();
        return r;
    }

    public void tanh() {
        int len = this.length();
        for (int i = 0; i < len; ++i) {
            double x = this.unsafeGet(i);
            this.unsafeSet(i, Math.tanh(x));
        }
    }

    public void logistic() {
        int len = this.length();
        for (int i = 0; i < len; ++i) {
            double x = this.unsafeGet(i);
            this.unsafeSet(i, Logistic.logisticFunction(x));
        }
    }

    public final void scale(AVector v) {
        this.multiply(v);
    }

    public double scaleToMagnitude(double targetMagnitude) {
        double oldMagnitude = this.magnitude();
        this.multiply(targetMagnitude / oldMagnitude);
        return oldMagnitude;
    }

    public void scaleAdd(double factor, AVector v) {
        this.multiply(factor);
        this.add(v);
    }

    public void interpolate(AVector v, double alpha) {
        this.multiply(1.0 - alpha);
        this.addMultiple(v, alpha);
    }

    public void interpolate(AVector a, AVector b, double alpha) {
        this.set(a);
        this.interpolate(b, alpha);
    }

    public double magnitudeSquared() {
        int len = this.length();
        double total = 0.0;
        for (int i = 0; i < len; ++i) {
            double x = this.unsafeGet(i);
            total += x * x;
        }
        return total;
    }

    @Override
    public final AVector getTranspose() {
        return this;
    }

    @Override
    public AVector getTransposeCopy() {
        return this.clone();
    }

    @Override
    public final AVector getTransposeView() {
        return this;
    }

    public /* varargs */ AVector select(int ... inds) {
        if (this.isMutable()) {
            return this.selectView(inds);
        }
        return this.selectClone(inds);
    }

    public /* varargs */ AVector selectView(int ... inds) {
        return IndexedSubVector.wrap(this, (int[])inds.clone());
    }

    public /* varargs */ AVector selectClone(int ... inds) {
        Vector v = Vector.createLength(inds.length);
        double[] tdata = v.getArray();
        for (int i = 0; i < inds.length; ++i) {
            tdata[i] = this.get(inds[i]);
        }
        return v;
    }

    public AMatrix outerProduct(AVector a) {
        int rc = this.length();
        int cc = a.length();
        Matrix m = Matrix.create(rc, cc);
        int di = 0;
        for (int i = 0; i < rc; ++i) {
            for (int j = 0; j < cc; ++j) {
                m.data[di++] = this.unsafeGet(i) * a.unsafeGet(j);
            }
        }
        return m;
    }

    @Override
    public INDArray outerProduct(INDArray a) {
        if (a instanceof AVector) {
            return this.outerProduct((AVector)a);
        }
        return super.outerProduct(a);
    }

    @Override
    public AScalar innerProduct(AVector v) {
        return Scalar.create(this.dotProduct(v));
    }

    public Scalar innerProduct(Vector v) {
        double[] data = v.getArray();
        int vl = data.length;
        this.checkLength(vl);
        return Scalar.create(this.dotProduct(data, 0));
    }

    public AVector innerProduct(AMatrix m) {
        int cc = m.columnCount();
        int rc = m.rowCount();
        this.checkLength(rc);
        Vector r = Vector.createLength(cc);
        List<AVector> cols = m.getColumns();
        for (int i = 0; i < cc; ++i) {
            double v = this.dotProduct(cols.get(i));
            r.unsafeSet(i, v);
        }
        return r;
    }

    @Override
    public AVector innerProduct(AScalar s) {
        return this.scaleCopy(s.get());
    }

    @Override
    public INDArray innerProduct(INDArray a) {
        if (a instanceof AVector) {
            return this.innerProduct((AVector)a);
        }
        if (a instanceof AScalar) {
            return this.innerProduct((AScalar)a);
        }
        if (a instanceof AMatrix) {
            return this.innerProduct((AMatrix)a);
        }
        if (a.dimensionality() <= 2) {
            return this.innerProduct(Arrayz.create(a));
        }
        int len = this.checkLength(a.sliceCount());
        List<INDArray> al = a.getSliceViews();
        INDArray result = Arrayz.newArray(al.get(0).getShape());
        for (int i = 0; i < len; ++i) {
            result.add(al.get(i).innerProduct(this.get(i)));
        }
        return result;
    }

    @Override
    public AVector innerProduct(double a) {
        return this.scaleCopy(a);
    }

    public double dotProduct(AVector v) {
        if (v instanceof ADenseArrayVector) {
            return this.dotProduct((ADenseArrayVector)v);
        }
        if (v instanceof ASparseVector) {
            return ((ASparseVector)v).dotProduct(this);
        }
        int len = this.checkSameLength(v);
        double total = 0.0;
        for (int i = 0; i < len; ++i) {
            total += this.unsafeGet(i) * v.unsafeGet(i);
        }
        return total;
    }

    public double dotProduct(Vector v) {
        v.checkLength(this.length());
        return this.dotProduct(v.getArray(), 0);
    }

    public double dotProduct(ADenseArrayVector v) {
        v.checkLength(this.length());
        return this.dotProduct(v.getArray(), v.getArrayOffset());
    }

    public double dotProduct(AVector v, Index ix) {
        int vl = v.length();
        if (vl != ix.length()) {
            throw new IllegalArgumentException("Mismatched source vector and index lengths. Index length should be " + vl);
        }
        double result = 0.0;
        for (int i = 0; i < vl; ++i) {
            result += this.unsafeGet(ix.get(i)) * v.unsafeGet(i);
        }
        return result;
    }

    public abstract double dotProduct(double[] var1, int var2);

    public void crossProduct(AVector a) {
        if (this.checkSameLength(a) != 3) {
            throw new IllegalArgumentException("Cross product requires length 3 vectors");
        }
        double x = this.unsafeGet(0);
        double y = this.unsafeGet(1);
        double z = this.unsafeGet(2);
        double x2 = a.unsafeGet(0);
        double y2 = a.unsafeGet(1);
        double z2 = a.unsafeGet(2);
        double tx = y * z2 - z * y2;
        double ty = z * x2 - x * z2;
        double tz = x * y2 - y * x2;
        this.unsafeSet(0, tx);
        this.unsafeSet(1, ty);
        this.unsafeSet(2, tz);
    }

    public void crossProduct(Vector3 a) {
        if (this.length() != 3) {
            throw new IllegalArgumentException("Cross product requires length 3 vectors");
        }
        double x = this.unsafeGet(0);
        double y = this.unsafeGet(1);
        double z = this.unsafeGet(2);
        double x2 = a.x;
        double y2 = a.y;
        double z2 = a.z;
        double tx = y * z2 - z * y2;
        double ty = z * x2 - x * z2;
        double tz = x * y2 - y * x2;
        this.unsafeSet(0, tx);
        this.unsafeSet(1, ty);
        this.unsafeSet(2, tz);
    }

    public double magnitude() {
        return Math.sqrt(this.magnitudeSquared());
    }

    public double distanceSquared(AVector v) {
        int len = this.checkSameLength(v);
        double total = 0.0;
        for (int i = 0; i < len; ++i) {
            double d = this.unsafeGet(i) - v.unsafeGet(i);
            total += d * d;
        }
        return total;
    }

    public double distance(AVector v) {
        return Math.sqrt(this.distanceSquared(v));
    }

    public double distanceL1(AVector v) {
        int len = this.checkSameLength(v);
        double total = 0.0;
        for (int i = 0; i < len; ++i) {
            double d = this.unsafeGet(i) - v.unsafeGet(i);
            total += Math.abs(d);
        }
        return total;
    }

    public double distanceLinf(AVector v) {
        int len = this.checkSameLength(v);
        double result = 0.0;
        for (int i = 0; i < len; ++i) {
            double d = Math.abs(this.unsafeGet(i) - v.unsafeGet(i));
            result = Math.max(result, d);
        }
        return result;
    }

    public double maxAbsElement() {
        int len = this.length();
        double result = 0.0;
        for (int i = 0; i < len; ++i) {
            double comp = Math.abs(this.unsafeGet(i));
            if (comp <= result) continue;
            result = comp;
        }
        return result;
    }

    public int maxAbsElementIndex() {
        int len = this.length();
        if (len == 0) {
            throw new IllegalArgumentException("Can't find maxAbsElementIndex of a 0-length vector");
        }
        int result = 0;
        double best = Math.abs(this.unsafeGet(0));
        for (int i = 1; i < len; ++i) {
            double comp = Math.abs(this.unsafeGet(i));
            if (comp <= best) continue;
            result = i;
            best = comp;
        }
        return result;
    }

    public final double maxElement() {
        return this.elementMax();
    }

    public int maxElementIndex() {
        int len = this.length();
        if (len == 0) {
            throw new IllegalArgumentException("Can't find maxElementIndex of a 0-length vector");
        }
        int result = 0;
        double best = this.unsafeGet(0);
        for (int i = 1; i < len; ++i) {
            double comp = this.unsafeGet(i);
            if (comp <= best) continue;
            result = i;
            best = comp;
        }
        return result;
    }

    public final double minElement() {
        return this.elementMin();
    }

    public int minElementIndex() {
        int len = this.length();
        if (len == 0) {
            throw new IllegalArgumentException("Can't find minElementIndex of a 0-length vector");
        }
        int result = 0;
        double best = this.unsafeGet(0);
        for (int i = 1; i < len; ++i) {
            double comp = this.unsafeGet(i);
            if (comp >= best) continue;
            result = i;
            best = comp;
        }
        return result;
    }

    public double normaliseMaxAbsElement() {
        double scale = this.maxAbsElement();
        if (scale != 0.0) {
            this.scale(1.0 / scale);
        }
        return scale;
    }

    @Override
    public double elementSum() {
        int len = this.length();
        double result = 0.0;
        for (int i = 0; i < len; ++i) {
            result += this.unsafeGet(i);
        }
        return result;
    }

    @Override
    public double elementProduct() {
        int len = this.length();
        double result = 1.0;
        for (int i = 0; i < len; ++i) {
            result *= this.unsafeGet(i);
        }
        return result;
    }

    @Override
    public double elementMax() {
        return this.unsafeGet(this.maxElementIndex());
    }

    @Override
    public double elementMin() {
        return this.unsafeGet(this.minElementIndex());
    }

    @Override
    public final double elementSquaredSum() {
        return this.magnitudeSquared();
    }

    @Override
    public double elementPowSum(double exponent) {
        int n = this.length();
        double result = 0.0;
        for (int i = 0; i < n; ++i) {
            double x = this.unsafeGet(i);
            result += Math.pow(x, exponent);
        }
        return result;
    }

    @Override
    public double elementAbsPowSum(double exponent) {
        int n = this.length();
        double result = 0.0;
        for (int i = 0; i < n; ++i) {
            double x = Math.abs(this.unsafeGet(i));
            result += Math.pow(x, exponent);
        }
        return result;
    }

    public double angle(AVector v) {
        return Math.acos(this.dotProduct(v) / (v.magnitude() * this.magnitude()));
    }

    @Override
    public double normalise() {
        double d = this.magnitude();
        if (d > 0.0) {
            this.multiply(1.0 / d);
        }
        return d;
    }

    @Override
    public AVector normaliseCopy() {
        double d = this.magnitude();
        if (d > 0.0) {
            return this.multiplyCopy(1.0 / d);
        }
        return this.copy();
    }

    @Override
    public void negate() {
        this.multiply(-1.0);
    }

    @Override
    public AVector negateCopy() {
        return this.multiplyCopy(-1.0);
    }

    @Override
    public final AVector scaleCopy(double d) {
        return this.multiplyCopy(d);
    }

    @Override
    public void pow(double exponent) {
        int len = this.length();
        for (int i = 0; i < len; ++i) {
            this.unsafeSet(i, Math.pow(this.unsafeGet(i), exponent));
        }
    }

    public void set(AVector src) {
        if (src instanceof ADenseArrayVector) {
            this.set((ADenseArrayVector)src);
        } else {
            int len = this.length();
            src.checkLength(len);
            for (int i = 0; i < len; ++i) {
                this.unsafeSet(i, src.unsafeGet(i));
            }
        }
    }

    public void set(ADenseArrayVector v) {
        v.checkLength(this.length());
        this.setElements(v.getArray(), v.getArrayOffset());
    }

    @Override
    public void set(double a) {
        this.fill(a);
    }

    @Deprecated
    public void set(double[] data) {
        this.setElements(data);
    }

    @Override
    public /* varargs */ void setElements(double ... data) {
        this.checkLength(data.length);
        this.setElements(data, 0);
    }

    @Override
    public void setElements(double[] data, int offset) {
        this.setElements(0, data, offset, this.length());
    }

    @Override
    public void set(INDArray a) {
        if (a instanceof AVector) {
            this.set((AVector)a);
            return;
        }
        if (a.dimensionality() != 1) {
            throw new IllegalArgumentException("Cannot set vector using array of dimensonality: " + a.dimensionality());
        }
        this.setElements(a.getElements());
    }

    @Override
    public void setElements(int pos, double[] values, int offset, int length) {
        this.checkRange(pos, length);
        for (int i = 0; i < length; ++i) {
            this.unsafeSet(i + pos, values[offset + i]);
        }
    }

    @Override
    public void getElements(double[] dest, int offset) {
        this.copyTo(0, dest, offset, this.length());
    }

    @Override
    public void getElements(Object[] dest, int offset) {
        int n = this.length();
        for (int i = 0; i < n; ++i) {
            dest[offset + i] = this.get(i);
        }
    }

    public void getElements(double[] data, int offset, int[] indices) {
        int n = indices.length;
        for (int i = 0; i < n; ++i) {
            data[offset + i] = this.unsafeGet(indices[i]);
        }
    }

    public void set(AVector src, int srcOffset) {
        int len = this.length();
        src.checkRange(srcOffset, len);
        for (int i = 0; i < len; ++i) {
            this.unsafeSet(i, src.unsafeGet(srcOffset + i));
        }
    }

    public long zeroCount() {
        return this.elementCount() - this.nonZeroCount();
    }

    @Override
    public AVector clone() {
        return Vector.create(this);
    }

    @Override
    public AVector copy() {
        if (!this.isMutable()) {
            return this;
        }
        return this.clone();
    }

    @Override
    public AVector sparseClone() {
        return Vectorz.createSparseMutable(this);
    }

    @Override
    public final AVector asVector() {
        return this;
    }

    @Override
    public /* varargs */ INDArray reshape(int ... dimensions) {
        int ndims = dimensions.length;
        if (ndims == 1) {
            return Vector.createFromVector(this, dimensions[0]);
        }
        if (ndims == 2) {
            return Matrixx.createFromVector(this, dimensions[0], dimensions[1]);
        }
        return Arrayz.createFromVector(this, dimensions);
    }

    @Override
    public AVector reorder(int[] order) {
        return this.reorder(0, order);
    }

    @Override
    public AVector reorder(int dim, int[] order) {
        this.checkDimension(dim);
        Vector result = Vector.createLength(order.length);
        result.set(this, order);
        return result;
    }

    @Override
    public boolean isView() {
        return true;
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public boolean isElementConstrained() {
        return false;
    }

    @Override
    public boolean isFullyMutable() {
        return this.isMutable();
    }

    public void add(AVector v) {
        if (v instanceof ADenseArrayVector) {
            this.add((ADenseArrayVector)v);
            return;
        }
        int length = this.checkSameLength(v);
        for (int i = 0; i < length; ++i) {
            this.addAt(i, v.unsafeGet(i));
        }
    }

    public final void add(ADenseArrayVector v) {
        this.checkSameLength(v);
        this.add(v.getArray(), v.getArrayOffset());
    }

    @Override
    public void add(INDArray a) {
        if (a instanceof AVector) {
            this.add((AVector)a);
        } else if (a instanceof AScalar) {
            this.add(a.get());
        } else {
            super.add(a);
        }
    }

    @Override
    public INDArray addCopy(INDArray a) {
        if (a instanceof AVector) {
            return this.addCopy((AVector)a);
        }
        if (a.dimensionality() == 1) {
            return this.addCopy(a.asVector());
        }
        if (a.dimensionality() == 0) {
            return this.addCopy(a.get());
        }
        return this.addCopy(a.broadcastLike(this));
    }

    @Override
    public AVector addCopy(AVector a) {
        AVector r = this.clone();
        r.add(a);
        return r;
    }

    public AVector addCopy(double a) {
        AVector r = this.clone();
        r.add(a);
        return r;
    }

    @Override
    public INDArray subCopy(INDArray a) {
        if (a instanceof AVector) {
            return this.subCopy((AVector)a);
        }
        if (a.dimensionality() == 1) {
            return this.subCopy(a.asVector());
        }
        return this.subCopy(a.broadcastLike(this));
    }

    @Override
    public AVector subCopy(AVector a) {
        AVector r = this.clone();
        r.sub(a);
        return r;
    }

    @Override
    public INDArray multiplyCopy(INDArray a) {
        if (a instanceof AVector) {
            return this.multiplyCopy((AVector)a);
        }
        if (a.dimensionality() == 1) {
            return this.multiplyCopy(a.asVector());
        }
        return this.multiplyCopy(a.broadcastLike(this));
    }

    @Override
    public INDArray divideCopy(INDArray a) {
        if (a instanceof AVector) {
            return this.divideCopy((AVector)a);
        }
        if (a.dimensionality() == 1) {
            return this.divideCopy(a.asVector());
        }
        return this.divideCopy(a.broadcastLike(this));
    }

    @Override
    public AVector multiplyCopy(AVector a) {
        AVector r = this.clone();
        r.multiply(a);
        return r;
    }

    @Override
    public AVector divideCopy(AVector a) {
        AVector r = this.clone();
        r.divide(a);
        return r;
    }

    @Override
    public void sub(INDArray a) {
        if (a instanceof AVector) {
            this.sub((AVector)a);
        } else if (a instanceof AScalar) {
            this.sub(a.get());
        } else {
            super.sub(a);
        }
    }

    public void add(AVector src, int srcOffset) {
        int length = this.length();
        src.checkRange(srcOffset, length);
        for (int i = 0; i < length; ++i) {
            this.addAt(i, src.unsafeGet(srcOffset + i));
        }
    }

    public void add(int offset, AVector src) {
        this.add(offset, src, 0, src.length());
    }

    public void add(int offset, AVector src, int srcOffset, int length) {
        for (int i = 0; i < length; ++i) {
            this.addAt(offset + i, src.unsafeGet(i + srcOffset));
        }
    }

    public void addProduct(AVector a, AVector b) {
        this.addProduct(a, b, 1.0);
    }

    public AVector addProductCopy(AVector a, AVector b) {
        AVector r = this.clone();
        r.addProduct(a, b);
        return r;
    }

    public AVector addProductCopy(AVector a, AVector b, double factor) {
        AVector r = this.clone();
        r.addProduct(a, b, factor);
        return r;
    }

    public void addProduct(AVector a, AVector b, double factor) {
        this.checkSameLength(a, b);
        if (factor == 0.0) {
            return;
        }
        if (a.isSparse() || b.isSparse()) {
            AVector t = a.multiplyCopy(b);
            this.addMultiple(t, factor);
        } else {
            this.addProduct(a, 0, b, 0, factor);
        }
    }

    public int checkLength(int length) {
        int len = this.length();
        if (len != length) {
            throw new IllegalArgumentException("Vector length mismatch, expected length = " + length + ", but got length = " + len);
        }
        return len;
    }

    @Override
    protected final void checkDimension(int dimension) {
        if (dimension != 0) {
            throw new IndexOutOfBoundsException(ErrorMessages.invalidDimension(this, dimension));
        }
    }

    public void addMultiple(AVector src, double factor) {
        this.checkSameLength(src);
        this.addMultiple(src, 0, factor);
    }

    public AVector addMultipleCopy(AVector src, double factor) {
        AVector r = this.clone();
        r.addMultiple(src, factor);
        return r;
    }

    public void addMultiple(AVector src, int srcOffset, double factor) {
        this.addMultiple(0, src, srcOffset, this.length(), factor);
    }

    public void addMultiple(int offset, AVector src, int srcOffset, int length, double factor) {
        this.checkRange(offset, length);
        src.checkRange(srcOffset, length);
        if (factor == 0.0) {
            return;
        }
        if (factor == 1.0) {
            this.add(offset, src, srcOffset, length);
        } else {
            for (int i = 0; i < length; ++i) {
                this.addAt(i + offset, src.unsafeGet(i + srcOffset) * factor);
            }
        }
    }

    public final void addMultiple(int offset, AVector v, double factor) {
        this.addMultiple(offset, v, 0, v.length(), factor);
    }

    public void addWeighted(AVector v, double factor) {
        this.multiply(1.0 - factor);
        this.addMultiple(v, factor);
    }

    public void sub(AVector v) {
        this.addMultiple(v, -1.0);
    }

    @Override
    public void sub(double d) {
        this.add(- d);
    }

    public void subAt(int i, double v) {
        this.addAt(i, - v);
    }

    @Override
    public boolean isZero() {
        return this.isRangeZero(0, this.length());
    }

    public boolean isRangeZero(int start, int length) {
        for (int i = 0; i < length; ++i) {
            if (this.unsafeGet(start + i) == 0.0) continue;
            return false;
        }
        return true;
    }

    public boolean isUnitLengthVector() {
        return this.isUnitLengthVector(1.0E-7);
    }

    public boolean isUnitLengthVector(double tolerance) {
        double mag = this.magnitudeSquared();
        return Math.abs(mag - 1.0) <= tolerance;
    }

    @Override
    public final boolean isSameShape(INDArray a) {
        if (a instanceof AVector) {
            return this.isSameShape((AVector)a);
        }
        if (a.dimensionality() != 1) {
            return false;
        }
        return this.length() == a.getShape(0);
    }

    public boolean isSameShape(AVector a) {
        return this.length() == a.length();
    }

    protected int checkSameLength(AVector v) {
        int len = this.length();
        if (len != v.length()) {
            throw new IllegalArgumentException(ErrorMessages.incompatibleShapes(this, (INDArray)v));
        }
        return len;
    }

    protected int checkSameLength(AVector v, AVector w) {
        int len = this.length();
        if (len != v.length()) {
            throw new IllegalArgumentException(ErrorMessages.incompatibleShapes(this, (INDArray)v));
        }
        if (len != w.length()) {
            throw new IllegalArgumentException(ErrorMessages.incompatibleShapes(this, (INDArray)w));
        }
        return len;
    }

    public int checkRange(int offset, int length) {
        int len = this.length();
        int end = offset + length;
        if (offset < 0 || end > len) {
            throw new IndexOutOfBoundsException(ErrorMessages.invalidRange(this, offset, length));
        }
        return len;
    }

    public int checkIndex(int i) {
        int len = this.length();
        if (i < 0 || i >= len) {
            throw new IndexOutOfBoundsException(ErrorMessages.invalidIndex((INDArray)this, (long)i));
        }
        return len;
    }

    protected int checkSameLength(ASizedVector v) {
        int len = this.length();
        if (len != v.length()) {
            throw new IllegalArgumentException(ErrorMessages.incompatibleShapes(this, (INDArray)v));
        }
        return len;
    }

    public void projectToPlane(AVector normal, double distance) {
        assert (Tools.epsilonEquals(normal.magnitude(), 1.0));
        double d = this.dotProduct(normal);
        this.addMultiple(normal, distance - d);
    }

    public void subMultiple(AVector v, double factor) {
        this.addMultiple(v, - factor);
    }

    @Override
    public String toString() {
        if (this.elementCount() > 10000L) {
            Index shape = Index.create(this.getShape());
            return "Large vector with shape: " + shape.toString();
        }
        return this.toStringFull();
    }

    @Override
    public String toStringFull() {
        StringBuilder sb = new StringBuilder();
        int length = this.length();
        sb.append('[');
        if (length > 0) {
            sb.append(this.unsafeGet(0));
            for (int i = 1; i < length; ++i) {
                sb.append(',');
                sb.append(this.unsafeGet(i));
            }
        }
        sb.append(']');
        return sb.toString();
    }

    @Override
    public Vector toVector() {
        return Vector.create(this);
    }

    @Override
    public AVector immutable() {
        if (!this.isMutable()) {
            return this;
        }
        return ImmutableVector.create(this);
    }

    @Override
    public AVector mutable() {
        if (this.isFullyMutable()) {
            return this;
        }
        return this.clone();
    }

    @Override
    public AVector sparse() {
        if (this instanceof ISparse) {
            return this;
        }
        return Vectorz.createSparse(this);
    }

    @Override
    public AVector dense() {
        return this.denseClone();
    }

    @Override
    public final Vector denseClone() {
        return Vector.wrap(this.toDoubleArray());
    }

    public AVector toNormal() {
        Vector v = Vector.create(this);
        v.normalise();
        return v;
    }

    @Override
    public List<Double> asElementList() {
        return new ListWrapper(this);
    }

    @Override
    public Iterator<Double> iterator() {
        return new VectorIterator(this);
    }

    @Override
    public Iterator<Double> elementIterator() {
        return this.iterator();
    }

    public void set(IVector vector) {
        this.set(vector.asVector());
    }

    public void addMultiple(Vector source, Index sourceToDest, double factor) {
        int len = source.length();
        if (len != sourceToDest.length()) {
            throw new IllegalArgumentException("Index length must match source length.");
        }
        double[] data = source.getArray();
        for (int i = 0; i < len; ++i) {
            int j = sourceToDest.data[i];
            this.addAt(j, data[i] * factor);
        }
    }

    public void addMultiple(AVector source, Index sourceToDest, double factor) {
        int len = source.length();
        if (len != sourceToDest.length()) {
            throw new IllegalArgumentException("Index length must match source length.");
        }
        for (int i = 0; i < len; ++i) {
            int j = sourceToDest.data[i];
            this.addAt(j, source.unsafeGet(i) * factor);
        }
    }

    public void addMultiple(Index destToSource, Vector source, double factor) {
        int len = this.length();
        if (len != destToSource.length()) {
            throw new IllegalArgumentException("Index length must match this vector length.");
        }
        double[] data = source.getArray();
        for (int i = 0; i < len; ++i) {
            int j = destToSource.data[i];
            this.addAt(i, data[j] * factor);
        }
    }

    public void addMultiple(Index destToSource, AVector source, double factor) {
        int len = this.length();
        if (len != destToSource.length()) {
            throw new IllegalArgumentException("Index length must match this vector length.");
        }
        for (int i = 0; i < len; ++i) {
            int j = destToSource.data[i];
            this.addAt(i, source.get(j) * factor);
        }
    }

    public final void set(AVector source, Index indexes) {
        this.set(source, indexes.data);
    }

    public void set(AVector source, int[] indexes) {
        int len = this.length();
        if (len != indexes.length) {
            throw new IllegalArgumentException("Index length must match this vector length.");
        }
        for (int i = 0; i < len; ++i) {
            this.unsafeSet(i, source.get(indexes[i]));
        }
    }

    @Override
    public void addToArray(double[] array, int offset) {
        this.addToArray(0, array, offset, this.length());
    }

    public void addToArray(double[] data, int offset, int stride) {
        int n = this.length();
        for (int i = 0; i < n; ++i) {
            double[] arrd = data;
            int n2 = offset + i * stride;
            arrd[n2] = arrd[n2] + this.unsafeGet(i);
        }
    }

    public void addToArray(int offset, double[] array, int arrayOffset, int length) {
        this.checkRange(offset, length);
        for (int i = 0; i < length; ++i) {
            double[] arrd = array;
            int n = i + arrayOffset;
            arrd[n] = arrd[n] + this.unsafeGet(i + offset);
        }
    }

    public void addMultipleToArray(double factor, int offset, double[] array, int arrayOffset, int length) {
        this.checkRange(offset, length);
        for (int i = 0; i < length; ++i) {
            double[] arrd = array;
            int n = i + arrayOffset;
            arrd[n] = arrd[n] + factor * this.unsafeGet(i + offset);
        }
    }

    public void addProductToArray(double factor, int offset, AVector other, int otherOffset, double[] array, int arrayOffset, int length) {
        if (other instanceof ADenseArrayVector) {
            this.addProductToArray(factor, offset, (ADenseArrayVector)other, otherOffset, array, arrayOffset, length);
            return;
        }
        if (offset < 0 || offset + length > this.length()) {
            throw new IndexOutOfBoundsException();
        }
        for (int i = 0; i < length; ++i) {
            double[] arrd = array;
            int n = i + arrayOffset;
            arrd[n] = arrd[n] + factor * this.unsafeGet(i + offset) * other.get(i + otherOffset);
        }
    }

    public void addProductToArray(double factor, int offset, ADenseArrayVector other, int otherOffset, double[] array, int arrayOffset, int length) {
        if (offset < 0 || offset + length > this.length()) {
            throw new IndexOutOfBoundsException();
        }
        double[] otherArray = other.getArray();
        otherOffset += other.getArrayOffset();
        for (int i = 0; i < length; ++i) {
            double[] arrd = array;
            int n = i + arrayOffset;
            arrd[n] = arrd[n] + factor * this.unsafeGet(i + offset) * otherArray[i + otherOffset];
        }
    }

    public void addProduct(AVector a, int aOffset, AVector b, int bOffset, double factor) {
        int length = this.length();
        a.checkRange(aOffset, length);
        b.checkRange(bOffset, length);
        for (int i = 0; i < length; ++i) {
            this.addAt(i, a.unsafeGet(i + aOffset) * b.unsafeGet(i + bOffset) * factor);
        }
    }

    @Override
    public void applyOp(IOperator op) {
        if (op instanceof Op) {
            this.applyOp((Op)op);
        }
        int len = this.length();
        for (int i = 0; i < len; ++i) {
            this.unsafeSet(i, op.apply(this.unsafeGet(i)));
        }
    }

    @Override
    public void applyOp(Op op) {
        int len = this.length();
        for (int i = 0; i < len; ++i) {
            this.unsafeSet(i, op.apply(this.unsafeGet(i)));
        }
    }

    @Override
    public AVector applyOpCopy(Op op) {
        AVector r = this.clone();
        r.applyOp(op);
        return r;
    }

    @Override
    public void addAt(int i, double v) {
        if (v == 0.0) {
            return;
        }
        this.unsafeSet(i, this.unsafeGet(i) + v);
    }

    @Override
    public void scaleAdd(double factor, double constant) {
        this.scale(factor);
        this.add(constant);
    }

    @Override
    public void add(double constant) {
        if (constant == 0.0) {
            return;
        }
        int len = this.length();
        for (int i = 0; i < len; ++i) {
            this.addAt(i, constant);
        }
    }

    public void add(double[] data, int offset) {
        int len = this.length();
        for (int i = 0; i < len; ++i) {
            double v = data[i + offset];
            this.addAt(i, v);
        }
    }

    public void add(double[] data) {
        this.checkLength(data.length);
        this.add(data, 0);
    }

    @Override
    public abstract AVector exactClone();

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
        for (int i = 0; i < length; ++i) {
            if (this.unsafeGet(i) == value) continue;
            return false;
        }
        return true;
    }

    @Override
    public boolean equalsArray(double[] data, int offset) {
        int len = this.length();
        for (int i = 0; i < len; ++i) {
            if (this.unsafeGet(i) == data[offset + i]) continue;
            return false;
        }
        return true;
    }

    public void setRange(int offset, double[] data, int dataOffset, int length) {
        this.checkRange(offset, length);
        for (int i = 0; i < length; ++i) {
            this.unsafeSet(offset + i, data[dataOffset + i]);
        }
    }

    @Override
    public /* varargs */ INDArray broadcast(int ... targetShape) {
        int tdims = targetShape.length;
        if (tdims == 0) {
            throw new IllegalArgumentException(ErrorMessages.incompatibleBroadcast((INDArray)this, targetShape));
        }
        int len = this.length();
        if (targetShape[tdims - 1] != len) {
            throw new IllegalArgumentException(ErrorMessages.incompatibleBroadcast((INDArray)this, targetShape));
        }
        if (tdims == 1) {
            return this;
        }
        if (tdims == 2) {
            int n = targetShape[0];
            INDArray[] vs = new AVector[n];
            for (int i = 0; i < n; ++i) {
                vs[i] = this;
            }
            return Matrixx.createFromVectors(vs);
        }
        int n = targetShape[0];
        INDArray s = this.broadcast(Arrays.copyOfRange(targetShape, 1, tdims));
        return SliceArray.repeat(s, n);
    }

    @Override
    public INDArray broadcastLike(INDArray target) {
        if (target instanceof AVector) {
            return this.broadcastLike((AVector)target);
        }
        if (target instanceof AMatrix) {
            return this.broadcastLike((AMatrix)target);
        }
        return this.broadcast(target.getShape());
    }

    @Override
    public AVector broadcastLike(AVector target) {
        if (this.length() == target.length()) {
            return this;
        }
        throw new IllegalArgumentException(ErrorMessages.incompatibleBroadcast((INDArray)this, target));
    }

    @Override
    public AMatrix broadcastLike(AMatrix target) {
        int cc = target.columnCount();
        if (this.length() == cc) {
            int rc = target.rowCount();
            if (rc == 1) {
                return RowMatrix.wrap(this);
            }
            return BroadcastVectorMatrix.wrap(this, rc);
        }
        throw new IllegalArgumentException(ErrorMessages.incompatibleBroadcast((INDArray)this, target));
    }

    @Override
    public void validate() {
        if (this.length() < 0) {
            throw new VectorzException("Illegal length! Length = " + this.length());
        }
        super.validate();
    }

    public Index nonSparseIndex() {
        return Index.of(this.nonZeroIndices());
    }

    public int[] nonZeroIndices() {
        int n = (int)this.nonZeroCount();
        int[] ret = new int[n];
        int length = this.length();
        int di = 0;
        for (int i = 0; i < length; ++i) {
            if (this.unsafeGet(i) == 0.0) continue;
            ret[di++] = i;
        }
        if (di != n) {
            throw new VectorzException("Invalid non-zero index count. Maybe concurrent modification?");
        }
        return ret;
    }

    @Override
    public AVector multiplyCopy(double d) {
        AVector r = this.clone();
        r.multiply(d);
        return r;
    }

    @Override
    public boolean hasUncountable() {
        int len = this.length();
        for (int i = 0; i < len; ++i) {
            double v = this.unsafeGet(i);
            if (!Vectorz.isUncountable(v)) continue;
            return true;
        }
        return false;
    }
}

