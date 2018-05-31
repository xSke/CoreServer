/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz.ops;

import mikera.vectorz.Op;
import mikera.vectorz.ops.ALinearOp;

public final class Sum
extends Op {
    public final Op a;
    public final Op b;

    private Sum(Op a, Op b) {
        this.a = a;
        this.b = b;
    }

    public static Op create(Op a, Op b) {
        if (b instanceof ALinearOp && !(a instanceof ALinearOp)) {
            return b.sum(a);
        }
        return new Sum(a, b);
    }

    @Override
    public double apply(double x) {
        return this.a.apply(x) + this.b.apply(x);
    }

    @Override
    public double averageValue() {
        return this.a.averageValue() + this.b.averageValue();
    }

    @Override
    public boolean hasDerivative() {
        return this.a.hasDerivative() && this.b.hasDerivative();
    }

    @Override
    public double derivative(double x) {
        return this.a.derivative(x) + this.b.derivative(x);
    }

    @Override
    public Op getDerivativeOp() {
        return this.a.getDerivativeOp().sum(this.b.getDerivativeOp());
    }

    @Override
    public boolean isStochastic() {
        return this.a.isStochastic() || this.b.isStochastic();
    }

    @Override
    public String toString() {
        return "Sum(" + this.a + "," + this.b + ")";
    }
}

