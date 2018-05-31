/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.impl;

import java.util.ArrayList;
import java.util.List;
import mikera.arrayz.INDArray;
import mikera.matrixx.AMatrix;
import mikera.matrixx.Matrix;
import mikera.matrixx.impl.ARectangularMatrix;
import mikera.matrixx.impl.SparseRowMatrix;
import mikera.vectorz.AVector;
import mikera.vectorz.Op;
import mikera.vectorz.Vector;
import mikera.vectorz.Vectorz;
import mikera.vectorz.impl.RepeatedElementVector;
import mikera.vectorz.impl.SparseIndexedVector;
import mikera.vectorz.util.VectorzException;

public abstract class ASparseRCMatrix
extends ARectangularMatrix {
    private static final long serialVersionUID = -4153075712517555814L;
    protected final AVector[] data;

    protected ASparseRCMatrix(int rows, int cols, AVector[] data) {
        super(rows, cols);
        this.data = data;
    }

    protected void unsafeSetVec(int i, AVector vec) {
        this.data[i] = vec;
    }

    public AVector unsafeGetVector(int i) {
        return this.data[i];
    }

    @Override
    public boolean isSparse() {
        return true;
    }

    @Override
    public void fill(double value) {
        RepeatedElementVector v = RepeatedElementVector.create(this.lineLength(), value);
        long n = this.componentCount();
        int i = 0;
        while ((long)i < n) {
            this.unsafeSetVec(i, v);
            ++i;
        }
    }

    @Override
    public void reciprocal() {
        RepeatedElementVector rr = RepeatedElementVector.create(this.lineLength(), Double.POSITIVE_INFINITY);
        long n = this.componentCount();
        int i = 0;
        while ((long)i < n) {
            AVector line = this.data[i];
            if (line == null) {
                this.data[i] = rr;
            } else {
                if (!line.isFullyMutable()) {
                    this.data[i] = line = line.sparseClone();
                }
                line.reciprocal();
            }
            ++i;
        }
    }

    @Override
    public void abs() {
        long n = this.componentCount();
        int i = 0;
        while ((long)i < n) {
            AVector line = this.data[i];
            if (line != null) {
                if (!line.isFullyMutable()) {
                    this.data[i] = line = line.absCopy();
                } else {
                    line.abs();
                }
            }
            ++i;
        }
    }

    @Override
    public void pow(double exponent) {
        long n = this.componentCount();
        int i = 0;
        while ((long)i < n) {
            AVector line = this.data[i];
            if (line != null) {
                if (!line.isFullyMutable()) {
                    this.data[i] = line = line.sparseClone();
                }
                line.pow(exponent);
            }
            ++i;
        }
    }

    @Override
    public void square() {
        long n = this.componentCount();
        int i = 0;
        while ((long)i < n) {
            AVector line = this.data[i];
            if (line != null) {
                if (!line.isFullyMutable()) {
                    this.data[i] = line = line.sparseClone();
                }
                line.square();
            }
            ++i;
        }
    }

    @Override
    public void sqrt() {
        long n = this.componentCount();
        int i = 0;
        while ((long)i < n) {
            AVector line = this.data[i];
            if (line != null) {
                if (!line.isFullyMutable()) {
                    this.data[i] = line = line.sparseClone();
                }
                line.sqrt();
            }
            ++i;
        }
    }

    @Override
    public void exp() {
        RepeatedElementVector rr = RepeatedElementVector.create(this.lineLength(), 1.0);
        long n = this.componentCount();
        int i = 0;
        while ((long)i < n) {
            AVector line = this.data[i];
            if (line == null) {
                this.data[i] = rr;
            } else {
                if (!line.isFullyMutable()) {
                    this.data[i] = line = line.sparseClone();
                }
                line.exp();
            }
            ++i;
        }
    }

    @Override
    public void log() {
        RepeatedElementVector rr = RepeatedElementVector.create(this.lineLength(), Math.log(0.0));
        long n = this.componentCount();
        int i = 0;
        while ((long)i < n) {
            AVector line = this.data[i];
            if (line == null) {
                this.data[i] = rr;
            } else {
                if (!line.isFullyMutable()) {
                    this.data[i] = line = line.sparseClone();
                }
                line.log();
            }
            ++i;
        }
    }

    @Override
    public final boolean isMutable() {
        return true;
    }

    @Override
    public final boolean isFullyMutable() {
        return true;
    }

    @Override
    public final boolean isZero() {
        for (AVector vec : this.data) {
            if (vec == null || vec.isZero()) continue;
            return false;
        }
        return true;
    }

    @Override
    public AMatrix innerProduct(AMatrix a) {
        return SparseRowMatrix.innerProduct((AMatrix)this, a);
    }

    @Override
    public AVector innerProduct(AVector a) {
        return SparseRowMatrix.innerProduct((AMatrix)this, a);
    }

    @Override
    public AVector getRowClone(int row) {
        return this.getRow(row).sparseClone();
    }

    @Override
    public AVector getColumnClone(int column) {
        return this.getColumn(column).sparseClone();
    }

    @Override
    public void copyRowTo(int i, double[] dest, int destOffset) {
        this.getRow(i).copyTo(dest, destOffset);
    }

    @Override
    public void copyColumnTo(int j, double[] dest, int destOffset) {
        this.getColumn(j).copyTo(dest, destOffset);
    }

    public List<AVector> getRotatedData(int outerLen, int innerLen) {
        int i;
        int numVecs = outerLen;
        int numElems = innerLen;
        ArrayList rotIndexList = new ArrayList(numElems);
        ArrayList rotValueList = new ArrayList(numElems);
        ArrayList<AVector> rotList = new ArrayList<AVector>(numElems);
        AVector emptyRotVec = Vectorz.createZeroVector(numVecs);
        for (i = 0; i < numElems; ++i) {
            rotIndexList.add(new ArrayList());
            rotValueList.add(new ArrayList());
        }
        for (i = 0; i < numVecs; ++i) {
            AVector vec = this.unsafeGetVector(i);
            if (null == vec) continue;
            int[] nonZeroIdxs = vec.nonZeroIndices();
            Vector nonZeroVals = null;
            nonZeroVals = vec instanceof SparseIndexedVector ? ((SparseIndexedVector)vec).nonSparseValues() : Vector.wrap(vec.nonZeroValues());
            assert (nonZeroIdxs.length == nonZeroVals.length());
            for (int j = 0; j < nonZeroIdxs.length; ++j) {
                int idx = nonZeroIdxs[j];
                double val = nonZeroVals.unsafeGet(j);
                ((ArrayList)rotIndexList.get(idx)).add(i);
                ((ArrayList)rotValueList.get(idx)).add(val);
            }
        }
        for (i = 0; i < numElems; ++i) {
            ArrayList rotIndex = (ArrayList)rotIndexList.get(i);
            ArrayList rotValue = (ArrayList)rotValueList.get(i);
            AVector rotVec = emptyRotVec;
            if (!rotIndex.isEmpty()) {
                int size = rotIndex.size();
                int[] indices = new int[size];
                double[] vals = new double[size];
                for (int j = 0; j < size; ++j) {
                    indices[j] = (Integer)rotIndex.get(j);
                    vals[j] = (Double)rotValue.get(j);
                }
                rotVec = SparseIndexedVector.wrap(numVecs, indices, vals);
            }
            rotList.add(rotVec);
        }
        return rotList;
    }

    @Override
    public double elementSum() {
        double result = 0.0;
        for (AVector vec : this.data) {
            if (vec == null) continue;
            result += vec.elementSum();
        }
        return result;
    }

    @Override
    public double elementSquaredSum() {
        double result = 0.0;
        for (AVector vec : this.data) {
            if (vec == null) continue;
            result += vec.elementSquaredSum();
        }
        return result;
    }

    @Override
    public double elementMin() {
        AVector fvec = this.data[0];
        double result = fvec == null ? 0.0 : fvec.elementMin();
        for (int i = 1; i < this.data.length; ++i) {
            double v;
            AVector vec = this.data[i];
            double d = v = vec == null ? 0.0 : vec.elementMin();
            if (v >= result) continue;
            result = v;
        }
        return result;
    }

    @Override
    public double elementMax() {
        AVector fvec = this.data[0];
        double result = fvec == null ? 0.0 : fvec.elementMax();
        for (int i = 1; i < this.data.length; ++i) {
            double v;
            AVector vec = this.data[i];
            double d = v = vec == null ? 0.0 : vec.elementMax();
            if (v <= result) continue;
            result = v;
        }
        return result;
    }

    @Override
    public void applyOp(Op op) {
        boolean stoch = op.isStochastic();
        RepeatedElementVector rr = stoch ? null : RepeatedElementVector.create(this.lineLength(), op.apply(0.0));
        long n = this.componentCount();
        int i = 0;
        while ((long)i < n) {
            block4 : {
                AVector v;
                block5 : {
                    block2 : {
                        block3 : {
                            v = this.unsafeGetVector(i);
                            if (v != null) break block2;
                            if (stoch) break block3;
                            this.unsafeSetVec(i, rr);
                            break block4;
                        }
                        v = Vector.createLength(this.lineLength());
                        this.unsafeSetVec(i, v);
                        break block5;
                    }
                    if (!v.isFullyMutable()) {
                        v = v.sparseClone();
                        this.unsafeSetVec(i, v);
                    }
                }
                v.applyOp(op);
            }
            ++i;
        }
    }

    @Override
    public final long nonZeroCount() {
        long result = 0L;
        for (AVector vec : this.data) {
            if (vec == null) continue;
            result += vec.nonZeroCount();
        }
        return result;
    }

    @Override
    public double[] toDoubleArray() {
        double[] result = Matrix.createStorage(this.rowCount(), this.columnCount());
        this.addToArray(result, 0);
        return result;
    }

    @Override
    public Matrix dense() {
        return this.toMatrix();
    }

    @Override
    public AMatrix sparse() {
        return this;
    }

    @Override
    public abstract int componentCount();

    @Override
    public abstract AVector getComponent(int var1);

    protected abstract int lineLength();

    @Override
    public void validate() {
        super.validate();
        int dlen = this.data.length;
        if (dlen != this.componentCount()) {
            throw new VectorzException("Too many rows");
        }
        for (int i = 0; i < dlen; ++i) {
            int vlen;
            AVector vec = this.unsafeGetVector(i);
            int n = vlen = vec == null ? this.lineLength() : vec.length();
            if (vlen == this.lineLength()) continue;
            throw new VectorzException("Wrong length data line vector, length " + vlen + " at position: " + i);
        }
    }
}

