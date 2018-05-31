/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx;

import java.nio.DoubleBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import mikera.arrayz.INDArray;
import mikera.matrixx.AMatrix;
import mikera.matrixx.algo.Multiplications;
import mikera.matrixx.impl.ADenseArrayMatrix;
import mikera.matrixx.impl.ARectangularMatrix;
import mikera.matrixx.impl.AStridedMatrix;
import mikera.matrixx.impl.DenseColumnMatrix;
import mikera.matrixx.impl.StridedMatrix;
import mikera.matrixx.impl.VectorMatrixMN;
import mikera.vectorz.AVector;
import mikera.vectorz.Op;
import mikera.vectorz.Vector;
import mikera.vectorz.Vectorz;
import mikera.vectorz.impl.ADenseArrayVector;
import mikera.vectorz.impl.AStridedVector;
import mikera.vectorz.impl.ArraySubVector;
import mikera.vectorz.impl.StridedElementIterator;
import mikera.vectorz.impl.StridedVector;
import mikera.vectorz.util.DoubleArrays;
import mikera.vectorz.util.ErrorMessages;

public final class Matrix
extends ADenseArrayMatrix {
    private static final long serialVersionUID = -3260581688928230431L;

    private Matrix(int rowCount, int columnCount) {
        this(rowCount, columnCount, Matrix.createStorage(rowCount, columnCount));
    }

    public static Matrix create(int rowCount, int columnCount) {
        return new Matrix(rowCount, columnCount);
    }

    public static /* varargs */ Matrix create(int ... shape) {
        int dims = shape.length;
        if (dims != 2) {
            throw new IllegalArgumentException("Cannot create Matrix with dimensionality: " + dims);
        }
        return Matrix.create(shape[0], shape[1]);
    }

    public static Matrix create(AMatrix m) {
        return new Matrix(m);
    }

    public Matrix(AMatrix m) {
        this(m.rowCount(), m.columnCount(), m.toDoubleArray());
    }

    public static double[] createStorage(int rowCount, int columnCount) {
        long elementCount = (long)rowCount * (long)columnCount;
        int ec = (int)elementCount;
        if ((long)ec != elementCount) {
            throw new IllegalArgumentException(ErrorMessages.tooManyElements(rowCount, columnCount));
        }
        return new double[ec];
    }

    public static Matrix createRandom(int rows, int cols) {
        Matrix m = Matrix.create(rows, cols);
        double[] d = m.data;
        for (int i = 0; i < d.length; ++i) {
            d[i] = Math.random();
        }
        return m;
    }

    public static Matrix create(INDArray m) {
        if (m.dimensionality() != 2) {
            throw new IllegalArgumentException("Can only create matrix from 2D array");
        }
        int rows = m.getShape(0);
        int cols = m.getShape(1);
        return Matrix.wrap(rows, cols, m.toDoubleArray());
    }

    public static /* varargs */ Matrix createFromRows(Object ... rowVectors) {
        int rc = rowVectors.length;
        ArrayList<AVector> vs = new ArrayList<AVector>(rc);
        for (Object o : rowVectors) {
            vs.add(Vectorz.create(o));
        }
        VectorMatrixMN m = VectorMatrixMN.create(vs);
        return Matrix.create(m);
    }

    public static Matrix create(double[][] data) {
        int rows = data.length;
        int cols = data[0].length;
        Matrix m = Matrix.create(rows, cols);
        for (int i = 0; i < rows; ++i) {
            double[] drow = data[i];
            if (drow.length != cols) {
                throw new IllegalArgumentException("Array shape is not rectangular! Row " + i + " has length " + drow.length);
            }
            System.arraycopy(drow, 0, m.data, i * cols, cols);
        }
        return m;
    }

    public static /* varargs */ Matrix create(AVector ... data) {
        int rc = data.length;
        int cc = rc == 0 ? 0 : data[0].length();
        Matrix m = Matrix.create(rc, cc);
        for (int i = 0; i < rc; ++i) {
            m.setRow(i, data[i]);
        }
        return m;
    }

    @Override
    public boolean isFullyMutable() {
        return true;
    }

    @Override
    public boolean isView() {
        return false;
    }

    @Override
    public boolean isBoolean() {
        return DoubleArrays.isBoolean(this.data, 0, this.data.length);
    }

    @Override
    public boolean isZero() {
        return DoubleArrays.isZero(this.data, 0, this.data.length);
    }

    @Override
    public boolean isPackedArray() {
        return true;
    }

    private Matrix(int rowCount, int columnCount, double[] data) {
        super(data, rowCount, columnCount);
    }

    public static Matrix wrap(int rowCount, int columnCount, double[] data) {
        if (data.length != rowCount * columnCount) {
            throw new IllegalArgumentException("data array is of wrong size: " + data.length);
        }
        return new Matrix(rowCount, columnCount, data);
    }

    @Override
    public AStridedMatrix subMatrix(int rowStart, int rows, int colStart, int cols) {
        if (rowStart < 0 || rowStart >= this.rows || colStart < 0 || colStart >= this.cols) {
            throw new IndexOutOfBoundsException("Invalid submatrix start position");
        }
        if (rowStart + rows > this.rows || colStart + cols > this.cols) {
            throw new IndexOutOfBoundsException("Invalid submatrix end position");
        }
        return StridedMatrix.wrap(this.data, rows, cols, rowStart * this.rowStride() + colStart * this.columnStride(), this.rowStride(), this.columnStride());
    }

    @Override
    public Vector innerProduct(AVector a) {
        if (a instanceof Vector) {
            return this.innerProduct((Vector)a);
        }
        return this.transform(a);
    }

    @Override
    public Matrix innerProduct(Matrix a) {
        return Multiplications.multiply(this, (AMatrix)a);
    }

    @Override
    public Matrix transposeInnerProduct(Matrix s) {
        Matrix r = this.toMatrixTranspose();
        return Multiplications.multiply(r, (AMatrix)s);
    }

    @Override
    public Matrix innerProduct(AMatrix a) {
        if (a instanceof Matrix) {
            return this.innerProduct((Matrix)a);
        }
        if (this.columnCount() != a.rowCount()) {
            throw new IllegalArgumentException(ErrorMessages.mismatch(this, a));
        }
        return Multiplications.multiply(this, a);
    }

    @Override
    public double elementSum() {
        return DoubleArrays.elementSum(this.data);
    }

    @Override
    public double elementSquaredSum() {
        return DoubleArrays.elementSquaredSum(this.data);
    }

    @Override
    public double elementMax() {
        return DoubleArrays.elementMax(this.data);
    }

    @Override
    public double elementMin() {
        return DoubleArrays.elementMin(this.data);
    }

    @Override
    public void abs() {
        DoubleArrays.abs(this.data);
    }

    @Override
    public void signum() {
        DoubleArrays.signum(this.data);
    }

    @Override
    public void square() {
        DoubleArrays.square(this.data);
    }

    @Override
    public void exp() {
        DoubleArrays.exp(this.data);
    }

    @Override
    public void log() {
        DoubleArrays.log(this.data);
    }

    @Override
    public long nonZeroCount() {
        return DoubleArrays.nonZeroCount(this.data);
    }

    @Override
    public final void copyRowTo(int row, double[] dest, int destOffset) {
        int srcOffset = row * this.cols;
        System.arraycopy(this.data, srcOffset, dest, destOffset, this.cols);
    }

    @Override
    public final void copyColumnTo(int col, double[] dest, int destOffset) {
        int colOffset = col;
        for (int i = 0; i < this.rows; ++i) {
            dest[destOffset + i] = this.data[colOffset + i * this.cols];
        }
    }

    @Override
    public Vector transform(AVector a) {
        Vector v = Vector.createLength(this.rows);
        double[] vdata = v.getArray();
        for (int i = 0; i < this.rows; ++i) {
            vdata[i] = a.dotProduct(this.data, i * this.cols);
        }
        return v;
    }

    @Override
    public Vector transform(Vector a) {
        Vector v = Vector.createLength(this.rows);
        this.transform(a, v);
        return v;
    }

    @Override
    public void transform(AVector source, AVector dest) {
        if (source instanceof Vector && dest instanceof Vector) {
            this.transform((Vector)source, (Vector)dest);
            return;
        }
        if (this.rows != dest.length()) {
            throw new IllegalArgumentException(ErrorMessages.wrongDestLength(dest));
        }
        if (this.cols != source.length()) {
            throw new IllegalArgumentException(ErrorMessages.wrongSourceLength(source));
        }
        for (int i = 0; i < this.rows; ++i) {
            dest.unsafeSet(i, source.dotProduct(this.data, i * this.cols));
        }
    }

    @Override
    public void transform(Vector source, Vector dest) {
        int rc = this.rowCount();
        int cc = this.columnCount();
        if (source.length() != cc) {
            throw new IllegalArgumentException(ErrorMessages.wrongSourceLength(source));
        }
        if (dest.length() != rc) {
            throw new IllegalArgumentException(ErrorMessages.wrongDestLength(dest));
        }
        int di = 0;
        double[] sdata = source.getArray();
        double[] ddata = dest.getArray();
        for (int row = 0; row < rc; ++row) {
            double total = 0.0;
            for (int column = 0; column < cc; ++column) {
                total += this.data[di + column] * sdata[column];
            }
            di += cc;
            ddata[row] = total;
        }
    }

    @Override
    public ArraySubVector getRowView(int row) {
        return ArraySubVector.wrap(this.data, row * this.cols, this.cols);
    }

    @Override
    public AStridedVector getColumnView(int col) {
        if (this.cols == 1) {
            if (col != 0) {
                throw new IndexOutOfBoundsException("Column does not exist: " + col);
            }
            return Vector.wrap(this.data);
        }
        return StridedVector.wrap(this.data, col, this.rows, this.cols);
    }

    @Override
    public void swapRows(int i, int j) {
        if (i == j) {
            return;
        }
        int a = i * this.cols;
        int b = j * this.cols;
        int cc = this.columnCount();
        for (int k = 0; k < cc; ++k) {
            int i1 = a + k;
            int i2 = b + k;
            double t = this.data[i1];
            this.data[i1] = this.data[i2];
            this.data[i2] = t;
        }
    }

    @Override
    public void swapColumns(int i, int j) {
        if (i == j) {
            return;
        }
        int rc = this.rowCount();
        int cc = this.columnCount();
        for (int k = 0; k < rc; ++k) {
            int x = k * cc;
            int i1 = i + x;
            int i2 = j + x;
            double t = this.data[i1];
            this.data[i1] = this.data[i2];
            this.data[i2] = t;
        }
    }

    @Override
    public void multiplyRow(int i, double factor) {
        int offset = i * this.cols;
        DoubleArrays.multiply(this.data, offset, this.cols, factor);
    }

    @Override
    public void addRowMultiple(int src, int dst, double factor) {
        int soffset = src * this.cols;
        int doffset = dst * this.cols;
        for (int j = 0; j < this.cols; ++j) {
            double[] arrd = this.data;
            int n = doffset + j;
            arrd[n] = arrd[n] + factor * this.data[soffset + j];
        }
    }

    @Override
    public Vector asVector() {
        return Vector.wrap(this.data);
    }

    @Override
    public Vector toVector() {
        return Vector.create(this.data);
    }

    @Override
    public final Matrix toMatrix() {
        return this;
    }

    @Override
    public final double[] toDoubleArray() {
        return DoubleArrays.copyOf(this.data);
    }

    @Override
    public Matrix toMatrixTranspose() {
        int rc = this.rowCount();
        int cc = this.columnCount();
        Matrix m = Matrix.create(cc, rc);
        double[] targetData = m.data;
        for (int j = 0; j < cc; ++j) {
            this.copyColumnTo(j, targetData, j * rc);
        }
        return m;
    }

    @Override
    public void toDoubleBuffer(DoubleBuffer dest) {
        dest.put(this.data);
    }

    @Override
    public double[] asDoubleArray() {
        return this.data;
    }

    @Override
    public double get(int i, int j) {
        this.checkColumn(j);
        return this.data[i * this.cols + j];
    }

    @Override
    public void unsafeSet(int i, int j, double value) {
        this.data[i * this.cols + j] = value;
    }

    @Override
    public double unsafeGet(int i, int j) {
        return this.data[i * this.cols + j];
    }

    @Override
    public void addAt(int i, int j, double d) {
        double[] arrd = this.data;
        int n = i * this.cols + j;
        arrd[n] = arrd[n] + d;
    }

    @Override
    public void addAt(int i, double d) {
        double[] arrd = this.data;
        int n = i;
        arrd[n] = arrd[n] + d;
    }

    @Override
    public void subAt(int i, double d) {
        double[] arrd = this.data;
        int n = i;
        arrd[n] = arrd[n] - d;
    }

    @Override
    public void divideAt(int i, double d) {
        double[] arrd = this.data;
        int n = i;
        arrd[n] = arrd[n] / d;
    }

    @Override
    public void multiplyAt(int i, double d) {
        double[] arrd = this.data;
        int n = i;
        arrd[n] = arrd[n] * d;
    }

    @Override
    public void set(int i, int j, double value) {
        this.checkColumn(j);
        this.data[i * this.cols + j] = value;
    }

    @Override
    public void applyOp(Op op) {
        op.applyTo(this.data);
    }

    public void addMultiple(Matrix m, double factor) {
        this.checkSameShape(m);
        DoubleArrays.addMultiple(this.data, m.data, factor);
    }

    public void setMultiple(Matrix m, double factor) {
        this.checkSameShape(m);
        DoubleArrays.scaleCopy(this.data, m.data, factor);
    }

    @Override
    public void add(AMatrix m) {
        this.checkSameShape(m);
        m.addToArray(this.data, 0);
    }

    @Override
    public Matrix addCopy(Matrix a) {
        this.checkSameShape(a);
        Matrix r = Matrix.create(this.rows, this.cols);
        Matrix.add(r, this, a);
        return r;
    }

    @Override
    public void add2(AMatrix a, AMatrix b) {
        if (a instanceof ADenseArrayMatrix) {
            if (a instanceof Matrix && b instanceof Matrix) {
                this.add2((Matrix)a, (Matrix)b);
                return;
            }
            if (b instanceof ADenseArrayMatrix) {
                super.add((ADenseArrayMatrix)a, (ADenseArrayMatrix)b);
                return;
            }
        }
        this.checkSameShape(a);
        this.checkSameShape(b);
        a.addToArray(this.data, 0);
        b.addToArray(this.data, 0);
    }

    public static void add(Matrix dest, Matrix a, Matrix b) {
        dest.checkSameShape(a);
        dest.checkSameShape(b);
        DoubleArrays.addResult(dest.data, a.data, b.data);
    }

    public static void scale(Matrix dest, Matrix src, double factor) {
        dest.checkSameShape(src);
        dest.setMultiple(src, factor);
    }

    public static void scaleAdd(Matrix dest, Matrix a, Matrix b, double bFactor) {
        dest.checkSameShape(a);
        dest.checkSameShape(b);
        int len = dest.data.length;
        for (int i = 0; i < len; ++i) {
            dest.data[i] = a.data[i] + bFactor * b.data[i];
        }
    }

    public void add2(Matrix a, Matrix b) {
        this.checkSameShape(a);
        this.checkSameShape(b);
        DoubleArrays.add2(this.data, a.data, b.data);
    }

    public void add(Matrix m) {
        this.checkSameShape(m);
        DoubleArrays.add(this.data, m.data);
    }

    @Override
    public void addMultiple(AMatrix m, double factor) {
        if (m instanceof Matrix) {
            this.addMultiple((Matrix)m, factor);
            return;
        }
        int rc = this.rowCount();
        int cc = this.columnCount();
        m.checkShape(rc, cc);
        for (int i = 0; i < rc; ++i) {
            m.getRow(i).addMultipleToArray(factor, 0, this.data, i * this.cols, cc);
        }
    }

    @Override
    public void add(double d) {
        DoubleArrays.add(this.data, d);
    }

    @Override
    public void multiply(double factor) {
        DoubleArrays.multiply(this.data, factor);
    }

    @Override
    public void set(AMatrix a) {
        this.checkSameShape(a);
        a.getElements(this.data, 0);
    }

    @Override
    public void set(AVector a) {
        if (this.rowCount() != a.length()) {
            throw new IllegalArgumentException(ErrorMessages.incompatibleBroadcast((INDArray)a, this));
        }
        a.getElements(this.data, 0);
        for (int i = 1; i < this.rows; ++i) {
            System.arraycopy(this.data, 0, this.data, i * this.cols, this.cols);
        }
    }

    @Override
    public void getElements(double[] dest, int offset) {
        System.arraycopy(this.data, 0, dest, offset, this.data.length);
    }

    @Override
    public Iterator<Double> elementIterator() {
        return new StridedElementIterator(this.data, 0, this.rows * this.cols, 1);
    }

    @Override
    public DenseColumnMatrix getTranspose() {
        return this.getTransposeView();
    }

    @Override
    public DenseColumnMatrix getTransposeView() {
        return DenseColumnMatrix.wrap(this.cols, this.rows, this.data);
    }

    @Override
    public void set(double value) {
        Arrays.fill(this.data, value);
    }

    @Override
    public void reciprocal() {
        DoubleArrays.reciprocal(this.data, 0, this.data.length);
    }

    @Override
    public void clamp(double min, double max) {
        DoubleArrays.clamp(this.data, 0, this.data.length, min, max);
    }

    @Override
    public Matrix clone() {
        return new Matrix(this.rows, this.cols, DoubleArrays.copyOf(this.data));
    }

    @Override
    public Matrix copy() {
        return this.clone();
    }

    @Override
    public Matrix exactClone() {
        return this.clone();
    }

    @Override
    public void setRow(int i, AVector row) {
        int cc = this.columnCount();
        row.checkLength(cc);
        row.getElements(this.data, i * cc);
    }

    @Override
    public void setColumn(int j, AVector col) {
        int rc = this.rows;
        if (col.length() != rc) {
            throw new IllegalArgumentException(ErrorMessages.mismatch(this.getColumn(j), col));
        }
        for (int i = 0; i < rc; ++i) {
            this.data[this.index((int)i, (int)j)] = col.unsafeGet(i);
        }
    }

    @Override
    public StridedVector getBand(int band) {
        int cc = this.columnCount();
        int rc = this.rowCount();
        if (band > cc || band < - rc) {
            throw new IndexOutOfBoundsException(ErrorMessages.invalidBand(this, band));
        }
        return StridedVector.wrap(this.data, band >= 0 ? band : (- band) * cc, this.bandLength(band), cc + 1);
    }

    @Override
    protected final int index(int i, int j) {
        return i * this.cols + j;
    }

    @Override
    public int getArrayOffset() {
        return 0;
    }

    @Override
    public double[] getArray() {
        return this.data;
    }

    public static Matrix createIdentity(int numRows, int numCols) {
        Matrix ret = Matrix.create(numRows, numCols);
        int small = numRows < numCols ? numRows : numCols;
        for (int i = 0; i < small; ++i) {
            ret.unsafeSet(i, i, 1.0);
        }
        return ret;
    }

    public static Matrix createIdentity(int dims) {
        Matrix ret = Matrix.create(dims, dims);
        for (int i = 0; i < dims; ++i) {
            ret.unsafeSet(i, i, 1.0);
        }
        return ret;
    }
}

