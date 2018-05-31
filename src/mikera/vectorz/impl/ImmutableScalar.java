/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz.impl;

import mikera.arrayz.INDArray;
import mikera.vectorz.AScalar;
import mikera.vectorz.Scalar;
import mikera.vectorz.util.ErrorMessages;

public final class ImmutableScalar
extends AScalar {
    private static final long serialVersionUID = 5798046998232751158L;
    public static final ImmutableScalar ONE = new ImmutableScalar(1.0);
    public static final ImmutableScalar ZERO = new ImmutableScalar(0.0);
    private final double value;

    private ImmutableScalar(double value) {
        this.value = value;
    }

    public static ImmutableScalar create(double value) {
        if (value == 0.0) {
            return ZERO;
        }
        if (value == 1.0) {
            return ONE;
        }
        return new ImmutableScalar(value);
    }

    public static ImmutableScalar create(AScalar a) {
        return ImmutableScalar.create(a.get());
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public boolean isFullyMutable() {
        return false;
    }

    @Override
    public boolean isView() {
        return false;
    }

    @Override
    public double get() {
        return this.value;
    }

    @Override
    public void getElements(double[] dest, int offset) {
        dest[offset] = this.value;
    }

    @Override
    public void set(double value) {
        throw new UnsupportedOperationException(ErrorMessages.immutable(this));
    }

    @Override
    public AScalar exactClone() {
        return new ImmutableScalar(this.value);
    }

    @Override
    public Scalar mutable() {
        return Scalar.create(this.value);
    }

    @Override
    public AScalar sparse() {
        return this;
    }

    @Override
    public ImmutableScalar immutable() {
        return this;
    }
}

