/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz.impl;

import java.util.Iterator;
import java.util.List;
import mikera.arrayz.INDArray;
import mikera.matrixx.AMatrix;
import mikera.vectorz.AScalar;
import mikera.vectorz.AVector;
import mikera.vectorz.Scalar;
import mikera.vectorz.Vector;
import mikera.vectorz.Vectorz;
import mikera.vectorz.impl.ASizedVector;
import mikera.vectorz.impl.RepeatedElementIterator;
import mikera.vectorz.util.DoubleArrays;
import mikera.vectorz.util.ErrorMessages;

public final class RepeatedElementVector
extends ASizedVector {
    private final double value;

    private RepeatedElementVector(int length, double value) {
        super(length);
        this.value = value;
    }

    public static RepeatedElementVector create(int length, double value) {
        if (length < 1) {
            throw new IllegalArgumentException("RepeatedElementVector must have at least one element");
        }
        return new RepeatedElementVector(length, value);
    }

    @Override
    public boolean isFullyMutable() {
        return false;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public boolean isZero() {
        return this.value == 0.0;
    }

    @Override
    public boolean isRangeZero(int start, int length) {
        return length == 0 || this.value == 0.0;
    }

    @Override
    public boolean isElementConstrained() {
        return true;
    }

    @Override
    public double get(int i) {
        this.checkIndex(i);
        return this.value;
    }

    @Override
    public double unsafeGet(int i) {
        return this.value;
    }

    @Override
    public double elementSum() {
        return (double)this.length * this.value;
    }

    @Override
    public double elementProduct() {
        return Math.pow(this.value, this.length);
    }

    @Override
    public double elementMax() {
        return this.value;
    }

    @Override
    public double elementMin() {
        return this.value;
    }

    @Override
    public double magnitudeSquared() {
        return (double)this.length * this.value * this.value;
    }

    @Override
    public double dotProduct(AVector v) {
        return this.value * v.elementSum();
    }

    @Override
    public double dotProduct(double[] data, int offset) {
        return this.value * DoubleArrays.elementSum(data, offset, this.length);
    }

    @Override
    public double dotProduct(Vector v) {
        return this.value * v.elementSum();
    }

    @Override
    public AVector innerProduct(double d) {
        return RepeatedElementVector.create(this.length, d * this.value);
    }

    @Override
    public AVector innerProduct(AMatrix m) {
        int rc = m.rowCount();
        int cc = m.columnCount();
        this.checkLength(rc);
        Vector r = Vector.createLength(cc);
        List<AVector> cols = m.getColumns();
        for (int i = 0; i < cc; ++i) {
            AVector col = cols.get(i);
            r.unsafeSet(i, this.value * col.elementSum());
        }
        return r;
    }

    @Override
    public Scalar innerProduct(AVector v) {
        return Scalar.create(this.dotProduct(v));
    }

    @Override
    public AVector reorder(int dim, int[] order) {
        this.checkDimension(dim);
        return this.reorder(order);
    }

    @Override
    public AVector reorder(int[] order) {
        int n = order.length;
        if (n == this.length) {
            return this;
        }
        return RepeatedElementVector.create(n, this.value);
    }

    @Override
    public AVector reciprocalCopy() {
        return RepeatedElementVector.create(this.length, 1.0 / this.value);
    }

    @Override
    public AVector absCopy() {
        return RepeatedElementVector.create(this.length, Math.abs(this.value));
    }

    @Override
    public AVector negateCopy() {
        return RepeatedElementVector.create(this.length, - this.value);
    }

    @Override
    public AVector addCopy(AVector v) {
        return v.addCopy(this.value);
    }

    @Override
    public AVector multiplyCopy(AVector v) {
        this.checkSameLength(v);
        return v.scaleCopy(this.value);
    }

    @Override
    public AVector addCopy(double v) {
        return Vectorz.createRepeatedElement(this.length, this.value + v);
    }

    @Override
    public void addToArray(double[] data, int offset) {
        DoubleArrays.add(data, offset, this.length, this.value);
    }

    @Override
    public long nonZeroCount() {
        return this.value == 0.0 ? 0L : (long)this.length;
    }

    @Override
    public void set(int i, double value) {
        throw new UnsupportedOperationException(ErrorMessages.immutable(this));
    }

    @Override
    public Iterator<Double> iterator() {
        return new RepeatedElementIterator(this.length, this.value);
    }

    @Override
    public AVector subVector(int offset, int length) {
        int len = this.checkRange(offset, length);
        if (length == len) {
            return this;
        }
        return Vectorz.createRepeatedElement(length, this.value);
    }

    @Override
    public AVector tryEfficientJoin(AVector a) {
        if (a instanceof RepeatedElementVector) {
            RepeatedElementVector ra = (RepeatedElementVector)a;
            if (ra.value == this.value) {
                return Vectorz.createRepeatedElement(this.length + ra.length, this.value);
            }
        }
        return null;
    }

    @Override
    public RepeatedElementVector exactClone() {
        return new RepeatedElementVector(this.length, this.value);
    }

    @Override
    public boolean equalsArray(double[] data, int offset) {
        for (int i = 0; i < this.length; ++i) {
            if (data[offset + i] == this.value) continue;
            return false;
        }
        return true;
    }

    @Override
    public boolean elementsEqual(double value) {
        return this.value == value;
    }

    @Override
    public boolean hasUncountable() {
        return Double.isNaN(this.value) || Double.isInfinite(this.value);
    }

    @Override
    public double elementPowSum(double p) {
        return (double)this.length * Math.pow(this.value, p);
    }

    @Override
    public double elementAbsPowSum(double p) {
        return (double)this.length * Math.pow(Math.abs(this.value), p);
    }
}

