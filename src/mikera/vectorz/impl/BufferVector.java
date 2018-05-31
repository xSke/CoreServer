/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz.impl;

import java.nio.Buffer;
import java.nio.DoubleBuffer;
import mikera.arrayz.INDArray;
import mikera.vectorz.AVector;
import mikera.vectorz.impl.ASizedVector;
import mikera.vectorz.impl.Vector0;

public class BufferVector
extends ASizedVector {
    private static final long serialVersionUID = -24132234222851156L;
    final DoubleBuffer buffer;

    protected BufferVector(int length) {
        this(DoubleBuffer.allocate(length), length);
    }

    protected BufferVector(DoubleBuffer buf, int length) {
        super(length);
        this.buffer = buf;
    }

    public static BufferVector wrap(double[] source) {
        return new BufferVector(DoubleBuffer.wrap(source), source.length);
    }

    public static BufferVector wrap(DoubleBuffer source, int length) {
        return new BufferVector(source, length);
    }

    public static BufferVector create(AVector v) {
        return BufferVector.wrap(v.toDoubleArray());
    }

    public static BufferVector createLength(int length) {
        return new BufferVector(length);
    }

    @Override
    public double get(int i) {
        return this.buffer.get(i);
    }

    @Override
    public void set(int i, double value) {
        this.buffer.put(i, value);
    }

    @Override
    public double unsafeGet(int i) {
        return this.buffer.get(i);
    }

    @Override
    public void unsafeSet(int i, double value) {
        this.buffer.put(i, value);
    }

    @Override
    public boolean isFullyMutable() {
        return true;
    }

    @Override
    public AVector subVector(int offset, int length) {
        this.checkRange(offset, length);
        if (length == 0) {
            return Vector0.INSTANCE;
        }
        if (length == this.length) {
            return this;
        }
        this.buffer.position(offset);
        this.buffer.limit(offset + length);
        DoubleBuffer newBuffer = this.buffer.slice();
        this.buffer.clear();
        return BufferVector.wrap(newBuffer, length);
    }

    @Override
    public void getElements(double[] dest, int offset) {
        this.buffer.get(dest, offset, this.length);
        this.buffer.clear();
    }

    @Override
    public BufferVector exactClone() {
        double[] newArray = new double[this.length];
        this.buffer.get(newArray);
        this.buffer.clear();
        return BufferVector.wrap(newArray);
    }

    @Override
    public double dotProduct(double[] data, int offset) {
        double result = 0.0;
        for (int i = 0; i < this.length; ++i) {
            result += data[offset + i] * this.buffer.get(i);
        }
        return result;
    }
}

