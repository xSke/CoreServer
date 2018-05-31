/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz.impl;

import mikera.arrayz.INDArray;
import mikera.vectorz.AScalar;
import mikera.vectorz.AVector;
import mikera.vectorz.Scalar;
import mikera.vectorz.impl.ImmutableScalar;
import mikera.vectorz.util.VectorzException;

public class VectorIndexScalar
extends AScalar {
    private static final long serialVersionUID = -5999714886554631904L;
    final AVector vector;
    final int index;

    private VectorIndexScalar(AVector vector, int index) {
        this.vector = vector;
        this.index = index;
    }

    public static VectorIndexScalar wrap(AVector vector, int index) {
        vector.checkIndex(index);
        return new VectorIndexScalar(vector, index);
    }

    @Override
    public double get() {
        return this.vector.unsafeGet(this.index);
    }

    @Override
    public void set(double value) {
        this.vector.unsafeSet(this.index, value);
    }

    @Override
    public boolean isMutable() {
        return this.vector.isMutable();
    }

    @Override
    public boolean isFullyMutable() {
        return this.vector.isFullyMutable();
    }

    @Override
    public boolean isView() {
        return true;
    }

    @Override
    public Scalar clone() {
        return new Scalar(this.get());
    }

    @Override
    public VectorIndexScalar exactClone() {
        return new VectorIndexScalar(this.vector.clone(), this.index);
    }

    @Override
    public AScalar mutable() {
        if (this.vector.isFullyMutable()) {
            return this;
        }
        return Scalar.create(this.get());
    }

    @Override
    public AScalar immutable() {
        if (this.vector.isMutable()) {
            return ImmutableScalar.create(this.get());
        }
        return this;
    }

    @Override
    public void validate() {
        if (this.index < 0 || this.index >= this.vector.length()) {
            throw new VectorzException("Index out of bounds");
        }
        super.validate();
    }
}

