/*
 * Decompiled with CFR 0_129.
 */
package mikera.transformz.impl;

import mikera.matrixx.AMatrix;
import mikera.matrixx.Matrixx;
import mikera.transformz.AAffineTransform;
import mikera.transformz.ATransform;
import mikera.vectorz.AVector;

public abstract class AConstantTransform
extends AAffineTransform {
    private final int inputDimensions;

    @Override
    public AAffineTransform inverse() {
        throw new UnsupportedOperationException("Cannot get inverse of a constant transform!");
    }

    AConstantTransform(int inputDimensions) {
        this.inputDimensions = inputDimensions;
    }

    @Override
    public boolean isIdentity() {
        return false;
    }

    @Override
    public AConstantTransform compose(ATransform trans) {
        return this;
    }

    @Override
    public int inputDimensions() {
        return this.inputDimensions;
    }

    @Override
    public AMatrix getMatrix() {
        return Matrixx.createImmutableZeroMatrix(this.outputDimensions(), this.inputDimensions());
    }

    public abstract AVector getConstantValue();
}

