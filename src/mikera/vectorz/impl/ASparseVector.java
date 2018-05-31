/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz.impl;

import java.util.Arrays;
import java.util.List;
import mikera.arrayz.INDArray;
import mikera.arrayz.ISparse;
import mikera.indexz.Index;
import mikera.matrixx.AMatrix;
import mikera.vectorz.AVector;
import mikera.vectorz.Vectorz;
import mikera.vectorz.impl.ADenseArrayVector;
import mikera.vectorz.impl.ASizedVector;
import mikera.vectorz.impl.ListWrapper;
import mikera.vectorz.impl.SparseIndexedVector;
import mikera.vectorz.util.ErrorMessages;
import mikera.vectorz.util.VectorzException;

public abstract class ASparseVector
extends ASizedVector
implements ISparse {
    protected ASparseVector(int length) {
        super(length);
    }

    public abstract int nonSparseElementCount();

    public abstract AVector nonSparseValues();

    @Override
    public abstract Index nonSparseIndex();

    public abstract boolean includesIndex(int var1);

    public ASparseVector roundToZero(double precision) {
        throw new VectorzException(ErrorMessages.notYetImplemented());
    }

    @Override
    public void copyTo(int offset, double[] destData, int destOffset, int length) {
        Arrays.fill(destData, destOffset, destOffset + length, 0.0);
        this.addToArray(offset, destData, destOffset, length);
    }

    @Override
    public boolean isZero() {
        return this.nonZeroCount() == 0L;
    }

    @Override
    public boolean isView() {
        return false;
    }

    @Override
    public double dotProduct(AVector v) {
        this.checkSameLength(v);
        double result = 0.0;
        Index ni = this.nonSparseIndex();
        for (int i = 0; i < ni.length(); ++i) {
            int ii = ni.get(i);
            result += this.unsafeGet(ii) * v.unsafeGet(ii);
        }
        return result;
    }

    @Override
    public final double dotProduct(ADenseArrayVector v) {
        this.checkSameLength(v);
        double[] array = v.getArray();
        int offset = v.getArrayOffset();
        return this.dotProduct(array, offset);
    }

    @Override
    public AVector innerProduct(AMatrix m) {
        int cc = m.columnCount();
        int rc = m.rowCount();
        this.checkLength(rc);
        AVector r = Vectorz.createSparseMutable(cc);
        Index ni = this.nonSparseIndex();
        for (int i = 0; i < ni.length(); ++i) {
            int ti = ni.get(i);
            double v = this.unsafeGet(ti);
            if (v == 0.0) continue;
            r.addMultiple(m.getRow(ti), v);
        }
        return r;
    }

    @Override
    public final boolean isSparse() {
        return true;
    }

    @Override
    public void add(AVector v) {
        if (v instanceof ASparseVector) {
            this.add((ASparseVector)v);
            return;
        }
        super.add(v);
    }

    @Override
    public void addMultiple(AVector src, double factor) {
        this.add(src.multiplyCopy(factor));
    }

    public abstract void add(ASparseVector var1);

    @Override
    public List<Double> getSlices() {
        return new ListWrapper(this);
    }

    @Override
    public double elementProduct() {
        int n = this.nonSparseElementCount();
        if (n < this.length) {
            return 0.0;
        }
        return this.nonSparseValues().elementProduct();
    }

    @Override
    public ASparseVector sparse() {
        return this;
    }

    @Override
    public AVector clone() {
        if (this.length < 20 || (double)this.nonSparseElementCount() > (double)this.elementCount() * 0.25) {
            return super.clone();
        }
        return SparseIndexedVector.create(this);
    }

    public boolean equals(ASparseVector v) {
        if (v == this) {
            return true;
        }
        if (v.length != this.length) {
            return false;
        }
        Index ni = this.nonSparseIndex();
        for (int i = 0; i < ni.length(); ++i) {
            int ii = ni.get(i);
            if (this.unsafeGet(ii) == v.unsafeGet(ii)) continue;
            return false;
        }
        Index ri = v.nonSparseIndex();
        for (int i = 0; i < ri.length(); ++i) {
            int ii = ri.get(i);
            if (this.unsafeGet(ii) == v.unsafeGet(ii)) continue;
            return false;
        }
        return true;
    }

    @Override
    public double[] toDoubleArray() {
        double[] data = new double[this.length];
        this.addToArray(data, 0);
        return data;
    }

    @Override
    public long nonZeroCount() {
        return this.nonSparseValues().nonZeroCount();
    }

    @Override
    public boolean equals(AVector v) {
        if (v instanceof ASparseVector) {
            return this.equals((ASparseVector)v);
        }
        if (v.length() != this.length) {
            return false;
        }
        Index ni = this.nonSparseIndex();
        int n = ni.length();
        AVector nv = this.nonSparseValues();
        int offset = 0;
        for (int i = 0; i < n; ++i) {
            int ii = ni.get(i);
            if (!v.isRangeZero(offset, ii - offset)) {
                return false;
            }
            if (nv.unsafeGet(i) != v.unsafeGet(ii)) {
                return false;
            }
            offset = ii + 1;
        }
        return v.isRangeZero(offset, this.length - offset);
    }

    @Override
    public boolean hasUncountable() {
        return this.nonSparseValues().hasUncountable();
    }

    @Override
    public double elementPowSum(double p) {
        return this.nonSparseValues().elementPowSum(p);
    }

    @Override
    public double elementAbsPowSum(double p) {
        return this.nonSparseValues().elementAbsPowSum(p);
    }
}

