/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz.impl;

import mikera.vectorz.impl.ASizedVector;

public abstract class ABooleanVector
extends ASizedVector {
    protected ABooleanVector(int length) {
        super(length);
    }

    @Override
    public boolean isBoolean() {
        return true;
    }

    @Override
    public boolean isElementConstrained() {
        return true;
    }

    @Override
    public boolean isFullyMutable() {
        return false;
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

