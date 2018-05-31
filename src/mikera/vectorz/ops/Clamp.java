/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz.ops;

import mikera.arrayz.INDArray;
import mikera.matrixx.AMatrix;
import mikera.vectorz.AVector;
import mikera.vectorz.ops.ABoundedOp;

public final class Clamp
extends ABoundedOp {
    private final double min;
    private final double max;
    public static final Clamp ZERO_TO_ONE = new Clamp(0.0, 1.0);

    public Clamp(double min, double max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public double apply(double x) {
        if (x <= this.min) {
            return this.min;
        }
        if (x >= this.max) {
            return this.max;
        }
        return x;
    }

    @Override
    public void applyTo(INDArray v) {
        v.clamp(this.min, this.max);
    }

    @Override
    public void applyTo(AVector v) {
        v.clamp(this.min, this.max);
    }

    @Override
    public void applyTo(AMatrix v) {
        v.clamp(this.min, this.max);
    }

    @Override
    public void applyTo(double[] data, int start, int length) {
        for (int i = 0; i < length; ++i) {
            double x = data[start + i];
            data[start + i] = x < this.min ? this.min : (x > this.max ? this.max : x);
        }
    }

    @Override
    public double minValue() {
        return this.min;
    }

    @Override
    public double maxValue() {
        return this.max;
    }

    @Override
    public boolean hasDerivative() {
        return true;
    }

    @Override
    public double derivative(double x) {
        if (x <= this.min || x >= this.max) {
            return 0.0;
        }
        return 1.0;
    }

    @Override
    public double derivativeForOutput(double y) {
        if (y <= this.min || y >= this.max) {
            return 0.0;
        }
        return 1.0;
    }
}

