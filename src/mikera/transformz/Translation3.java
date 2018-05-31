/*
 * Decompiled with CFR 0_129.
 */
package mikera.transformz;

import mikera.matrixx.AMatrix;
import mikera.matrixx.Matrix33;
import mikera.transformz.AAffineTransform;
import mikera.transformz.ATransform;
import mikera.transformz.ATranslation;
import mikera.transformz.Translation;
import mikera.transformz.marker.ISpecialisedTransform;
import mikera.vectorz.AVector;
import mikera.vectorz.Vector3;

public final class Translation3
extends ATranslation
implements ISpecialisedTransform {
    public double dx;
    public double dy;
    public double dz;

    public Translation3(AVector v) {
        assert (v.length() == 3);
        this.dx = v.get(0);
        this.dy = v.get(1);
        this.dz = v.get(2);
    }

    public Translation3(ATranslation t) {
        this(t.getTranslationVector());
    }

    public Translation3(double dx, double dy, double dz) {
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
    }

    @Override
    public double calculateElement(int i, AVector v) {
        return v.get(i) + this.getTranslationComponent(i);
    }

    @Override
    public double getTranslationComponent(int i) {
        switch (i) {
            case 0: {
                return this.dx;
            }
            case 1: {
                return this.dy;
            }
            case 2: {
                return this.dz;
            }
        }
        throw new IndexOutOfBoundsException("Index = " + i);
    }

    public void transformNormal(Vector3 source, Vector3 dest) {
        dest.set(source);
    }

    @Override
    public Vector3 getTranslationVector() {
        return Vector3.of(this.dx, this.dy, this.dz);
    }

    @Override
    public AMatrix getMatrix() {
        return this.copyOfMatrix();
    }

    @Override
    public Vector3 copyOfTranslationVector() {
        return Vector3.of(this.dx, this.dy, this.dz);
    }

    @Override
    public Matrix33 copyOfMatrix() {
        return Matrix33.createIdentityMatrix();
    }

    @Override
    public ATranslation getTranslation() {
        return this;
    }

    @Override
    public int dimensions() {
        return 3;
    }

    @Override
    public int inputDimensions() {
        return 3;
    }

    @Override
    public int outputDimensions() {
        return 3;
    }

    @Override
    public boolean isIdentity() {
        return this.dx == 0.0 && this.dy == 0.0 && this.dz == 0.0;
    }

    @Override
    public void transform(AVector source, AVector dest) {
        if (source instanceof Vector3 && dest instanceof Vector3) {
            this.transform((Vector3)source, (Vector3)dest);
            return;
        }
        dest.set(0, source.get(0) + this.dx);
        dest.set(1, source.get(1) + this.dy);
        dest.set(2, source.get(2) + this.dz);
    }

    @Override
    public void transformInPlace(AVector v) {
        if (v instanceof Vector3) {
            this.transformInPlace((Vector3)v);
            return;
        }
        v.set(0, v.get(0) + this.dx);
        v.set(1, v.get(1) + this.dy);
        v.set(2, v.get(2) + this.dz);
    }

    public void transform(Vector3 source, Vector3 dest) {
        dest.x = source.x + this.dx;
        dest.y = source.y + this.dy;
        dest.z = source.z + this.dz;
    }

    public void transformInPlace(Vector3 v) {
        v.x += this.dx;
        v.y += this.dy;
        v.z += this.dz;
    }

    @Override
    public void composeWith(ATransform t) {
        if (t instanceof Translation3) {
            this.composeWith((Translation3)t);
            return;
        }
        if (t instanceof Translation) {
            this.composeWith((Translation)t);
            return;
        }
        super.composeWith(t);
    }

    public void composeWith(Translation t) {
        AVector v = t.getTranslationVector();
        this.dx += v.get(0);
        this.dy += v.get(1);
        this.dz += v.get(2);
    }

    public void composeWith(Translation3 t) {
        this.dx += t.dx;
        this.dy += t.dy;
        this.dz += t.dz;
    }

    @Override
    public Translation3 inverse() {
        return new Translation3(- this.dx, - this.dy, - this.dz);
    }
}

