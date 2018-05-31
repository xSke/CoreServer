/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz;

import java.nio.DoubleBuffer;
import mikera.arrayz.INDArray;
import mikera.vectorz.AVector;
import mikera.vectorz.Op;
import mikera.vectorz.Tools;
import mikera.vectorz.Vector;
import mikera.vectorz.Vectorz;
import mikera.vectorz.impl.APrimitiveVector;
import mikera.vectorz.util.ErrorMessages;

public final class Vector3
extends APrimitiveVector {
    private static final long serialVersionUID = 2338611313487869443L;
    public double x;
    public double y;
    public double z;

    public Vector3() {
    }

    public Vector3(Vector3 source) {
        this.x = source.x;
        this.y = source.y;
        this.z = source.z;
    }

    public Vector3(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void applyOp(Op op) {
        this.x = op.apply(this.x);
        this.y = op.apply(this.y);
        this.z = op.apply(this.z);
    }

    @Override
    public double normalise() {
        double d = this.magnitude();
        if (d > 0.0) {
            this.multiply(1.0 / d);
        }
        return d;
    }

    public /* varargs */ Vector3(double ... values) {
        if (values.length != this.length()) {
            throw new IllegalArgumentException("Can't create " + this.length() + "D vector from values with length: " + values.length);
        }
        this.x = values[0];
        this.y = values[1];
        this.z = values[2];
    }

    public Vector3(AVector v) {
        assert (v.length() == 3);
        this.set(v);
    }

    public static Vector3 of(double x, double y, double z) {
        return new Vector3(x, y, z);
    }

    public static /* varargs */ Vector3 of(double ... values) {
        return new Vector3(values);
    }

    public static Vector3 create(Object o) {
        return Vector3.create(Vectorz.create(o));
    }

    public static Vector3 create(AVector v) {
        return new Vector3(v);
    }

    @Override
    public boolean isZero() {
        return this.x == 0.0 && this.y == 0.0 && this.z == 0.0;
    }

    @Override
    public double angle(AVector v) {
        if (v instanceof Vector3) {
            return this.angle((Vector3)v);
        }
        return super.angle(v);
    }

    public double angle(Vector3 v) {
        double mag2 = this.x * this.x + this.y * this.y + this.z * this.z;
        double vmag2 = v.x * v.x + v.y * v.y + v.z * v.z;
        double dot = this.x * v.x + this.y * v.y + this.z * v.z;
        return Math.acos(dot / Math.sqrt(mag2 * vmag2));
    }

    public void add(double dx, double dy, double dz) {
        this.x += dx;
        this.y += dy;
        this.z += dz;
    }

    @Override
    public double magnitudeSquared() {
        return this.x * this.x + this.y * this.y + this.z * this.z;
    }

    public double distanceSquared(Vector3 v) {
        double dx = this.x - v.x;
        double dy = this.y - v.y;
        double dz = this.z - v.z;
        return dx * dx + dy * dy + dz * dz;
    }

    public double distance(Vector3 v) {
        return Math.sqrt(this.distanceSquared(v));
    }

    @Override
    public double distance(AVector v) {
        if (v instanceof Vector3) {
            return this.distance((Vector3)v);
        }
        return super.distance(v);
    }

    @Override
    public double magnitude() {
        return Math.sqrt(this.magnitudeSquared());
    }

    public void set(Vector3 a) {
        this.x = a.x;
        this.y = a.y;
        this.z = a.z;
    }

    @Override
    public void multiply(double d) {
        this.x *= d;
        this.y *= d;
        this.z *= d;
    }

    @Override
    public Vector3 multiplyCopy(double d) {
        return new Vector3(this.x * d, this.y * d, this.z * d);
    }

    public void addMultiple(double dx, double dy, double dz, double factor) {
        this.x += dx * factor;
        this.y += dy * factor;
        this.z += dz * factor;
    }

    @Override
    public void addMultiple(AVector v, double factor) {
        if (v instanceof Vector3) {
            this.addMultiple((Vector3)v, factor);
        } else {
            this.x += v.unsafeGet(0) * factor;
            this.y += v.unsafeGet(1) * factor;
            this.z += v.unsafeGet(2) * factor;
        }
    }

    public void addMultiple(Vector3 v, double factor) {
        this.x += v.x * factor;
        this.y += v.y * factor;
        this.z += v.z * factor;
    }

    public void addProduct(Vector3 a, Vector3 b) {
        this.x += a.x * b.x;
        this.y += a.y * b.y;
        this.z += a.z * b.z;
    }

    public void addProduct(Vector3 a, Vector3 b, double factor) {
        this.x += a.x * b.x * factor;
        this.y += a.y * b.y * factor;
        this.z += a.z * b.z * factor;
    }

    public void subtractMultiple(Vector3 v, double factor) {
        this.x -= v.x * factor;
        this.y -= v.y * factor;
        this.z -= v.z * factor;
    }

    @Override
    public void add(AVector v) {
        if (v instanceof Vector3) {
            this.add((Vector3)v);
        } else {
            v.checkLength(3);
            this.x += v.unsafeGet(0);
            this.y += v.unsafeGet(1);
            this.z += v.unsafeGet(2);
        }
    }

    @Override
    public Vector3 addCopy(AVector v) {
        if (v instanceof Vector3) {
            return this.addCopy((Vector3)v);
        }
        v.checkLength(3);
        return new Vector3(this.x + v.unsafeGet(0), this.y + v.unsafeGet(1), this.z + v.unsafeGet(2));
    }

    public Vector3 addCopy(Vector3 v) {
        return new Vector3(this.x + v.x, this.y + v.y, this.z + v.z);
    }

    public void add(Vector3 v) {
        this.x += v.x;
        this.y += v.y;
        this.z += v.z;
    }

    public void sub(Vector3 v) {
        this.x -= v.x;
        this.y -= v.y;
        this.z -= v.z;
    }

    public void subMultiple(Vector3 v, double factor) {
        this.addMultiple(v, - factor);
    }

    public double dotProduct(Vector3 a) {
        return this.x * a.x + this.y * a.y + this.z * a.z;
    }

    @Override
    public double dotProduct(AVector v) {
        v.checkLength(3);
        return this.x * v.unsafeGet(0) + this.y * v.unsafeGet(1) + this.z * v.unsafeGet(2);
    }

    @Override
    public double dotProduct(Vector v) {
        v.checkLength(3);
        double[] data = v.getArray();
        return this.x * data[0] + this.y * data[1] + this.z * data[2];
    }

    @Override
    public double dotProduct(double[] data, int offset) {
        return this.x * data[offset + 0] + this.y * data[offset + 1] + this.z * data[offset + 2];
    }

    @Override
    public void crossProduct(AVector a) {
        if (a instanceof Vector3) {
            this.crossProduct((Vector3)a);
            return;
        }
        double x2 = a.unsafeGet(0);
        double y2 = a.unsafeGet(1);
        double z2 = a.unsafeGet(2);
        double tx = this.y * z2 - this.z * y2;
        double ty = this.z * x2 - this.x * z2;
        double tz = this.x * y2 - this.y * x2;
        this.x = tx;
        this.y = ty;
        this.z = tz;
    }

    @Override
    public void crossProduct(Vector3 a) {
        double tx = this.y * a.z - this.z * a.y;
        double ty = this.z * a.x - this.x * a.z;
        double tz = this.x * a.y - this.y * a.x;
        this.x = tx;
        this.y = ty;
        this.z = tz;
    }

    @Override
    public void projectToPlane(AVector normal, double distance) {
        if (normal instanceof Vector3) {
            this.projectToPlane((Vector3)normal, distance);
            return;
        }
        super.projectToPlane(normal, distance);
    }

    public void projectToPlane(Vector3 normal, double distance) {
        assert (Tools.epsilonEquals(normal.magnitude(), 1.0));
        double d = this.dotProduct(normal);
        this.addMultiple(normal, distance - d);
    }

    @Override
    public int length() {
        return 3;
    }

    @Override
    public double elementSum() {
        return this.x + this.y + this.z;
    }

    @Override
    public double elementProduct() {
        return this.x * this.y * this.z;
    }

    @Override
    public void scaleAdd(double factor, double constant) {
        this.x = this.x * factor + constant;
        this.y = this.y * factor + constant;
        this.z = this.z * factor + constant;
    }

    @Override
    public void scaleAdd(double factor, AVector constant) {
        if (constant instanceof Vector3) {
            this.scaleAdd(factor, (Vector3)constant);
            return;
        }
        this.x = this.x * factor + constant.unsafeGet(0);
        this.y = this.y * factor + constant.unsafeGet(1);
        this.z = this.z * factor + constant.unsafeGet(2);
    }

    public void scaleAdd(double factor, Vector3 constant) {
        this.x = this.x * factor + constant.x;
        this.y = this.y * factor + constant.y;
        this.z = this.z * factor + constant.z;
    }

    @Override
    public void add(double constant) {
        this.x += constant;
        this.y += constant;
        this.z += constant;
    }

    @Override
    public double get(int i) {
        switch (i) {
            case 0: {
                return this.x;
            }
            case 1: {
                return this.y;
            }
            case 2: {
                return this.z;
            }
        }
        throw new IndexOutOfBoundsException(ErrorMessages.invalidIndex((INDArray)this, (long)i));
    }

    @Override
    public double unsafeGet(int i) {
        switch (i) {
            case 0: {
                return this.x;
            }
            case 1: {
                return this.y;
            }
        }
        return this.z;
    }

    @Override
    public void set(AVector v) {
        if (v.length() != 3) {
            throw new IllegalArgumentException(ErrorMessages.incompatibleShapes(this, (INDArray)v));
        }
        this.x = v.unsafeGet(0);
        this.y = v.unsafeGet(1);
        this.z = v.unsafeGet(2);
    }

    @Override
    public void fill(double v) {
        this.x = v;
        this.y = v;
        this.z = v;
    }

    @Override
    public void set(int i, double value) {
        switch (i) {
            case 0: {
                this.x = value;
                return;
            }
            case 1: {
                this.y = value;
                return;
            }
            case 2: {
                this.z = value;
                return;
            }
        }
        throw new IndexOutOfBoundsException(ErrorMessages.invalidIndex((INDArray)this, (long)i));
    }

    @Override
    public void unsafeSet(int i, double value) {
        switch (i) {
            case 0: {
                this.x = value;
                return;
            }
            case 1: {
                this.y = value;
                return;
            }
        }
        this.z = value;
    }

    @Override
    public void addAt(int i, double value) {
        switch (i) {
            case 0: {
                this.x += value;
                return;
            }
            case 1: {
                this.y += value;
                return;
            }
        }
        this.z += value;
    }

    public void setValues(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void negate() {
        this.x = - this.x;
        this.y = - this.y;
        this.z = - this.z;
    }

    @Override
    public Vector3 negateCopy() {
        return new Vector3(- this.x, - this.y, - this.z);
    }

    @Override
    public void getElements(double[] data, int offset) {
        data[offset] = this.x;
        data[offset + 1] = this.y;
        data[offset + 2] = this.z;
    }

    @Override
    public void toDoubleBuffer(DoubleBuffer dest) {
        dest.put(this.x);
        dest.put(this.y);
        dest.put(this.z);
    }

    @Override
    public double[] toDoubleArray() {
        return new double[]{this.x, this.y, this.z};
    }

    @Override
    public Vector3 toNormal() {
        double d = this.magnitude();
        return d == 0.0 ? new Vector3() : new Vector3(this.x / d, this.y / d, this.z / d);
    }

    @Override
    public Vector3 clone() {
        return new Vector3(this.x, this.y, this.z);
    }

    @Override
    public Vector3 copy() {
        return this.clone();
    }

    @Override
    public double getX() {
        return this.x;
    }

    @Override
    public double getY() {
        return this.y;
    }

    @Override
    public double getZ() {
        return this.z;
    }

    @Override
    public Vector3 exactClone() {
        return this.clone();
    }

    @Override
    public boolean equals(AVector v) {
        if (v == this) {
            return true;
        }
        if (v instanceof Vector3) {
            return this.equals((Vector3)v);
        }
        return v.length() == 3 && this.x == v.unsafeGet(0) && this.y == v.unsafeGet(1) && this.z == v.unsafeGet(2);
    }

    public boolean equals(Vector3 v) {
        return this.x == v.x && this.y == v.y && this.z == v.z;
    }
}

