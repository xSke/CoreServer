/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz.ops;

import mikera.vectorz.Op;

public class Composed
extends Op {
    public final Op inner;
    public final Op outer;

    private Composed(Op outer, Op inner) {
        this.outer = outer;
        this.inner = inner;
    }

    public static Op compose(Op outer, Op inner) {
        if (inner instanceof Composed) {
            Composed ci = (Composed)inner;
            return outer.compose(ci.outer).compose(ci.inner);
        }
        return new Composed(outer, inner);
    }

    public static Op create(Op a, Op b) {
        return Composed.compose(a, b);
    }

    @Override
    public double apply(double x) {
        return this.outer.apply(this.inner.apply(x));
    }

    @Override
    public void applyTo(double[] data, int start, int length) {
        this.inner.applyTo(data, start, length);
        this.outer.applyTo(data, start, length);
    }

    @Override
    public boolean isBounded() {
        return this.outer.isBounded();
    }

    @Override
    public double minValue() {
        return this.outer.minValue();
    }

    @Override
    public boolean hasDerivative() {
        return this.outer.hasDerivative() && this.inner.hasDerivative();
    }

    @Override
    public boolean hasDerivativeForOutput() {
        return this.outer.hasInverse() && this.outer.hasDerivativeForOutput() && this.inner.hasDerivativeForOutput();
    }

    @Override
    public double derivativeForOutput(double y) {
        return this.outer.derivativeForOutput(y) * this.inner.derivativeForOutput(this.outer.applyInverse(y));
    }

    @Override
    public double derivative(double x) {
        double y = this.inner.apply(x);
        return this.outer.derivative(y) * this.inner.derivative(x);
    }

    @Override
    public Op getDerivativeOp() {
        return this.outer.getDerivativeOp().compose(this.inner).product(this.inner.getDerivativeOp());
    }

    @Override
    public double maxValue() {
        return this.outer.maxValue();
    }

    @Override
    public boolean hasInverse() {
        return this.outer.hasInverse() && this.inner.hasInverse();
    }

    @Override
    public boolean isStochastic() {
        return this.outer.isStochastic() || this.inner.isStochastic();
    }

    @Override
    public Op getInverse() {
        return this.inner.getInverse().compose(this.outer.getInverse());
    }

    @Override
    public double averageValue() {
        return this.outer.averageValue();
    }

    @Override
    public String toString() {
        return "Composed(" + this.outer + "," + this.inner + ")";
    }
}

