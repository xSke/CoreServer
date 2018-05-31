/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz.impl;

import mikera.arrayz.INDArray;
import mikera.vectorz.AScalar;
import mikera.vectorz.AVector;
import mikera.vectorz.impl.ASizedVector;
import mikera.vectorz.impl.ImmutableScalar;
import mikera.vectorz.util.ErrorMessages;

public abstract class AComputedVector
extends ASizedVector {
    protected AComputedVector(int length) {
        super(length);
    }

    @Override
    public abstract double get(int var1);

    @Override
    public final void set(int i, double value) {
        throw new UnsupportedOperationException(ErrorMessages.immutable(this));
    }

    @Override
    public final void unsafeSet(int i, double value) {
        throw new UnsupportedOperationException(ErrorMessages.immutable(this));
    }

    @Override
    public ImmutableScalar slice(int i) {
        return ImmutableScalar.create(this.get(i));
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public AComputedVector exactClone() {
        return this;
    }

    @Override
    public double dotProduct(double[] data, int offset) {
        double result = 0.0;
        for (int i = 0; i < this.length; ++i) {
            result += data[offset + i] * this.unsafeGet(i);
        }
        return result;
    }
}

