/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz.impl;

import mikera.vectorz.AVector;
import mikera.vectorz.impl.AComputedVector;
import mikera.vectorz.impl.Vector0;
import mikera.vectorz.util.ErrorMessages;

public class RangeVector
extends AComputedVector {
    private static final long serialVersionUID = 2068299118332621781L;
    private final int start;

    private RangeVector(int start, int length) {
        super(length);
        this.start = start;
    }

    public static AVector create(int start, int length) {
        if (length == 0) {
            return Vector0.INSTANCE;
        }
        if (length < 0) {
            throw new IllegalArgumentException(ErrorMessages.illegalSize(length));
        }
        return new RangeVector(start, length);
    }

    @Override
    public double elementMin() {
        return this.start;
    }

    @Override
    public double elementMax() {
        return this.start + this.length - 1;
    }

    @Override
    public double elementSum() {
        return (double)this.start * (double)this.length + (double)(this.length * (this.length - 1) / 2);
    }

    @Override
    public double get(int i) {
        this.checkIndex(i);
        return this.start + i;
    }

    @Override
    public double unsafeGet(int i) {
        return this.start + i;
    }

    @Override
    public double dotProduct(double[] data, int offset) {
        double res = 0.0;
        for (int i = 0; i < this.length; ++i) {
            res += (double)(i + this.start) * data[i + offset];
        }
        return res;
    }

    @Override
    public AVector subVector(int start, int length) {
        int len = this.checkRange(start, length);
        if (length == 0) {
            return Vector0.INSTANCE;
        }
        if (length == len) {
            return this;
        }
        return RangeVector.create(this.start + start, length);
    }

    @Override
    public AVector tryEfficientJoin(AVector a) {
        if (a instanceof RangeVector) {
            RangeVector ra = (RangeVector)a;
            if (ra.start == this.start + this.length) {
                return RangeVector.create(this.start, this.length + ra.length);
            }
        }
        return null;
    }
}

