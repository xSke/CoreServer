/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz.impl;

import java.nio.DoubleBuffer;
import mikera.arrayz.INDArray;
import mikera.vectorz.AVector;
import mikera.vectorz.Op;
import mikera.vectorz.impl.ADenseArrayVector;
import mikera.vectorz.impl.AJoinedVector;
import mikera.vectorz.impl.JoinedVector;
import mikera.vectorz.impl.Vector0;
import mikera.vectorz.util.ErrorMessages;
import mikera.vectorz.util.IntArrays;
import mikera.vectorz.util.VectorzException;

public final class JoinedMultiVector
extends AJoinedVector {
    private static final long serialVersionUID = 6226205676178066609L;
    private final int n;
    private final AVector[] vecs;
    private final int[] splits;

    private JoinedMultiVector(int length, AVector[] vecs, int[] splits) {
        super(length);
        this.n = vecs.length;
        this.vecs = vecs;
        this.splits = splits;
    }

    private JoinedMultiVector(AVector[] vs) {
        this(JoinedMultiVector.sumOfLengths(vs), vs, new int[vs.length + 1]);
        int j = 0;
        for (int i = 0; i < this.n; ++i) {
            this.splits[i + 1] = j += vs[i].length();
        }
    }

    private static final int sumOfLengths(AVector[] vs) {
        int result = 0;
        for (AVector v : vs) {
            result += v.length();
        }
        return result;
    }

    @Override
    public boolean isFullyMutable() {
        for (AVector v : this.vecs) {
            if (v.isFullyMutable()) continue;
            return false;
        }
        return true;
    }

    @Override
    public void copyTo(AVector dest, int offset) {
        for (AVector v : this.vecs) {
            v.copyTo(dest, offset);
            offset += v.length();
        }
    }

    @Override
    public void toDoubleBuffer(DoubleBuffer dest) {
        for (AVector v : this.vecs) {
            v.toDoubleBuffer(dest);
        }
    }

    @Override
    public void addToArray(int offset, double[] array, int arrayOffset, int length) {
        int i2;
        int start = offset;
        int end = start + length;
        if (start < 0 || end > this.length || length < 0) {
            throw new IndexOutOfBoundsException(ErrorMessages.invalidRange(this, offset, length));
        }
        int i1 = IntArrays.indexLookup(this.splits, start);
        if (i1 == (i2 = IntArrays.indexLookup(this.splits, end - 1))) {
            this.vecs[i1].addToArray(offset -= this.splits[i1], array, arrayOffset, length);
            return;
        }
        this.vecs[i1].addToArray(offset - this.splits[i1], array, arrayOffset, this.splits[i1 + 1] - offset);
        this.vecs[i2].addToArray(0, array, arrayOffset + this.splits[i2] - offset, end - this.splits[i2]);
        for (int i = i1 + 1; i < i2; ++i) {
            int io = this.splits[i] - offset;
            this.vecs[i].addToArray(array, arrayOffset + io);
        }
    }

    @Override
    public void addToArray(double[] dest, int offset, int stride) {
        for (int i = 0; i < this.n; ++i) {
            this.vecs[i].addToArray(dest, offset + stride * this.splits[i], stride);
        }
    }

    @Override
    public void addMultipleToArray(double factor, int offset, double[] array, int arrayOffset, int length) {
        int i2;
        int start = offset;
        int end = start + length;
        if (start < 0 || end > this.length || length < 0) {
            throw new IndexOutOfBoundsException(ErrorMessages.invalidRange(this, offset, length));
        }
        int i1 = IntArrays.indexLookup(this.splits, start);
        if (i1 == (i2 = IntArrays.indexLookup(this.splits, end - 1))) {
            this.vecs[i1].addMultipleToArray(factor, offset -= this.splits[i1], array, arrayOffset, length);
            return;
        }
        this.vecs[i1].addMultipleToArray(factor, offset - this.splits[i1], array, arrayOffset, this.splits[i1 + 1] - offset);
        this.vecs[i2].addMultipleToArray(factor, 0, array, arrayOffset + this.splits[i2] - offset, end - this.splits[i2]);
        for (int i = i1 + 1; i < i2; ++i) {
            int io = this.splits[i] - offset;
            this.vecs[i].addMultipleToArray(factor, 0, array, arrayOffset + io, this.vecs[i].length());
        }
    }

    @Override
    public void addAt(int i, double v) {
        int j = IntArrays.indexLookup(this.splits, i);
        int joff = i - this.splits[j];
        this.vecs[j].addAt(joff, v);
    }

    @Override
    public void getElements(double[] data, int offset) {
        for (int i = 0; i < this.n; ++i) {
            this.vecs[i].getElements(data, offset + this.splits[i]);
        }
    }

    @Override
    public void multiplyTo(double[] data, int offset) {
        for (int i = 0; i < this.n; ++i) {
            this.vecs[i].multiplyTo(data, offset + this.splits[i]);
        }
    }

    @Override
    public void divideTo(double[] data, int offset) {
        for (int i = 0; i < this.n; ++i) {
            this.vecs[i].divideTo(data, offset + this.splits[i]);
        }
    }

    @Override
    public void copyTo(int start, AVector dest, int destOffset, int length) {
        this.subVector(start, length).copyTo(dest, destOffset);
    }

    @Override
    public AVector subVector(int start, int length) {
        int i2;
        int end = start + length;
        if (length == 0) {
            return Vector0.INSTANCE;
        }
        if (start < 0 || end > this.length || length < 0) {
            throw new IndexOutOfBoundsException(ErrorMessages.invalidRange(this, start, length));
        }
        if (length == this.length) {
            return this;
        }
        int i1 = IntArrays.indexLookup(this.splits, start);
        if (i1 == (i2 = IntArrays.indexLookup(this.splits, end - 1))) {
            return this.vecs[i1].subVector(start - this.splits[i1], length);
        }
        int nn = i2 - i1 + 1;
        AVector[] nvecs = new AVector[nn];
        nvecs[0] = this.vecs[i1].subVector(start - this.splits[i1], this.splits[i1 + 1] - start);
        nvecs[nn - 1] = this.vecs[i2].subVector(0, end - this.splits[i2]);
        for (int i = 1; i < i2 - i1; ++i) {
            nvecs[i] = this.vecs[i1 + i];
        }
        return new JoinedMultiVector(nvecs);
    }

    @Override
    public AVector tryEfficientJoin(AVector v) {
        if (v instanceof JoinedMultiVector) {
            return this.join((JoinedMultiVector)v);
        }
        if (v instanceof JoinedVector) {
            return this.join((JoinedVector)v);
        }
        AVector ej = this.vecs[this.n - 1].tryEfficientJoin(v);
        if (ej != null) {
            AVector[] nvecs = (AVector[])this.vecs.clone();
            nvecs[this.n - 1] = ej;
            return new JoinedMultiVector(nvecs);
        }
        AVector[] nvecs = new AVector[this.n + 1];
        System.arraycopy(this.vecs, 0, nvecs, 0, this.n);
        nvecs[this.n] = v;
        return new JoinedMultiVector(nvecs);
    }

    public AVector join(JoinedMultiVector v) {
        AVector[] nvecs = new AVector[this.n + v.n];
        System.arraycopy(this.vecs, 0, nvecs, 0, this.n);
        System.arraycopy(v.vecs, 0, nvecs, this.n, v.n);
        return new JoinedMultiVector(nvecs);
    }

    public AVector join(JoinedVector v) {
        AVector ej = this.vecs[this.n - 1].tryEfficientJoin(v.left);
        if (ej != null) {
            AVector[] nvecs = new AVector[this.n + 1];
            System.arraycopy(this.vecs, 0, nvecs, 0, this.n);
            nvecs[this.n - 1] = ej;
            nvecs[this.n] = v.right;
            return new JoinedMultiVector(nvecs);
        }
        AVector[] nvecs = new AVector[this.n + 2];
        System.arraycopy(this.vecs, 0, nvecs, 0, this.n);
        nvecs[this.n] = v.left;
        nvecs[this.n + 1] = v.right;
        return new JoinedMultiVector(nvecs);
    }

    @Override
    public void add(AVector a) {
        assert (this.length() == a.length());
        if (a instanceof JoinedMultiVector) {
            this.add((JoinedMultiVector)a);
        } else {
            this.add(a, 0);
        }
    }

    public void add(JoinedMultiVector a) {
        if (IntArrays.equals(this.splits, a.splits)) {
            for (int i = 0; i < this.n; ++i) {
                this.vecs[i].add(a.vecs[i]);
            }
        } else {
            this.add(a, 0);
        }
    }

    @Override
    public void scaleAdd(double factor, double constant) {
        for (int i = 0; i < this.n; ++i) {
            this.vecs[i].scaleAdd(factor, constant);
        }
    }

    @Override
    public void add(double constant) {
        for (int i = 0; i < this.n; ++i) {
            this.vecs[i].add(constant);
        }
    }

    @Override
    public void reciprocal() {
        for (int i = 0; i < this.n; ++i) {
            this.vecs[i].reciprocal();
        }
    }

    @Override
    public void clamp(double min, double max) {
        for (int i = 0; i < this.n; ++i) {
            this.vecs[i].clamp(min, max);
        }
    }

    @Override
    public double dotProduct(AVector v) {
        if (v instanceof ADenseArrayVector) {
            ADenseArrayVector av = (ADenseArrayVector)v;
            return this.dotProduct(av.getArray(), av.getArrayOffset());
        }
        return super.dotProduct(v);
    }

    @Override
    public double dotProduct(double[] data, int offset) {
        double result = 0.0;
        for (int i = 0; i < this.n; ++i) {
            result += this.vecs[i].dotProduct(data, offset + this.splits[i]);
        }
        return result;
    }

    @Override
    public void add(AVector a, int aOffset) {
        for (int i = 0; i < this.n; ++i) {
            this.vecs[i].add(a, aOffset + this.splits[i]);
        }
    }

    @Override
    public void add(double[] data, int offset) {
        for (int i = 0; i < this.n; ++i) {
            this.vecs[i].add(data, offset + this.splits[i]);
        }
    }

    @Override
    public void add(int offset, AVector a) {
        this.add(offset, a, 0, a.length());
    }

    @Override
    public void addMultiple(AVector a, double factor) {
        for (int i = 0; i < this.n; ++i) {
            this.vecs[i].addMultiple(a, this.splits[i], factor);
        }
    }

    @Override
    public void addMultiple(AVector a, int aOffset, double factor) {
        for (int i = 0; i < this.n; ++i) {
            this.vecs[i].addMultiple(a, aOffset + this.splits[i], factor);
        }
    }

    @Override
    public void addProduct(AVector a, AVector b, double factor) {
        for (int i = 0; i < this.n; ++i) {
            int off = this.splits[i];
            this.vecs[i].addProduct(a, off, b, off, factor);
        }
    }

    @Override
    public void addProduct(AVector a, int aOffset, AVector b, int bOffset, double factor) {
        for (int i = 0; i < this.n; ++i) {
            int off = this.splits[i];
            this.vecs[i].addProduct(a, aOffset + off, b, bOffset + off, factor);
        }
    }

    @Override
    public void signum() {
        for (int i = 0; i < this.n; ++i) {
            this.vecs[i].signum();
        }
    }

    @Override
    public void abs() {
        for (int i = 0; i < this.n; ++i) {
            this.vecs[i].abs();
        }
    }

    @Override
    public void exp() {
        for (int i = 0; i < this.n; ++i) {
            this.vecs[i].exp();
        }
    }

    @Override
    public void log() {
        for (int i = 0; i < this.n; ++i) {
            this.vecs[i].log();
        }
    }

    @Override
    public void negate() {
        for (int i = 0; i < this.n; ++i) {
            this.vecs[i].negate();
        }
    }

    @Override
    public void applyOp(Op op) {
        for (int i = 0; i < this.n; ++i) {
            this.vecs[i].applyOp(op);
        }
    }

    @Override
    public double elementSum() {
        double result = 0.0;
        for (int i = 0; i < this.n; ++i) {
            result += this.vecs[i].elementSum();
        }
        return result;
    }

    @Override
    public double elementProduct() {
        double result = 1.0;
        for (int i = 0; i < this.n; ++i) {
            if ((result *= this.vecs[i].elementProduct()) != 0.0) continue;
            return 0.0;
        }
        return result;
    }

    @Override
    public double elementMax() {
        double result = this.vecs[0].elementMax();
        for (int i = 0; i < this.n; ++i) {
            double m = this.vecs[i].elementMax();
            if (m <= result) continue;
            result = m;
        }
        return result;
    }

    @Override
    public double elementMin() {
        double result = this.vecs[0].elementMin();
        for (int i = 0; i < this.n; ++i) {
            double m = this.vecs[i].elementMin();
            if (m >= result) continue;
            result = m;
        }
        return result;
    }

    @Override
    public double magnitudeSquared() {
        double result = 0.0;
        for (int i = 0; i < this.n; ++i) {
            result += this.vecs[i].magnitudeSquared();
        }
        return result;
    }

    @Override
    public long nonZeroCount() {
        long result = 0L;
        for (int i = 0; i < this.n; ++i) {
            result += this.vecs[i].nonZeroCount();
        }
        return result;
    }

    @Override
    public double get(int i) {
        this.checkIndex(i);
        int j = IntArrays.indexLookup(this.splits, i);
        return this.vecs[j].unsafeGet(i - this.splits[j]);
    }

    @Override
    public void set(AVector src) {
        this.checkSameLength(src);
        this.set(src, 0);
    }

    @Override
    public double unsafeGet(int i) {
        int j = IntArrays.indexLookup(this.splits, i);
        return this.vecs[j].unsafeGet(i - this.splits[j]);
    }

    @Override
    public void set(AVector src, int srcOffset) {
        for (int i = 0; i < this.n; ++i) {
            this.vecs[i].set(src, srcOffset + this.splits[i]);
        }
    }

    @Override
    public void set(int i, double value) {
        this.checkIndex(i);
        this.unsafeSet(i, value);
    }

    @Override
    public void unsafeSet(int i, double value) {
        int j = IntArrays.indexLookup(this.splits, i);
        this.vecs[j].unsafeSet(i - this.splits[j], value);
    }

    @Override
    public void fill(double value) {
        for (int i = 0; i < this.n; ++i) {
            this.vecs[i].fill(value);
        }
    }

    @Override
    public void square() {
        for (int i = 0; i < this.n; ++i) {
            this.vecs[i].square();
        }
    }

    @Override
    public void sqrt() {
        for (int i = 0; i < this.n; ++i) {
            this.vecs[i].sqrt();
        }
    }

    @Override
    public void tanh() {
        for (int i = 0; i < this.n; ++i) {
            this.vecs[i].tanh();
        }
    }

    @Override
    public void logistic() {
        for (int i = 0; i < this.n; ++i) {
            this.vecs[i].logistic();
        }
    }

    @Override
    public void multiply(double value) {
        for (int i = 0; i < this.n; ++i) {
            this.vecs[i].multiply(value);
        }
    }

    @Override
    public double[] toDoubleArray() {
        double[] data = new double[this.length];
        for (int i = 0; i < this.n; ++i) {
            this.vecs[i].copyTo(data, this.splits[i]);
        }
        return data;
    }

    @Override
    public boolean equals(AVector v) {
        if (v instanceof JoinedMultiVector) {
            return this.equals((JoinedMultiVector)v);
        }
        if (v instanceof ADenseArrayVector) {
            ADenseArrayVector av = (ADenseArrayVector)v;
            return this.equalsArray(av.getArray(), av.getArrayOffset());
        }
        return super.equals(v);
    }

    public boolean equals(JoinedMultiVector v) {
        if (IntArrays.equals(this.splits, v.splits)) {
            for (int i = 0; i < this.n; ++i) {
                if (this.vecs[i].equals(v.vecs[i])) continue;
                return false;
            }
        }
        return super.equals(v);
    }

    @Override
    public boolean equalsArray(double[] data, int offset) {
        for (int i = 0; i < this.n; ++i) {
            if (this.vecs[i].equalsArray(data, offset + this.splits[i])) continue;
            return false;
        }
        return true;
    }

    @Override
    public JoinedMultiVector exactClone() {
        AVector[] nvecs = new AVector[this.n];
        for (int i = 0; i < this.n; ++i) {
            nvecs[i] = this.vecs[i].exactClone();
        }
        return new JoinedMultiVector(nvecs);
    }

    public static /* varargs */ AVector wrap(AVector ... vecs) {
        return new JoinedMultiVector(vecs);
    }

    public static /* varargs */ AVector create(AVector ... vecs) {
        return new JoinedMultiVector((AVector[])vecs.clone());
    }

    @Override
    public void validate() {
        super.validate();
        if (this.splits[this.n] != this.length) {
            throw new VectorzException("Unexpected final slit position - not equal to JoinedMultVector length");
        }
    }

    @Override
    public int componentCount() {
        return this.n;
    }

    @Override
    public AVector getComponent(int k) {
        return this.vecs[k];
    }

    @Override
    public AJoinedVector withComponents(INDArray[] segments) {
        AVector[] nvecs = new AVector[this.n];
        for (int i = 0; i < this.n; ++i) {
            AVector v;
            INDArray a = segments[i];
            if (!this.isSameShape(a)) {
                throw new IllegalArgumentException(ErrorMessages.incompatibleShapes(this, a));
            }
            nvecs[i] = v = segments[i].asVector();
        }
        return new JoinedMultiVector(this.length, nvecs, this.splits);
    }
}

