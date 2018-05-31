/*
 * Decompiled with CFR 0_129.
 */
package mikera.transformz;

import mikera.matrixx.AMatrix;
import mikera.matrixx.Matrixx;
import mikera.transformz.AAffineTransform;
import mikera.transformz.AffineMN;
import mikera.transformz.Transformz;
import mikera.vectorz.AVector;

public abstract class ATranslation
extends AAffineTransform {
    @Override
    public abstract AVector getTranslationVector();

    public int dimensions() {
        return this.getTranslationVector().length();
    }

    @Override
    public double calculateElement(int i, AVector v) {
        return v.unsafeGet(i) + this.getTranslationComponent(i);
    }

    public double getTranslationComponent(int i) {
        return this.getTranslationVector().unsafeGet(i);
    }

    @Override
    public void transform(AVector source, AVector dest) {
        dest.set(source);
        dest.add(this.getTranslationVector());
    }

    @Override
    public void transformNormal(AVector source, AVector dest) {
        dest.set(source);
    }

    @Override
    public void transformInPlace(AVector v) {
        v.add(this.getTranslationVector());
    }

    @Override
    public AAffineTransform toAffineTransform() {
        return new AffineMN((AMatrix)Matrixx.createImmutableIdentityMatrix(this.dimensions()), this);
    }

    public ATranslation toMutableTranslation() {
        return Transformz.createMutableTranslation(this);
    }

    @Override
    public boolean isIdentity() {
        return this.getTranslationVector().isZero();
    }

    public boolean equals(ATranslation a) {
        return this.getTranslationVector().equals(a.getTranslationVector());
    }

    @Override
    public boolean equals(AAffineTransform a) {
        return this.equals(a.getTranslation()) && a.getMatrix().isIdentity();
    }

    @Override
    public ATranslation inverse() {
        AVector v = this.getTranslationVector().clone();
        v.negate();
        return Transformz.createTranslation(v);
    }

    @Override
    public boolean isSquare() {
        return true;
    }

    @Override
    public boolean isInvertible() {
        return true;
    }

    @Override
    public int hashCode() {
        return this.getTranslationVector().hashCode();
    }
}

