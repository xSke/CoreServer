/*
 * Decompiled with CFR 0_129.
 */
package mikera.transformz;

import mikera.matrixx.AMatrix;
import mikera.matrixx.Matrixx;
import mikera.transformz.ATransform;
import mikera.transformz.ATranslation;
import mikera.vectorz.AVector;
import mikera.vectorz.Vectorz;

public final class Translation
extends ATranslation {
    private final AVector translationVector;
    private final int dimensions;

    public Translation(AVector source) {
        this.translationVector = source;
        this.dimensions = source.length();
    }

    public Translation(ATranslation t) {
        this(Vectorz.create(t.getTranslation().getTranslationVector()));
    }

    @Override
    public double calculateElement(int i, AVector v) {
        return v.get(i) + this.translationVector.get(i);
    }

    public Translation(double[] v) {
        this.dimensions = v.length;
        this.translationVector = Vectorz.create(v);
    }

    @Override
    public AVector getTranslationVector() {
        return this.translationVector;
    }

    @Override
    public void transform(AVector source, AVector dest) {
        dest.set(source);
        dest.add(this.translationVector);
    }

    @Override
    public void transformInPlace(AVector v) {
        v.add(this.translationVector);
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
    public int inputDimensions() {
        return this.dimensions;
    }

    @Override
    public int outputDimensions() {
        return this.dimensions;
    }

    @Override
    public int dimensions() {
        return this.dimensions;
    }

    @Override
    public void composeWith(ATransform t) {
        if (t instanceof ATranslation) {
            this.composeWith((ATranslation)t);
            return;
        }
        super.composeWith(t);
    }

    public void composeWith(ATranslation t) {
        if (t instanceof Translation) {
            this.composeWith((Translation)t);
            return;
        }
        AVector v = t.getTranslationVector();
        this.translationVector.add(v);
    }

    public void composeWith(Translation t) {
        assert (t.dimensions() == this.dimensions());
        this.translationVector.add(t.translationVector);
    }
}

