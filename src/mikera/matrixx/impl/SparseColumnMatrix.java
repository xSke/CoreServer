/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.impl;

import java.util.List;
import mikera.arrayz.INDArray;
import mikera.arrayz.ISparse;
import mikera.matrixx.AMatrix;
import mikera.matrixx.Matrix;
import mikera.matrixx.impl.ASparseRCMatrix;
import mikera.matrixx.impl.IFastColumns;
import mikera.matrixx.impl.SparseRowMatrix;
import mikera.vectorz.AVector;
import mikera.vectorz.Vector;
import mikera.vectorz.Vectorz;
import mikera.vectorz.impl.SingleElementVector;
import mikera.vectorz.impl.SparseIndexedVector;
import mikera.vectorz.util.ErrorMessages;
import mikera.vectorz.util.VectorzException;

public class SparseColumnMatrix
extends ASparseRCMatrix
implements ISparse,
IFastColumns {
    private static final long serialVersionUID = -5994473197711276621L;
    private static final long SPARSE_ELEMENT_THRESHOLD = 1000L;
    private final AVector emptyColumn;

    protected SparseColumnMatrix(int rowCount, int columnCount) {
        this(new AVector[columnCount], rowCount, columnCount);
    }

    protected SparseColumnMatrix(AVector[] data, int rowCount, int columnCount) {
        super(rowCount, columnCount, data);
        if (data.length != columnCount) {
            throw new IllegalArgumentException(ErrorMessages.incompatibleShapes(columnCount, data.length));
        }
        this.emptyColumn = Vectorz.createZeroVector(rowCount);
    }

    protected /* varargs */ SparseColumnMatrix(AVector ... vectors) {
        this(vectors, vectors[0].length(), vectors.length);
    }

    protected SparseColumnMatrix(List<AVector> data, int rowCount, int columnCount) {
        this(data.toArray(new AVector[0]), rowCount, columnCount);
    }

    protected SparseColumnMatrix(List<AVector> data) {
        this(data.toArray(new AVector[0]));
    }

    public static SparseColumnMatrix create(int rows, int cols) {
        return new SparseColumnMatrix(rows, cols);
    }

    public static SparseColumnMatrix create(AVector[] data, int rows, int cols) {
        return new SparseColumnMatrix(data, rows, cols);
    }

    public static /* varargs */ SparseColumnMatrix create(AVector ... vecs) {
        return new SparseColumnMatrix(vecs);
    }

    public static SparseColumnMatrix create(List<AVector> columns) {
        return SparseColumnMatrix.create(columns.toArray(new AVector[columns.size()]));
    }

    public static SparseColumnMatrix wrap(AVector[] vecs, int rows, int cols) {
        return SparseColumnMatrix.create(vecs, rows, cols);
    }

    public static /* varargs */ SparseColumnMatrix wrap(AVector ... vecs) {
        return SparseColumnMatrix.create(vecs);
    }

    public static SparseColumnMatrix create(AMatrix source) {
        if (source instanceof SparseRowMatrix) {
            return ((SparseRowMatrix)source).toSparseColumnMatrix();
        }
        int cc = source.columnCount();
        int rc = source.rowCount();
        AVector[] data = new AVector[cc];
        for (int i = 0; i < cc; ++i) {
            AVector col = source.getColumn(i);
            if (col.isZero()) continue;
            data[i] = Vectorz.createSparse(col);
        }
        return new SparseColumnMatrix(data, rc, cc);
    }

    public static SparseColumnMatrix wrap(List<AVector> vecs) {
        return SparseColumnMatrix.create(vecs);
    }

    @Override
    public int componentCount() {
        return this.cols;
    }

    @Override
    public AVector getComponent(int k) {
        AVector v = this.data[k];
        if (v == null) {
            return this.emptyColumn;
        }
        return v;
    }

    @Override
    protected int lineLength() {
        return this.rows;
    }

    @Override
    public double get(int i, int j) {
        return this.getColumn(j).get(i);
    }

    @Override
    public void set(int i, int j, double value) {
        this.checkIndex(i, j);
        this.unsafeSet(i, j, value);
    }

    @Override
    public double unsafeGet(int row, int column) {
        return this.getColumn(column).unsafeGet(row);
    }

    @Override
    public void unsafeSet(int i, int j, double value) {
        AVector v = this.unsafeGetVector(j);
        if (v == null) {
            if (value == 0.0) {
                return;
            }
            v = SingleElementVector.create(value, i, this.rows);
        } else {
            if (v.isFullyMutable()) {
                v.unsafeSet(i, value);
                return;
            }
            v = v.sparseClone();
            v.unsafeSet(i, value);
        }
        this.unsafeSetVec(j, v);
    }

    @Override
    public void set(AMatrix a) {
        this.checkSameShape(a);
        List<AVector> scols = a.getColumns();
        for (int i = 0; i < this.cols; ++i) {
            this.setColumn(i, scols.get(i));
        }
    }

    @Override
    public void addAt(int i, int j, double d) {
        AVector v = this.getColumn(j);
        if (v.isFullyMutable()) {
            v.addAt(i, d);
        } else {
            v = v.mutable();
            v.addAt(i, d);
            this.replaceColumn(j, v);
        }
    }

    @Override
    public void addToArray(double[] targetData, int offset) {
        for (int i = 0; i < this.cols; ++i) {
            AVector v = this.unsafeGetVector(i);
            if (v == null) continue;
            v.addToArray(targetData, offset + i, this.cols);
        }
    }

    @Override
    public List<AVector> getRows() {
        return this.getRotatedData(this.cols, this.rows);
    }

    public SparseRowMatrix toSparseRowMatrix() {
        AVector[] rowVecs = this.getRows().toArray(new AVector[this.rows]);
        SparseRowMatrix rm = SparseRowMatrix.create(rowVecs, this.rows, this.cols);
        return rm;
    }

    private AVector ensureMutableColumn(int i) {
        AVector v = this.unsafeGetVector(i);
        if (v == null) {
            SparseIndexedVector nv = SparseIndexedVector.createLength(this.rows);
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
    public AVector getColumn(int j) {
        AVector v = this.unsafeGetVector(j);
        if (v == null) {
            return this.emptyColumn;
        }
        return v;
    }

    @Override
    public AVector getColumnView(int j) {
        return this.ensureMutableColumn(j);
    }

    @Override
    public boolean isLowerTriangular() {
        int cc = this.columnCount();
        for (int i = 1; i < cc; ++i) {
            if (this.getColumn(i).isRangeZero(0, i)) continue;
            return false;
        }
        return true;
    }

    @Override
    public void swapColumns(int i, int j) {
        if (i == j) {
            return;
        }
        AVector a = this.unsafeGetVector(i);
        AVector b = this.unsafeGetVector(j);
        this.unsafeSetVec(i, b);
        this.unsafeSetVec(j, a);
    }

    @Override
    public void replaceColumn(int i, AVector vec) {
        this.checkColumn(i);
        if (vec.length() != this.rows) {
            throw new IllegalArgumentException(ErrorMessages.incompatibleShape(vec));
        }
        this.unsafeSetVec(i, vec);
    }

    @Override
    public void add(AMatrix a) {
        int count = this.columnCount();
        for (int i = 0; i < count; ++i) {
            AVector myVec = this.unsafeGetVector(i);
            AVector aVec = a.getColumn(i);
            if (myVec == null) {
                if (aVec.isZero()) continue;
                this.unsafeSetVec(i, aVec.copy());
                continue;
            }
            if (myVec.isMutable()) {
                myVec.add(aVec);
                continue;
            }
            this.unsafeSetVec(i, myVec.addCopy(aVec));
        }
    }

    @Override
    public void copyColumnTo(int i, double[] targetData, int offset) {
        this.getColumn(i).copyTo(targetData, offset);
    }

    @Override
    public void copyRowTo(int row, double[] targetData, int offset) {
        for (int i = 0; i < this.cols; ++i) {
            AVector e = this.unsafeGetVector(i);
            targetData[offset + i] = e == null ? 0.0 : e.unsafeGet(row);
        }
    }

    @Override
    public SparseRowMatrix getTransposeView() {
        return SparseRowMatrix.wrap(this.data, this.cols, this.rows);
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
        return SparseColumnMatrix.wrap(ndata, this.rows, this.cols);
    }

    @Override
    public AVector innerProduct(AVector a) {
        return this.transform(a);
    }

    @Override
    public AVector transform(AVector a) {
        Vector r = Vector.createLength(this.rows);
        for (int i = 0; i < this.cols; ++i) {
            this.getColumn(i).addMultipleToArray(a.get(i), 0, r.getArray(), 0, this.rows);
        }
        return r;
    }

    @Override
    public Matrix toMatrixTranspose() {
        Matrix m = Matrix.create(this.cols, this.rows);
        for (int i = 0; i < this.cols; ++i) {
            this.getColumn(i).copyTo(m.data, this.rows * i);
        }
        return m;
    }

    @Override
    public double[] toDoubleArray() {
        Matrix m = Matrix.create(this.rows, this.cols);
        for (int i = 0; i < this.cols; ++i) {
            AVector v = this.unsafeGetVector(i);
            if (v == null) continue;
            m.getColumn(i).set(v);
        }
        return m.getArray();
    }

    @Override
    public AMatrix transposeInnerProduct(AMatrix a) {
        return this.getTranspose().innerProduct(a);
    }

    @Override
    public SparseColumnMatrix exactClone() {
        SparseColumnMatrix result = new SparseColumnMatrix(this.rows, this.cols);
        for (int i = 0; i < this.cols; ++i) {
            AVector col = this.unsafeGetVector(i);
            if (col == null) continue;
            result.replaceColumn(i, col.exactClone());
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
        for (int i = 0; i < this.cols; ++i) {
            if (this.getColumn(i).length() == this.rows) continue;
            throw new VectorzException("Invalid row count at column: " + i);
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
        for (int i = 0; i < this.cols; ++i) {
            AVector v = this.unsafeGetVector(i);
            AVector ov = m.getColumn(i);
            if (!(v == null ? !ov.isZero() : !v.equals(ov))) continue;
            return false;
        }
        return true;
    }
}

