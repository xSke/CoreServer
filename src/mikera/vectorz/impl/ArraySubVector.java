/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz.impl;

import mikera.arrayz.INDArray;
import mikera.randomz.Hash;
import mikera.vectorz.AVector;
import mikera.vectorz.impl.ADenseArrayVector;
import mikera.vectorz.impl.ASizedVector;
import mikera.vectorz.impl.Vector0;

public final class ArraySubVector
extends ADenseArrayVector {
    private static final long serialVersionUID = 1262951505515197105L;
    private final int offset;

    public static ArraySubVector wrap(double[] values) {
        return new ArraySubVector(values);
    }

    private ArraySubVector(double[] values) {
        this(values, 0, values.length);
    }

    private ArraySubVector(double[] data, int offset, int length) {
        super(length, data);
        this.offset = offset;
    }

    public static ArraySubVector wrap(double[] data, int offset, int length) {
        return new ArraySubVector(data, offset, length);
    }

    public ArraySubVector(ADenseArrayVector source, int offset, int length) {
        super(length, source.getArray());
        source.checkRange(offset, length);
        this.offset = source.getArrayOffset() + offset;
    }

    @Override
    public double get(int i) {
        this.checkIndex(i);
        return this.data[this.offset + i];
    }

    @Override
    public void set(int i, double value) {
        this.checkIndex(i);
        this.data[this.offset + i] = value;
    }

    @Override
    public double unsafeGet(int i) {
        return this.data[this.offset + i];
    }

    @Override
    public void unsafeSet(int i, double value) {
        this.data[this.offset + i] = value;
    }

    @Override
    public void add(AVector v) {
        this.checkSameLength(v);
        v.addToArray(this.data, this.offset);
    }

    @Override
    public void addMultiple(AVector v, double factor) {
        this.checkSameLength(v);
        v.addMultipleToArray(factor, 0, this.data, this.offset, this.length);
    }

    @Override
    public void addMultiple(ADenseArrayVector v, double factor) {
        this.checkSameLength(v);
        v.addMultipleToArray(factor, 0, this.data, this.offset, this.length);
    }

    @Override
    public void addAt(int i, double v) {
        assert (i >= 0 && i < this.length);
        double[] arrd = this.data;
        int n = i + this.offset;
        arrd[n] = arrd[n] + v;
    }

    @Override
    public int hashCode() {
        int hashCode = 1;
        for (int i = 0; i < this.length; ++i) {
            hashCode = 31 * hashCode + Hash.hashCode(this.data[this.offset + i]);
        }
        return hashCode;
    }

    @Override
    public int getArrayOffset() {
        return this.offset;
    }

    @Override
    public boolean isView() {
        return true;
    }

    @Override
    public AVector subVector(int start, int length) {
        int len = this.checkRange(start, length);
        if (length == 0) {
            return Vector0.INSTANCE;
        }
        if (len == length) {
            return this;
        }
        return ArraySubVector.wrap(this.data, this.offset + start, length);
    }

    @Override
    public ArraySubVector exactClone() {
        return new ArraySubVector((double[])this.data.clone(), this.offset, this.length);
    }

    @Override
    protected int index(int i) {
        return this.offset + i;
    }
}

