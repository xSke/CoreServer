/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz.impl;

import mikera.arrayz.INDArray;
import mikera.indexz.Index;
import mikera.matrixx.AMatrix;
import mikera.matrixx.impl.AVectorMatrix;
import mikera.matrixx.impl.SparseColumnMatrix;
import mikera.matrixx.impl.SparseRowMatrix;
import mikera.vectorz.AVector;
import mikera.vectorz.Op;
import mikera.vectorz.Vector;
import mikera.vectorz.Vectorz;
import mikera.vectorz.impl.ADenseArrayVector;
import mikera.vectorz.impl.ASizedVector;
import mikera.vectorz.impl.ASparseIndexedVector;
import mikera.vectorz.impl.ASparseVector;
import mikera.vectorz.impl.SparseHashedVector;
import mikera.vectorz.impl.SparseImmutableVector;
import mikera.vectorz.impl.ZeroVector;
import mikera.vectorz.util.DoubleArrays;
import mikera.vectorz.util.IntArrays;
import mikera.vectorz.util.VectorzException;

public class SparseIndexedVector
extends ASparseIndexedVector {
    private static final long serialVersionUID = 750093598603613879L;
    private Index index;
    private double[] data;

    private SparseIndexedVector(int length, Index index) {
        this(length, index, new double[index.length()]);
    }

    private SparseIndexedVector(int length, Index index, double[] data) {
        super(length);
        this.index = index;
        this.data = data;
    }

    private SparseIndexedVector(int length, Index index, AVector source) {
        this(length, index, source.toDoubleArray());
    }

    public static SparseIndexedVector wrap(int length, Index index, double[] data) {
        assert (index.length() == data.length);
        assert (index.isDistinctSorted());
        return new SparseIndexedVector(length, index, data);
    }

    public static SparseIndexedVector wrap(int length, int[] indices, double[] data) {
        Index index = Index.wrap(indices);
        assert (index.length() == data.length);
        assert (index.isDistinctSorted());
        return new SparseIndexedVector(length, index, data);
    }

    public static SparseIndexedVector create(int length, Index index, double[] data) {
        if (!index.isDistinctSorted()) {
            throw new VectorzException("Index must be sorted and distinct");
        }
        if (index.length() != data.length) {
            throw new VectorzException("Length of index: mismatch woth data");
        }
        return new SparseIndexedVector(length, index.clone(), DoubleArrays.copyOf(data));
    }

    public static AVector createWithIndices(AVector v, int[] ixs) {
        int length = v.length();
        int n = ixs.length;
        double[] data = new double[n];
        v.getElements(data, 0, ixs);
        return SparseIndexedVector.wrap(length, ixs, data);
    }

    public static SparseIndexedVector createLength(int length) {
        return new SparseIndexedVector(length, Index.EMPTY, DoubleArrays.EMPTY);
    }

    public static SparseIndexedVector create(int length, Index index, AVector data) {
        SparseIndexedVector sv = SparseIndexedVector.create(length, index, new double[index.length()]);
        data.getElements(sv.data, 0);
        return sv;
    }

    public static SparseIndexedVector create(AVector source) {
        if (source instanceof ASparseVector) {
            return SparseIndexedVector.create((ASparseVector)source);
        }
        int srcLength = source.length();
        if (srcLength == 0) {
            throw new IllegalArgumentException("Can't create a length 0 SparseIndexedVector");
        }
        int[] indexes = source.nonZeroIndices();
        int len = indexes.length;
        double[] vals = new double[len];
        for (int i = 0; i < len; ++i) {
            vals[i] = source.unsafeGet(indexes[i]);
        }
        return SparseIndexedVector.wrap(srcLength, Index.wrap(indexes), vals);
    }

    public static SparseIndexedVector create(ASparseVector source) {
        int length = source.length();
        if (length == 0) {
            throw new IllegalArgumentException("Can't create a length 0 SparseIndexedVector");
        }
        Index ixs = source.nonSparseIndex();
        int n = ixs.length();
        double[] vals = new double[n];
        for (int i = 0; i < n; ++i) {
            vals[i] = source.unsafeGet(ixs.unsafeGet(i));
        }
        return SparseIndexedVector.wrap(length, ixs, vals);
    }

    public static SparseIndexedVector create(SparseHashedVector source) {
        int length = source.length();
        if (length == 0) {
            throw new IllegalArgumentException("Can't create a length 0 SparseIndexedVector");
        }
        Index ixs = source.nonSparseIndex();
        int n = ixs.length();
        double[] vals = new double[n];
        for (int i = 0; i < n; ++i) {
            vals[i] = source.unsafeGet(ixs.unsafeGet(i));
        }
        return SparseIndexedVector.wrap(length, ixs, vals);
    }

    public static AVector createFromRow(AMatrix m, int row) {
        if (m instanceof AVectorMatrix) {
            return SparseIndexedVector.create(m.getRow(row));
        }
        return SparseIndexedVector.create(m.getRow(row));
    }

    @Override
    public int nonSparseElementCount() {
        return this.data.length;
    }

    public AVector innerProduct(SparseRowMatrix m) {
        int cc = m.columnCount();
        int rc = m.rowCount();
        this.checkLength(rc);
        SparseIndexedVector r = SparseIndexedVector.createLength(cc);
        int n = this.nonSparseElementCount();
        for (int ii = 0; ii < n; ++ii) {
            int i;
            AVector row;
            double value = this.data[ii];
            if (value == 0.0 || (row = m.unsafeGetVector(i = this.index.get(ii))) == null) continue;
            r.addMultiple(row, value);
        }
        return r;
    }

    public AVector innerProduct(SparseColumnMatrix m) {
        int cc = m.columnCount();
        int rc = m.rowCount();
        this.checkLength(rc);
        SparseIndexedVector r = SparseIndexedVector.createLength(cc);
        for (int i = 0; i < cc; ++i) {
            r.unsafeSet(i, this.dotProduct((ASparseVector)m.getColumn(i)));
        }
        return r;
    }

    @Override
    public AVector innerProduct(AMatrix m) {
        if (m instanceof SparseRowMatrix) {
            return this.innerProduct((SparseRowMatrix)m);
        }
        if (m instanceof SparseColumnMatrix) {
            return this.innerProduct((SparseColumnMatrix)m);
        }
        int cc = m.columnCount();
        int rc = m.rowCount();
        this.checkLength(rc);
        SparseIndexedVector r = SparseIndexedVector.createLength(cc);
        for (int i = 0; i < cc; ++i) {
            r.unsafeSet(i, this.dotProduct(m.getColumn(i)));
        }
        return r;
    }

    @Override
    public void add(AVector v) {
        if (v instanceof ASparseVector) {
            this.add((ASparseVector)v);
            return;
        }
        this.includeIndices(v);
        for (int i = 0; i < this.data.length; ++i) {
            double[] arrd = this.data;
            int n = i;
            arrd[n] = arrd[n] + v.unsafeGet(this.index.get(i));
        }
    }

    @Override
    public void addMultiple(AVector v, double factor) {
        if (factor == 0.0) {
            return;
        }
        if (v instanceof ASparseVector) {
            this.addMultiple((ASparseVector)v, factor);
            return;
        }
        super.addMultiple(v, factor);
    }

    @Override
    public void add(double[] src, int srcOffset) {
        this.includeIndices(Vectorz.wrap(src, srcOffset, this.length));
        for (int i = 0; i < this.data.length; ++i) {
            double[] arrd = this.data;
            int n = i;
            arrd[n] = arrd[n] + src[srcOffset + this.index.get(i)];
        }
    }

    @Override
    public void add(ASparseVector v) {
        this.checkSameLength(v);
        if (v instanceof ZeroVector) {
            return;
        }
        this.includeIndices(v);
        for (int i = 0; i < this.data.length; ++i) {
            double[] arrd = this.data;
            int n = i;
            arrd[n] = arrd[n] + v.unsafeGet(this.index.get(i));
        }
    }

    public void addMultiple(ASparseVector v, double factor) {
        this.checkSameLength(v);
        if (factor == 0.0 || v instanceof ZeroVector) {
            return;
        }
        this.includeIndices(v);
        for (int i = 0; i < this.data.length; ++i) {
            double[] arrd = this.data;
            int n = i;
            arrd[n] = arrd[n] + v.unsafeGet(this.index.get(i)) * factor;
        }
    }

    @Override
    public void sub(AVector v) {
        if (v instanceof ASparseVector) {
            this.sub((ASparseVector)v);
            return;
        }
        this.includeIndices(v);
        for (int i = 0; i < this.data.length; ++i) {
            double[] arrd = this.data;
            int n = i;
            arrd[n] = arrd[n] - v.unsafeGet(this.index.get(i));
        }
    }

    public void sub(ASparseVector v) {
        if (v instanceof ZeroVector) {
            return;
        }
        this.includeIndices(v);
        for (int i = 0; i < this.data.length; ++i) {
            double[] arrd = this.data;
            int n = i;
            arrd[n] = arrd[n] - v.unsafeGet(this.index.get(i));
        }
    }

    @Override
    public void multiply(double d) {
        if (d == 0.0) {
            this.data = DoubleArrays.EMPTY;
            this.index = Index.EMPTY;
        } else {
            DoubleArrays.multiply(this.data, d);
        }
    }

    @Override
    public void multiply(AVector v) {
        if (v instanceof ADenseArrayVector) {
            this.multiply((ADenseArrayVector)v);
            return;
        }
        if (v instanceof ASparseVector) {
            this.multiply((ASparseVector)v);
        } else {
            this.checkSameLength(v);
            double[] data = this.data;
            int[] ixs = this.index.data;
            for (int i = 0; i < data.length; ++i) {
                double[] arrd = data;
                int n = i;
                arrd[n] = arrd[n] * v.unsafeGet(ixs[i]);
            }
        }
    }

    public void multiply(ASparseVector v) {
        this.checkSameLength(v);
        int[] thisIndex = this.index.data;
        int[] thatIndex = v.nonSparseIndex().data;
        int[] tix = IntArrays.intersectSorted(thatIndex, thisIndex);
        int n = tix.length;
        double[] ndata = new double[n];
        int i1 = 0;
        int i2 = 0;
        for (int i = 0; i < n; ++i) {
            int ti = tix[i];
            while (thatIndex[i1] != ti) {
                ++i1;
            }
            while (thisIndex[i2] != ti) {
                ++i2;
            }
            ndata[i] = v.unsafeGet(thatIndex[i1]) * this.unsafeGet(thisIndex[i2]);
        }
        this.data = ndata;
        this.index = Index.wrap(tix);
    }

    @Override
    public void multiply(ADenseArrayVector v) {
        this.multiply(v.getArray(), v.getArrayOffset());
    }

    @Override
    public void multiply(double[] array, int offset) {
        double[] data = this.data;
        int[] ixs = this.index.data;
        for (int i = 0; i < data.length; ++i) {
            double[] arrd = data;
            int n = i;
            arrd[n] = arrd[n] * array[offset + ixs[i]];
        }
    }

    @Override
    public double maxAbsElement() {
        double[] data = this.data;
        double result = 0.0;
        for (int i = 0; i < data.length; ++i) {
            double d = Math.abs(data[i]);
            if (d <= result) continue;
            result = d;
        }
        return result;
    }

    @Override
    public int maxElementIndex() {
        int ind;
        double[] data = this.data;
        if (data.length == 0) {
            return 0;
        }
        double result = data[0];
        int di = 0;
        for (int i = 1; i < data.length; ++i) {
            double d = data[i];
            if (d <= result) continue;
            result = d;
            di = i;
        }
        if (result < 0.0 && (ind = this.index.findMissing()) > 0) {
            return ind;
        }
        return this.index.get(di);
    }

    @Override
    public int maxAbsElementIndex() {
        double[] data = this.data;
        if (data.length == 0) {
            return 0;
        }
        double result = data[0];
        int di = 0;
        for (int i = 1; i < data.length; ++i) {
            double d = Math.abs(data[i]);
            if (d <= result) continue;
            result = d;
            di = i;
        }
        return this.index.get(di);
    }

    @Override
    public int minElementIndex() {
        int ind;
        double[] data = this.data;
        if (data.length == 0) {
            return 0;
        }
        double result = data[0];
        int di = 0;
        for (int i = 1; i < data.length; ++i) {
            double d = data[i];
            if (d >= result) continue;
            result = d;
            di = i;
        }
        if (result > 0.0 && (ind = this.index.findMissing()) >= 0) {
            return ind;
        }
        return this.index.get(di);
    }

    @Override
    public void negate() {
        DoubleArrays.negate(this.data);
    }

    @Override
    public void applyOp(Op op) {
        int dlen = this.data.length;
        if (dlen < this.length() && (op.isStochastic() || op.apply(0.0) != 0.0)) {
            super.applyOp(op);
        } else {
            op.applyTo(this.data);
        }
    }

    @Override
    public void abs() {
        DoubleArrays.abs(this.data);
    }

    @Override
    public double get(int i) {
        this.checkIndex(i);
        int ip = this.index.indexPosition(i);
        if (ip < 0) {
            return 0.0;
        }
        return this.data[ip];
    }

    @Override
    public double unsafeGet(int i) {
        int ip = this.index.indexPosition(i);
        if (ip < 0) {
            return 0.0;
        }
        return this.data[ip];
    }

    @Override
    public boolean isFullyMutable() {
        return true;
    }

    @Override
    public boolean isMutable() {
        return this.length > 0;
    }

    @Override
    public void setElements(double[] array, int offset) {
        int nz = DoubleArrays.nonZeroCount(array, offset, this.length);
        int[] ixs = new int[nz];
        double[] data = new double[nz];
        this.data = data;
        int di = 0;
        for (int i = 0; i < this.length; ++i) {
            double x = array[offset + i];
            if (x == 0.0) continue;
            ixs[di] = i;
            data[di] = x;
            ++di;
        }
        this.index = Index.wrap(ixs);
    }

    @Override
    public void setElements(int pos, double[] array, int offset, int length) {
        if (length >= this.length) {
            this.setElements(array, offset);
            return;
        }
        int nz = DoubleArrays.nonZeroCount(array, offset, length);
        int[] ixs = new int[nz];
        double[] data = new double[nz];
        this.data = data;
        int di = pos;
        for (int i = 0; i < length; ++i) {
            double x = array[offset + i];
            if (x == 0.0) continue;
            ixs[di] = i;
            data[di] = x;
            ++di;
        }
        this.index = Index.wrap(ixs);
    }

    @Override
    public void set(AVector v) {
        this.checkSameLength(v);
        if (v instanceof ADenseArrayVector) {
            this.set((ADenseArrayVector)v);
            return;
        }
        if (v instanceof ASparseVector) {
            int[] nzi = v.nonZeroIndices();
            this.index = Index.wrap(nzi);
            if (nzi.length != this.data.length) {
                this.data = new double[nzi.length];
            }
            for (int i = 0; i < this.index.length(); ++i) {
                double val;
                this.data[i] = val = v.unsafeGet(this.index.get(i));
            }
            return;
        }
        double[] data = this.data;
        int nz = (int)v.nonZeroCount();
        if (nz != data.length) {
            this.data = data = new double[nz];
            this.index = Index.createLength(nz);
        }
        int di = 0;
        for (int i = 0; i < nz; ++i) {
            double val = v.unsafeGet(i);
            if (val == 0.0) continue;
            data[di] = val;
            this.index.set(di, i);
            ++di;
        }
    }

    @Override
    public void set(ADenseArrayVector v) {
        this.checkSameLength(v);
        this.setElements(v.getArray(), v.getArrayOffset());
    }

    @Override
    public void set(int i, double value) {
        this.checkIndex(i);
        this.unsafeSet(i, value);
    }

    @Override
    public void unsafeSet(int i, double value) {
        int ip = this.index.indexPosition(i);
        if (ip < 0) {
            if (value == 0.0) {
                return;
            }
            int npos = this.index.seekPosition(i);
            this.data = DoubleArrays.insert(this.data, npos, value);
            this.index = this.index.insert(npos, i);
        } else {
            this.data[ip] = value;
        }
    }

    @Override
    public ASparseVector roundToZero(double precision) {
        int[] aboveInds = new int[this.data.length];
        double[] aboveData = new double[this.data.length];
        int ai = 0;
        for (int i = 0; i < this.index.length(); ++i) {
            if (this.data[i] <= precision) continue;
            aboveInds[ai] = this.index.get(i);
            aboveData[ai] = this.data[i];
            ++ai;
        }
        int[] newInds = new int[ai];
        double[] newData = new double[ai];
        System.arraycopy(aboveInds, 0, newInds, 0, ai);
        System.arraycopy(aboveData, 0, newData, 0, ai);
        return SparseIndexedVector.wrap(this.length, newInds, newData);
    }

    @Override
    public void addAt(int i, double value) {
        if (value == 0.0) {
            return;
        }
        int ip = this.index.indexPosition(i);
        if (ip < 0) {
            this.unsafeSet(i, value);
        } else {
            double[] arrd = this.data;
            int n = ip;
            arrd[n] = arrd[n] + value;
        }
    }

    @Override
    public Vector nonSparseValues() {
        return Vector.wrap(this.data);
    }

    @Override
    public Index nonSparseIndex() {
        return this.index;
    }

    @Override
    public Vector toVector() {
        Vector v = Vector.createLength(this.length);
        double[] data = this.data;
        int[] ixs = this.index.data;
        for (int i = 0; i < data.length; ++i) {
            v.unsafeSet(ixs[i], data[i]);
        }
        return v;
    }

    @Override
    public SparseIndexedVector clone() {
        return this.exactClone();
    }

    public void includeIndices(int[] ixs) {
        int[] nixs = IntArrays.mergeSorted(this.index.data, ixs);
        if (nixs.length == this.index.length()) {
            return;
        }
        int nl = nixs.length;
        double[] data = this.data;
        double[] ndata = new double[nl];
        int si = 0;
        for (int i = 0; i < nl && si < data.length; ++i) {
            int z = this.index.data[si];
            if (z != nixs[i]) continue;
            ndata[i] = data[si];
            ++si;
        }
        this.data = ndata;
        this.index = Index.wrap(nixs);
    }

    public void includeIndices(Index ixs) {
        this.includeIndices(ixs.data);
    }

    public void includeIndices(AVector v) {
        if (v instanceof ASparseIndexedVector) {
            this.includeIndices((ASparseIndexedVector)v);
        } else {
            this.includeIndices(v.nonSparseIndex());
        }
    }

    public void includeIndices(ASparseIndexedVector v) {
        this.includeIndices(v.internalIndex());
    }

    @Override
    public SparseIndexedVector sparseClone() {
        return this.exactClone();
    }

    @Override
    public SparseIndexedVector exactClone() {
        return new SparseIndexedVector(this.length, this.index.clone(), (double[])this.data.clone());
    }

    @Override
    public void validate() {
        if (this.index.length() != this.data.length) {
            throw new VectorzException("Inconsistent data and index!");
        }
        if (!this.index.isDistinctSorted()) {
            throw new VectorzException("Invalid index: " + this.index);
        }
        super.validate();
    }

    @Override
    public AVector immutable() {
        return SparseImmutableVector.create(this);
    }

    @Override
    double[] internalData() {
        return this.data;
    }

    @Override
    Index internalIndex() {
        return this.index;
    }
}

