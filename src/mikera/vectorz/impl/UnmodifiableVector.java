/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz.impl;

import mikera.arrayz.INDArray;
import mikera.vectorz.AVector;
import mikera.vectorz.impl.BaseDerivedVector;
import mikera.vectorz.impl.SparseImmutableVector;
import mikera.vectorz.util.ErrorMessages;

public class UnmodifiableVector
extends BaseDerivedVector {
    private static final long serialVersionUID = 2709404707262677811L;

    private UnmodifiableVector(AVector source) {
        super(source);
    }

    public UnmodifiableVector wrap(AVector v) {
        return new UnmodifiableVector(v);
    }

    @Override
    public void set(int i, double value) {
        throw new UnsupportedOperationException(ErrorMessages.immutable(this));
    }

    @Override
    public void unsafeSet(int i, double value) {
        throw new UnsupportedOperationException(ErrorMessages.immutable(this));
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
    public UnmodifiableVector exactClone() {
        return new UnmodifiableVector(this.source.exactClone());
    }

    @Override
    public AVector subVector(int offset, int length) {
        AVector ssv = this.source.subVector(offset, length);
        if (ssv == this.source) {
            return this;
        }
        return new UnmodifiableVector(ssv);
    }

    @Override
    public AVector sparse() {
        return SparseImmutableVector.create(this.source);
    }
}

