/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz.ops;

import mikera.util.Rand;
import mikera.vectorz.Op;
import mikera.vectorz.ops.Constant;

public class GaussianNoise
extends Op {
    private final double std;
    public static final GaussianNoise UNIT_NOISE = new GaussianNoise(1.0);

    private GaussianNoise(double stdev) {
        this.std = stdev;
    }

    public static GaussianNoise create(double stdDev) {
        return new GaussianNoise(stdDev);
    }

    @Override
    public boolean isStochastic() {
        return true;
    }

    @Override
    public double apply(double x) {
        return x + Rand.nextGaussian() * this.std;
    }

    @Override
    public void applyTo(double[] data, int start, int length) {
        for (int i = 0; i < length; ++i) {
            double[] arrd = data;
            int n = i + start;
            arrd[n] = arrd[n] + Rand.nextGaussian() * this.std;
        }
    }

    @Override
    public boolean hasDerivative() {
        return true;
    }

    @Override
    public double derivative(double x) {
        return 1.0;
    }

    @Override
    public double derivativeForOutput(double y) {
        return 1.0;
    }

    @Override
    public Op getDerivativeOp() {
        return Constant.ONE;
    }

    @Override
    public double averageValue() {
        return 0.0;
    }
}

