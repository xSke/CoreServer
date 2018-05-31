/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.impl;

import mikera.arrayz.INDArray;
import mikera.matrixx.AMatrix;
import mikera.vectorz.util.VectorzException;

public abstract class ABooleanMatrix
extends AMatrix {
    private static final long serialVersionUID = 1599922421314660198L;

    @Override
    public boolean isBoolean() {
        return true;
    }

    @Override
    public boolean isFullyMutable() {
        return false;
    }

    @Override
    public double elementMax() {
        if (this.elementCount() == 0L) {
            return -1.7976931348623157E308;
        }
        return this.isZero() ? 0.0 : 1.0;
    }

    @Override
    public double elementSum() {
        return this.nonZeroCount();
    }

    @Override
    public double elementSquaredSum() {
        return this.nonZeroCount();
    }

    @Override
    public AMatrix signumCopy() {
        return this.copy();
    }

    @Override
    public AMatrix squareCopy() {
        return this.copy();
    }

    @Override
    public AMatrix absCopy() {
        return this.copy();
    }

    @Override
    public void validate() {
        if (!this.clone().isBoolean()) {
            throw new VectorzException("Clone of boolean matrix should be boolean!");
        }
        super.validate();
    }

    @Override
    public boolean hasUncountable() {
        return false;
    }

    @Override
    public double elementPowSum(double p) {
        return this.nonZeroCount();
    }

    @Override
    public double elementAbsPowSum(double p) {
        return this.elementPowSum(p);
    }
}

