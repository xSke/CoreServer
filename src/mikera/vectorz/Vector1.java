/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz;

import mikera.arrayz.INDArray;
import mikera.vectorz.AVector;
import mikera.vectorz.Op;
import mikera.vectorz.Vector;
import mikera.vectorz.impl.APrimitiveVector;
import mikera.vectorz.util.ErrorMessages;

public final class Vector1
extends APrimitiveVector {
    private static final long serialVersionUID = -6312801771839902928L;
    public double x;

    public Vector1() {
    }

    public Vector1(double x) {
        this.x = x;
    }

    public /* varargs */ Vector1(double ... values) {
        if (values.length != this.length()) {
            throw new IllegalArgumentException("Can't create " + this.length() + "D vector from values with length: " + values.length);
        }
        this.x = values[0];
    }

    public static Vector1 of(double x) {
        return new Vector1(x);
    }

    public static /* varargs */ Vector1 of(double ... values) {
        return new Vector1(values);
    }

    @Override
    public double dotProduct(AVector a) {
        a.checkLength(1);
        return this.x * a.unsafeGet(0);
    }

    @Override
    public double dotProduct(Vector v) {
        v.checkLength(1);
        double[] data = v.getArray();
        return this.x * data[0];
    }

    @Override
    public double dotProduct(double[] data, int offset) {
        return this.x * data[offset + 0];
    }

    @Override
    public int length() {
        return 1;
    }

    @Override
    public double elementSum() {
        return this.x;
    }

    @Override
    public double elementProduct() {
        return this.x;
    }

    @Override
    public double elementMax() {
        return this.x;
    }

    @Override
    public double elementMin() {
        return this.x;
    }

    @Override
    public void applyOp(Op op) {
        this.x = op.apply(this.x);
    }

    @Override
    public double get(int i) {
        if (i == 0) {
            return this.x;
        }
        throw new IndexOutOfBoundsException(ErrorMessages.invalidIndex((INDArray)this, (long)i));
    }

    @Override
    public double unsafeGet(int i) {
        return this.x;
    }

    @Override
    public void set(int i, double value) {
        if (i != 0) {
            throw new IndexOutOfBoundsException(ErrorMessages.invalidIndex((INDArray)this, (long)i));
        }
        this.x = value;
    }

    @Override
    public void unsafeSet(int i, double value) {
        this.x = value;
    }

    @Override
    public void fill(double v) {
        this.x = v;
    }

    @Override
    public void getElements(double[] data, int offset) {
        data[offset] = this.x;
    }

    @Override
    public void add(AVector v) {
        if (v.length() != 1) {
            throw new IllegalArgumentException(ErrorMessages.incompatibleShapes(this, (INDArray)v));
        }
        this.x += v.unsafeGet(0);
    }

    @Override
    public void addAt(int i, double value) {
        switch (i) {
            case 0: {
                this.x += value;
                return;
            }
        }
        throw new IndexOutOfBoundsException(ErrorMessages.invalidIndex((INDArray)this, (long)i));
    }

    public void setValues(double x) {
        this.x = x;
    }

    @Override
    public void negate() {
        this.x = - this.x;
    }

    @Override
    public boolean isZero() {
        return this.x == 0.0;
    }

    @Override
    public Vector1 clone() {
        return new Vector1(this.x);
    }

    @Override
    public double[] toDoubleArray() {
        return new double[]{this.x};
    }

    @Override
    public double getX() {
        return this.x;
    }

    @Override
    public Vector1 exactClone() {
        return this.clone();
    }
}

