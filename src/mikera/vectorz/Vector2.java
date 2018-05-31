/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz;

import java.nio.DoubleBuffer;
import java.util.Arrays;
import mikera.arrayz.INDArray;
import mikera.vectorz.AVector;
import mikera.vectorz.Op;
import mikera.vectorz.Vector;
import mikera.vectorz.impl.APrimitiveVector;
import mikera.vectorz.util.ErrorMessages;

public final class Vector2
extends APrimitiveVector {
    private static final long serialVersionUID = -7815583836324137277L;
    public double x;
    public double y;

    public Vector2() {
    }

    public Vector2(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public static Vector2 of(double x, double y) {
        return new Vector2(x, y);
    }

    public static /* varargs */ Vector2 of(double ... values) {
        if (values.length != 2) {
            throw new IllegalArgumentException("Can't create Vector2 vector from: " + Arrays.toString(values));
        }
        return new Vector2(values[0], values[1]);
    }

    public static Vector2 create(AVector v) {
        if (v.length() != 2) {
            throw new IllegalArgumentException("Can't create Vector2 from vector with length " + v.length());
        }
        return new Vector2(v.unsafeGet(0), v.unsafeGet(1));
    }

    @Override
    public void applyOp(Op op) {
        this.x = op.apply(this.x);
        this.y = op.apply(this.y);
    }

    @Override
    public boolean isZero() {
        return this.x == 0.0 && this.y == 0.0;
    }

    public void add(Vector2 v) {
        this.x += v.x;
        this.y += v.y;
    }

    public void sub(Vector2 v) {
        this.x -= v.x;
        this.y -= v.y;
    }

    public void addMultiple(Vector2 v, double factor) {
        this.x += v.x * factor;
        this.y += v.y * factor;
    }

    public void addProduct(Vector2 a, Vector2 b) {
        this.x += a.x * b.x;
        this.y += a.y * b.y;
    }

    public void addProduct(Vector2 a, Vector2 b, double factor) {
        this.x += a.x * b.x * factor;
        this.y += a.y * b.y * factor;
    }

    @Override
    public double dotProduct(AVector a) {
        a.checkLength(2);
        return this.x * a.unsafeGet(0) + this.y * a.unsafeGet(1);
    }

    @Override
    public double dotProduct(Vector v) {
        v.checkLength(2);
        double[] data = v.getArray();
        return this.x * data[0] + this.y * data[1];
    }

    @Override
    public double dotProduct(double[] data, int offset) {
        return this.x * data[offset + 0] + this.y * data[offset + 1];
    }

    public double dotProduct(Vector2 a) {
        return this.x * a.x + this.y * a.y;
    }

    @Override
    public void scaleAdd(double factor, double constant) {
        this.x = this.x * factor + constant;
        this.y = this.y * factor + constant;
    }

    @Override
    public void scaleAdd(double factor, AVector constant) {
        constant.checkLength(2);
        this.x = this.x * factor + constant.unsafeGet(0);
        this.y = this.y * factor + constant.unsafeGet(1);
    }

    public void scaleAdd(double factor, Vector2 constant) {
        this.x = this.x * factor + constant.x;
        this.y = this.y * factor + constant.y;
    }

    public void complexMultiply(Vector2 a) {
        double nx = this.x * a.x - this.y * a.y;
        double ny = this.x * a.y + this.y * a.x;
        this.x = nx;
        this.y = ny;
    }

    public Vector2 complexConjugate() {
        return new Vector2(this.x, - this.y);
    }

    public Vector2 complexReciprocal() {
        double d = this.x * this.x + this.y * this.y;
        return new Vector2(this.x / d, (- this.y) / d);
    }

    public Vector2 complexNegation() {
        return new Vector2(- this.x, - this.y);
    }

    @Override
    public void negate() {
        this.x = - this.x;
        this.y = - this.y;
    }

    @Override
    public void add(double constant) {
        this.x += constant;
        this.y += constant;
    }

    public void add(double dx, double dy) {
        this.x += dx;
        this.y += dy;
    }

    @Override
    public void add(AVector v) {
        v.checkLength(2);
        this.x += v.unsafeGet(0);
        this.y += v.unsafeGet(1);
    }

    @Override
    public int length() {
        return 2;
    }

    @Override
    public double elementSum() {
        return this.x + this.y;
    }

    @Override
    public double elementProduct() {
        return this.x * this.y;
    }

    @Override
    public double elementMax() {
        return Math.max(this.x, this.y);
    }

    @Override
    public double elementMin() {
        return Math.min(this.x, this.y);
    }

    @Override
    public double magnitudeSquared() {
        return this.x * this.x + this.y * this.y;
    }

    @Override
    public double magnitude() {
        return Math.sqrt(this.magnitudeSquared());
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
        }
        throw new IndexOutOfBoundsException(ErrorMessages.invalidIndex((INDArray)this, (long)i));
    }

    @Override
    public double unsafeGet(int i) {
        return i == 0 ? this.x : this.y;
    }

    @Override
    public void getElements(double[] data, int offset) {
        data[offset] = this.x;
        data[offset + 1] = this.y;
    }

    @Override
    public void toDoubleBuffer(DoubleBuffer dest) {
        dest.put(this.x);
        dest.put(this.y);
    }

    @Override
    public double[] toDoubleArray() {
        return new double[]{this.x, this.y};
    }

    @Override
    public Vector2 toNormal() {
        double d = this.magnitude();
        return d == 0.0 ? new Vector2() : new Vector2(this.x / d, this.y / d);
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
        }
        this.y = value;
    }

    @Override
    public void fill(double v) {
        this.x = v;
        this.y = v;
    }

    @Override
    public void addAt(int i, double value) {
        switch (i) {
            case 0: {
                this.x += value;
                return;
            }
        }
        this.y += value;
    }

    public void rotateInPlace(int angle) {
        double ca = Math.cos(angle);
        double sa = Math.sin(angle);
        double nx = this.x * ca - this.y * sa;
        double ny = this.x * sa + this.y * ca;
        this.x = nx;
        this.y = ny;
    }

    public void setValues(double x, double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public Vector2 clone() {
        return new Vector2(this.x, this.y);
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
    public Vector2 exactClone() {
        return this.clone();
    }

    @Override
    public boolean equals(AVector v) {
        if (v instanceof Vector2) {
            return this.equals((Vector2)v);
        }
        return v.length() == 2 && this.x == v.unsafeGet(0) && this.y == v.unsafeGet(1);
    }

    public boolean equals(Vector2 v) {
        return this.x == v.x && this.y == v.y;
    }
}

