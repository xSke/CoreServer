/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz.impl;

import mikera.arrayz.INDArray;
import mikera.vectorz.AScalar;
import mikera.vectorz.AVector;
import mikera.vectorz.impl.AWrappedVector;
import mikera.vectorz.util.ErrorMessages;

public class WrappedScalarVector
extends AWrappedVector<AScalar> {
    private static final long serialVersionUID = 1912695454407729415L;
    public final AScalar scalar;

    public WrappedScalarVector(AScalar s) {
        this.scalar = s;
    }

    @Override
    public boolean isMutable() {
        return this.scalar.isMutable();
    }

    @Override
    public boolean isFullyMutable() {
        return this.scalar.isFullyMutable();
    }

    @Override
    public int componentCount() {
        return 1;
    }

    @Override
    public AScalar getComponent(int k) {
        if (k != 0) {
            throw new IndexOutOfBoundsException(ErrorMessages.invalidComponent(this, k));
        }
        return this.scalar;
    }

    @Override
    public int length() {
        return 1;
    }

    @Override
    public double get(int i) {
        if (i != 0) {
            throw new IndexOutOfBoundsException("Index: " + i);
        }
        return this.scalar.get();
    }

    @Override
    public boolean isBoolean() {
        return this.scalar.isBoolean();
    }

    @Override
    public boolean isZero() {
        return this.scalar.isZero();
    }

    @Override
    public double unsafeGet(int i) {
        return this.scalar.get();
    }

    @Override
    public void set(int i, double value) {
        if (i != 0) {
            throw new IndexOutOfBoundsException();
        }
        this.scalar.set(value);
    }

    @Override
    public double elementSum() {
        return this.scalar.get();
    }

    @Override
    public double elementMax() {
        return this.scalar.get();
    }

    @Override
    public double elementMin() {
        return this.scalar.get();
    }

    @Override
    public double dotProduct(double[] data, int offset) {
        return data[offset] * this.scalar.get();
    }

    @Override
    public WrappedScalarVector exactClone() {
        return new WrappedScalarVector(this.scalar.exactClone());
    }

    @Override
    public AScalar getWrappedObject() {
        return this.scalar;
    }

    @Override
    public void addAt(int i, double v) {
        this.scalar.add(v);
    }
}

