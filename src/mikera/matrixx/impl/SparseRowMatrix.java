/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import mikera.arrayz.INDArray;
import mikera.arrayz.ISparse;
import mikera.matrixx.AMatrix;
import mikera.matrixx.Matrixx;
import mikera.matrixx.impl.ASparseRCMatrix;
import mikera.matrixx.impl.IFastRows;
import mikera.matrixx.impl.SparseColumnMatrix;
import mikera.vectorz.AVector;
import mikera.vectorz.Vector;
import mikera.vectorz.Vectorz;
import mikera.vectorz.impl.SingleElementVector;
import mikera.vectorz.impl.SparseIndexedVector;
import mikera.vectorz.util.ErrorMessages;
import mikera.vectorz.util.VectorzException;

public class SparseRowMatrix
extends ASparseRCMatrix
implements ISparse,
IFastRows {
    private static final long serialVersionUID = 8646257152425415773L;
    private static final long SPARSE_ELEMENT_THRESHOLD = 1000L;
    private final AVector emptyRow;

    protected SparseRowMatrix(int rowCount, int columnCount) {
        this(new AVector[rowCount], rowCount, columnCount);
    }

    protected SparseRowMatrix(AVector[] data, int rowCount, int columnCount) {
        super(rowCount, columnCount, data);
        if (data.length != rowCount) {
            throw new IllegalArgumentException(ErrorMessages.incompatibleShapes(rowCount, data.length));
        }
        this.emptyRow = Vectorz.createZeroVector(columnCount);
    }

    protected /* varargs */ SparseRowMatrix(AVector ... vectors) {
        this(vectors, vectors.length, vectors[0].length());
    }

    protected SparseRowMatrix(List<AVector> data, int rowCount, int columnCount) {
        this(data.toArray(new AVector[0]), rowCount, columnCount);
    }

    protected SparseRowMatrix(List<AVector> data) {
        this(data.toArray(new AVector[0]));
    }

    public static SparseRowMatrix create(int rows, int cols) {
        return new SparseRowMatrix(rows, cols);
    }

    public static SparseRowMatrix create(AVector[] data, int rows, int cols) {
        return new SparseRowMatrix(data, rows, cols);
    }

    public static /* varargs */ SparseRowMatrix create(AVector ... vecs) {
        return new SparseRowMatrix(vecs);
    }

    public static SparseRowMatrix create(List<AVector> rows) {
        return SparseRowMatrix.create(rows.toArray(new AVector[rows.size()]));
    }

    public static INDArray create(ArrayList<INDArray> slices, int rows, int cols) {
        AVector[] vecs = new AVector[rows];
        for (int i = 0; i < rows; ++i) {
            INDArray a = slices.get(i);
            if (a.dimensionality() != 1 || a.sliceCount() != cols) {
                throw new IllegalArgumentException(ErrorMessages.incompatibleShape(a));
            }
            vecs[i] = a.asVector();
        }
        return SparseRowMatrix.wrap(vecs, rows, cols);
    }

    public static SparseRowMatrix wrap(AVector[] vecs, int rows, int cols) {
        return SparseRowMatrix.create(vecs, rows, cols);
    }

    public static /* varargs */ SparseRowMatrix wrap(AVector ... vecs) {
        return SparseRowMatrix.create(vecs);
    }

    public static SparseRowMatrix create(AMatrix source) {
        if (source instanceof SparseColumnMatrix) {
            return ((SparseColumnMatrix)source).toSparseRowMatrix();
        }
        int rc = source.rowCount();
        int cc = source.columnCount();
        AVector[] data = new AVector[rc];
        List<AVector> rows = source.getRows();
        for (int i = 0; i < rc; ++i) {
            AVector row = rows.get(i);
            if (row.isZero()) continue;
            data[i] = Vectorz.createSparse(row);
        }
        return SparseRowMatrix.wrap(data, rc, cc);
    }

    public static SparseRowMatrix wrap(List<AVector> vecs) {
        return SparseRowMatrix.create(vecs);
    }

    @Override
    public int componentCount() {
        return this.rows;
    }

    @Override
    public AVector getComponent(int k) {
        AVector v = this.data[k];
        if (v == null) {
            return this.emptyRow;
        }
        return v;
    }

    @Override
    protected int lineLength() {
        return this.cols;
    }

    @Override
    public double get(int i, int j) {
        return this.getRow(i).get(j);
    }

    @Override
    public void set(int i, int j, double value) {
        this.checkIndex(i, j);
        this.unsafeSet(i, j, value);
    }

    @Override
    public double unsafeGet(int row, int column) {
        return this.getRow(row).unsafeGet(column);
    }

    @Override
    public void unsafeSet(int i, int j, double value) {
        AVector v = this.unsafeGetVector(i);
        if (v == null) {
            if (value == 0.0) {
                return;
            }
            v = SingleElementVector.create(value, j, this.cols);
        } else {
            if (v.isFullyMutable()) {
                v.set(j, value);
                return;
            }
            v = v.sparseClone();
            v.unsafeSet(j, value);
        }
        this.unsafeSetVec(i, v);
    }

    @Override
    public void set(AMatrix a) {
        this.checkSameShape(a);
        List<AVector> srows = a.getRows();
        for (int i = 0; i < this.rows; ++i) {
            this.setRow(i, srows.get(i));
        }
    }

    @Override
    public void setRow(int i, AVector v) {
        this.data[i] = v.copy();
    }

    @Override
    public void addAt(int i, int j, double d) {
        if (d == 0.0) {
            return;
        }
        AVector v = this.unsafeGetVector(i);
        if (v.isFullyMutable()) {
            v.addAt(j, d);
        } else {
            v = v.mutable();
            v.addAt(j, d);
            this.replaceRow(i, v);
        }
    }

    @Override
    public void addToArray(double[] targetData, int offset) {
        for (int i = 0; i < this.rows; ++i) {
            AVector v = this.unsafeGetVector(i);
            if (v == null) continue;
            v.addToArray(targetData, offset + this.cols * i);
        }
    }

    private AVector ensureMutableRow(int i) {
        AVector v = this.unsafeGetVector(i);
        if (v == null) {
            SparseIndexedVector nv = SparseIndexedVector.createLength(this.cols);
            this.unsafeSetVec(i, nv);
            return nv;
        }
        if (v.isFullyMutable()) {
            return v;
        }
        AVector mv = v.mutable();
        this.unsafeSetVec(i, mv);
        return mv;
    }

    @Override
    public List<AVector> getColumns() {
        return this.getRotatedData(this.rows, this.cols);
    }

    public SparseColumnMatrix toSparseColumnMatrix() {
        AVector[] colVecs = this.getColumns().toArray(new AVector[this.cols]);
        SparseColumnMatrix cm = SparseColumnMatrix.create(colVecs, this.rows, this.cols);
        return cm;
    }

    @Override
    public AVector getRow(int i) {
        AVector v = this.unsafeGetVector(i);
        if (v == null) {
            return this.emptyRow;
        }
        return v;
    }

    @Override
    public List<AVector> getRows() {
        ArrayList<AVector> rowList = new ArrayList<AVector>(this.rows);
        for (int i = 0; i < this.rows; ++i) {
            AVector v = this.unsafeGetVector(i);
            if (v == null) {
                v = this.emptyRow;
            }
            rowList.add(v);
        }
        return rowList;
    }

    @Override
    public AVector getRowView(int i) {
        return this.ensureMutableRow(i);
    }

    @Override
    public boolean isUpperTriangular() {
        int rc = this.rowCount();
        for (int i = 1; i < rc; ++i) {
            if (this.getRow(i).isRangeZero(0, i)) continue;
            return false;
        }
        return true;
    }

    @Override
    public void swapRows(int i, int j) {
        if (i == j) {
            return;
        }
        AVector a = this.unsafeGetVector(i);
        AVector b = this.unsafeGetVector(j);
        this.unsafeSetVec(i, b);
        this.unsafeSetVec(j, a);
    }

    @Override
    public void replaceRow(int i, AVector vec) {
        if (vec.length() != this.cols) {
            throw new IllegalArgumentException(ErrorMessages.incompatibleShape(vec));
        }
        this.unsafeSetVec(i, vec);
    }

    @Override
    public void add(AMatrix a) {
        this.checkSameShape(a);
        int rc = this.rowCount();
        for (int i = 0; i < rc; ++i) {
            AVector myVec = this.unsafeGetVector(i);
            AVector aVec = a.getRow(i);
            if (myVec == null) {
                if (aVec.isZero()) continue;
                this.unsafeSetVec(i, aVec.copy());
                continue;
            }
            if (myVec.isFullyMutable()) {
                myVec.add(aVec);
                continue;
            }
            this.unsafeSetVec(i, myVec.addCopy(aVec));
        }
    }

    @Override
    public void copyRowTo(int i, double[] data, int offset) {
        AVector v = this.unsafeGetVector(i);
        if (v == null) {
            Arrays.fill(data, offset, offset + this.cols, 0.0);
        } else {
            v.getElements(data, offset);
        }
    }

    @Override
    public void copyColumnTo(int col, double[] targetData, int offset) {
        for (int i = 0; i < this.rows; ++i) {
            AVector v = this.unsafeGetVector(i);
            targetData[offset + i] = v == null ? 0.0 : v.unsafeGet(col);
        }
    }

    @Override
    public SparseColumnMatrix getTransposeView() {
        return SparseColumnMatrix.wrap(this.data, this.cols, this.rows);
    }

    @Override
    public AMatrix multiplyCopy(double a) {
        long n = this.componentCount();
        AVector[] ndata = new AVector[(int)n];
        int i = 0;
        while ((long)i < n) {
            AVector v = this.unsafeGetVector(i);
            if (v != null) {
                ndata[i] = v.multiplyCopy(a);
            }
            ++i;
        }
        return SparseRowMatrix.wrap(ndata, this.rows, this.cols);
    }

    @Override
    public void multiplyRow(int i, double value) {
        if (value == 0.0) {
            this.unsafeSetVec(i, null);
            return;
        }
        AVector v = this.unsafeGetVector(i);
        if (v == null) {
            return;
        }
        v = v.multiplyCopy(value);
        this.unsafeSetVec(i, v);
    }

    @Override
    public AVector innerProduct(AVector a) {
        return this.transform(a);
    }

    @Override
    public AVector transform(AVector a) {
        Vector r = Vector.createLength(this.rows);
        for (int i = 0; i < this.rows; ++i) {
            r.set(i, this.getRow(i).dotProduct(a));
        }
        return r;
    }

    @Override
    public double[] toDoubleArray() {
        double[] ds = new double[this.rows * this.cols];
        for (int i = 0; i < this.rows; ++i) {
            AVector v = this.unsafeGetVector(i);
            if (v == null) continue;
            v.addToArray(ds, i * this.cols);
        }
        return ds;
    }

    @Override
    public AMatrix innerProduct(AMatrix a) {
        if (a instanceof SparseColumnMatrix) {
            return this.innerProduct((SparseColumnMatrix)a);
        }
        SparseRowMatrix r = Matrixx.createSparse(this.rows, a.columnCount());
        for (int i = 0; i < this.rows; ++i) {
            AVector row = this.unsafeGetVector(i);
            if (row == null || row.isZero()) continue;
            r.replaceRow(i, row.innerProduct(a));
        }
        return r;
    }

    public SparseRowMatrix innerProduct(SparseColumnMatrix a) {
        return this.innerProduct(SparseRowMatrix.create(a));
    }

    public SparseRowMatrix innerProduct(SparseRowMatrix a) {
        SparseRowMatrix r = Matrixx.createSparse(this.rows, a.columnCount());
        for (int i = 0; i < this.rows; ++i) {
            AVector row = this.unsafeGetVector(i);
            if (row == null) continue;
            r.replaceRow(i, row.innerProduct(a));
        }
        return r;
    }

    @Override
    public SparseRowMatrix exactClone() {
        SparseRowMatrix result = new SparseRowMatrix(this.rows, this.cols);
        for (int i = 0; i < this.rows; ++i) {
            AVector row = this.unsafeGetVector(i);
            if (row == null) continue;
            result.replaceRow(i, row.exactClone());
        }
        return result;
    }

    @Override
    public AMatrix clone() {
        if (this.elementCount() < 1000L) {
            return super.clone();
        }
        return this.exactClone();
    }

    @Override
    public AMatrix sparse() {
        return this;
    }

    @Override
    public void validate() {
        super.validate();
        for (int i = 0; i < this.rows; ++i) {
            if (this.getRow(i).length() == this.cols) continue;
            throw new VectorzException("Invalid column count at row: " + i);
        }
    }

    @Override
    public boolean equals(AMatrix m) {
        if (m == this) {
            return true;
        }
        if (!this.isSameShape(m)) {
            return false;
        }
        for (int i = 0; i < this.rows; ++i) {
            AVector v = this.unsafeGetVector(i);
            AVector ov = m.getRow(i);
            if (!(v == null ? !ov.isZero() : !v.equals(ov))) continue;
            return false;
        }
        return true;
    }

    public static AVector innerProduct(AMatrix a, AVector b) {
        return SparseRowMatrix.create(a).innerProduct(b);
    }

    public static AMatrix innerProduct(AMatrix a, AMatrix b) {
        return SparseRowMatrix.create(a).innerProduct(b);
    }
}

