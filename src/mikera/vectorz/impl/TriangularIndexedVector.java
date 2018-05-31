/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz.impl;

import mikera.arrayz.INDArray;
import mikera.vectorz.AVector;
import mikera.vectorz.impl.AArrayVector;
import mikera.vectorz.impl.Vector0;

public final class TriangularIndexedVector
extends AArrayVector {
    private static final long serialVersionUID = -3987292933848795478L;
    private int offset;
    private int baseStride2;

    protected TriangularIndexedVector(int length, double[] data, int offset, int baseStride2) {
        super(length, data);
        this.offset = offset;
        this.baseStride2 = baseStride2;
    }

    public static TriangularIndexedVector wrap(int length, double[] data, int offset, int baseStride) {
        return new TriangularIndexedVector(length, data, offset, baseStride * 2);
    }

    @Override
    protected int index(int i) {
        return this.offset + ((this.baseStride2 + i + 1) * i >> 1);
    }

    @Override
    public double get(int i) {
        this.checkIndex(i);
        return this.data[this.index(i)];
    }

    @Override
    public void set(int i, double value) {
        this.checkIndex(i);
        this.data[this.index((int)i)] = value;
    }

    @Override
    public double unsafeGet(int i) {
        return this.data[this.index(i)];
    }

    @Override
    public void unsafeSet(int i, double value) {
        this.data[this.index((int)i)] = value;
    }

    @Override
    public AVector subVector(int start, int length) {
        int len = this.checkRange(start, length);
        if (length == 0) {
            return Vector0.INSTANCE;
        }
        if (length == len) {
            return this;
        }
        return TriangularIndexedVector.wrap(length, this.data, this.index(start), (this.baseStride2 >> 1) + start);
    }

    @Override
    public AVector exactClone() {
        return new TriangularIndexedVector(this.length, (double[])this.data.clone(), this.offset, this.baseStride2);
    }

    @Override
    public double dotProduct(double[] data, int offset) {
        double result = 0.0;
        for (int i = 0; i < this.length; ++i) {
            result += data[offset + i] * this.unsafeGet(i);
        }
        return result;
    }
}

