/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz.impl;

import mikera.arrayz.INDArray;
import mikera.vectorz.AScalar;
import mikera.vectorz.AVector;

@Deprecated
public final class DoubleScalar
extends AScalar {
    private static final long serialVersionUID = -8968335296175000888L;
    public double value;

    public DoubleScalar(double value) {
        this.value = value;
    }

    public static DoubleScalar create(double value) {
        return new DoubleScalar(value);
    }

    public static DoubleScalar create(AScalar a) {
        return DoubleScalar.create(a.get());
    }

    @Override
    public double get() {
        return this.value;
    }

    @Override
    public void set(double value) {
        this.value = value;
    }

    @Override
    public void abs() {
        this.value = Math.abs(this.value);
    }

    @Override
    public void add(double d) {
        this.value += d;
    }

    @Override
    public void sub(double d) {
        this.value -= d;
    }

    @Override
    public void add(AScalar s) {
        this.value += s.get();
    }

    @Override
    public void multiply(double factor) {
        this.value *= factor;
    }

    @Override
    public void negate() {
        this.value = - this.value;
    }

    @Override
    public void scaleAdd(double factor, double constant) {
        this.value = this.value * factor + constant;
    }

    @Override
    public boolean isView() {
        return false;
    }

    @Override
    public void getElements(double[] dest, int offset) {
        dest[offset] = this.value;
    }

    @Override
    public DoubleScalar exactClone() {
        return new DoubleScalar(this.value);
    }

    public static DoubleScalar createFromVector(AVector data) {
        return new DoubleScalar(data.length() > 0 ? data.get(0) : 0.0);
    }
}

