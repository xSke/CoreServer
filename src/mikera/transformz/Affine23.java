/*
 * Decompiled with CFR 0_129.
 */
package mikera.transformz;

import mikera.matrixx.AMatrix;
import mikera.matrixx.Matrix22;
import mikera.transformz.AAffineTransform;
import mikera.transformz.ATransform;
import mikera.transformz.ATranslation;
import mikera.transformz.Transformz;
import mikera.transformz.marker.ISpecialisedTransform;
import mikera.vectorz.AVector;
import mikera.vectorz.Vector2;

public final class Affine23
extends AAffineTransform
implements ISpecialisedTransform {
    public double m00;
    public double m01;
    public double tr0;
    public double m10;
    public double m11;
    public double tr1;

    public Affine23() {
    }

    public Affine23(double m00, double m01, double tr0, double m10, double m11, double tr1) {
        this.m00 = m00;
        this.m01 = m01;
        this.m10 = m10;
        this.m11 = m11;
        this.tr0 = tr0;
        this.tr1 = tr1;
    }

    public Affine23(AMatrix matrix, ATranslation trans) {
        this(matrix, trans.getTranslationVector());
    }

    public Affine23(AMatrix m, AVector v) {
        if (v.length() != 2 || m.columnCount() != 2 || m.rowCount() != 2) {
            throw new IllegalArgumentException("Wrong source sizes for Affine23");
        }
        this.m00 = m.unsafeGet(0, 0);
        this.m01 = m.unsafeGet(0, 1);
        this.m10 = m.unsafeGet(1, 0);
        this.m11 = m.unsafeGet(1, 1);
        this.tr0 = v.unsafeGet(0);
        this.tr1 = v.unsafeGet(1);
    }

    public Affine23(Matrix22 m, AVector v) {
        assert (v.length() == 2);
        assert (m.columnCount() == 2);
        assert (m.rowCount() == 2);
        this.m00 = m.m00;
        this.m01 = m.m01;
        this.m10 = m.m10;
        this.m11 = m.m11;
        this.tr0 = v.unsafeGet(0);
        this.tr1 = v.unsafeGet(1);
    }

    @Override
    public AMatrix getMatrix() {
        return this.copyOfMatrix();
    }

    @Override
    public ATranslation getTranslation() {
        return Transformz.createTranslation(this.copyOfTranslationVector());
    }

    @Override
    public Matrix22 copyOfMatrix() {
        return new Matrix22(this.m00, this.m01, this.m10, this.m11);
    }

    @Override
    public Vector2 copyOfTranslationVector() {
        return Vector2.of(this.tr0, this.tr1);
    }

    @Override
    public void transform(AVector source, AVector dest) {
        if (source instanceof Vector2 && dest instanceof Vector2) {
            this.transform((Vector2)source, (Vector2)dest);
            return;
        }
        double x = source.unsafeGet(0);
        double y = source.unsafeGet(1);
        dest.set(0, this.m00 * x + this.m01 * y + this.tr0);
        dest.set(1, this.m10 * x + this.m11 * y + this.tr1);
    }

    @Override
    public void transformNormal(AVector source, AVector dest) {
        if (source instanceof Vector2 && dest instanceof Vector2) {
            this.transformNormal((Vector2)source, (Vector2)dest);
            return;
        }
        this.transform(source, dest);
        dest.normalise();
    }

    public void transformNormal(Vector2 source, Vector2 dest) {
        double x = source.x;
        double y = source.y;
        dest.set(0, this.m00 * x + this.m01 * y);
        dest.set(1, this.m10 * x + this.m11 * y);
        dest.normalise();
    }

    @Override
    public void transformInPlace(AVector dest) {
        if (dest instanceof Vector2) {
            this.transformInPlace((Vector2)dest);
            return;
        }
        double x = dest.unsafeGet(0);
        double y = dest.get(1);
        dest.set(0, this.m00 * x + this.m01 * y + this.tr0);
        dest.set(1, this.m10 * x + this.m11 * y + this.tr1);
    }

    public void transform(Vector2 source, Vector2 dest) {
        Vector2 s = source;
        dest.x = this.m00 * s.x + this.m01 * s.y + this.tr0;
        dest.y = this.m10 * s.x + this.m11 * s.y + this.tr1;
    }

    public void transformInPlace(Vector2 dest) {
        Vector2 s = dest;
        double tx = this.m00 * s.x + this.m01 * s.y + this.tr0;
        double ty = this.m10 * s.x + this.m11 * s.y + this.tr1;
        s.x = tx;
        s.y = ty;
    }

    @Override
    public int inputDimensions() {
        return 2;
    }

    @Override
    public int outputDimensions() {
        return 2;
    }

    @Override
    public void composeWith(ATransform a) {
        if (a instanceof Affine23) {
            this.composeWith((Affine23)a);
            return;
        }
        super.composeWith(a);
    }

    public void composeWith(Affine23 a) {
        double t00 = this.m00 * a.m00 + this.m01 * a.m10;
        double t01 = this.m00 * a.m01 + this.m01 * a.m11;
        double t10 = this.m10 * a.m00 + this.m11 * a.m10;
        double t11 = this.m10 * a.m01 + this.m11 * a.m11;
        double t02 = this.m00 * a.tr0 + this.m01 * a.tr1 + this.tr0;
        double t12 = this.m10 * a.tr0 + this.m11 * a.tr1 + this.tr1;
        this.m00 = t00;
        this.m01 = t01;
        this.tr0 = t02;
        this.m10 = t10;
        this.m11 = t11;
        this.tr1 = t12;
    }

    public void composeWith(Matrix22 a) {
        double t00 = this.m00 * a.m00 + this.m01 * a.m10;
        double t01 = this.m00 * a.m01 + this.m01 * a.m11;
        double t10 = this.m10 * a.m00 + this.m11 * a.m10;
        double t11 = this.m10 * a.m01 + this.m11 * a.m11;
        this.m00 = t00;
        this.m01 = t01;
        this.m10 = t10;
        this.m11 = t11;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Affine23) {
            return this.equals((Affine23)o);
        }
        return super.equals(o);
    }

    public boolean equals(Affine23 m) {
        return this.m00 == m.m00 && this.m01 == m.m01 && this.tr0 == m.tr0 && this.m10 == m.m10 && this.m11 == m.m11 && this.tr1 == m.tr1;
    }
}

