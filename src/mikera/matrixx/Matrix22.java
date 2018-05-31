/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx;

import mikera.arrayz.INDArray;
import mikera.matrixx.AMatrix;
import mikera.matrixx.impl.APrimitiveMatrix;
import mikera.transformz.marker.ISpecialisedTransform;
import mikera.vectorz.AVector;
import mikera.vectorz.Vector;
import mikera.vectorz.Vector2;
import mikera.vectorz.util.ErrorMessages;

public final class Matrix22
extends APrimitiveMatrix
implements ISpecialisedTransform {
    private static final long serialVersionUID = 2696617102233017028L;
    public double m00;
    public double m01;
    public double m10;
    public double m11;

    public Matrix22() {
    }

    public Matrix22(Matrix22 source) {
        Matrix22 s = source;
        this.m00 = s.m00;
        this.m01 = s.m01;
        this.m10 = s.m10;
        this.m11 = s.m11;
    }

    public Matrix22(double m00, double m01, double m10, double m11) {
        this.m00 = m00;
        this.m01 = m01;
        this.m10 = m10;
        this.m11 = m11;
    }

    public Matrix22(AMatrix m) {
        if (m instanceof Matrix22) {
            this.set((Matrix22)m);
        } else {
            this.unsafeSet(m);
        }
    }

    public void set(Matrix22 a) {
        this.m00 = a.m00;
        this.m01 = a.m01;
        this.m10 = a.m10;
        this.m11 = a.m11;
    }

    @Override
    public void set(AMatrix m) {
        m.checkShape(2, 2);
        this.m00 = m.unsafeGet(0, 0);
        this.m01 = m.unsafeGet(0, 1);
        this.m10 = m.unsafeGet(1, 0);
        this.m11 = m.unsafeGet(1, 1);
    }

    public void unsafeSet(AMatrix m) {
        this.m00 = m.unsafeGet(0, 0);
        this.m01 = m.unsafeGet(0, 1);
        this.m10 = m.unsafeGet(1, 0);
        this.m11 = m.unsafeGet(1, 1);
    }

    public static Matrix22 create(double a, double b, double c, double d) {
        return new Matrix22(a, b, c, d);
    }

    public static Matrix22 createRotationMatrix(double angle) {
        double sa = Math.sin(angle);
        double ca = Math.cos(angle);
        return new Matrix22(ca, - sa, sa, ca);
    }

    public static Matrix22 createScaleMatrix(double d) {
        return new Matrix22(d, 0.0, 0.0, d);
    }

    public static Matrix22 createIdentity() {
        return new Matrix22(1.0, 0.0, 0.0, 1.0);
    }

    public static Matrix22 createReflectionMatrix(AVector normal) {
        return Matrix22.createReflectionMatrix(Vector2.create(normal));
    }

    public static Matrix22 createReflectionMatrix(Vector2 normal) {
        double x = normal.x;
        double y = normal.y;
        double ca = x * x - y * y;
        double sa = 2.0 * x * y;
        return new Matrix22(ca, sa, sa, - ca);
    }

    @Override
    public void multiply(double factor) {
        this.m00 *= factor;
        this.m01 *= factor;
        this.m10 *= factor;
        this.m11 *= factor;
    }

    @Override
    public double determinant() {
        return this.m00 * this.m11 - this.m01 * this.m10;
    }

    @Override
    public long elementCount() {
        return 4L;
    }

    @Override
    public double elementSum() {
        return this.m00 + this.m01 + this.m10 + this.m11;
    }

    @Override
    public double elementMin() {
        return Math.min(Math.min(this.m00, this.m01), Math.min(this.m10, this.m11));
    }

    @Override
    public double elementMax() {
        return Math.max(Math.max(this.m00, this.m01), Math.max(this.m10, this.m11));
    }

    @Override
    public double trace() {
        return this.m00 + this.m11;
    }

    @Override
    public Matrix22 inverse() {
        double det = this.determinant();
        if (det == 0.0) {
            return null;
        }
        double invDet = 1.0 / det;
        return new Matrix22(invDet * this.m11, (- invDet) * this.m01, (- invDet) * this.m10, invDet * this.m00);
    }

    @Override
    public int rowCount() {
        return 2;
    }

    @Override
    public int columnCount() {
        return 2;
    }

    @Override
    public int checkSquare() {
        return 2;
    }

    @Override
    public void add(AMatrix a) {
        if (a instanceof Matrix22) {
            this.add((Matrix22)a);
            return;
        }
        a.checkShape(2, 2);
        this.m00 += a.unsafeGet(0, 0);
        this.m01 += a.unsafeGet(0, 1);
        this.m10 += a.unsafeGet(1, 0);
        this.m11 += a.unsafeGet(1, 1);
    }

    public void add(Matrix22 a) {
        this.m00 += a.m00;
        this.m01 += a.m01;
        this.m10 += a.m10;
        this.m11 += a.m11;
    }

    public void sub(Matrix22 a) {
        this.m00 -= a.m00;
        this.m01 -= a.m01;
        this.m10 -= a.m10;
        this.m11 -= a.m11;
    }

    @Override
    public Vector2 getRowClone(int row) {
        switch (row) {
            case 0: {
                return Vector2.of(this.m00, this.m01);
            }
            case 1: {
                return Vector2.of(this.m10, this.m11);
            }
        }
        throw new IndexOutOfBoundsException("Row index = " + row);
    }

    @Override
    public Vector2 getColumnClone(int column) {
        switch (column) {
            case 0: {
                return Vector2.of(this.m00, this.m10);
            }
            case 1: {
                return Vector2.of(this.m01, this.m11);
            }
        }
        throw new IndexOutOfBoundsException("Column index = " + column);
    }

    @Override
    public void copyRowTo(int row, double[] dest, int destOffset) {
        if (row == 0) {
            dest[destOffset++] = this.m00;
            dest[destOffset++] = this.m01;
        } else {
            dest[destOffset++] = this.m10;
            dest[destOffset++] = this.m11;
        }
    }

    @Override
    public double get(int row, int column) {
        switch (row) {
            case 0: {
                switch (column) {
                    case 0: {
                        return this.m00;
                    }
                    case 1: {
                        return this.m01;
                    }
                }
                throw new IndexOutOfBoundsException(ErrorMessages.invalidIndex((INDArray)this, row, column));
            }
            case 1: {
                switch (column) {
                    case 0: {
                        return this.m10;
                    }
                    case 1: {
                        return this.m11;
                    }
                }
                throw new IndexOutOfBoundsException(ErrorMessages.invalidIndex((INDArray)this, row, column));
            }
        }
        throw new IndexOutOfBoundsException(ErrorMessages.invalidIndex((INDArray)this, row, column));
    }

    @Override
    public void set(int row, int column, double value) {
        switch (row) {
            case 0: {
                switch (column) {
                    case 0: {
                        this.m00 = value;
                        return;
                    }
                    case 1: {
                        this.m01 = value;
                        return;
                    }
                }
                throw new IndexOutOfBoundsException(ErrorMessages.invalidIndex((INDArray)this, row, column));
            }
            case 1: {
                switch (column) {
                    case 0: {
                        this.m10 = value;
                        return;
                    }
                    case 1: {
                        this.m11 = value;
                        return;
                    }
                }
                throw new IndexOutOfBoundsException(ErrorMessages.invalidIndex((INDArray)this, row, column));
            }
        }
        throw new IndexOutOfBoundsException(ErrorMessages.invalidIndex((INDArray)this, row, column));
    }

    @Override
    public AMatrix innerProduct(AMatrix a) {
        if (a instanceof Matrix22) {
            return this.innerProduct((Matrix22)a);
        }
        return super.innerProduct(a);
    }

    @Override
    public AVector innerProduct(AVector a) {
        if (a instanceof Vector2) {
            return this.innerProduct((Vector2)a);
        }
        return super.innerProduct(a);
    }

    public Vector2 innerProduct(Vector2 a) {
        return this.transform(a);
    }

    public Matrix22 innerProduct(Matrix22 a) {
        Matrix22 r = new Matrix22();
        r.m00 = this.m00 * a.m00 + this.m01 * a.m10;
        r.m01 = this.m00 * a.m01 + this.m01 * a.m11;
        r.m10 = this.m10 * a.m00 + this.m11 * a.m10;
        r.m11 = this.m10 * a.m01 + this.m11 * a.m11;
        return r;
    }

    @Override
    public void transform(AVector source, AVector dest) {
        if (source instanceof Vector2) {
            this.transform((Vector2)source, dest);
            return;
        }
        super.transform(source, dest);
    }

    public void transform(Vector2 source, AVector dest) {
        if (dest instanceof Vector2) {
            this.transform(source, (Vector2)dest);
            return;
        }
        Vector2 s = source;
        dest.set(0, this.m00 * s.x + this.m01 * s.y);
        dest.set(1, this.m10 * s.x + this.m11 * s.y);
    }

    public void transform(Vector2 source, Vector2 dest) {
        Vector2 s = source;
        dest.x = this.m00 * s.x + this.m01 * s.y;
        dest.y = this.m10 * s.x + this.m11 * s.y;
    }

    public Vector2 transform(Vector2 source) {
        Vector2 s = source;
        Vector2 result = new Vector2(this.m00 * s.x + this.m01 * s.y, this.m10 * s.x + this.m11 * s.y);
        return result;
    }

    public void transformInPlace(Vector2 dest) {
        Vector2 s = dest;
        double tx = this.m00 * s.x + this.m01 * s.y;
        double ty = this.m10 * s.x + this.m11 * s.y;
        s.x = tx;
        s.y = ty;
    }

    @Override
    public boolean isSymmetric() {
        return this.m01 == this.m10;
    }

    @Override
    public Vector toVector() {
        return Vector.of(this.m00, this.m01, this.m10, this.m11);
    }

    @Override
    public Matrix22 getTranspose() {
        return new Matrix22(this.m00, this.m10, this.m01, this.m11);
    }

    @Override
    public void getElements(double[] data, int offset) {
        data[offset++] = this.m00;
        data[offset++] = this.m01;
        data[offset++] = this.m10;
        data[offset++] = this.m11;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Matrix22) {
            return this.equals((Matrix22)o);
        }
        return super.equals(o);
    }

    public boolean equals(Matrix22 m) {
        return this.m00 == m.m00 && this.m01 == m.m01 && this.m10 == m.m10 && this.m11 == m.m11;
    }

    @Override
    public Matrix22 clone() {
        return new Matrix22(this);
    }

    @Override
    public Matrix22 exactClone() {
        return new Matrix22(this);
    }

    @Override
    public double[] toDoubleArray() {
        return new double[]{this.m00, this.m01, this.m10, this.m11};
    }

    @Override
    public boolean isZero() {
        return this.m00 == 0.0 && this.m01 == 0.0 && this.m10 == 0.0 && this.m11 == 0.0;
    }
}

