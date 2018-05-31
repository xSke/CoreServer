/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz.impl;

import java.io.ObjectStreamException;
import mikera.arrayz.INDArray;
import mikera.arrayz.ISparse;
import mikera.arrayz.impl.IDense;
import mikera.vectorz.AVector;
import mikera.vectorz.Vector;
import mikera.vectorz.impl.APrimitiveVector;
import mikera.vectorz.util.DoubleArrays;
import mikera.vectorz.util.ErrorMessages;
import mikera.vectorz.util.IntArrays;

public final class Vector0
extends APrimitiveVector
implements IDense,
ISparse {
    private static final long serialVersionUID = -8153360223054646075L;
    public static Vector0 INSTANCE = new Vector0();

    private Vector0() {
    }

    public static Vector0 of() {
        return INSTANCE;
    }

    public static /* varargs */ Vector0 of(double ... values) {
        if (values.length != 0) {
            throw new IllegalArgumentException("Vector0 cannot have components!");
        }
        return INSTANCE;
    }

    @Override
    public int length() {
        return 0;
    }

    @Override
    public double elementSum() {
        return 0.0;
    }

    @Override
    public double elementProduct() {
        return 1.0;
    }

    @Override
    public long nonZeroCount() {
        return 0L;
    }

    @Override
    public int[] nonZeroIndices() {
        return IntArrays.EMPTY_INT_ARRAY;
    }

    @Override
    public double get(int i) {
        throw new IndexOutOfBoundsException("Attempt to get on zero length vector!");
    }

    @Override
    public void set(int i, double value) {
        throw new IndexOutOfBoundsException("Attempt to set on zero length vector!");
    }

    @Override
    public void addToArray(int offset, double[] array, int arrayOffset, int length) {
    }

    @Override
    public Vector0 clone() {
        return this;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public boolean isFullyMutable() {
        return true;
    }

    @Override
    public int hashCode() {
        return 1;
    }

    @Override
    public boolean isZero() {
        return true;
    }

    @Override
    public boolean isRangeZero(int start, int length) {
        return true;
    }

    @Override
    public double magnitudeSquared() {
        return 0.0;
    }

    @Override
    public double magnitude() {
        return 0.0;
    }

    @Override
    public AVector join(AVector v) {
        return v;
    }

    @Override
    public Vector0 immutable() {
        return this;
    }

    @Override
    public Vector dense() {
        return Vector.EMPTY;
    }

    @Override
    public double[] toDoubleArray() {
        return DoubleArrays.EMPTY;
    }

    @Override
    public boolean equals(AVector v) {
        return v.length() == 0;
    }

    @Override
    public boolean equalsArray(double[] data, int offset) {
        return true;
    }

    @Override
    public Vector0 subVector(int start, int length) {
        if (start == 0 && length == 0) {
            return this;
        }
        throw new IndexOutOfBoundsException(ErrorMessages.invalidRange(this, start, length));
    }

    private Object readResolve() throws ObjectStreamException {
        return INSTANCE;
    }

    @Override
    public Vector0 exactClone() {
        return this;
    }

    @Override
    public double dotProduct(double[] data, int offset) {
        return 0.0;
    }
}

