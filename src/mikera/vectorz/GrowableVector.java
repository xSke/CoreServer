/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz;

import mikera.arrayz.INDArray;
import mikera.vectorz.AVector;
import mikera.vectorz.Vector;
import mikera.vectorz.Vectorz;
import mikera.vectorz.util.DoubleArrays;
import mikera.vectorz.util.ErrorMessages;
import mikera.vectorz.util.VectorzException;

public final class GrowableVector
extends AVector {
    private static final long serialVersionUID = -4560854157937758671L;
    private double[] data;
    private int count;

    public GrowableVector(AVector v) {
        this(v.length());
        this.append(v);
    }

    public GrowableVector(int initialCapacity) {
        this(new double[initialCapacity], 0);
    }

    public GrowableVector() {
        this(4);
    }

    public static GrowableVector ofInitialCapacity(int capacity) {
        return new GrowableVector(capacity);
    }

    private GrowableVector(double[] array, int length) {
        this.data = array;
        this.count = length;
    }

    @Override
    public int length() {
        return this.count;
    }

    public int currentCapacity() {
        return this.data.length;
    }

    public void ensureCapacity(int capacity) {
        int cc = this.currentCapacity();
        if (capacity <= cc) {
            return;
        }
        double[] newData = new double[Math.max(capacity + 5, cc * 2)];
        System.arraycopy(this.data, 0, newData, 0, this.count);
        this.data = newData;
    }

    @Override
    public double get(int i) {
        if (i < 0 || i >= this.count) {
            throw new IndexOutOfBoundsException(ErrorMessages.invalidIndex((INDArray)this, (long)i));
        }
        return this.data[i];
    }

    @Override
    public void set(int i, double value) {
        if (i < 0) {
            throw new IndexOutOfBoundsException(ErrorMessages.invalidIndex((INDArray)this, (long)i));
        }
        this.ensureCapacity(i + 1);
        this.data[i] = value;
    }

    @Override
    public double unsafeGet(int i) {
        return this.data[i];
    }

    @Override
    public void unsafeSet(int i, double value) {
        this.data[i] = value;
    }

    @Override
    public boolean isView() {
        return false;
    }

    public void append(double v) {
        this.ensureCapacity(this.count + 1);
        this.data[this.count++] = v;
    }

    public /* varargs */ void append(double ... vs) {
        int n = vs.length;
        this.ensureCapacity(this.count + n);
        System.arraycopy(vs, 0, this.data, this.count, n);
        this.count += n;
    }

    public void append(AVector v) {
        int vl = v.length();
        this.ensureCapacity(this.count + vl);
        v.getElements(this.data, this.count);
        this.count += vl;
    }

    public AVector build() {
        return Vectorz.create(this);
    }

    @Override
    public GrowableVector clone() {
        return new GrowableVector((double[])this.data.clone(), this.count);
    }

    public void clear() {
        this.count = 0;
    }

    @Override
    public GrowableVector exactClone() {
        GrowableVector g = new GrowableVector(this.data.length);
        g.append(this);
        return g;
    }

    @Override
    public void validate() {
        if (this.count > this.data.length) {
            throw new VectorzException("data array is wrong size!?!");
        }
        super.validate();
    }

    @Override
    public Vector toVector() {
        return Vector.create(this);
    }

    @Override
    public void getElements(double[] dest, int offset) {
        System.arraycopy(this.data, 0, dest, offset, this.count);
    }

    @Override
    public double dotProduct(double[] data, int offset) {
        return DoubleArrays.dotProduct(data, offset, this.data, 0, this.count);
    }
}

