/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz.ops;

import mikera.util.Rand;
import mikera.vectorz.ops.ABoundedOp;

public class StochasticBinary
extends ABoundedOp {
    public static final StochasticBinary INSTANCE = new StochasticBinary();

    @Override
    public boolean isStochastic() {
        return true;
    }

    @Override
    public double apply(double x) {
        return Rand.nextDouble() < x ? 1.0 : 0.0;
    }

    @Override
    public double applyInverse(double y) {
        return y;
    }

    @Override
    public double minValue() {
        return 0.0;
    }

    @Override
    public double maxValue() {
        return 1.0;
    }

    @Override
    public boolean hasDerivative() {
        return true;
    }

    @Override
    public double derivative(double x) {
        if (x < 0.0 || x > 1.0) {
            return 0.0;
        }
        return 1.0;
    }

    @Override
    public double derivativeForOutput(double y) {
        return 1.0;
    }
}

