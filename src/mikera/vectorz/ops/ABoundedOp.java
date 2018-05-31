/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz.ops;

import mikera.vectorz.Op;

public abstract class ABoundedOp
extends Op {
    @Override
    public boolean isBounded() {
        return true;
    }

    @Override
    public abstract double minValue();

    @Override
    public abstract double maxValue();

    @Override
    public double averageValue() {
        return (this.minValue() + this.maxValue()) * 0.5;
    }
}

