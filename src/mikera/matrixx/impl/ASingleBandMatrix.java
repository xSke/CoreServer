/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.impl;

import mikera.matrixx.impl.ABandedMatrix;
import mikera.vectorz.AVector;
import mikera.vectorz.Vectorz;

public abstract class ASingleBandMatrix
extends ABandedMatrix {
    private static final long serialVersionUID = -213068993524224396L;

    public abstract int nonZeroBand();

    public abstract AVector getNonZeroBand();

    @Override
    public boolean isSymmetric() {
        if (this.rowCount() != this.columnCount()) {
            return false;
        }
        if (this.nonZeroBand() == 0 || this.getNonZeroBand().isZero()) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isIdentity() {
        return this.isSquare() && this.nonZeroBand() == 0 && this.getNonZeroBand().elementsEqual(1.0);
    }

    @Override
    public boolean isZero() {
        return this.getNonZeroBand().isZero();
    }

    @Override
    public boolean isSparse() {
        return true;
    }

    @Override
    public long nonZeroCount() {
        return this.getNonZeroBand().nonZeroCount();
    }

    @Override
    public AVector getBand(int band) {
        if (band == this.nonZeroBand()) {
            return this.getNonZeroBand();
        }
        return Vectorz.createZeroVector(this.bandLength(band));
    }

    @Override
    public boolean hasUncountable() {
        return this.getNonZeroBand().hasUncountable();
    }

    @Override
    public int rank() {
        return (int)this.getNonZeroBand().nonZeroCount();
    }

    @Override
    public double elementPowSum(double p) {
        return this.getNonZeroBand().elementPowSum(p);
    }

    @Override
    public double elementAbsPowSum(double p) {
        return this.getNonZeroBand().elementAbsPowSum(p);
    }
}

