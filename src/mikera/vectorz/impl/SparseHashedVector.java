/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import mikera.arrayz.INDArray;
import mikera.indexz.Index;
import mikera.matrixx.AMatrix;
import mikera.vectorz.AVector;
import mikera.vectorz.Vector;
import mikera.vectorz.impl.ADenseArrayVector;
import mikera.vectorz.impl.ASparseVector;
import mikera.vectorz.impl.SparseIndexedVector;
import mikera.vectorz.util.ErrorMessages;
import mikera.vectorz.util.VectorzException;

public class SparseHashedVector
extends ASparseVector {
    private static final long serialVersionUID = 750093598603613879L;
    private HashMap<Integer, Double> hash;

    private SparseHashedVector(int length) {
        this(length, new HashMap<Integer, Double>());
    }

    private SparseHashedVector(int length, HashMap<Integer, Double> hashMap) {
        super(length);
        if (length <= 0) {
            throw new IllegalArgumentException("Can't create SparseHashedVector of length " + length);
        }
        this.hash = hashMap;
    }

    public static SparseHashedVector create(AVector v) {
        int n = v.length();
        HashMap<Integer, Double> hm = new HashMap<Integer, Double>();
        for (int i = 0; i < n; ++i) {
            double val = v.unsafeGet(i);
            if (val == 0.0) continue;
            hm.put(i, val);
        }
        return new SparseHashedVector(n, hm);
    }

    public static SparseHashedVector create(int length, Index index, Vector values) {
        int n = index.length();
        if (values.length() != n) {
            throw new IllegalArgumentException("Mismatched values length: " + values.length());
        }
        HashMap<Integer, Double> hm = new HashMap<Integer, Double>();
        for (int i = 0; i < n; ++i) {
            double v = values.get(i);
            if (v == 0.0) continue;
            hm.put(index.get(i), v);
        }
        return new SparseHashedVector(length, hm);
    }

    public static SparseHashedVector createLength(int length) {
        return new SparseHashedVector(length);
    }

    public static AVector createFromRow(AMatrix m, int row) {
        return SparseHashedVector.create(m.getRow(row));
    }

    @Override
    public int nonSparseElementCount() {
        return this.hash.size();
    }

    @Override
    public boolean isZero() {
        return this.hash.size() == 0;
    }

    @Override
    public boolean isElementConstrained() {
        return false;
    }

    @Override
    public double get(int i) {
        this.checkIndex(i);
        return this.unsafeGet(i);
    }

    @Override
    public double unsafeGet(int i) {
        Double d = this.hash.get(i);
        if (d != null) {
            return d;
        }
        return 0.0;
    }

    @Override
    public boolean isFullyMutable() {
        return true;
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public long nonZeroCount() {
        return this.hash.size();
    }

    @Override
    public void multiply(double d) {
        if (d == 1.0) {
            return;
        }
        if (d == 0.0) {
            this.hash.clear();
            return;
        }
        for (Map.Entry<Integer, Double> e : this.hash.entrySet()) {
            double r = e.getValue() * d;
            e.setValue(r);
        }
    }

    @Override
    public double dotProduct(AVector v) {
        v.checkLength(this.length);
        double result = 0.0;
        Iterator<Integer> iterator = this.hash.keySet().iterator();
        while (iterator.hasNext()) {
            int i = iterator.next();
            result += this.hash.get(i) * v.unsafeGet(i);
        }
        return result;
    }

    @Override
    public double dotProduct(double[] data, int offset) {
        double result = 0.0;
        for (Integer i : this.hash.keySet()) {
            result += this.hash.get(i) * data[offset + i];
        }
        return result;
    }

    @Override
    public void addMultipleToArray(double factor, int offset, double[] array, int arrayOffset, int length) {
        int aOffset = arrayOffset - offset;
        for (Integer i : this.hash.keySet()) {
            if (i < offset || i >= offset + length) continue;
            double[] arrd = array;
            int n = aOffset + i;
            arrd[n] = arrd[n] + factor * this.hash.get(i);
        }
    }

    @Override
    public void addToArray(int offset, double[] array, int arrayOffset, int length) {
        int aOffset = arrayOffset - offset;
        Iterator<Integer> iterator = this.hash.keySet().iterator();
        while (iterator.hasNext()) {
            int i = iterator.next();
            if (i < offset || i >= offset + length) continue;
            double[] arrd = array;
            int n = aOffset + i;
            arrd[n] = arrd[n] + this.hash.get(i);
        }
    }

    @Override
    public void addToArray(double[] dest, int offset, int stride) {
        for (Map.Entry<Integer, Double> e : this.hash.entrySet()) {
            int i = e.getKey();
            double[] arrd = dest;
            int n = offset + i * stride;
            arrd[n] = arrd[n] + e.getValue();
        }
    }

    @Override
    public void addProductToArray(double factor, int offset, AVector other, int otherOffset, double[] array, int arrayOffset, int length) {
        int aOffset = arrayOffset - offset;
        int oOffset = otherOffset - offset;
        for (Map.Entry<Integer, Double> e : this.hash.entrySet()) {
            Integer io = e.getKey();
            int i = io;
            if (i < offset || i >= offset + length) continue;
            double[] arrd = array;
            int n = aOffset + i;
            arrd[n] = arrd[n] + factor * e.getValue() * other.get(i + oOffset);
        }
    }

    @Override
    public void addProductToArray(double factor, int offset, ADenseArrayVector other, int otherOffset, double[] array, int arrayOffset, int length) {
        int aOffset = arrayOffset - offset;
        int oArrayOffset = other.getArrayOffset() + otherOffset - offset;
        double[] oArray = other.getArray();
        for (Map.Entry<Integer, Double> e : this.hash.entrySet()) {
            double ov;
            Integer io = e.getKey();
            int i = io;
            if (i < offset || i >= offset + length || (ov = oArray[i + oArrayOffset]) == 0.0) continue;
            double[] arrd = array;
            int n = aOffset + i;
            arrd[n] = arrd[n] + factor * e.getValue() * ov;
        }
    }

    @Override
    public void getElements(double[] array, int offset) {
        Arrays.fill(array, offset, offset + this.length, 0.0);
        this.copySparseValuesTo(array, offset);
    }

    public void copySparseValuesTo(double[] array, int offset) {
        for (Map.Entry<Integer, Double> e : this.hash.entrySet()) {
            int i = e.getKey();
            array[offset + i] = e.getValue();
        }
    }

    @Override
    public void copyTo(AVector v, int offset) {
        if (v instanceof ADenseArrayVector) {
            ADenseArrayVector av = (ADenseArrayVector)v;
            this.getElements(av.getArray(), av.getArrayOffset() + offset);
        }
        v.fillRange(offset, this.length, 0.0);
        for (Map.Entry e : this.hash.entrySet()) {
            int i = (Integer)e.getKey();
            v.unsafeSet(offset + i, (Double)e.getValue());
        }
    }

    @Override
    public void set(int i, double value) {
        this.checkIndex(i);
        if (value != 0.0) {
            this.hash.put(i, value);
        } else {
            this.hash.remove(i);
        }
    }

    @Override
    public void set(AVector v) {
        if (v instanceof SparseHashedVector) {
            this.set((SparseHashedVector)v);
            return;
        }
        v.checkLength(this.length);
        this.hash = new HashMap();
        for (int i = 0; i < this.length; ++i) {
            double val = v.unsafeGet(i);
            if (val == 0.0) continue;
            this.hash.put(i, val);
        }
    }

    public void set(SparseHashedVector v) {
        v.checkLength(this.length);
        this.hash = (HashMap)v.hash.clone();
    }

    @Override
    public void unsafeSet(int i, double value) {
        if (value != 0.0) {
            this.hash.put(i, value);
        } else {
            this.hash.remove(i);
        }
    }

    private void unsafeSetInteger(Integer i, double value) {
        if (value != 0.0) {
            this.hash.put(i, value);
        } else {
            this.hash.remove(i);
        }
    }

    @Override
    public void addAt(int i, double value) {
        Integer ind = i;
        this.unsafeSetInteger(ind, value + this.unsafeGet(ind));
    }

    @Override
    public double maxAbsElement() {
        double result = 0.0;
        for (Map.Entry<Integer, Double> e : this.hash.entrySet()) {
            double d = Math.abs(e.getValue());
            if (d <= result) continue;
            result = d;
        }
        return result;
    }

    @Override
    public double elementMax() {
        double result = -1.7976931348623157E308;
        for (Map.Entry<Integer, Double> e : this.hash.entrySet()) {
            double d = e.getValue();
            if (d <= result) continue;
            result = d;
        }
        if (result < 0.0 && this.hash.size() < this.length) {
            return 0.0;
        }
        return result;
    }

    @Override
    public double elementMin() {
        double result = Double.MAX_VALUE;
        for (Map.Entry<Integer, Double> e : this.hash.entrySet()) {
            double d = e.getValue();
            if (d >= result) continue;
            result = d;
        }
        if (result > 0.0 && this.hash.size() < this.length) {
            return 0.0;
        }
        return result;
    }

    @Override
    public int maxElementIndex() {
        if (this.hash.size() == 0) {
            return 0;
        }
        int ind = 0;
        double result = -1.7976931348623157E308;
        for (Map.Entry<Integer, Double> e : this.hash.entrySet()) {
            double d = e.getValue();
            if (d <= result) continue;
            result = d;
            ind = e.getKey();
        }
        if (result < 0.0 && this.hash.size() < this.length) {
            return this.sparseElementIndex();
        }
        return ind;
    }

    @Override
    public int maxAbsElementIndex() {
        if (this.hash.size() == 0) {
            return 0;
        }
        int ind = 0;
        double result = this.unsafeGet(0);
        for (Map.Entry<Integer, Double> e : this.hash.entrySet()) {
            double d = Math.abs(e.getValue());
            if (d <= result) continue;
            result = d;
            ind = e.getKey();
        }
        return ind;
    }

    @Override
    public int minElementIndex() {
        if (this.hash.size() == 0) {
            return 0;
        }
        int ind = 0;
        double result = Double.MAX_VALUE;
        for (Map.Entry<Integer, Double> e : this.hash.entrySet()) {
            double d = e.getValue();
            if (d >= result) continue;
            result = d;
            ind = e.getKey();
        }
        if (result > 0.0 && this.hash.size() < this.length) {
            return this.sparseElementIndex();
        }
        return ind;
    }

    private int sparseElementIndex() {
        if (this.hash.size() == this.length) {
            return -1;
        }
        for (int i = 0; i < this.length; ++i) {
            if (this.hash.containsKey(i)) continue;
            return i;
        }
        throw new VectorzException(ErrorMessages.impossible());
    }

    @Override
    public double elementSum() {
        double result = 0.0;
        for (Map.Entry<Integer, Double> e : this.hash.entrySet()) {
            double d = e.getValue();
            result += d;
        }
        return result;
    }

    @Override
    public double magnitudeSquared() {
        double result = 0.0;
        for (Map.Entry<Integer, Double> e : this.hash.entrySet()) {
            double d = e.getValue();
            result += d * d;
        }
        return result;
    }

    @Override
    public Vector nonSparseValues() {
        int n = this.hash.size();
        double[] vs = new double[n];
        Index index = this.nonSparseIndex();
        for (int i = 0; i < n; ++i) {
            vs[i] = this.hash.get(index.get(i));
        }
        return Vector.wrap(vs);
    }

    @Override
    public int[] nonZeroIndices() {
        int n = this.hash.size();
        int[] ret = new int[n];
        int di = 0;
        for (Map.Entry<Integer, Double> e : this.hash.entrySet()) {
            ret[di++] = e.getKey();
        }
        Arrays.sort(ret);
        return ret;
    }

    @Override
    public Index nonSparseIndex() {
        int n = this.hash.size();
        int[] in = new int[n];
        int di = 0;
        for (Map.Entry<Integer, Double> e : this.hash.entrySet()) {
            in[di++] = e.getKey();
        }
        Index result = Index.wrap(in);
        result.sort();
        return result;
    }

    @Override
    public boolean includesIndex(int i) {
        return this.hash.containsKey(i);
    }

    @Override
    public void add(ASparseVector v) {
        Index ind = v.nonSparseIndex();
        int n = ind.length();
        for (int i = 0; i < n; ++i) {
            int ii = ind.get(i);
            this.addAt(ii, v.unsafeGet(ii));
        }
    }

    @Override
    public boolean equalsArray(double[] data, int offset) {
        for (int i = 0; i < this.length; ++i) {
            Double d;
            double v = data[offset + i];
            if (!(v == 0.0 ? this.hash.containsKey(i) : (d = this.hash.get(i)) == null || d != v)) continue;
            return false;
        }
        return true;
    }

    @Override
    public SparseIndexedVector clone() {
        return this.sparseClone();
    }

    @Override
    public SparseHashedVector exactClone() {
        return new SparseHashedVector(this.length, (HashMap)this.hash.clone());
    }

    @Override
    public SparseIndexedVector sparseClone() {
        return SparseIndexedVector.create(this);
    }

    @Override
    public void validate() {
        if (this.length <= 0) {
            throw new VectorzException("Illegal length: " + this.length);
        }
        for (Map.Entry<Integer, Double> e : this.hash.entrySet()) {
            int i = e.getKey();
            if (i < 0 || i >= this.length) {
                throw new VectorzException(ErrorMessages.invalidIndex((INDArray)this, (long)i));
            }
            if (e.getValue() != 0.0) continue;
            throw new VectorzException("Unexpected zero at index: " + i);
        }
        super.validate();
    }
}

