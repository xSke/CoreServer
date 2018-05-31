/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz.impl;

import java.nio.DoubleBuffer;
import mikera.arrayz.INDArray;
import mikera.vectorz.AVector;
import mikera.vectorz.Op;
import mikera.vectorz.impl.AJoinedVector;
import mikera.vectorz.impl.JoinedMultiVector;
import mikera.vectorz.util.VectorzException;

public final class JoinedVector
extends AJoinedVector {
    private static final long serialVersionUID = -5535850407701653222L;
    final AVector left;
    final AVector right;
    private final int split;

    private JoinedVector(AVector left, AVector right) {
        super(left.length() + right.length());
        this.left = left;
        this.right = right;
        this.split = left.length();
    }

    public static AVector joinVectors(AVector left, AVector right) {
        int ll = left.length();
        if (ll == 0) {
            return right;
        }
        int rl = right.length();
        if (rl == 0) {
            return left;
        }
        return new JoinedVector(left, right);
    }

    @Override
    public boolean isFullyMutable() {
        return this.left.isFullyMutable() && this.right.isFullyMutable();
    }

    @Override
    public boolean isZero() {
        return this.left.isZero() && this.right.isZero();
    }

    @Override
    public boolean isRangeZero(int start, int length) {
        int end = start + length;
        if (start >= this.split) {
            return this.right.isRangeZero(start - this.split, length);
        }
        if (end <= this.split) {
            return this.left.isRangeZero(start, length);
        }
        int ll = this.split - start;
        return this.left.isRangeZero(start, ll) && this.right.isRangeZero(0, length - ll);
    }

    @Override
    public void copyTo(AVector dest, int offset) {
        this.left.copyTo(dest, offset);
        this.right.copyTo(dest, offset + this.split);
    }

    @Override
    public void toDoubleBuffer(DoubleBuffer dest) {
        this.left.toDoubleBuffer(dest);
        this.right.toDoubleBuffer(dest);
    }

    @Override
    public void addToArray(int offset, double[] array, int arrayOffset, int length) {
        assert (arrayOffset + length <= array.length);
        assert (offset + length <= this.length());
        if (offset >= this.split) {
            this.right.addToArray(offset - this.split, array, arrayOffset, length);
        } else if (offset + length <= this.split) {
            this.left.addToArray(offset, array, arrayOffset, length);
        } else {
            this.left.addToArray(offset, array, arrayOffset, this.split - offset);
            this.right.addToArray(0, array, arrayOffset + (this.split - offset), length - (this.split - offset));
        }
    }

    @Override
    public void addToArray(double[] dest, int offset, int stride) {
        this.left.addToArray(dest, offset, stride);
        this.right.addToArray(dest, offset + this.split * stride, stride);
    }

    @Override
    public void addMultipleToArray(double factor, int offset, double[] array, int arrayOffset, int length) {
        assert (arrayOffset + length <= array.length);
        assert (offset + length <= this.length());
        if (offset >= this.split) {
            this.right.addMultipleToArray(factor, offset - this.split, array, arrayOffset, length);
        } else if (offset + length <= this.split) {
            this.left.addMultipleToArray(factor, offset, array, arrayOffset, length);
        } else {
            this.left.addMultipleToArray(factor, offset, array, arrayOffset, this.split - offset);
            this.right.addMultipleToArray(factor, 0, array, arrayOffset + (this.split - offset), length - (this.split - offset));
        }
    }

    @Override
    public void addAt(int i, double v) {
        if (i < this.split) {
            this.left.addAt(i, v);
        } else {
            this.right.addAt(i - this.split, v);
        }
    }

    @Override
    public void getElements(double[] data, int offset) {
        this.left.getElements(data, offset);
        this.right.getElements(data, offset + this.split);
    }

    @Override
    public void multiplyTo(double[] data, int offset) {
        this.left.multiplyTo(data, offset);
        this.right.multiplyTo(data, offset + this.split);
    }

    @Override
    public void divideTo(double[] data, int offset) {
        this.left.divideTo(data, offset);
        this.right.divideTo(data, offset + this.split);
    }

    @Override
    public void copyTo(int start, AVector dest, int destOffset, int length) {
        this.subVector(start, length).copyTo(dest, destOffset);
    }

    @Override
    public AVector subVector(int start, int length) {
        int len = this.checkRange(start, length);
        if (length == len) {
            return this;
        }
        if (start >= this.split) {
            return this.right.subVector(start - this.split, length);
        }
        if (start + length <= this.split) {
            return this.left.subVector(start, length);
        }
        int cut = this.split - start;
        AVector v1 = this.left.subVector(start, cut);
        AVector v2 = this.right.subVector(0, length - cut);
        return v1.join(v2);
    }

    @Override
    public AVector tryEfficientJoin(AVector a) {
        if (a instanceof JoinedVector) {
            return this.join((JoinedVector)a);
        }
        AVector ej = this.right.tryEfficientJoin(a);
        if (ej != null) {
            return new JoinedVector(this.left, ej);
        }
        return JoinedMultiVector.wrap(this.left, this.right, a);
    }

    public AVector join(JoinedVector a) {
        AVector ej = this.right.tryEfficientJoin(a.left);
        if (ej == null) {
            return JoinedMultiVector.wrap(this.left, this.right, a.left, a.right);
        }
        return JoinedMultiVector.wrap(this.left, ej, a.right);
    }

    @Override
    public void add(AVector a) {
        assert (this.length() == a.length());
        if (a instanceof JoinedVector) {
            this.add((JoinedVector)a);
        } else {
            this.add(a, 0);
        }
    }

    @Override
    public AVector addCopy(AVector a) {
        AVector lsum = this.left.addCopy(a.subVector(0, this.split));
        AVector rsum = this.right.addCopy(a.subVector(this.split, this.length - this.split));
        return lsum.join(rsum);
    }

    public void add(JoinedVector a) {
        if (this.split == a.split) {
            this.left.add(a.left);
            this.right.add(a.right);
        } else {
            this.add(a, 0);
        }
    }

    @Override
    public void scaleAdd(double factor, double constant) {
        this.left.scaleAdd(factor, constant);
        this.right.scaleAdd(factor, constant);
    }

    @Override
    public void add(double constant) {
        this.left.add(constant);
        this.right.add(constant);
    }

    @Override
    public void reciprocal() {
        this.left.reciprocal();
        this.right.reciprocal();
    }

    @Override
    public void clamp(double min, double max) {
        this.left.clamp(min, max);
        this.right.clamp(min, max);
    }

    @Override
    public double dotProduct(AVector v) {
        if (v instanceof JoinedVector) {
            JoinedVector jv = (JoinedVector)v;
            return this.dotProduct(jv);
        }
        return super.dotProduct(v);
    }

    @Override
    public double dotProduct(double[] data, int offset) {
        return this.left.dotProduct(data, offset) + this.right.dotProduct(data, offset + this.split);
    }

    public double dotProduct(JoinedVector jv) {
        if (jv.left.length() == this.left.length()) {
            return this.left.dotProduct(jv.left) + this.right.dotProduct(jv.right);
        }
        return super.dotProduct(jv);
    }

    @Override
    public void add(AVector a, int aOffset) {
        this.left.add(a, aOffset);
        this.right.add(a, aOffset + this.split);
    }

    @Override
    public void add(double[] data, int offset) {
        this.left.add(data, offset);
        this.right.add(data, offset + this.split);
    }

    @Override
    public void add(int offset, AVector a) {
        this.add(offset, a, 0, a.length());
    }

    @Override
    public void add(int offset, AVector a, int aOffset, int length) {
        if (offset >= this.split) {
            this.right.add(offset - this.split, a, aOffset, length);
        } else if (offset + length <= this.split) {
            this.left.add(offset, a, aOffset, length);
        } else {
            this.left.add(offset, a, aOffset, this.split - offset);
            this.right.add(0, a, aOffset + this.split - offset, length - (this.split - offset));
        }
    }

    @Override
    public void addMultiple(AVector a, double factor) {
        if (a instanceof JoinedVector) {
            this.addMultiple((JoinedVector)a, factor);
        } else {
            this.left.addMultiple(a, 0, factor);
            this.right.addMultiple(a, this.split, factor);
        }
    }

    public void addMultiple(JoinedVector a, double factor) {
        if (this.split == a.split) {
            this.left.addMultiple(a.left, factor);
            this.right.addMultiple(a.right, factor);
        } else {
            this.left.addMultiple((AVector)a, 0, factor);
            this.right.addMultiple((AVector)a, this.split, factor);
        }
    }

    @Override
    public void addMultiple(AVector a, int aOffset, double factor) {
        this.left.addMultiple(a, aOffset, factor);
        this.right.addMultiple(a, aOffset + this.split, factor);
    }

    @Override
    public void addProduct(AVector a, AVector b, double factor) {
        this.checkSameLength(a, b);
        this.left.addProduct(a, 0, b, 0, factor);
        this.right.addProduct(a, this.split, b, this.split, factor);
    }

    @Override
    public void addProduct(AVector a, int aOffset, AVector b, int bOffset, double factor) {
        this.left.addProduct(a, aOffset, b, bOffset, factor);
        this.right.addProduct(a, aOffset + this.split, b, bOffset + this.split, factor);
    }

    @Override
    public void signum() {
        this.left.signum();
        this.right.signum();
    }

    @Override
    public void abs() {
        this.left.abs();
        this.right.abs();
    }

    @Override
    public AVector absCopy() {
        return this.left.absCopy().join(this.right.absCopy());
    }

    @Override
    public void exp() {
        this.left.exp();
        this.right.exp();
    }

    @Override
    public void log() {
        this.left.log();
        this.right.log();
    }

    @Override
    public void negate() {
        this.left.negate();
        this.right.negate();
    }

    @Override
    public AVector negateCopy() {
        return this.left.negateCopy().join(this.right.negateCopy());
    }

    @Override
    public void applyOp(Op op) {
        this.left.applyOp(op);
        this.right.applyOp(op);
    }

    @Override
    public double elementSum() {
        return this.left.elementSum() + this.right.elementSum();
    }

    @Override
    public double elementProduct() {
        double r = this.left.elementProduct();
        if (r == 0.0) {
            return 0.0;
        }
        return r * this.right.elementProduct();
    }

    @Override
    public double elementMax() {
        return Math.max(this.left.elementMax(), this.right.elementMax());
    }

    @Override
    public double elementMin() {
        return Math.min(this.left.elementMin(), this.right.elementMin());
    }

    @Override
    public double magnitudeSquared() {
        return this.left.magnitudeSquared() + this.right.magnitudeSquared();
    }

    @Override
    public long nonZeroCount() {
        return this.left.nonZeroCount() + this.right.nonZeroCount();
    }

    @Override
    public double get(int i) {
        this.checkIndex(i);
        if (i < this.split) {
            return this.left.unsafeGet(i);
        }
        return this.right.unsafeGet(i - this.split);
    }

    @Override
    public void set(AVector src) {
        this.checkSameLength(src);
        this.set(src, 0);
    }

    @Override
    public double unsafeGet(int i) {
        if (i < this.split) {
            return this.left.unsafeGet(i);
        }
        return this.right.unsafeGet(i - this.split);
    }

    @Override
    public void set(AVector src, int srcOffset) {
        this.left.set(src, srcOffset);
        this.right.set(src, srcOffset + this.split);
    }

    @Override
    public void setElements(int pos, double[] values, int offset, int length) {
        int l1;
        int l0 = Math.min(length, this.split - pos);
        if (l0 > 0) {
            this.left.setElements(pos, values, offset, l0);
        }
        if ((l1 = Math.min(length, pos + length - this.split)) > 0) {
            this.right.setElements(pos + length - this.split - l1, values, offset + this.split, l1);
        }
    }

    @Override
    public void set(int i, double value) {
        this.checkIndex(i);
        if (i < this.split) {
            this.left.unsafeSet(i, value);
        } else {
            this.right.unsafeSet(i - this.split, value);
        }
    }

    @Override
    public void unsafeSet(int i, double value) {
        if (i < this.split) {
            this.left.unsafeSet(i, value);
        } else {
            this.right.unsafeSet(i - this.split, value);
        }
    }

    @Override
    public void fill(double value) {
        this.left.fill(value);
        this.right.fill(value);
    }

    @Override
    public void square() {
        this.left.square();
        this.right.square();
    }

    @Override
    public void sqrt() {
        this.left.sqrt();
        this.right.sqrt();
    }

    @Override
    public void tanh() {
        this.left.tanh();
        this.right.tanh();
    }

    @Override
    public void logistic() {
        this.left.logistic();
        this.right.logistic();
    }

    @Override
    public void multiply(double value) {
        this.left.multiply(value);
        this.right.multiply(value);
    }

    public static int depthCalc(AVector v) {
        if (v instanceof JoinedVector) {
            JoinedVector jv = (JoinedVector)v;
            return 1 + Math.max(JoinedVector.depthCalc(jv.left), JoinedVector.depthCalc(jv.right));
        }
        return 1;
    }

    public int depth() {
        return JoinedVector.depthCalc(this);
    }

    @Override
    public double[] toDoubleArray() {
        double[] data = new double[this.length];
        this.left.getElements(data, 0);
        this.right.getElements(data, this.split);
        return data;
    }

    @Override
    public boolean equals(AVector v) {
        if (v instanceof JoinedVector) {
            return this.equals((JoinedVector)v);
        }
        return super.equals(v);
    }

    public boolean equals(JoinedVector v) {
        if (this.split == v.split) {
            return this.left.equals(v.left) && this.right.equals(v.right);
        }
        return super.equals(v);
    }

    @Override
    public boolean equalsArray(double[] data, int offset) {
        if (!this.left.equalsArray(data, offset)) {
            return false;
        }
        if (!this.right.equalsArray(data, offset + this.split)) {
            return false;
        }
        return true;
    }

    @Override
    public JoinedVector exactClone() {
        return new JoinedVector(this.left.exactClone(), this.right.exactClone());
    }

    @Override
    public void validate() {
        if (this.left.tryEfficientJoin(this.right) != null) {
            throw new VectorzException("Should have used efficient join!");
        }
        super.validate();
    }

    @Override
    public int componentCount() {
        return 2;
    }

    @Override
    public AVector getComponent(int k) {
        return k <= 0 ? this.left : this.right;
    }

    @Override
    public JoinedVector withComponents(INDArray[] segments) {
        AVector left = segments[0].asVector();
        AVector right = segments[1].asVector();
        return new JoinedVector(left, right);
    }
}

