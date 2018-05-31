/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx;

import mikera.arrayz.INDArray;
import mikera.matrixx.AMatrix;
import mikera.matrixx.impl.APrimitiveMatrix;
import mikera.transformz.AAffineTransform;
import mikera.transformz.Affine34;
import mikera.transformz.marker.ISpecialisedTransform;
import mikera.vectorz.AVector;
import mikera.vectorz.Vector3;
import mikera.vectorz.util.ErrorMessages;

public final class Matrix33
extends APrimitiveMatrix
implements ISpecialisedTransform {
    private static final long serialVersionUID = 238200620223028897L;
    public double m00;
    public double m01;
    public double m02;
    public double m10;
    public double m11;
    public double m12;
    public double m20;
    public double m21;
    public double m22;

    public Matrix33() {
    }

    public Matrix33(Matrix33 source) {
        Matrix33 s = source;
        this.m00 = s.m00;
        this.m01 = s.m01;
        this.m02 = s.m02;
        this.m10 = s.m10;
        this.m11 = s.m11;
        this.m12 = s.m12;
        this.m20 = s.m20;
        this.m21 = s.m21;
        this.m22 = s.m22;
    }

    public Matrix33(double m00, double m01, double m02, double m10, double m11, double m12, double m20, double m21, double m22) {
        this.m00 = m00;
        this.m01 = m01;
        this.m02 = m02;
        this.m10 = m10;
        this.m11 = m11;
        this.m12 = m12;
        this.m20 = m20;
        this.m21 = m21;
        this.m22 = m22;
    }

    public Matrix33(AMatrix m) {
        this.m00 = m.unsafeGet(0, 0);
        this.m01 = m.unsafeGet(0, 1);
        this.m02 = m.unsafeGet(0, 2);
        this.m10 = m.unsafeGet(1, 0);
        this.m11 = m.unsafeGet(1, 1);
        this.m12 = m.unsafeGet(1, 2);
        this.m20 = m.unsafeGet(2, 0);
        this.m21 = m.unsafeGet(2, 1);
        this.m22 = m.unsafeGet(2, 2);
    }

    @Override
    public double determinant() {
        return this.m00 * this.m11 * this.m22 + this.m01 * this.m12 * this.m20 + this.m02 * this.m10 * this.m21 - this.m00 * this.m12 * this.m21 - this.m01 * this.m10 * this.m22 - this.m02 * this.m11 * this.m20;
    }

    @Override
    public long elementCount() {
        return 9L;
    }

    @Override
    public void multiply(double factor) {
        this.m00 *= factor;
        this.m01 *= factor;
        this.m02 *= factor;
        this.m10 *= factor;
        this.m11 *= factor;
        this.m12 *= factor;
        this.m20 *= factor;
        this.m21 *= factor;
        this.m22 *= factor;
    }

    @Override
    public int rowCount() {
        return 3;
    }

    @Override
    public int columnCount() {
        return 3;
    }

    @Override
    public int checkSquare() {
        return 3;
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
                    case 2: {
                        return this.m02;
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
                    case 2: {
                        return this.m12;
                    }
                }
                throw new IndexOutOfBoundsException(ErrorMessages.invalidIndex((INDArray)this, row, column));
            }
            case 2: {
                switch (column) {
                    case 0: {
                        return this.m20;
                    }
                    case 1: {
                        return this.m21;
                    }
                    case 2: {
                        return this.m22;
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
                    case 2: {
                        this.m02 = value;
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
                    case 2: {
                        this.m12 = value;
                        return;
                    }
                }
                throw new IndexOutOfBoundsException(ErrorMessages.invalidIndex((INDArray)this, row, column));
            }
            case 2: {
                switch (column) {
                    case 0: {
                        this.m20 = value;
                        return;
                    }
                    case 1: {
                        this.m21 = value;
                        return;
                    }
                    case 2: {
                        this.m22 = value;
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
        if (a instanceof Matrix33) {
            return this.innerProduct((Matrix33)a);
        }
        return super.innerProduct(a);
    }

    @Override
    public AVector innerProduct(AVector a) {
        if (a instanceof Vector3) {
            return this.innerProduct((Vector3)a);
        }
        return super.innerProduct(a);
    }

    public Vector3 innerProduct(Vector3 a) {
        return this.transform(a);
    }

    public Matrix33 innerProduct(Matrix33 a) {
        Matrix33 r = new Matrix33();
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                double acc = 0.0;
                for (int k = 0; k < 3; ++k) {
                    acc += this.unsafeGet(i, k) * a.unsafeGet(k, j);
                }
                r.set(i, j, acc);
            }
        }
        return r;
    }

    @Override
    public void transform(AVector source, AVector dest) {
        if (source instanceof Vector3) {
            this.transform((Vector3)source, dest);
            return;
        }
        super.transform(source, dest);
    }

    public void transform(Vector3 source, AVector dest) {
        if (dest instanceof Vector3) {
            this.transform(source, (Vector3)dest);
            return;
        }
        if (dest.length() != 3) {
            throw new IllegalArgumentException(ErrorMessages.mismatch(source, dest));
        }
        Vector3 s = source;
        dest.unsafeSet(0, this.m00 * s.x + this.m01 * s.y + this.m02 * s.z);
        dest.unsafeSet(1, this.m10 * s.x + this.m11 * s.y + this.m12 * s.z);
        dest.unsafeSet(2, this.m20 * s.x + this.m21 * s.y + this.m22 * s.z);
    }

    public void transform(Vector3 source, Vector3 dest) {
        double x = source.x;
        double y = source.y;
        double z = source.z;
        dest.x = this.m00 * x + this.m01 * y + this.m02 * z;
        dest.y = this.m10 * x + this.m11 * y + this.m12 * z;
        dest.z = this.m20 * x + this.m21 * y + this.m22 * z;
    }

    public void transformNormal(AVector source, AVector dest) {
        if (source instanceof Vector3 && dest instanceof Vector3) {
            this.transformNormal((Vector3)source, (Vector3)dest);
            return;
        }
        this.transform(source, dest);
        dest.normalise();
    }

    public void transformNormal(Vector3 source, Vector3 dest) {
        this.transform(source, dest);
        dest.normalise();
    }

    public Vector3 transform(Vector3 source) {
        Vector3 s = source;
        Vector3 result = new Vector3(this.m00 * s.x + this.m01 * s.y + this.m02 * s.z, this.m10 * s.x + this.m11 * s.y + this.m12 * s.z, this.m20 * s.x + this.m21 * s.y + this.m22 * s.z);
        return result;
    }

    @Override
    public void transformInPlace(AVector dest) {
        if (dest instanceof Vector3) {
            this.transformInPlace((Vector3)dest);
            return;
        }
        if (dest.length() != 3) {
            throw new IllegalArgumentException("Wrong target vector length");
        }
        double sx = dest.unsafeGet(0);
        double sy = dest.unsafeGet(1);
        double sz = dest.unsafeGet(2);
        double tx = this.m00 * sx + this.m01 * sy + this.m02 * sz;
        double ty = this.m10 * sx + this.m11 * sy + this.m12 * sz;
        double tz = this.m20 * sx + this.m21 * sy + this.m22 * sz;
        dest.set(0, tx);
        dest.set(1, ty);
        dest.set(2, tz);
    }

    public void transformInPlace(Vector3 dest) {
        Vector3 s = dest;
        double tx = this.m00 * s.x + this.m01 * s.y + this.m02 * s.z;
        double ty = this.m10 * s.x + this.m11 * s.y + this.m12 * s.z;
        double tz = this.m20 * s.x + this.m21 * s.y + this.m22 * s.z;
        s.x = tx;
        s.y = ty;
        s.z = tz;
    }

    @Override
    public boolean isSymmetric() {
        return this.m01 == this.m10 && this.m20 == this.m02 && this.m21 == this.m12;
    }

    @Override
    public Affine34 toAffineTransform() {
        return new Affine34(this.m00, this.m01, this.m02, 0.0, this.m10, this.m11, this.m12, 0.0, this.m20, this.m21, this.m22, 0.0);
    }

    @Override
    public Matrix33 getTranspose() {
        return new Matrix33(this.m00, this.m10, this.m20, this.m01, this.m11, this.m21, this.m02, this.m12, this.m22);
    }

    @Override
    public Vector3 getRowClone(int row) {
        switch (row) {
            case 0: {
                return Vector3.of(this.m00, this.m01, this.m02);
            }
            case 1: {
                return Vector3.of(this.m10, this.m11, this.m12);
            }
            case 2: {
                return Vector3.of(this.m20, this.m21, this.m22);
            }
        }
        throw new IndexOutOfBoundsException("Row index = " + row);
    }

    @Override
    public void copyRowTo(int row, double[] dest, int destOffset) {
        if (row == 0) {
            dest[destOffset++] = this.m00;
            dest[destOffset++] = this.m01;
            dest[destOffset++] = this.m02;
        } else if (row == 1) {
            dest[destOffset++] = this.m10;
            dest[destOffset++] = this.m11;
            dest[destOffset++] = this.m12;
        } else {
            dest[destOffset++] = this.m20;
            dest[destOffset++] = this.m21;
            dest[destOffset++] = this.m22;
        }
    }

    @Override
    public Matrix33 inverse() {
        double det = this.determinant();
        if (det == 0.0) {
            return null;
        }
        double invDet = 1.0 / det;
        return new Matrix33(invDet * (this.m11 * this.m22 - this.m12 * this.m21), invDet * (this.m02 * this.m21 - this.m01 * this.m22), invDet * (this.m01 * this.m12 - this.m02 * this.m11), invDet * (this.m12 * this.m20 - this.m10 * this.m22), invDet * (this.m00 * this.m22 - this.m02 * this.m20), invDet * (this.m02 * this.m10 - this.m00 * this.m12), invDet * (this.m10 * this.m21 - this.m11 * this.m20), invDet * (this.m01 * this.m20 - this.m00 * this.m21), invDet * (this.m00 * this.m11 - this.m01 * this.m10));
    }

    @Override
    public double trace() {
        return this.m00 + this.m11 + this.m22;
    }

    @Override
    public double diagonalProduct() {
        return this.m00 * this.m11 * this.m22;
    }

    @Override
    public Matrix33 clone() {
        return new Matrix33(this);
    }

    @Override
    public double[] toDoubleArray() {
        return new double[]{this.m00, this.m01, this.m02, this.m10, this.m11, this.m12, this.m20, this.m21, this.m22};
    }

    @Override
    public Matrix33 exactClone() {
        return new Matrix33(this);
    }

    @Override
    public void getElements(double[] data, int offset) {
        data[offset++] = this.m00;
        data[offset++] = this.m01;
        data[offset++] = this.m02;
        data[offset++] = this.m10;
        data[offset++] = this.m11;
        data[offset++] = this.m12;
        data[offset++] = this.m20;
        data[offset++] = this.m21;
        data[offset++] = this.m22;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Matrix33) {
            return this.equals((Matrix33)o);
        }
        return super.equals(o);
    }

    public boolean equals(Matrix33 m) {
        return this.m00 == m.m00 && this.m01 == m.m01 && this.m02 == m.m02 && this.m10 == m.m10 && this.m11 == m.m11 && this.m12 == m.m12 && this.m20 == m.m20 && this.m21 == m.m21 && this.m22 == m.m22;
    }

    public static Matrix33 createIdentityMatrix() {
        return new Matrix33(1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0);
    }

    public static Matrix33 createScaleMatrix(double d) {
        return new Matrix33(d, 0.0, 0.0, 0.0, d, 0.0, 0.0, 0.0, d);
    }

    @Override
    public boolean isZero() {
        return this.m00 == 0.0 && this.m01 == 0.0 && this.m02 == 0.0 && this.m10 == 0.0 && this.m11 == 0.0 && this.m12 == 0.0 && this.m20 == 0.0 && this.m21 == 0.0 && this.m22 == 0.0;
    }
}

