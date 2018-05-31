/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.impl;

import java.util.Arrays;
import mikera.arrayz.INDArray;
import mikera.matrixx.AMatrix;
import mikera.matrixx.Matrix;
import mikera.matrixx.impl.ARectangularMatrix;
import mikera.matrixx.impl.ASingleBandMatrix;
import mikera.matrixx.impl.DiagonalMatrix;
import mikera.vectorz.AVector;
import mikera.vectorz.Vector;
import mikera.vectorz.Vectorz;
import mikera.vectorz.impl.ADenseArrayVector;
import mikera.vectorz.impl.ASingleElementVector;
import mikera.vectorz.impl.SingleElementVector;
import mikera.vectorz.util.ErrorMessages;
import mikera.vectorz.util.VectorzException;

public abstract class ADiagonalMatrix
extends ASingleBandMatrix {
    private static final long serialVersionUID = -6770867175103162837L;
    protected final int dimensions;

    protected ADiagonalMatrix(int dimensions) {
        this.dimensions = dimensions;
    }

    @Override
    public int nonZeroBand() {
        return 0;
    }

    @Override
    public boolean isSquare() {
        return true;
    }

    @Override
    public boolean isZero() {
        return this.getLeadingDiagonal().isZero();
    }

    @Override
    public boolean isBoolean() {
        return this.getLeadingDiagonal().isBoolean();
    }

    @Override
    public boolean isSymmetric() {
        return true;
    }

    @Override
    public boolean isDiagonal() {
        return true;
    }

    @Override
    public boolean isRectangularDiagonal() {
        return true;
    }

    @Override
    public boolean isUpperTriangular() {
        return true;
    }

    @Override
    public boolean isLowerTriangular() {
        return true;
    }

    @Override
    public abstract boolean isMutable();

    @Override
    public boolean isFullyMutable() {
        return this.dimensions <= 1 && this.getLeadingDiagonal().isFullyMutable();
    }

    @Override
    protected void checkSameShape(AMatrix m) {
        int dims = this.dimensions;
        if (dims != m.rowCount() || dims != m.columnCount()) {
            throw new IndexOutOfBoundsException(ErrorMessages.mismatch(this, m));
        }
    }

    @Override
    protected void checkSameShape(ARectangularMatrix m) {
        int dims = this.dimensions;
        if (dims != m.rowCount() || dims != m.columnCount()) {
            throw new IndexOutOfBoundsException(ErrorMessages.mismatch(this, m));
        }
    }

    @Override
    protected final void checkIndex(int i, int j) {
        if (i < 0 || i >= this.dimensions || j < 0 || j >= this.dimensions) {
            throw new IndexOutOfBoundsException(ErrorMessages.invalidIndex((INDArray)this, i, j));
        }
    }

    public final boolean isSameShape(ARectangularMatrix m) {
        return this.dimensions == m.rows && this.dimensions == m.cols;
    }

    @Override
    public final int upperBandwidthLimit() {
        return 0;
    }

    @Override
    public final int lowerBandwidthLimit() {
        return 0;
    }

    @Override
    public AVector getBand(int band) {
        if (band == 0) {
            return this.getLeadingDiagonal();
        }
        if (band > this.dimensions || band < - this.dimensions) {
            throw new IndexOutOfBoundsException(ErrorMessages.invalidBand(this, band));
        }
        return Vectorz.createZeroVector(this.bandLength(band));
    }

    @Override
    public AVector getNonZeroBand() {
        return this.getLeadingDiagonal();
    }

    @Override
    public double determinant() {
        double det = 1.0;
        for (int i = 0; i < this.dimensions; ++i) {
            det *= this.unsafeGetDiagonalValue(i);
        }
        return det;
    }

    @Override
    public int rank() {
        return (int)this.getLeadingDiagonal().nonZeroCount();
    }

    public int dimensions() {
        return this.dimensions;
    }

    @Override
    public boolean isSameShape(AMatrix m) {
        return this.dimensions == m.rowCount() && this.dimensions == m.columnCount();
    }

    @Override
    public int checkSquare() {
        return this.dimensions;
    }

    @Override
    public double elementMax() {
        double ldv = this.getLeadingDiagonal().elementMax();
        if (this.dimensions > 1) {
            return Math.max(0.0, ldv);
        }
        return ldv;
    }

    @Override
    public double elementMin() {
        double ldv = this.getLeadingDiagonal().elementMin();
        if (this.dimensions > 1) {
            return Math.min(0.0, ldv);
        }
        return ldv;
    }

    @Override
    public double elementSum() {
        return this.getLeadingDiagonal().elementSum();
    }

    @Override
    public double elementSquaredSum() {
        return this.getLeadingDiagonal().elementSquaredSum();
    }

    @Override
    public long nonZeroCount() {
        return this.getLeadingDiagonal().nonZeroCount();
    }

    @Override
    public void copyRowTo(int row, double[] dest, int destOffset) {
        Arrays.fill(dest, destOffset, destOffset + this.dimensions, 0.0);
        dest[destOffset + row] = this.unsafeGetDiagonalValue(row);
    }

    @Override
    public void addToArray(double[] dest, int offset) {
        this.getLeadingDiagonal().addToArray(dest, offset, this.dimensions + 1);
    }

    @Override
    public AMatrix addCopy(AMatrix a) {
        if (a.isDiagonal()) {
            if (a.rowCount() != this.dimensions) {
                throw new IllegalArgumentException(ErrorMessages.incompatibleShapes(this, a));
            }
            DiagonalMatrix m = DiagonalMatrix.create(this.getLeadingDiagonal());
            a.getLeadingDiagonal().addToArray(m.data, 0);
            return m;
        }
        return a.addCopy(this);
    }

    @Override
    public void copyColumnTo(int col, double[] dest, int destOffset) {
        this.copyRowTo(col, dest, destOffset);
    }

    public AMatrix innerProduct(ADiagonalMatrix a) {
        int dims = this.dimensions;
        if (dims != a.dimensions) {
            throw new IllegalArgumentException(ErrorMessages.incompatibleShapes(this, a));
        }
        DiagonalMatrix result = DiagonalMatrix.createDimensions(dims);
        for (int i = 0; i < dims; ++i) {
            result.data[i] = this.unsafeGetDiagonalValue(i) * a.unsafeGetDiagonalValue(i);
        }
        return result;
    }

    @Override
    public AMatrix innerProduct(AMatrix a) {
        if (a instanceof ADiagonalMatrix) {
            return this.innerProduct((ADiagonalMatrix)a);
        }
        if (a instanceof Matrix) {
            return this.innerProduct((Matrix)a);
        }
        if (this.dimensions != a.rowCount()) {
            throw new IllegalArgumentException(ErrorMessages.incompatibleShapes(this, a));
        }
        AMatrix m = a.clone();
        for (int i = 0; i < this.dimensions; ++i) {
            double dv = this.unsafeGetDiagonalValue(i);
            m.multiplyRow(i, dv);
        }
        return m;
    }

    @Override
    public Matrix innerProduct(Matrix a) {
        if (this.dimensions != a.rowCount()) {
            throw new IllegalArgumentException(ErrorMessages.incompatibleShapes(this, a));
        }
        Matrix m = a.clone();
        for (int i = 0; i < this.dimensions; ++i) {
            double dv = this.unsafeGetDiagonalValue(i);
            m.multiplyRow(i, dv);
        }
        return m;
    }

    @Override
    public Matrix transposeInnerProduct(Matrix s) {
        return this.innerProduct(s);
    }

    @Override
    public void transformInPlace(AVector v) {
        if (v instanceof ADenseArrayVector) {
            this.transformInPlace((ADenseArrayVector)v);
            return;
        }
        if (v.length() != this.dimensions) {
            throw new IllegalArgumentException(ErrorMessages.incompatibleShapes(this, (INDArray)v));
        }
        for (int i = 0; i < this.dimensions; ++i) {
            v.unsafeSet(i, v.unsafeGet(i) * this.unsafeGetDiagonalValue(i));
        }
    }

    @Override
    public void transformInPlace(ADenseArrayVector v) {
        double[] data = v.getArray();
        int offset = v.getArrayOffset();
        for (int i = 0; i < this.dimensions; ++i) {
            double[] arrd = data;
            int n = i + offset;
            arrd[n] = arrd[n] * this.unsafeGetDiagonalValue(i);
        }
    }

    @Override
    public void transform(Vector source, Vector dest) {
        int rc;
        int cc = rc = this.rowCount();
        if (source.length() != cc) {
            throw new IllegalArgumentException(ErrorMessages.wrongSourceLength(source));
        }
        if (dest.length() != rc) {
            throw new IllegalArgumentException(ErrorMessages.wrongDestLength(dest));
        }
        double[] sdata = source.getArray();
        double[] ddata = dest.getArray();
        for (int row = 0; row < rc; ++row) {
            ddata[row] = sdata[row] * this.unsafeGetDiagonalValue(row);
        }
    }

    @Override
    public int rowCount() {
        return this.dimensions;
    }

    @Override
    public int columnCount() {
        return this.dimensions;
    }

    @Override
    public boolean isIdentity() {
        return this.getLeadingDiagonal().elementsEqual(1.0);
    }

    @Override
    public void transposeInPlace() {
    }

    @Override
    public double calculateElement(int i, AVector v) {
        return v.unsafeGet(i) * this.unsafeGetDiagonalValue(i);
    }

    @Override
    public void set(int row, int column, double value) {
        throw new UnsupportedOperationException(ErrorMessages.notFullyMutable(this, row, column));
    }

    @Override
    public abstract AVector getLeadingDiagonal();

    public double getDiagonalValue(int i) {
        if (i < 0 || i >= this.dimensions) {
            throw new IndexOutOfBoundsException();
        }
        return this.unsafeGet(i, i);
    }

    @Override
    public ASingleElementVector getRow(int row) {
        return SingleElementVector.create(this.getDiagonalValue(row), row, this.dimensions);
    }

    @Override
    public ASingleElementVector getColumn(int col) {
        return this.getRow(col);
    }

    public double unsafeGetDiagonalValue(int i) {
        return this.unsafeGet(i, i);
    }

    @Override
    public ADiagonalMatrix getTranspose() {
        return this;
    }

    @Override
    public ADiagonalMatrix getTransposeView() {
        return this;
    }

    @Override
    public double density() {
        return 1.0 / (double)this.dimensions;
    }

    @Override
    public Matrix toMatrix() {
        Matrix m = Matrix.create(this.dimensions, this.dimensions);
        for (int i = 0; i < this.dimensions; ++i) {
            m.data[i * (this.dimensions + 1)] = this.unsafeGetDiagonalValue(i);
        }
        return m;
    }

    @Override
    public double trace() {
        return this.getLeadingDiagonal().elementSum();
    }

    @Override
    public double diagonalProduct() {
        return this.getLeadingDiagonal().elementProduct();
    }

    @Override
    public double[] toDoubleArray() {
        double[] data = new double[this.dimensions * this.dimensions];
        this.getLeadingDiagonal().addToArray(data, 0, this.dimensions + 1);
        return data;
    }

    @Override
    public final Matrix toMatrixTranspose() {
        return this.toMatrix();
    }

    @Override
    public boolean equalsTranspose(AMatrix m) {
        return this.equals(m);
    }

    @Override
    public void validate() {
        if (this.dimensions != this.getLeadingDiagonal().length()) {
            throw new VectorzException("dimension mismatch: " + this.dimensions);
        }
        super.validate();
    }

    @Override
    public abstract ADiagonalMatrix exactClone();

    @Override
    public AMatrix clone() {
        if (this.dimensions < 50) {
            return super.clone();
        }
        return this.sparseClone();
    }

    @Override
    public boolean hasUncountable() {
        return this.getLeadingDiagonal().hasUncountable();
    }

    @Override
    public double elementPowSum(double p) {
        return this.getLeadingDiagonal().elementPowSum(p);
    }

    @Override
    public double elementAbsPowSum(double p) {
        return this.getLeadingDiagonal().elementAbsPowSum(p);
    }
}

