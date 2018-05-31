/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz;

import mikera.arrayz.INDArray;
import mikera.vectorz.AScalar;
import mikera.vectorz.AVector;
import mikera.vectorz.impl.ImmutableScalar;

public final class Scalar
extends AScalar {
    private static final long serialVersionUID = 8975126510371645366L;
    public double value;

    public Scalar(double value) {
        this.value = value;
    }

    public static Scalar create(double value) {
        return new Scalar(value);
    }

    public static Scalar create(AScalar a) {
        return Scalar.create(a.get());
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
    public boolean isZero() {
        return this.value == 0.0;
    }

    @Override
    public void getElements(double[] dest, int offset) {
        dest[offset] = this.value;
    }

    @Override
    public Scalar exactClone() {
        return this.clone();
    }

    public static Scalar createFromVector(AVector data) {
        return new Scalar(data.length() > 0 ? data.get(0) : 0.0);
    }

    @Override
    public Scalar mutable() {
        return this;
    }

    @Override
    public ImmutableScalar immutable() {
        return ImmutableScalar.create(this.value);
    }
}

