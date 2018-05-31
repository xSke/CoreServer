/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz.ops;

import mikera.arrayz.INDArray;
import mikera.matrixx.AMatrix;
import mikera.vectorz.AVector;
import mikera.vectorz.Op;
import mikera.vectorz.Ops;
import mikera.vectorz.ops.Constant;
import mikera.vectorz.ops.Identity;
import mikera.vectorz.util.DoubleArrays;

public final class Power
extends Op {
    private final double exponent;
    private final Op inverse;

    private Power(double d) {
        this.exponent = d;
        this.inverse = new Power(1.0 / d, this);
    }

    private Power(double d, Op inv) {
        this.exponent = d;
        this.inverse = inv;
    }

    public static Op create(double exponent) {
        if (exponent == -1.0) {
            return Ops.RECIPROCAL;
        }
        if (exponent == 0.0) {
            return Constant.ONE;
        }
        if (exponent == 1.0) {
            return Identity.INSTANCE;
        }
        if (exponent == 2.0) {
            return Ops.SQUARE;
        }
        return new Power(exponent);
    }

    @Override
    public double minDomain() {
        if (this.exponent != (double)((long)this.exponent)) {
            return 0.0;
        }
        return super.minDomain();
    }

    @Override
    public double apply(double x) {
        return Math.pow(x, this.exponent);
    }

    @Override
    public void applyTo(INDArray a) {
        a.pow(this.exponent);
    }

    @Override
    public void applyTo(AMatrix a) {
        a.pow(this.exponent);
    }

    @Override
    public void applyTo(AVector a) {
        a.pow(this.exponent);
    }

    @Override
    public void applyTo(double[] data, int start, int length) {
        DoubleArrays.pow(data, start, length, this.exponent);
    }

    @Override
    public double applyInverse(double x) {
        return Math.pow(x, 1.0 / this.exponent);
    }

    @Override
    public boolean hasDerivative() {
        return true;
    }

    @Override
    public double derivative(double x) {
        return this.exponent * Math.pow(x, this.exponent - 1.0);
    }

    @Override
    public double derivativeForOutput(double y) {
        return y * Math.pow(y, 1.0 / this.exponent) / this.exponent;
    }

    @Override
    public Op getDerivativeOp() {
        return Ops.product(Constant.create(this.exponent), Power.create(this.exponent - 1.0));
    }

    @Override
    public boolean hasInverse() {
        return true;
    }

    @Override
    public Op getInverse() {
        return this.inverse;
    }

    @Override
    public double averageValue() {
        return 1.0;
    }

    public double getExponent() {
        return this.exponent;
    }
}

