/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz.ops;

import mikera.vectorz.Op;

public class Inverse
extends Op {
    private final Op op;

    public Inverse(Op op) {
        this.op = op;
    }

    @Override
    public double apply(double x) {
        return this.op.applyInverse(x);
    }

    @Override
    public double applyInverse(double y) {
        return this.op.apply(y);
    }

    @Override
    public double minValue() {
        return this.op.minDomain();
    }

    @Override
    public double maxValue() {
        return this.op.maxDomain();
    }

    @Override
    public double minDomain() {
        return this.op.minValue();
    }

    @Override
    public double maxDomain() {
        return this.op.maxValue();
    }

    @Override
    public double derivative(double y) {
        return 1.0 / this.op.derivative(this.op.applyInverse(y));
    }

    @Override
    public Op getInverse() {
        return this.op;
    }

    @Override
    public double averageValue() {
        return this.op.applyInverse(this.op.averageValue());
    }

    @Override
    public boolean hasInverse() {
        return true;
    }

    @Override
    public boolean hasDerivative() {
        return this.op.hasDerivative();
    }
}

