/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz.ops;

import mikera.vectorz.Op;

public final class ScaledLogistic
extends Op {
    private final double factor;
    private final double inverseFactor;
    private static final double DEFAULT_SCALE_FACTOR = 8.0;
    public static final ScaledLogistic INSTANCE = new ScaledLogistic(8.0);

    public ScaledLogistic(double d) {
        this.factor = d;
        this.inverseFactor = 1.0 / d;
    }

    private double scaledLogisticFunction(double a) {
        double ea = Math.exp((- this.factor) * a);
        double df = 1.0 / (1.0 + ea);
        if (Double.isNaN(df)) {
            return a > 0.0 ? 1.0 : 0.0;
        }
        return df;
    }

    private double inverseLogistic(double a) {
        if (a >= 1.0) {
            return 800.0 * this.inverseFactor;
        }
        if (a <= 0.0) {
            return -800.0 * this.inverseFactor;
        }
        double ea = a / (1.0 - a);
        return this.inverseFactor * Math.log(ea);
    }

    @Override
    public double apply(double x) {
        return this.scaledLogisticFunction(x);
    }

    @Override
    public double applyInverse(double y) {
        return this.inverseLogistic(y);
    }

    @Override
    public void applyTo(double[] data, int start, int length) {
        for (int i = 0; i < length; ++i) {
            data[i + start] = this.scaledLogisticFunction(data[i + start]);
        }
    }

    @Override
    public boolean hasDerivative() {
        return true;
    }

    @Override
    public double derivativeForOutput(double y) {
        return this.factor * y * (1.0 - y);
    }

    @Override
    public double derivative(double x) {
        double y = this.scaledLogisticFunction(x);
        return this.factor * y * (1.0 - y);
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
    public double averageValue() {
        return 0.5;
    }
}

