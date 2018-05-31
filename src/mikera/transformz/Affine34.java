/*
 * Decompiled with CFR 0_129.
 */
package mikera.transformz;

import mikera.matrixx.AMatrix;
import mikera.matrixx.Matrix33;
import mikera.transformz.AAffineTransform;
import mikera.transformz.ATransform;
import mikera.transformz.ATranslation;
import mikera.transformz.Transformz;
import mikera.transformz.Translation3;
import mikera.transformz.marker.ISpecialisedTransform;
import mikera.vectorz.AVector;
import mikera.vectorz.Vector3;

public final class Affine34
extends AAffineTransform
implements ISpecialisedTransform {
    public double m00;
    public double m01;
    public double m02;
    public double tr0;
    public double m10;
    public double m11;
    public double m12;
    public double tr1;
    public double m20;
    public double m21;
    public double m22;
    public double tr2;

    public Affine34() {
    }

    public Affine34(double m00, double m01, double m02, double tr0, double m10, double m11, double m12, double tr1, double m20, double m21, double m22, double tr2) {
        this.m00 = m00;
        this.m01 = m01;
        this.m02 = m02;
        this.m10 = m10;
        this.m11 = m11;
        this.m12 = m12;
        this.m20 = m20;
        this.m21 = m21;
        this.m22 = m22;
        this.tr0 = tr0;
        this.tr1 = tr1;
        this.tr2 = tr2;
    }

    public Affine34(AMatrix matrix, ATranslation trans) {
        this(matrix, trans.getTranslationVector());
    }

    public Affine34(AMatrix m, AVector v) {
        if (v.length() != 3 || m.columnCount() != 3 || m.rowCount() != 3) {
            throw new IllegalArgumentException("Wrong source sizes for Affine34");
        }
        this.m00 = m.unsafeGet(0, 0);
        this.m01 = m.unsafeGet(0, 1);
        this.m02 = m.unsafeGet(0, 2);
        this.m10 = m.unsafeGet(1, 0);
        this.m11 = m.unsafeGet(1, 1);
        this.m12 = m.unsafeGet(1, 2);
        this.m20 = m.unsafeGet(2, 0);
        this.m21 = m.unsafeGet(2, 1);
        this.m22 = m.unsafeGet(2, 2);
        this.tr0 = v.unsafeGet(0);
        this.tr1 = v.unsafeGet(1);
        this.tr2 = v.unsafeGet(2);
    }

    public Affine34(Matrix33 m, AVector v) {
        assert (v.length() == 3);
        assert (m.columnCount() == 3);
        assert (m.rowCount() == 3);
        this.m00 = m.m00;
        this.m01 = m.m01;
        this.m02 = m.m02;
        this.m10 = m.m10;
        this.m11 = m.m11;
        this.m12 = m.m12;
        this.m20 = m.m20;
        this.m21 = m.m21;
        this.m22 = m.m22;
        this.tr0 = v.get(0);
        this.tr1 = v.get(1);
        this.tr2 = v.get(2);
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
    public Matrix33 copyOfMatrix() {
        return new Matrix33(this.m00, this.m01, this.m02, this.m10, this.m11, this.m12, this.m20, this.m21, this.m22);
    }

    @Override
    public Vector3 copyOfTranslationVector() {
        return Vector3.of(this.tr0, this.tr1, this.tr2);
    }

    @Override
    public void transform(AVector source, AVector dest) {
        if (source instanceof Vector3 && dest instanceof Vector3) {
            this.transform((Vector3)source, (Vector3)dest);
            return;
        }
        double x = source.unsafeGet(0);
        double y = source.unsafeGet(1);
        double z = source.get(2);
        dest.set(0, this.m00 * x + this.m01 * y + this.m02 * z + this.tr0);
        dest.set(1, this.m10 * x + this.m11 * y + this.m12 * z + this.tr1);
        dest.set(2, this.m20 * x + this.m21 * y + this.m22 * z + this.tr2);
    }

    @Override
    public void transformNormal(AVector source, AVector dest) {
        if (source instanceof Vector3 && dest instanceof Vector3) {
            this.transformNormal((Vector3)source, (Vector3)dest);
            return;
        }
        this.transform(source, dest);
        dest.normalise();
    }

    public void transformNormal(Vector3 source, Vector3 dest) {
        double x = source.x;
        double y = source.y;
        double z = source.z;
        dest.set(0, this.m00 * x + this.m01 * y + this.m02 * z);
        dest.set(1, this.m10 * x + this.m11 * y + this.m12 * z);
        dest.set(2, this.m20 * x + this.m21 * y + this.m22 * z);
        dest.normalise();
    }

    @Override
    public void transformInPlace(AVector dest) {
        if (dest instanceof Vector3) {
            this.transformInPlace((Vector3)dest);
            return;
        }
        double x = dest.unsafeGet(0);
        double y = dest.unsafeGet(1);
        double z = dest.get(2);
        dest.set(0, this.m00 * x + this.m01 * y + this.m02 * z + this.tr0);
        dest.set(1, this.m10 * x + this.m11 * y + this.m12 * z + this.tr1);
        dest.set(2, this.m20 * x + this.m21 * y + this.m22 * z + this.tr2);
    }

    public void transform(Vector3 source, Vector3 dest) {
        Vector3 s = source;
        dest.x = this.m00 * s.x + this.m01 * s.y + this.m02 * s.z + this.tr0;
        dest.y = this.m10 * s.x + this.m11 * s.y + this.m12 * s.z + this.tr1;
        dest.z = this.m20 * s.x + this.m21 * s.y + this.m22 * s.z + this.tr2;
    }

    public void transformInPlace(Vector3 dest) {
        Vector3 s = dest;
        double tx = this.m00 * s.x + this.m01 * s.y + this.m02 * s.z + this.tr0;
        double ty = this.m10 * s.x + this.m11 * s.y + this.m12 * s.z + this.tr1;
        double tz = this.m20 * s.x + this.m21 * s.y + this.m22 * s.z + this.tr2;
        s.x = tx;
        s.y = ty;
        s.z = tz;
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
    public void composeWith(ATransform a) {
        if (a instanceof Affine34) {
            this.composeWith((Affine34)a);
            return;
        }
        if (a instanceof Translation3) {
            this.composeWith((Translation3)a);
            return;
        }
        super.composeWith(a);
    }

    public void composeWith(Affine34 a) {
        double t00 = this.m00 * a.m00 + this.m01 * a.m10 + this.m02 * a.m20;
        double t01 = this.m00 * a.m01 + this.m01 * a.m11 + this.m02 * a.m21;
        double t02 = this.m00 * a.m02 + this.m01 * a.m12 + this.m02 * a.m22;
        double t10 = this.m10 * a.m00 + this.m11 * a.m10 + this.m12 * a.m20;
        double t11 = this.m10 * a.m01 + this.m11 * a.m11 + this.m12 * a.m21;
        double t12 = this.m10 * a.m02 + this.m11 * a.m12 + this.m12 * a.m22;
        double t20 = this.m20 * a.m00 + this.m21 * a.m10 + this.m22 * a.m20;
        double t21 = this.m20 * a.m01 + this.m21 * a.m11 + this.m22 * a.m21;
        double t22 = this.m20 * a.m02 + this.m21 * a.m12 + this.m22 * a.m22;
        double t03 = this.m00 * a.tr0 + this.m01 * a.tr1 + this.m02 * a.tr2 + this.tr0;
        double t13 = this.m10 * a.tr0 + this.m11 * a.tr1 + this.m12 * a.tr2 + this.tr1;
        double t23 = this.m20 * a.tr0 + this.m21 * a.tr1 + this.m22 * a.tr2 + this.tr2;
        this.m00 = t00;
        this.m01 = t01;
        this.m02 = t02;
        this.tr0 = t03;
        this.m10 = t10;
        this.m11 = t11;
        this.m12 = t12;
        this.tr1 = t13;
        this.m20 = t20;
        this.m21 = t21;
        this.m22 = t22;
        this.tr2 = t23;
    }

    public void composeWith(Matrix33 a) {
        double t00 = this.m00 * a.m00 + this.m01 * a.m10 + this.m02 * a.m20;
        double t01 = this.m00 * a.m01 + this.m01 * a.m11 + this.m02 * a.m21;
        double t02 = this.m00 * a.m02 + this.m01 * a.m12 + this.m02 * a.m22;
        double t10 = this.m10 * a.m00 + this.m11 * a.m10 + this.m12 * a.m20;
        double t11 = this.m10 * a.m01 + this.m11 * a.m11 + this.m12 * a.m21;
        double t12 = this.m10 * a.m02 + this.m11 * a.m12 + this.m12 * a.m22;
        double t20 = this.m20 * a.m00 + this.m21 * a.m10 + this.m22 * a.m20;
        double t21 = this.m20 * a.m01 + this.m21 * a.m11 + this.m22 * a.m21;
        double t22 = this.m20 * a.m02 + this.m21 * a.m12 + this.m22 * a.m22;
        this.m00 = t00;
        this.m01 = t01;
        this.m02 = t02;
        this.m10 = t10;
        this.m11 = t11;
        this.m12 = t12;
        this.m20 = t20;
        this.m21 = t21;
        this.m22 = t22;
    }

    public void composeWith(Translation3 a) {
        this.tr0 += a.dx;
        this.tr1 += a.dy;
        this.tr2 += a.dz;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Affine34) {
            return this.equals((Affine34)o);
        }
        return super.equals(o);
    }

    public boolean equals(Affine34 m) {
        return this.m00 == m.m00 && this.m01 == m.m01 && this.m02 == m.m02 && this.m10 == m.m10 && this.m11 == m.m11 && this.m12 == m.m12 && this.m20 == m.m20 && this.m21 == m.m21 && this.m22 == m.m22 && this.tr0 == m.tr0 && this.tr1 == m.tr1 && this.tr2 == m.tr2;
    }
}

