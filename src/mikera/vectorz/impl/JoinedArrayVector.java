/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import mikera.arrayz.INDArray;
import mikera.vectorz.AVector;
import mikera.vectorz.Op;
import mikera.vectorz.Vector;
import mikera.vectorz.Vectorz;
import mikera.vectorz.impl.ADenseArrayVector;
import mikera.vectorz.impl.AJoinedVector;
import mikera.vectorz.impl.ArraySubVector;
import mikera.vectorz.impl.Vector0;
import mikera.vectorz.util.DoubleArrays;
import mikera.vectorz.util.ErrorMessages;
import mikera.vectorz.util.VectorzException;

public final class JoinedArrayVector
extends AJoinedVector {
    private static final long serialVersionUID = -8470277860344236392L;
    private final int numArrays;
    private final double[][] data;
    private final int[] offsets;
    private final int[] pos;

    private JoinedArrayVector(int length, double[][] newData, int[] offsets, int[] pos) {
        super(length);
        this.numArrays = newData.length;
        this.offsets = offsets;
        this.pos = pos;
        this.data = newData;
    }

    public static final JoinedArrayVector create(AVector v) {
        int length = v.length();
        double[][] data = new double[][]{new double[length]};
        v.getElements(data[0], 0);
        JoinedArrayVector jav = new JoinedArrayVector(length, data, new int[1], new int[]{0, length});
        return jav;
    }

    public static JoinedArrayVector wrap(ADenseArrayVector v) {
        return new JoinedArrayVector(v.length(), new double[][]{v.getArray()}, new int[]{v.getArrayOffset()}, new int[]{0, v.length()});
    }

    public int numArrays() {
        return this.numArrays;
    }

    private int findArrayNum(int index) {
        assert (index >= 0 && index < this.length);
        int i = 0;
        int j = this.numArrays - 1;
        while (i < j) {
            int m = i + j >> 1;
            int p = this.pos[m];
            if (index < p) {
                j = m;
                continue;
            }
            int p2 = this.pos[m + 1];
            if (index >= p2) {
                i = m + 1;
                continue;
            }
            return m;
        }
        return i;
    }

    private int subLength(int j) {
        return this.pos[j + 1] - this.pos[j];
    }

    @Override
    public ArraySubVector getComponent(int i) {
        return ArraySubVector.wrap(this.data[i], this.offsets[i], this.subLength(i));
    }

    public List<ADenseArrayVector> toSubArrays() {
        ArrayList<ADenseArrayVector> al = new ArrayList<ADenseArrayVector>(this.numArrays);
        for (int i = 0; i < this.numArrays; ++i) {
            al.add(this.getComponent(i));
        }
        return al;
    }

    @Override
    public boolean isView() {
        return true;
    }

    @Override
    public boolean isFullyMutable() {
        return true;
    }

    @Override
    public double get(int i) {
        if (i < 0 || i >= this.length) {
            throw new IndexOutOfBoundsException(ErrorMessages.invalidIndex((INDArray)this, (long)i));
        }
        int ai = this.findArrayNum(i);
        return this.data[ai][i - this.pos[ai] + this.offsets[ai]];
    }

    @Override
    public double unsafeGet(int i) {
        int ai = this.findArrayNum(i);
        return this.data[ai][i - this.pos[ai] + this.offsets[ai]];
    }

    @Override
    public void set(int i, double value) {
        if (i < 0 || i >= this.length) {
            throw new IndexOutOfBoundsException(ErrorMessages.invalidIndex((INDArray)this, (long)i));
        }
        int ai = this.findArrayNum(i);
        this.data[ai][i - this.pos[ai] + this.offsets[ai]] = value;
    }

    @Override
    public void unsafeSet(int i, double value) {
        int ai = this.findArrayNum(i);
        this.data[ai][i - this.pos[ai] + this.offsets[ai]] = value;
    }

    @Override
    public void addAt(int i, double value) {
        int ai = this.findArrayNum(i);
        double[] arrd = this.data[ai];
        int n = i - this.pos[ai] + this.offsets[ai];
        arrd[n] = arrd[n] + value;
    }

    @Override
    public void copyTo(AVector dest, int offset) {
        for (int j = 0; j < this.numArrays; ++j) {
            dest.setRange(this.pos[j] + offset, this.data[j], this.offsets[j], this.subLength(j));
        }
    }

    @Override
    public double elementSum() {
        double result = 0.0;
        for (int j = 0; j < this.numArrays; ++j) {
            result += DoubleArrays.elementSum(this.data[j], this.offsets[j], this.subLength(j));
        }
        return result;
    }

    @Override
    public long nonZeroCount() {
        long result = 0L;
        for (int j = 0; j < this.numArrays; ++j) {
            result += (long)DoubleArrays.nonZeroCount(this.data[j], this.offsets[j], this.subLength(j));
        }
        return result;
    }

    @Override
    public double dotProduct(AVector v) {
        if (v instanceof ADenseArrayVector) {
            ADenseArrayVector av = (ADenseArrayVector)v;
            return this.dotProduct(av);
        }
        return super.dotProduct(v);
    }

    @Override
    public double dotProduct(ADenseArrayVector v) {
        double result = 0.0;
        double[] arr = v.getArray();
        int ao = v.getArrayOffset();
        for (int j = 0; j < this.numArrays; ++j) {
            result += DoubleArrays.dotProduct(this.data[j], this.offsets[j], arr, ao + this.pos[j], this.subLength(j));
        }
        return result;
    }

    @Override
    public double dotProduct(double[] arr, int offset) {
        double result = 0.0;
        for (int j = 0; j < this.numArrays; ++j) {
            result += DoubleArrays.dotProduct(this.data[j], this.offsets[j], arr, offset + this.pos[j], this.subLength(j));
        }
        return result;
    }

    @Override
    public void add(AVector a) {
        this.add(0, a, 0, this.length);
    }

    @Override
    public Vector addCopy(AVector a) {
        Vector r = this.toVector();
        r.add(a);
        return r;
    }

    @Override
    public Vector subCopy(AVector a) {
        Vector r = this.toVector();
        r.sub(a);
        return r;
    }

    @Override
    public void add(double[] srcData, int srcOffset) {
        for (int i = 0; i < this.numArrays; ++i) {
            DoubleArrays.add(srcData, srcOffset + this.pos[i], this.data[i], this.offsets[i], this.subLength(i));
        }
    }

    @Override
    public void add(int offset, AVector a) {
        this.add(offset, a, 0, a.length());
    }

    @Override
    public void add(int offset, AVector a, int aOffset, int length) {
        int alen = length;
        for (int j = 0; j < this.numArrays; ++j) {
            if (offset >= this.pos[j + 1]) continue;
            int segmentOffset = Math.max(0, offset - this.pos[j]);
            int len = Math.min(this.subLength(j) - segmentOffset, offset + alen - this.pos[j]);
            if (len <= 0) continue;
            a.addToArray(aOffset + this.pos[j] + segmentOffset - offset, this.data[j], this.offsets[j] + segmentOffset, len);
        }
    }

    @Override
    public void addToArray(int offset, double[] a, int aOffset, int length) {
        int alen = length;
        for (int j = 0; j < this.numArrays; ++j) {
            if (offset >= this.pos[j + 1]) continue;
            int segmentOffset = Math.max(0, offset - this.pos[j]);
            int len = Math.min(this.subLength(j) - segmentOffset, offset + alen - this.pos[j]);
            if (len <= 0) continue;
            DoubleArrays.add(this.data[j], this.offsets[j] + segmentOffset, a, aOffset + this.pos[j] + segmentOffset - offset, len);
        }
    }

    @Override
    public void addProduct(AVector a, AVector b, double factor) {
        this.addProduct(a, 0, b, 0, factor);
    }

    @Override
    public void addMultiple(AVector a, double factor) {
        this.addMultiple(0, a, 0, this.length(), factor);
    }

    @Override
    public void addMultiple(int offset, AVector a, int aOffset, int length, double factor) {
        int alen = length;
        for (int j = 0; j < this.numArrays; ++j) {
            if (offset >= this.pos[j + 1]) continue;
            int segmentOffset = Math.max(0, offset - this.pos[j]);
            int len = Math.min(this.subLength(j) - segmentOffset, offset + alen - this.pos[j]);
            if (len <= 0) continue;
            a.addMultipleToArray(factor, aOffset + this.pos[j] + segmentOffset - offset, this.data[j], this.offsets[j] + segmentOffset, len);
        }
    }

    @Override
    public void addMultipleToArray(double factor, int offset, double[] array, int arrayOffset, int length) {
        int startArray = this.findArrayNum(offset);
        int endPos = offset + length;
        int j = startArray;
        int sp = this.pos[j];
        while (sp < endPos) {
            int ep = this.pos[j + 1];
            int clen = Math.min(ep - sp, endPos - sp);
            double[] sa = this.data[j];
            int soffset = this.offsets[j];
            if (offset <= sp) {
                DoubleArrays.addMultiple(array, arrayOffset, sa, soffset, clen, factor);
            } else {
                int skip = offset - sp;
                assert (skip > 0);
                DoubleArrays.addMultiple(array, arrayOffset, sa, soffset + skip, clen -= skip, factor);
            }
            arrayOffset += clen;
            sp = this.pos[++j];
        }
    }

    @Override
    public void addProduct(AVector a, int aOffset, AVector b, int bOffset, double factor) {
        for (int j = 0; j < this.numArrays; ++j) {
            a.addProductToArray(factor, aOffset + this.pos[j], b, bOffset + this.pos[j], this.data[j], this.offsets[j], this.subLength(j));
        }
    }

    @Override
    public void applyOp(Op op) {
        for (int j = 0; j < this.numArrays; ++j) {
            op.applyTo(this.data[j], this.offsets[j], this.subLength(j));
        }
    }

    @Override
    public void getElements(double[] destArray, int offset) {
        for (int j = 0; j < this.numArrays; ++j) {
            System.arraycopy(this.data[j], this.offsets[j], destArray, offset + this.pos[j], this.subLength(j));
        }
    }

    @Override
    public void multiplyTo(double[] target, int offset) {
        for (int j = 0; j < this.numArrays; ++j) {
            DoubleArrays.arraymultiply(this.data[j], this.offsets[j], target, offset + this.pos[j], this.subLength(j));
        }
    }

    @Override
    public void divideTo(double[] target, int offset) {
        for (int j = 0; j < this.numArrays; ++j) {
            DoubleArrays.arraydivide(this.data[j], this.offsets[j], target, offset + this.pos[j], this.subLength(j));
        }
    }

    @Override
    public void fill(double value) {
        for (int j = 0; j < this.numArrays; ++j) {
            Arrays.fill(this.data[j], this.offsets[j], this.offsets[j] + this.subLength(j), value);
        }
    }

    @Override
    public void set(AVector v) {
        for (int j = 0; j < this.numArrays; ++j) {
            v.copyTo(this.pos[j], this.data[j], this.offsets[j], this.subLength(j));
        }
    }

    @Override
    public void multiply(double value) {
        for (int j = 0; j < this.numArrays; ++j) {
            DoubleArrays.multiply(this.data[j], this.offsets[j], this.subLength(j), value);
        }
    }

    @Override
    public void scaleAdd(double factor, double constant) {
        for (int j = 0; j < this.numArrays; ++j) {
            DoubleArrays.scaleAdd(this.data[j], this.offsets[j], this.subLength(j), factor, constant);
        }
    }

    @Override
    public void clamp(double min, double max) {
        for (int j = 0; j < this.numArrays; ++j) {
            DoubleArrays.clamp(this.data[j], this.offsets[j], this.subLength(j), min, max);
        }
    }

    @Override
    public void reciprocal() {
        for (int j = 0; j < this.numArrays; ++j) {
            DoubleArrays.reciprocal(this.data[j], this.offsets[j], this.subLength(j));
        }
    }

    @Override
    public void add(double value) {
        for (int j = 0; j < this.numArrays; ++j) {
            DoubleArrays.add(this.data[j], this.offsets[j], this.subLength(j), value);
        }
    }

    @Override
    public void abs() {
        for (int j = 0; j < this.numArrays; ++j) {
            DoubleArrays.abs(this.data[j], this.offsets[j], this.subLength(j));
        }
    }

    @Override
    public void exp() {
        for (int j = 0; j < this.numArrays; ++j) {
            DoubleArrays.exp(this.data[j], this.offsets[j], this.subLength(j));
        }
    }

    @Override
    public void log() {
        for (int j = 0; j < this.numArrays; ++j) {
            DoubleArrays.log(this.data[j], this.offsets[j], this.subLength(j));
        }
    }

    @Override
    public void signum() {
        for (int j = 0; j < this.numArrays; ++j) {
            DoubleArrays.signum(this.data[j], this.offsets[j], this.subLength(j));
        }
    }

    @Override
    public void square() {
        for (int j = 0; j < this.numArrays; ++j) {
            DoubleArrays.square(this.data[j], this.offsets[j], this.subLength(j));
        }
    }

    @Override
    public void tanh() {
        for (int j = 0; j < this.numArrays; ++j) {
            DoubleArrays.tanh(this.data[j], this.offsets[j], this.subLength(j));
        }
    }

    @Override
    public void logistic() {
        for (int j = 0; j < this.numArrays; ++j) {
            DoubleArrays.logistic(this.data[j], this.offsets[j], this.subLength(j));
        }
    }

    @Override
    public void sqrt() {
        for (int j = 0; j < this.numArrays; ++j) {
            DoubleArrays.sqrt(this.data[j], this.offsets[j], this.subLength(j));
        }
    }

    @Override
    public void negate() {
        for (int j = 0; j < this.numArrays; ++j) {
            DoubleArrays.negate(this.data[j], this.offsets[j], this.subLength(j));
        }
    }

    @Override
    public JoinedArrayVector exactClone() {
        double[][] newData = new double[this.numArrays][];
        int[] zeroOffsets = new int[this.numArrays];
        for (int i = 0; i < this.numArrays; ++i) {
            int alen = this.subLength(i);
            double[] arr = new double[alen];
            newData[i] = arr;
            System.arraycopy(this.data[i], this.offsets[i], arr, 0, alen);
        }
        return new JoinedArrayVector(this.length, newData, zeroOffsets, this.pos);
    }

    @Override
    public AVector subVector(int start, int length) {
        if (start < 0 || start + length > this.length) {
            throw new IndexOutOfBoundsException(ErrorMessages.invalidRange(this, start, length));
        }
        if (length == this.length) {
            return this;
        }
        if (length == 0) {
            return Vector0.INSTANCE;
        }
        int a = this.findArrayNum(start);
        int b = this.findArrayNum(start + length - 1);
        int n = b - a + 1;
        if (n == 1) {
            return Vectorz.wrap(this.data[a], this.offsets[a] + (start - this.pos[a]), length);
        }
        double[][] newData = (double[][])Arrays.copyOfRange(this.data, a, b + 1);
        int[] offs = new int[n];
        offs[0] = this.offsets[a] + (start - this.pos[a]);
        for (int j = 1; j < n; ++j) {
            offs[j] = this.offsets[a + j];
        }
        int[] poses = new int[n + 1];
        poses[0] = 0;
        for (int j = 1; j < n; ++j) {
            poses[j] = this.pos[a + j] - start;
        }
        poses[n] = length;
        return new JoinedArrayVector(length, newData, offs, poses);
    }

    @Override
    public AVector tryEfficientJoin(AVector v) {
        if (v instanceof JoinedArrayVector) {
            return JoinedArrayVector.joinVectors(this, (JoinedArrayVector)v);
        }
        if (v instanceof ADenseArrayVector) {
            return this.join((ADenseArrayVector)v);
        }
        return super.tryEfficientJoin(v);
    }

    public JoinedArrayVector join(ADenseArrayVector v) {
        int newLen = this.length + v.length();
        int[] newOffsets = new int[this.numArrays + 1];
        System.arraycopy(this.offsets, 0, newOffsets, 0, this.numArrays);
        newOffsets[this.numArrays] = v.getArrayOffset();
        int[] newPos = new int[this.numArrays + 2];
        System.arraycopy(this.pos, 0, newPos, 0, this.numArrays + 1);
        newPos[this.numArrays + 1] = newLen;
        double[][] newData = new double[this.numArrays + 1][];
        System.arraycopy(this.data, 0, newData, 0, this.numArrays);
        newData[this.numArrays] = v.getArray();
        return new JoinedArrayVector(newLen, newData, newOffsets, newPos);
    }

    public JoinedArrayVector join(JoinedArrayVector v) {
        return JoinedArrayVector.joinVectors(this, v);
    }

    public static JoinedArrayVector joinVectors(JoinedArrayVector a, JoinedArrayVector b) {
        int newLen = a.length + b.length();
        int naa = a.numArrays;
        int nab = b.numArrays;
        if (a.data[naa - 1] == b.data[0] && a.offsets[naa - 1] + (a.pos[naa] - a.pos[naa - 1]) == b.offsets[0]) {
            int[] newOffsets = new int[naa + nab - 1];
            System.arraycopy(a.offsets, 0, newOffsets, 0, naa);
            System.arraycopy(b.offsets, 1, newOffsets, naa, nab - 1);
            int[] newPos = new int[naa + nab - 1 + 1];
            System.arraycopy(a.pos, 0, newPos, 0, naa);
            System.arraycopy(b.pos, 1, newPos, naa, nab);
            int i = naa;
            while (i < newPos.length) {
                int[] arrn = newPos;
                int n = i++;
                arrn[n] = arrn[n] + a.length;
            }
            double[][] newData = new double[naa + nab - 1][];
            System.arraycopy(a.data, 0, newData, 0, naa);
            System.arraycopy(b.data, 1, newData, naa, nab - 1);
            return new JoinedArrayVector(newLen, newData, newOffsets, newPos);
        }
        int[] newOffsets = new int[naa + nab];
        System.arraycopy(a.offsets, 0, newOffsets, 0, naa);
        System.arraycopy(b.offsets, 0, newOffsets, naa, nab);
        int[] newPos = new int[naa + nab + 1];
        System.arraycopy(a.pos, 0, newPos, 0, naa);
        System.arraycopy(b.pos, 0, newPos, naa, nab + 1);
        int i = naa;
        while (i < newPos.length) {
            int[] arrn = newPos;
            int n = i++;
            arrn[n] = arrn[n] + a.length;
        }
        double[][] newData = new double[naa + nab][];
        System.arraycopy(a.data, 0, newData, 0, naa);
        System.arraycopy(b.data, 0, newData, naa, nab);
        return new JoinedArrayVector(newLen, newData, newOffsets, newPos);
    }

    public static AVector joinVectors(ADenseArrayVector a, ADenseArrayVector b) {
        if (a.getArray() == b.getArray() && a.getArrayOffset() + a.length() == b.getArrayOffset()) {
            return Vectorz.wrap(a.getArray(), a.getArrayOffset(), a.length() + b.length());
        }
        int alen = a.length();
        int blen = b.length();
        return new JoinedArrayVector(alen + blen, new double[][]{a.getArray(), b.getArray()}, new int[]{a.getArrayOffset(), b.getArrayOffset()}, new int[]{0, alen, alen + blen});
    }

    @Override
    public boolean equalsArray(double[] data, int offset) {
        for (int i = 0; i < this.numArrays; ++i) {
            if (DoubleArrays.equals(data, offset + this.pos[i], this.data[i], this.offsets[i], this.subLength(i))) continue;
            return false;
        }
        return true;
    }

    @Override
    public void validate() {
        if (this.length != this.pos[this.numArrays]) {
            throw new VectorzException("End position incorrect!?!");
        }
        for (int i = 0; i < this.numArrays; ++i) {
            this.getComponent(i).validate();
        }
        super.validate();
    }

    @Override
    public int componentCount() {
        return this.numArrays;
    }

    @Override
    public AJoinedVector withComponents(INDArray[] segments) {
        int sc = this.componentCount();
        double[][] newData = new double[sc][];
        int[] offs = (int[])this.offsets.clone();
        for (int i = 0; i < sc; ++i) {
            INDArray a = segments[i];
            if (a.dimensionality() != 1) {
                throw new IllegalArgumentException(ErrorMessages.incompatibleShape(a));
            }
            AVector v = a.asVector();
            v.checkLength(this.subLength(i));
            if (v instanceof ADenseArrayVector) {
                newData[i] = ((ADenseArrayVector)v).getArray();
                offs[i] = ((ADenseArrayVector)v).getArrayOffset();
                continue;
            }
            newData[i] = segments[i].toDoubleArray();
            offs[i] = 0;
        }
        return new JoinedArrayVector(this.length, newData, offs, this.pos);
    }
}

