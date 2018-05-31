/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz;

import java.nio.DoubleBuffer;
import mikera.arrayz.INDArray;
import mikera.vectorz.AVector;
import mikera.vectorz.Op;
import mikera.vectorz.impl.APrimitiveVector;
import mikera.vectorz.util.ErrorMessages;

public final class Vector4
extends APrimitiveVector {
    private static final long serialVersionUID = -6018622211027585397L;
    public double x;
    public double y;
    public double z;
    public double t;

    public Vector4() {
    }

    public Vector4(Vector4 source) {
        this.x = source.x;
        this.y = source.y;
        this.z = source.z;
        this.t = source.t;
    }

    public Vector4(double x, double y, double z, double t) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.t = t;
    }

    public /* varargs */ Vector4(double ... values) {
        if (values.length != this.length()) {
            throw new IllegalArgumentException("Can't create " + this.length() + "D vector from values of length: " + values.length);
        }
        this.x = values[0];
        this.y = values[1];
        this.z = values[2];
        this.t = values[3];
    }

    public static Vector4 of(double x, double y, double z, double t) {
        return new Vector4(x, y, z, t);
    }

    public static /* varargs */ Vector4 of(double ... values) {
        return new Vector4(values);
    }

    @Override
    public void applyOp(Op op) {
        this.x = op.apply(this.x);
        this.y = op.apply(this.y);
        this.z = op.apply(this.z);
        this.t = op.apply(this.t);
    }

    @Override
    public boolean isZero() {
        return this.x == 0.0 && this.y == 0.0 && this.z == 0.0 && this.t == 0.0;
    }

    public void add(double dx, double dy, double dz, double dt) {
        this.x += dx;
        this.y += dy;
        this.z += dz;
        this.t += dt;
    }

    @Override
    public void add(AVector v) {
        if (v.length() != 4) {
            throw new IllegalArgumentException(ErrorMessages.incompatibleShapes(this, (INDArray)v));
        }
        this.x += v.unsafeGet(0);
        this.y += v.unsafeGet(1);
        this.z += v.unsafeGet(2);
        this.t += v.unsafeGet(3);
    }

    public void add(Vector4 a) {
        this.x += a.x;
        this.y += a.y;
        this.z += a.z;
        this.t += a.t;
    }

    public void set(Vector4 a) {
        this.x = a.x;
        this.y = a.y;
        this.z = a.z;
        this.t = a.t;
    }

    @Override
    public void negate() {
        this.x = - this.x;
        this.y = - this.y;
        this.z = - this.z;
        this.t = - this.t;
    }

    public void addMultiple(double dx, double dy, double dz, double dt, double factor) {
        this.x += dx * factor;
        this.y += dy * factor;
        this.z += dz * factor;
        this.t += dt * factor;
    }

    @Override
    public void addMultiple(AVector v, double factor) {
        if (v instanceof Vector4) {
            this.addMultiple((Vector4)v, factor);
        } else {
            v.checkLength(4);
            this.x += v.unsafeGet(0) * factor;
            this.y += v.unsafeGet(1) * factor;
            this.z += v.unsafeGet(2) * factor;
            this.t += v.unsafeGet(3) * factor;
        }
    }

    public void addMultiple(Vector4 v, double factor) {
        this.x += v.x * factor;
        this.y += v.y * factor;
        this.z += v.z * factor;
        this.t += v.t * factor;
    }

    public void addProduct(Vector4 a, Vector4 b) {
        this.x += a.x * b.x;
        this.y += a.y * b.y;
        this.z += a.z * b.z;
        this.t += a.t * b.t;
    }

    public void addProduct(Vector4 a, Vector4 b, double factor) {
        this.x += a.x * b.x * factor;
        this.y += a.y * b.y * factor;
        this.z += a.z * b.z * factor;
        this.t += a.t * b.t * factor;
    }

    public double dotProduct(Vector4 a) {
        return this.x * a.x + this.y * a.y + this.z * a.z + this.t * a.t;
    }

    @Override
    public double dotProduct(double[] as, int offset) {
        return this.x * as[offset] + this.y * as[offset + 1] + this.z * as[offset + 2] + this.t * as[offset + 3];
    }

    @Override
    public double dotProduct(AVector v) {
        v.checkLength(4);
        return this.x * v.unsafeGet(0) + this.y * v.unsafeGet(1) + this.z * v.unsafeGet(2) + this.t * v.unsafeGet(3);
    }

    @Override
    public int length() {
        return 4;
    }

    @Override
    public double elementSum() {
        return this.x + this.y + this.z + this.t;
    }

    @Override
    public double elementProduct() {
        return this.x * this.y * this.z * this.t;
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
            case 3: {
                return this.t;
            }
        }
        throw new IndexOutOfBoundsException(ErrorMessages.invalidIndex((INDArray)this, (long)i));
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
            case 3: {
                this.t = value;
                return;
            }
        }
        throw new IndexOutOfBoundsException(ErrorMessages.invalidIndex((INDArray)this, (long)i));
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
            case 2: {
                this.z += value;
                return;
            }
            case 3: {
                this.t += value;
                return;
            }
        }
        throw new IndexOutOfBoundsException(ErrorMessages.invalidIndex((INDArray)this, (long)i));
    }

    public void setValues(double x, double y, double z, double t) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.t = t;
    }

    @Override
    public void getElements(double[] data, int offset) {
        data[offset] = this.x;
        data[offset + 1] = this.y;
        data[offset + 2] = this.z;
        data[offset + 3] = this.t;
    }

    @Override
    public void toDoubleBuffer(DoubleBuffer dest) {
        dest.put(this.x);
        dest.put(this.y);
        dest.put(this.z);
        dest.put(this.t);
    }

    @Override
    public double[] toDoubleArray() {
        return new double[]{this.x, this.y, this.z, this.t};
    }

    @Override
    public Vector4 clone() {
        return new Vector4(this.x, this.y, this.z, this.t);
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
    public double getT() {
        return this.t;
    }

    @Override
    public Vector4 exactClone() {
        return this.clone();
    }
}

