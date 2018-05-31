/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz.ops;

import mikera.vectorz.Op;
import mikera.vectorz.Ops;
import mikera.vectorz.ops.AFunctionOp;
import mikera.vectorz.ops.Exp;
import mikera.vectorz.ops.Linear;

public final class LogN
extends AFunctionOp {
    private final double base;
    private final double logBase;
    public static final LogN LOG10 = LogN.create(10.0);

    private LogN(double base) {
        this.base = base;
        this.logBase = Math.log(base);
    }

    public static LogN create(double base) {
        return new LogN(base);
    }

    public double getBase() {
        return this.base;
    }

    @Override
    public double apply(double x) {
        return Math.log(x) / this.logBase;
    }

    @Override
    public double derivative(double x) {
        return 1.0 / x;
    }

    @Override
    public double derivativeForOutput(double y) {
        return 1.0 / Math.exp(y);
    }

    @Override
    public double applyInverse(double y) {
        return Math.exp(y * this.logBase);
    }

    @Override
    public boolean hasDerivative() {
        return true;
    }

    @Override
    public boolean hasInverse() {
        return true;
    }

    @Override
    public double minDomain() {
        return Double.MIN_VALUE;
    }

    @Override
    public Op getInverse() {
        return Exp.INSTANCE.compose(Linear.create(this.logBase, 0.0));
    }

    @Override
    public Op getDerivativeOp() {
        return Ops.RECIPROCAL;
    }
}

