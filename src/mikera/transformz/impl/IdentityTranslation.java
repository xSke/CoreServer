/*
 * Decompiled with CFR 0_129.
 */
package mikera.transformz.impl;

import mikera.matrixx.AMatrix;
import mikera.matrixx.Matrixx;
import mikera.randomz.Hash;
import mikera.transformz.ATranslation;
import mikera.vectorz.AVector;
import mikera.vectorz.Vector;
import mikera.vectorz.Vectorz;

public final class IdentityTranslation
extends ATranslation {
    private static final int INSTANCE_COUNT = 6;
    private final int dimensions;
    private static final IdentityTranslation[] INSTANCES = new IdentityTranslation[6];

    private IdentityTranslation(int dims) {
        this.dimensions = dims;
    }

    public static IdentityTranslation create(int i) {
        if (i < 6) {
            return INSTANCES[i];
        }
        return new IdentityTranslation(i);
    }

    @Override
    public AMatrix getMatrix() {
        return Matrixx.createImmutableIdentityMatrix(this.dimensions);
    }

    @Override
    public ATranslation getTranslation() {
        return this;
    }

    @Override
    public void transform(AVector source, AVector dest) {
        dest.set(source);
    }

    @Override
    public Vector transform(AVector source) {
        return source.toVector();
    }

    @Override
    public void transformInPlace(AVector v) {
    }

    @Override
    public double calculateElement(int i, AVector v) {
        return v.get(i);
    }

    @Override
    public boolean isIdentity() {
        return true;
    }

    @Override
    public int inputDimensions() {
        return this.dimensions;
    }

    @Override
    public int outputDimensions() {
        return this.dimensions;
    }

    @Override
    public AVector getTranslationVector() {
        return Vectorz.immutableZeroVector(this.dimensions);
    }

    @Override
    public int hashCode() {
        return Hash.zeroVectorHash(this.dimensions);
    }

    static {
        for (int i = 0; i < 6; ++i) {
            IdentityTranslation.INSTANCES[i] = new IdentityTranslation(i);
        }
    }
}

