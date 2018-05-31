/*
 * Decompiled with CFR 0_129.
 */
package mikera.transformz;

import mikera.matrixx.AMatrix;
import mikera.transformz.ATransform;
import mikera.transformz.ATranslation;
import mikera.transformz.AffineMN;
import mikera.transformz.MatrixTransform;
import mikera.transformz.Transformz;
import mikera.vectorz.AVector;

public abstract class AAffineTransform
extends ATransform {
    public abstract AMatrix getMatrix();

    public abstract ATranslation getTranslation();

    public AVector copyOfTranslationVector() {
        return this.getTranslation().getTranslationVector().clone();
    }

    public AMatrix copyOfMatrix() {
        return this.getMatrix().clone();
    }

    public MatrixTransform getMatrixTransform() {
        return new MatrixTransform(this.getMatrix());
    }

    public AVector getTranslationVector() {
        return this.getTranslation().getTranslationVector();
    }

    @Override
    public boolean isIdentity() {
        return this.getMatrix().isIdentity() && this.getTranslation().isIdentity();
    }

    @Override
    public ATransform compose(ATransform a) {
        if (a instanceof AAffineTransform) {
            return this.compose((AAffineTransform)a);
        }
        return super.compose(a);
    }

    public ATransform compose(AAffineTransform a) {
        AVector v = a.copyOfTranslationVector();
        AMatrix thisM = this.getMatrix();
        thisM.transformInPlace(v);
        v.add(this.getTranslation().getTranslationVector());
        AMatrix m = thisM.compose(a.getMatrix());
        return Transformz.createAffineTransform(m, v);
    }

    @Override
    public void transform(AVector source, AVector dest) {
        this.getMatrix().transform(source, dest);
        this.getTranslation().transformInPlace(dest);
    }

    public void transformNormal(AVector source, AVector dest) {
        this.getMatrix().transform(source, dest);
        dest.normalise();
    }

    @Override
    public double calculateElement(int i, AVector v) {
        return this.getMatrix().calculateElement(i, v) + this.getTranslation().getTranslationComponent(i);
    }

    @Override
    public void transformInPlace(AVector v) {
        this.getMatrix().transformInPlace(v);
        this.getTranslation().transformInPlace(v);
    }

    public AAffineTransform toAffineTransform() {
        return new AffineMN(this);
    }

    public int hashCode() {
        return this.getMatrix().hashCode() + this.getTranslation().hashCode();
    }

    public boolean equals(Object o) {
        if (!(o instanceof AAffineTransform)) {
            return false;
        }
        return this.equals((AAffineTransform)o);
    }

    public boolean equals(AAffineTransform a) {
        return a.getMatrix().equals(this.getMatrix()) && a.getTranslation().equals(this.getTranslation());
    }

    @Override
    public AAffineTransform inverse() {
        AMatrix m = this.getMatrix().inverse();
        AVector v = this.getTranslation().getTranslationVector().clone();
        v.negate();
        m.transformInPlace(v);
        return Transformz.createAffineTransform(m, v);
    }

    @Override
    public boolean isLinear() {
        return true;
    }

    @Override
    public boolean isInvertible() {
        return this.getMatrix().isInvertible();
    }
}

