/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz;

import mikera.arrayz.INDArray;
import mikera.vectorz.AVector;
import mikera.vectorz.impl.ABooleanVector;

public final class BitVector
extends ABooleanVector {
    private static final long serialVersionUID = 349277216077562294L;
    public static final double BIT_ON = 1.0;
    public static final double BIT_OFF = 0.0;
    public static final double BIT_THRESHOLD = 0.0;
    private final long[] data;

    public BitVector(int length) {
        super(length);
        this.data = new long[this.requiredArraySize(length)];
    }

    private BitVector(AVector source) {
        this(source.length());
        this.set(source);
    }

    private BitVector(BitVector source) {
        this((long[])source.data.clone(), source.length());
    }

    private BitVector(long[] data, int length) {
        super(length);
        this.data = data;
    }

    private int requiredArraySize(int length) {
        assert (length >= 0);
        return (length + 63) / 64;
    }

    public static BitVector createLength(int length) {
        return new BitVector(length);
    }

    public static BitVector create(AVector source) {
        return new BitVector(source);
    }

    private final boolean getBit(int i) {
        return (this.data[i >>> 6] >>> i % 64 & 1L) != 0L;
    }

    @Override
    public double unsafeGet(int i) {
        return this.getBit(i) ? 1.0 : 0.0;
    }

    @Override
    public double get(int i) {
        if (i < 0 || i >= this.length) {
            throw new IndexOutOfBoundsException("Index = " + i);
        }
        return this.getBit(i) ? 1.0 : 0.0;
    }

    @Override
    public boolean isFullyMutable() {
        return false;
    }

    @Override
    public boolean isView() {
        return false;
    }

    @Override
    public boolean isZero() {
        for (int i = 0; i < this.data.length; ++i) {
            if (this.data[i] == 0L) continue;
            return false;
        }
        return true;
    }

    @Override
    public double elementSum() {
        return this.nonZeroCount();
    }

    @Override
    public double elementMax() {
        if (this.length < 1) {
            return -1.7976931348623157E308;
        }
        return this.isZero() ? 0.0 : 1.0;
    }

    @Override
    public double magnitudeSquared() {
        return this.elementSum();
    }

    @Override
    public long nonZeroCount() {
        long result = 0L;
        for (int i = 0; i < this.data.length; ++i) {
            result += (long)Long.bitCount(this.data[i]);
        }
        return result;
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public double dotProduct(double[] data, int offset) {
        double result = 0.0;
        for (int i = 0; i < this.data.length; ++i) {
            long mask = this.data[i];
            for (int j = 0; j < 64 && mask != 0L; mask >>>= 1, ++j) {
                if ((mask & 1L) == 0L) continue;
                result += data[offset + j];
            }
            offset += 64;
        }
        return result;
    }

    @Override
    public double dotProduct(AVector v) {
        double result = 0.0;
        int offset = 0;
        for (int i = 0; i < this.data.length; ++i) {
            long mask = this.data[i];
            for (int j = 0; j < 64 && mask != 0L; mask >>>= 1, ++j) {
                if ((mask & 1L) == 0L) continue;
                result += v.unsafeGet(offset + j);
            }
            offset += 64;
        }
        return result;
    }

    @Override
    public void getElements(double[] data, int offset) {
        int len = this.length();
        for (int i = 0; i < len; ++i) {
            data[i + offset] = this.unsafeGet(i);
        }
    }

    @Override
    public void set(int i, double value) {
        if (i < 0 || i >= this.length) {
            throw new IndexOutOfBoundsException("Index: " + i);
        }
        int bit = i % 64;
        long mask = 1L << bit;
        int p = i >>> 6;
        this.data[p] = this.data[p] & (mask ^ -1L) | (value > 0.0 ? mask : 0L);
    }

    public static /* varargs */ BitVector of(double ... values) {
        int len = values.length;
        BitVector b = new BitVector(len);
        b.setElements(values);
        return b;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        int length = this.length();
        sb.append('[');
        if (length > 0) {
            sb.append(this.getBit(0) ? '1' : '0');
            for (int i = 1; i < length; ++i) {
                sb.append(',');
                sb.append(this.getBit(i) ? '1' : '0');
            }
        }
        sb.append(']');
        return sb.toString();
    }

    @Override
    public BitVector exactClone() {
        return new BitVector(this);
    }
}

