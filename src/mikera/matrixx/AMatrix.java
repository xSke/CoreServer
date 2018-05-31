/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx;

import java.nio.DoubleBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import mikera.arrayz.Array;
import mikera.arrayz.Arrayz;
import mikera.arrayz.INDArray;
import mikera.arrayz.ISparse;
import mikera.arrayz.impl.AbstractArray;
import mikera.arrayz.impl.IDense;
import mikera.arrayz.impl.JoinedArray;
import mikera.arrayz.impl.SliceArray;
import mikera.indexz.Index;
import mikera.matrixx.IMatrix;
import mikera.matrixx.Matrix;
import mikera.matrixx.Matrixx;
import mikera.matrixx.algo.Definite;
import mikera.matrixx.algo.Determinant;
import mikera.matrixx.algo.Inverse;
import mikera.matrixx.algo.Multiplications;
import mikera.matrixx.algo.Rank;
import mikera.matrixx.impl.ADenseArrayMatrix;
import mikera.matrixx.impl.ARectangularMatrix;
import mikera.matrixx.impl.DenseColumnMatrix;
import mikera.matrixx.impl.IFastRows;
import mikera.matrixx.impl.IdentityMatrix;
import mikera.matrixx.impl.ImmutableMatrix;
import mikera.matrixx.impl.MatrixAsVector;
import mikera.matrixx.impl.MatrixBandView;
import mikera.matrixx.impl.MatrixColumnList;
import mikera.matrixx.impl.MatrixColumnView;
import mikera.matrixx.impl.MatrixElementIterator;
import mikera.matrixx.impl.MatrixRowIterator;
import mikera.matrixx.impl.MatrixRowList;
import mikera.matrixx.impl.MatrixRowView;
import mikera.matrixx.impl.SparseColumnMatrix;
import mikera.matrixx.impl.SparseRowMatrix;
import mikera.matrixx.impl.TransposedMatrix;
import mikera.matrixx.impl.ZeroMatrix;
import mikera.randomz.Hash;
import mikera.transformz.AAffineTransform;
import mikera.transformz.ATranslation;
import mikera.transformz.AffineMN;
import mikera.transformz.impl.IdentityTranslation;
import mikera.util.Maths;
import mikera.vectorz.AScalar;
import mikera.vectorz.AVector;
import mikera.vectorz.IOperator;
import mikera.vectorz.Op;
import mikera.vectorz.Tools;
import mikera.vectorz.Vector;
import mikera.vectorz.Vectorz;
import mikera.vectorz.impl.ADenseArrayVector;
import mikera.vectorz.impl.Vector0;
import mikera.vectorz.util.DoubleArrays;
import mikera.vectorz.util.ErrorMessages;
import mikera.vectorz.util.VectorzException;

public abstract class AMatrix
extends AbstractArray<AVector>
implements IMatrix {
    private static final long serialVersionUID = 4854869374064155441L;
    private static final double TOLERANCE = 1.0E-8;

    @Override
    public abstract int rowCount();

    @Override
    public abstract int columnCount();

    @Override
    public abstract double get(int var1, int var2);

    @Override
    public abstract void set(int var1, int var2, double var3);

    @Override
    public final double get(int row) {
        throw new VectorzException("1D get not supported on matrix!");
    }

    @Override
    public final double get() {
        throw new VectorzException("0D get not supported on matrix!");
    }

    @Override
    public void set(int row, double value) {
        throw new VectorzException("1D get not supported on matrix!");
    }

    @Override
    public void set(double value) {
        this.fill(value);
    }

    @Override
    public void fill(double value) {
        int len = this.rowCount();
        for (int i = 0; i < len; ++i) {
            this.getRowView(i).fill(value);
        }
    }

    public void unsafeSet(int row, int column, double value) {
        this.set(row, column, value);
    }

    public double unsafeGet(int row, int column) {
        return this.get(row, column);
    }

    @Override
    public void clamp(double min, double max) {
        int len = this.rowCount();
        for (int i = 0; i < len; ++i) {
            this.getRowView(i).clamp(min, max);
        }
    }

    @Override
    public void pow(double exponent) {
        int len = this.rowCount();
        for (int i = 0; i < len; ++i) {
            AVector v = this.getRowView(i);
            v.pow(exponent);
        }
    }

    @Override
    public void square() {
        int len = this.rowCount();
        for (int i = 0; i < len; ++i) {
            this.getRowView(i).square();
        }
    }

    @Override
    public void set(int[] indexes, double value) {
        if (indexes.length != 2) {
            throw new VectorzException("" + indexes.length + "D set not supported on AMatrix");
        }
        this.set(indexes[0], indexes[1], value);
    }

    @Override
    public final int dimensionality() {
        return 2;
    }

    public final int inputDimensions() {
        return this.columnCount();
    }

    public final int outputDimensions() {
        return this.rowCount();
    }

    @Override
    public long elementCount() {
        return (long)this.rowCount() * (long)this.columnCount();
    }

    @Override
    public final AVector slice(int row) {
        return this.getRowView(row);
    }

    @Override
    public AVector slice(int dimension, int index) {
        this.checkDimension(dimension);
        return dimension == 0 ? this.getRowView(index) : this.getColumnView(index);
    }

    @Override
    public int sliceCount() {
        return this.rowCount();
    }

    @Override
    public final List<AVector> getSlices() {
        return this.getRows();
    }

    @Override
    public List<AVector> getRows() {
        return new MatrixRowList(this);
    }

    @Override
    public List<AVector> getColumns() {
        return new MatrixColumnList(this);
    }

    @Override
    public List<INDArray> getSlices(int dimension) {
        this.checkDimension(dimension);
        int l = this.getShape(dimension);
        ArrayList<INDArray> al = new ArrayList<INDArray>(l);
        for (int i = 0; i < l; ++i) {
            al.add(this.slice(dimension, i));
        }
        return al;
    }

    @Override
    public List<INDArray> getSliceViews() {
        int rc = this.rowCount();
        ArrayList<INDArray> al = new ArrayList<INDArray>(rc);
        for (int i = 0; i < rc; ++i) {
            al.add(this.getRowView(i));
        }
        return al;
    }

    @Override
    public INDArray join(INDArray a, int dimension) {
        if (a instanceof AMatrix) {
            // empty if block
        }
        return JoinedArray.join(this, a, dimension);
    }

    @Override
    public int[] getShape() {
        return new int[]{this.rowCount(), this.columnCount()};
    }

    @Override
    public int[] getShapeClone() {
        return new int[]{this.rowCount(), this.columnCount()};
    }

    @Override
    public int getShape(int dim) {
        if (dim == 0) {
            return this.rowCount();
        }
        if (dim == 1) {
            return this.columnCount();
        }
        throw new IndexOutOfBoundsException(ErrorMessages.invalidDimension(this, dim));
    }

    @Override
    public long[] getLongShape() {
        return new long[]{this.rowCount(), this.columnCount()};
    }

    @Override
    public /* varargs */ double get(int ... indexes) {
        assert (indexes.length == 2);
        return this.get(indexes[0], indexes[1]);
    }

    public AVector getLeadingDiagonal() {
        return this.getBand(0);
    }

    public double calculateElement(int i, AVector v) {
        return this.getRow(i).dotProduct(v);
    }

    public double calculateElement(int i, Vector v) {
        return this.getRow(i).dotProduct(v);
    }

    public AAffineTransform toAffineTransform() {
        return new AffineMN(this, IdentityTranslation.create(this.rowCount()));
    }

    public boolean isIdentity() {
        int cc;
        int rc = this.rowCount();
        if (rc != (cc = this.columnCount())) {
            return false;
        }
        for (int i = 0; i < rc; ++i) {
            for (int j = 0; j < cc; ++j) {
                double expected;
                double d = expected = i == j ? 1.0 : 0.0;
                if (this.unsafeGet(i, j) == expected) continue;
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isSquare() {
        return this.rowCount() == this.columnCount();
    }

    public boolean isOrthogonal() {
        return this.isOrthogonal(1.0E-8);
    }

    public boolean isOrthogonal(double tolerance) {
        if (!this.isSquare()) {
            return false;
        }
        DenseColumnMatrix Q = DenseColumnMatrix.wrap(this.rowCount(), this.columnCount(), this.getTransposeView().toDoubleArray());
        for (int i = 0; i < Q.columnCount(); ++i) {
            AVector a = Q.getColumn(i);
            if (!a.isUnitLengthVector(tolerance)) {
                return false;
            }
            for (int j = i + 1; j < Q.columnCount(); ++j) {
                double val = a.innerProduct(Q.getColumn(j)).get();
                if (Math.abs(val) <= 1.0E-8) continue;
                return false;
            }
        }
        return true;
    }

    public boolean hasOrthonormalColumns() {
        return this.getTranspose().innerProduct(this).epsilonEquals(IdentityMatrix.create(this.columnCount()));
    }

    public boolean hasOrthonormalRows() {
        return this.innerProduct(this.getTranspose()).epsilonEquals(IdentityMatrix.create(this.rowCount()));
    }

    @Override
    public /* varargs */ INDArray reshape(int ... dimensions) {
        int ndims = dimensions.length;
        if (ndims == 1) {
            return this.toVector().subVector(0, dimensions[0]);
        }
        if (ndims == 2) {
            return Matrixx.createFromVector(this.asVector(), dimensions[0], dimensions[1]);
        }
        return Arrayz.createFromVector(this.toVector(), dimensions);
    }

    public Matrix reshape(int rows, int cols) {
        return Matrixx.createFromVector(this.asVector(), rows, cols);
    }

    @Override
    public AMatrix reorder(int[] order) {
        return this.reorder(0, order);
    }

    @Override
    public AMatrix reorder(int dim, int[] order) {
        int n = order.length;
        switch (dim) {
            case 0: {
                if (n == 0) {
                    return ZeroMatrix.create(0, this.columnCount());
                }
                ArrayList<AVector> al = new ArrayList<AVector>(n);
                for (int si : order) {
                    al.add(this.slice(si));
                }
                return SparseRowMatrix.wrap(al);
            }
            case 1: {
                if (n == 0) {
                    return ZeroMatrix.create(this.rowCount(), 0);
                }
                ArrayList<AVector> al = new ArrayList<AVector>(n);
                for (int si : order) {
                    al.add(this.slice(1, si));
                }
                return SparseColumnMatrix.wrap(al);
            }
        }
        throw new IndexOutOfBoundsException(ErrorMessages.invalidDimension(this, dim));
    }

    public AMatrix subMatrix(int rowStart, int rows, int colStart, int cols) {
        if (rows == 0 || cols == 0) {
            return ZeroMatrix.create(rows, cols);
        }
        AVector[] vs = new AVector[rows];
        for (int i = 0; i < rows; ++i) {
            vs[i] = this.getRowView(rowStart + i).subVector(colStart, cols);
        }
        return SparseRowMatrix.wrap(vs);
    }

    @Override
    public AMatrix subArray(int[] offsets, int[] shape) {
        if (offsets.length != 2) {
            throw new IllegalArgumentException(ErrorMessages.invalidIndex((INDArray)this, offsets));
        }
        if (shape.length != 2) {
            throw new IllegalArgumentException(ErrorMessages.illegalSize(shape));
        }
        return this.subMatrix(offsets[0], shape[0], offsets[1], shape[1]);
    }

    @Override
    public INDArray rotateView(int dimension, int shift) {
        int n = this.getShape(dimension);
        if (n == 0) {
            return this;
        }
        if ((shift = Maths.mod(shift, n)) == 0) {
            return this;
        }
        int[] off = new int[2];
        int[] shp = this.getShapeClone();
        shp[dimension] = shift;
        AMatrix right = this.subArray(off, shp);
        shp[dimension] = n - shift;
        off[dimension] = shift;
        AMatrix left = this.subArray(off, shp);
        return left.join(right, dimension);
    }

    @Override
    public AVector transform(AVector source) {
        Vector v = Vector.createLength(this.rowCount());
        if (source instanceof Vector) {
            this.transform((Vector)source, v);
        } else {
            this.transform(source, (AVector)v);
        }
        return v;
    }

    public Vector transform(Vector source) {
        Vector v = Vector.createLength(this.rowCount());
        this.transform(source, v);
        return v;
    }

    @Override
    public void transform(AVector source, AVector dest) {
        if (source instanceof Vector && dest instanceof Vector) {
            this.transform((Vector)source, (Vector)dest);
            return;
        }
        int rc = this.rowCount();
        int cc = this.columnCount();
        if (source.length() != cc) {
            throw new IllegalArgumentException(ErrorMessages.wrongSourceLength(source));
        }
        if (dest.length() != rc) {
            throw new IllegalArgumentException(ErrorMessages.wrongDestLength(dest));
        }
        for (int row = 0; row < rc; ++row) {
            dest.unsafeSet(row, this.getRow(row).dotProduct(source));
        }
    }

    public void transform(Vector source, Vector dest) {
        int rc = this.rowCount();
        int cc = this.columnCount();
        if (source.length() != cc) {
            throw new IllegalArgumentException(ErrorMessages.wrongSourceLength(source));
        }
        if (dest.length() != rc) {
            throw new IllegalArgumentException(ErrorMessages.wrongDestLength(dest));
        }
        for (int row = 0; row < rc; ++row) {
            dest.unsafeSet(row, this.getRow(row).dotProduct(source));
        }
    }

    @Override
    public void transformInPlace(AVector v) {
        if (v instanceof ADenseArrayVector) {
            this.transformInPlace((ADenseArrayVector)v);
            return;
        }
        double[] temp = new double[v.length()];
        int rc = this.rowCount();
        int cc = this.columnCount();
        if (v.length() != rc) {
            throw new IllegalArgumentException(ErrorMessages.incompatibleShapes(this, (INDArray)v));
        }
        if (rc != cc) {
            throw new UnsupportedOperationException("Cannot transform in place with a non-square transformation");
        }
        for (int row = 0; row < rc; ++row) {
            temp[row] = this.getRow(row).dotProduct(v);
        }
        v.setElements(temp);
    }

    public void transformInPlace(ADenseArrayVector v) {
        double[] temp = new double[v.length()];
        int rc = this.rowCount();
        int cc = this.columnCount();
        if (v.length() != rc) {
            throw new IllegalArgumentException(ErrorMessages.incompatibleShapes(this, (INDArray)v));
        }
        if (rc != cc) {
            throw new UnsupportedOperationException("Cannot transform in place with a non-square transformation");
        }
        double[] data = v.getArray();
        int offset = v.getArrayOffset();
        for (int row = 0; row < rc; ++row) {
            temp[row] = this.getRow(row).dotProduct(data, offset);
        }
        v.setElements(temp);
    }

    @Override
    public AVector getRow(int row) {
        return this.getRowView(row);
    }

    @Override
    public AVector getColumn(int column) {
        return this.getColumnView(column);
    }

    public AVector getRowView(int row) {
        return new MatrixRowView(this, row);
    }

    public AVector getColumnView(int column) {
        return new MatrixColumnView(this, column);
    }

    public AVector getRowClone(int row) {
        int cc = this.columnCount();
        Vector v = Vector.createLength(cc);
        this.copyRowTo(row, v.getArray(), 0);
        return v;
    }

    public AVector getColumnClone(int column) {
        int rc = this.rowCount();
        Vector v = Vector.createLength(rc);
        this.copyColumnTo(column, v.getArray(), 0);
        return v;
    }

    public void set(AMatrix a) {
        int rc = this.rowCount();
        int cc = this.columnCount();
        a.checkShape(rc, cc);
        for (int i = 0; i < rc; ++i) {
            this.setRow(i, a.getRow(i));
        }
    }

    @Override
    public void set(INDArray a) {
        if (a instanceof AMatrix) {
            this.set((AMatrix)a);
            return;
        }
        if (a instanceof AVector) {
            this.set((AVector)a);
            return;
        }
        if (a instanceof AScalar) {
            this.set(a.get());
            return;
        }
        throw new UnsupportedOperationException("Can't set matrix to array: " + a.getClass() + " with shape: " + Arrays.toString(a.getShape()));
    }

    public void set(AVector v) {
        int rc = this.rowCount();
        for (int i = 0; i < rc; ++i) {
            this.getRowView(i).set(v);
        }
    }

    @Override
    public void set(Object o) {
        if (o instanceof INDArray) {
            this.set((INDArray)o);
        } else if (o instanceof Number) {
            this.set(((Number)o).doubleValue());
        } else {
            this.set(Matrixx.toMatrix(o));
        }
    }

    @Override
    public void getElements(double[] dest, int offset) {
        int rc = this.rowCount();
        int cc = this.columnCount();
        for (int i = 0; i < rc; ++i) {
            this.copyRowTo(i, dest, offset + i * cc);
        }
    }

    @Override
    public final void copyTo(double[] arr) {
        this.getElements(arr, 0);
    }

    @Override
    public /* varargs */ void setElements(double ... values) {
        int vl = values.length;
        if ((long)vl != this.elementCount()) {
            throw new IllegalArgumentException("Incorrect number of elements in array: " + vl);
        }
        this.setElements(values, 0);
    }

    @Override
    public void setElements(double[] values, int offset) {
        int rc = this.rowCount();
        int cc = this.columnCount();
        for (int i = 0; i < rc; ++i) {
            this.slice(i).setElements(values, offset + i * cc);
        }
    }

    @Override
    public abstract boolean isFullyMutable();

    @Override
    public boolean isMutable() {
        return this.isFullyMutable();
    }

    @Override
    public boolean isElementConstrained() {
        return false;
    }

    @Override
    public AMatrix clone() {
        return Matrixx.deepCopy(this);
    }

    @Override
    public AMatrix copy() {
        if (this.isMutable()) {
            return this.clone();
        }
        return this;
    }

    public final AVector cloneRow(int i) {
        return this.getRowClone(i);
    }

    public final AVector cloneColumn(int j) {
        return this.getColumnClone(j);
    }

    @Override
    public AMatrix sparseClone() {
        return Matrixx.createSparse(this);
    }

    @Override
    public AMatrix ensureMutable() {
        if (this.isFullyMutable() && !this.isView()) {
            return this;
        }
        return this.clone();
    }

    public double determinant() {
        return Determinant.calculate(this);
    }

    public int rank() {
        return Rank.compute(this);
    }

    public AMatrix toMutableMatrix() {
        return Matrixx.create(this);
    }

    public void transposeInPlace() {
        int dims = this.checkSquare();
        for (int i = 0; i < dims; ++i) {
            for (int j = i + 1; j < dims; ++j) {
                double temp = this.unsafeGet(i, j);
                this.unsafeSet(i, j, this.unsafeGet(j, i));
                this.unsafeSet(j, i, temp);
            }
        }
    }

    @Override
    public AMatrix getTranspose() {
        return this.getTransposeView();
    }

    @Override
    public AMatrix getTransposeView() {
        return TransposedMatrix.wrap(this);
    }

    @Override
    public AMatrix getTransposeCopy() {
        return this.copy().getTranspose();
    }

    public void add(AMatrix m) {
        int rc = this.rowCount();
        int cc = this.columnCount();
        m.checkShape(rc, cc);
        for (int i = 0; i < rc; ++i) {
            this.getRowView(i).add(m.getRow(i));
        }
    }

    public void add(AVector v) {
        int rc = this.rowCount();
        int cc = this.columnCount();
        if (cc != v.length()) {
            throw new IllegalArgumentException(ErrorMessages.mismatch(this, v));
        }
        for (int i = 0; i < rc; ++i) {
            this.getRowView(i).add(v);
        }
    }

    public void sub(AVector v) {
        int rc = this.rowCount();
        int cc = this.columnCount();
        if (cc != v.length()) {
            throw new IllegalArgumentException(ErrorMessages.incompatibleShapes(this, (INDArray)v));
        }
        for (int i = 0; i < rc; ++i) {
            this.getRowView(i).sub(v);
        }
    }

    @Override
    public void sub(double d) {
        this.add(- d);
    }

    @Override
    public final void scaleAdd(double factor, double constant) {
        this.multiply(factor);
        this.add(constant);
    }

    @Override
    public void multiply(double factor) {
        int rc = this.rowCount();
        for (int i = 0; i < rc; ++i) {
            this.getRowView(i).multiply(factor);
        }
    }

    @Override
    public AMatrix multiplyCopy(double factor) {
        AMatrix r = this.clone();
        r.multiply(factor);
        return r;
    }

    @Override
    public double elementSum() {
        int rc = this.rowCount();
        double result = 0.0;
        for (int i = 0; i < rc; ++i) {
            result += this.getRow(i).elementSum();
        }
        return result;
    }

    @Override
    public double elementSquaredSum() {
        int rc = this.rowCount();
        double result = 0.0;
        for (int i = 0; i < rc; ++i) {
            result += this.getRow(i).elementSquaredSum();
        }
        return result;
    }

    @Override
    public Iterator<Double> elementIterator() {
        return new MatrixElementIterator(this);
    }

    @Override
    public boolean isBoolean() {
        double[] data = Tools.getElements(this);
        return DoubleArrays.isBoolean(data, 0, data.length);
    }

    @Override
    public long nonZeroCount() {
        long result = 0L;
        int rc = this.rowCount();
        for (int i = 0; i < rc; ++i) {
            result += this.getRow(i).nonZeroCount();
        }
        return result;
    }

    public void sub(AMatrix m) {
        this.addMultiple(m, -1.0);
    }

    public void sub(AScalar a) {
        this.add(- a.get());
    }

    public void add(AScalar a) {
        this.add(a.get());
    }

    @Override
    public void negate() {
        this.multiply(-1.0);
    }

    @Override
    public void reciprocal() {
        int sc = this.rowCount();
        for (int i = 0; i < sc; ++i) {
            this.getRowView(i).reciprocal();
        }
    }

    @Override
    public void abs() {
        int sc = this.rowCount();
        for (int i = 0; i < sc; ++i) {
            this.getRowView(i).abs();
        }
    }

    @Override
    public void sqrt() {
        int sc = this.rowCount();
        for (int i = 0; i < sc; ++i) {
            this.getRowView(i).sqrt();
        }
    }

    @Override
    public void log() {
        int sc = this.rowCount();
        for (int i = 0; i < sc; ++i) {
            this.getRowView(i).log();
        }
    }

    @Override
    public void exp() {
        int sc = this.rowCount();
        for (int i = 0; i < sc; ++i) {
            this.getRowView(i).exp();
        }
    }

    @Override
    public void signum() {
        int sc = this.rowCount();
        for (int i = 0; i < sc; ++i) {
            this.getRowView(i).signum();
        }
    }

    public void elementMul(AMatrix m) {
        int rc = this.rowCount();
        this.checkSameShape(m);
        for (int i = 0; i < rc; ++i) {
            this.getRowView(i).multiply(m.getRow(i));
        }
    }

    private void elementDiv(AMatrix m) {
        int rc = this.rowCount();
        int cc = this.columnCount();
        m.checkShape(rc, cc);
        for (int i = 0; i < rc; ++i) {
            this.getRowView(i).divide(m.getRow(i));
        }
    }

    public void mul(AMatrix a) {
        this.composeWith(a);
    }

    public void multiplyRow(int i, double factor) {
        this.getRowView(i).multiply(factor);
    }

    public void addRowMultiple(int src, int dst, double factor) {
        this.getRowView(dst).addMultiple(this.getRow(src), factor);
    }

    @Override
    public void addToArray(double[] data, int offset) {
        int cc = this.columnCount();
        int rc = this.rowCount();
        for (int i = 0; i < rc; ++i) {
            this.getRow(i).addToArray(data, offset + i * cc);
        }
    }

    public void swapRows(int i, int j) {
        if (i == j) {
            return;
        }
        AVector a = this.getRowView(i);
        AVector b = this.getRowView(j);
        int cc = this.columnCount();
        for (int k = 0; k < cc; ++k) {
            double t = a.unsafeGet(k);
            a.unsafeSet(k, b.unsafeGet(k));
            b.unsafeSet(k, t);
        }
    }

    public void swapColumns(int i, int j) {
        if (i == j) {
            return;
        }
        AVector a = this.getColumnView(i);
        AVector b = this.getColumnView(j);
        int rc = this.rowCount();
        for (int k = 0; k < rc; ++k) {
            double t = a.unsafeGet(k);
            a.unsafeSet(k, b.unsafeGet(k));
            b.unsafeSet(k, t);
        }
    }

    public void composeWith(AMatrix a) {
        AMatrix t = this.compose(a);
        this.set(t);
    }

    @Override
    public boolean isView() {
        return false;
    }

    public void addMultiple(AMatrix m, double factor) {
        int rc = this.rowCount();
        int cc = this.columnCount();
        m.checkShape(rc, cc);
        for (int i = 0; i < rc; ++i) {
            this.getRowView(i).addMultiple(m.getRow(i), factor);
        }
    }

    @Override
    public Iterator<AVector> iterator() {
        return new MatrixRowIterator(this);
    }

    @Override
    public boolean epsilonEquals(INDArray a) {
        return this.epsilonEquals(a, 1.0E-7);
    }

    @Override
    public boolean epsilonEquals(INDArray a, double epsilon) {
        if (a instanceof AMatrix) {
            return this.epsilonEquals((AMatrix)a, epsilon);
        }
        if (a.dimensionality() != 2) {
            return false;
        }
        int sc = this.rowCount();
        if (a.sliceCount() != sc) {
            return false;
        }
        for (int i = 0; i < sc; ++i) {
            AVector s = this.getRow(i);
            if (s.epsilonEquals(a.slice(i), epsilon)) continue;
            return false;
        }
        return true;
    }

    public boolean epsilonEquals(AMatrix a, double epsilon) {
        if (a == this) {
            return true;
        }
        int sc = this.rowCount();
        if (a.rowCount() != sc) {
            return false;
        }
        for (int i = 0; i < sc; ++i) {
            AVector s = this.getRow(i);
            if (s.epsilonEquals(a.getRow(i), epsilon)) continue;
            return false;
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof AMatrix) {
            return this.equals((AMatrix)o);
        }
        if (o instanceof INDArray) {
            return this.equals((INDArray)o);
        }
        return false;
    }

    public boolean equals(AMatrix a) {
        if (a instanceof ADenseArrayMatrix) {
            return a.equals(this);
        }
        if (a == this) {
            return true;
        }
        int rc = this.rowCount();
        if (rc != a.rowCount()) {
            return false;
        }
        return this.equalsByRows(a);
    }

    @Override
    public boolean equalsArray(double[] data, int offset) {
        int rc = this.rowCount();
        int cc = this.columnCount();
        for (int i = 0; i < rc; ++i) {
            if (this.getRow(i).equalsArray(data, offset + i * cc)) continue;
            return false;
        }
        return true;
    }

    @Override
    public boolean elementsEqual(double value) {
        int rc = this.rowCount();
        for (int i = 0; i < rc; ++i) {
            if (this.getRow(i).elementsEqual(value)) continue;
            return false;
        }
        return true;
    }

    public boolean equalsTranspose(AMatrix a) {
        int rc = this.rowCount();
        if (rc != a.columnCount()) {
            return false;
        }
        int cc = this.columnCount();
        if (cc != a.rowCount()) {
            return false;
        }
        for (int i = 0; i < rc; ++i) {
            if (this.getRow(i).equals(a.getColumn(i))) continue;
            return false;
        }
        return true;
    }

    @Override
    public boolean equals(INDArray v) {
        if (v instanceof AMatrix) {
            return this.equals((AMatrix)v);
        }
        if (!this.isSameShape(v)) {
            return false;
        }
        int rc = this.rowCount();
        for (int i = 0; i < rc; ++i) {
            if (this.getRow(i).equals(v.slice(i))) continue;
            return false;
        }
        return true;
    }

    public boolean equals(ADenseArrayMatrix a) {
        if (!this.isSameShape(a)) {
            return false;
        }
        return this.equalsArray(a.getArray(), a.getArrayOffset());
    }

    public boolean epsilonEquals(AMatrix a) {
        int rc = this.rowCount();
        int cc = this.columnCount();
        a.checkShape(rc, cc);
        if (this instanceof IFastRows && a instanceof IFastRows) {
            for (int i = 0; i < rc; ++i) {
                if (this.getRow(i).epsilonEquals(a.getRow(i))) continue;
                return false;
            }
        } else {
            for (int i = 0; i < rc; ++i) {
                for (int j = 0; j < cc; ++j) {
                    if (Tools.epsilonEquals(this.unsafeGet(i, j), a.unsafeGet(i, j))) continue;
                    return false;
                }
            }
        }
        return true;
    }

    protected boolean equalsByRows(AMatrix m) {
        int rc = this.rowCount();
        for (int i = 0; i < rc; ++i) {
            if (this.getRow(i).equals(m.getRow(i))) continue;
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        if (this.elementCount() > 10000L) {
            Index shape = Index.create(this.getShape());
            return "Large matrix with shape: " + shape.toString();
        }
        return this.toStringFull();
    }

    @Override
    public String toStringFull() {
        StringBuilder sb = new StringBuilder();
        int rc = this.rowCount();
        sb.append("[");
        for (int i = 0; i < rc; ++i) {
            if (i > 0) {
                sb.append(",\n");
            }
            sb.append(this.getRow(i).toString());
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int hashCode = 1;
        int rc = this.rowCount();
        int cc = this.columnCount();
        for (int i = 0; i < rc; ++i) {
            for (int j = 0; j < cc; ++j) {
                hashCode = 31 * hashCode + Hash.hashCode(this.unsafeGet(i, j));
            }
        }
        return hashCode;
    }

    @Override
    public AVector asVector() {
        int rc = this.rowCount();
        if (rc == 0) {
            return Vector0.INSTANCE;
        }
        if (rc == 1) {
            return this.getRowView(0);
        }
        int cc = this.columnCount();
        if (cc == 1) {
            return this.getColumnView(0);
        }
        return new MatrixAsVector(this);
    }

    @Override
    public List<Double> asElementList() {
        return this.asVector().asElementList();
    }

    public final AMatrix compose(AMatrix a) {
        return this.innerProduct(a);
    }

    public AMatrix innerProduct(AMatrix a) {
        return Multiplications.multiply(this, a);
    }

    public final Vector innerProduct(Vector v) {
        return this.transform(v);
    }

    public AMatrix innerProduct(Matrix a) {
        return Multiplications.multiply(this, (AMatrix)a);
    }

    @Override
    public AVector innerProduct(AVector v) {
        if (v instanceof Vector) {
            return this.transform((Vector)v);
        }
        return this.transform(v);
    }

    @Override
    public final AMatrix innerProduct(AScalar s) {
        return this.innerProduct(s.get());
    }

    @Override
    public final AMatrix innerProduct(double d) {
        return this.multiplyCopy(d);
    }

    public AMatrix transposeInnerProduct(AMatrix s) {
        if (s instanceof Matrix) {
            return this.transposeInnerProduct((Matrix)s);
        }
        if (this.isSparse()) {
            AMatrix t = this.getTranspose();
            if (t instanceof TransposedMatrix) {
                t = t.sparseClone();
            }
            return t.innerProduct(s);
        }
        Matrix t = this.toMatrixTranspose();
        return t.innerProduct(s);
    }

    public AMatrix transposeInnerProduct(Matrix s) {
        Matrix r = this.toMatrixTranspose();
        return Multiplications.multiply(r, (AMatrix)s);
    }

    @Override
    public INDArray innerProduct(INDArray a) {
        if (a instanceof AVector) {
            return this.innerProduct((AVector)a);
        }
        if (a instanceof AMatrix) {
            return this.compose((AMatrix)a);
        }
        if (a instanceof AScalar) {
            return this.innerProduct((AScalar)a);
        }
        if (a.dimensionality() <= 2) {
            return this.innerProduct(Arrayz.create(a));
        }
        int rc = this.rowCount();
        List<AVector> al = this.getRows();
        ArrayList<INDArray> rl = new ArrayList<INDArray>(rc);
        for (AVector v : al) {
            rl.add(v.innerProduct(a));
        }
        return SliceArray.create(rl);
    }

    @Override
    public INDArray outerProduct(INDArray a) {
        ArrayList<INDArray> al = new ArrayList<INDArray>(this.sliceCount());
        for (AVector s : this) {
            al.add(s.outerProduct(a));
        }
        return Arrayz.create(al);
    }

    @Override
    public AMatrix inverse() {
        return Inverse.calculate(this);
    }

    public double trace() {
        int rc = Math.min(this.rowCount(), this.columnCount());
        double result = 0.0;
        for (int i = 0; i < rc; ++i) {
            result += this.unsafeGet(i, i);
        }
        return result;
    }

    @Override
    public double diagonalProduct() {
        int rc = Math.min(this.rowCount(), this.columnCount());
        double result = 1.0;
        for (int i = 0; i < rc; ++i) {
            result *= this.unsafeGet(i, i);
        }
        return result;
    }

    @Override
    public boolean isInvertible() {
        return this.isSquare() && this.determinant() != 0.0;
    }

    @Override
    public Vector toVector() {
        int rc = this.rowCount();
        int cc = this.columnCount();
        Vector v = Vector.createLength(rc * cc);
        this.getElements(v.getArray(), 0);
        return v;
    }

    @Override
    public Array toArray() {
        return Array.create(this);
    }

    public Matrix toMatrix() {
        int rc = this.rowCount();
        int cc = this.columnCount();
        return Matrix.wrap(rc, cc, this.toDoubleArray());
    }

    public Matrix toMatrixTranspose() {
        int rc = this.rowCount();
        int cc = this.columnCount();
        return Matrix.wrap(cc, rc, this.getTranspose().toDoubleArray());
    }

    @Override
    public void toDoubleBuffer(DoubleBuffer dest) {
        int n = this.rowCount();
        for (int i = 0; i < n; ++i) {
            this.getRow(i).toDoubleBuffer(dest);
        }
    }

    @Override
    public double[] toDoubleArray() {
        double[] result = Matrix.createStorage(this.rowCount(), this.columnCount());
        this.getElements(result, 0);
        return result;
    }

    @Override
    public INDArray[] toSliceArray() {
        int n = this.sliceCount();
        INDArray[] al = new INDArray[n];
        for (int i = 0; i < n; ++i) {
            al[i] = this.slice(i);
        }
        return al;
    }

    @Override
    public double[] asDoubleArray() {
        return null;
    }

    @Override
    public void applyOp(Op op) {
        int rc = this.rowCount();
        for (int i = 0; i < rc; ++i) {
            this.getRowView(i).applyOp(op);
        }
    }

    @Override
    public void applyOp(IOperator op) {
        if (op instanceof Op) {
            this.applyOp((Op)op);
            return;
        }
        int rc = this.rowCount();
        for (int i = 0; i < rc; ++i) {
            this.getRowView(i).applyOp(op);
        }
    }

    @Override
    public void add(INDArray a) {
        if (a instanceof AMatrix) {
            this.add((AMatrix)a);
        } else if (a instanceof AVector) {
            this.add((AVector)a);
        } else {
            int dims = a.dimensionality();
            if (dims > 2) {
                throw new IllegalArgumentException(ErrorMessages.incompatibleShapes(this, a));
            }
            if (dims == 0) {
                this.add(a.get());
            } else if (dims == 1) {
                this.add(Vectorz.toVector(a));
            } else if (dims == 2) {
                this.add(Matrixx.toMatrix(a));
            }
        }
    }

    public void multiply(AVector v) {
        int rc = this.rowCount();
        for (int i = 0; i < rc; ++i) {
            this.getRowView(i).multiply(v);
        }
    }

    @Override
    public void multiply(INDArray a) {
        if (a instanceof AMatrix) {
            this.elementMul((AMatrix)a);
        } else if (a instanceof AVector) {
            this.multiply((AVector)a);
        } else {
            int dims = a.dimensionality();
            if (dims == 0) {
                this.multiply(a.get());
            } else if (dims == 1) {
                this.multiply(Vectorz.toVector(a));
            } else if (dims == 2) {
                this.multiply(Matrixx.toMatrix(a));
            } else {
                throw new IllegalArgumentException(ErrorMessages.incompatibleShapes(this, a));
            }
        }
    }

    public void divide(AVector a) {
        int rc = this.rowCount();
        for (int i = 0; i < rc; ++i) {
            this.getRowView(i).divide(a);
        }
    }

    @Override
    public void divide(INDArray a) {
        if (a instanceof AMatrix) {
            this.elementDiv((AMatrix)a);
        } else if (a instanceof AVector) {
            this.divide((AVector)a);
        } else {
            int dims = a.dimensionality();
            int rc = this.rowCount();
            if (dims == 0) {
                this.multiply(1.0 / a.get());
            } else if (dims == 1) {
                for (int i = 0; i < rc; ++i) {
                    this.slice(i).divide(a);
                }
            } else if (dims == 2) {
                for (int i = 0; i < rc; ++i) {
                    this.slice(i).divide(a.slice(i));
                }
            } else {
                throw new IllegalArgumentException(ErrorMessages.incompatibleShapes(this, a));
            }
        }
    }

    @Override
    public void divide(double factor) {
        this.multiply(1.0 / factor);
    }

    @Override
    public void sub(INDArray a) {
        if (a instanceof AMatrix) {
            this.sub((AMatrix)a);
        } else if (a instanceof AVector) {
            this.sub((AVector)a);
        } else {
            int dims = a.dimensionality();
            if (dims == 0) {
                this.sub(a.get());
            } else if (dims == 1) {
                this.sub(Vectorz.toVector(a));
            } else if (dims == 2) {
                this.sub(Matrixx.toMatrix(a));
            }
        }
    }

    @Override
    public void add(double d) {
        int rc = this.rowCount();
        for (int i = 0; i < rc; ++i) {
            this.getRowView(i).add(d);
        }
    }

    public void addAt(int i, int j, double d) {
        this.unsafeSet(i, j, this.unsafeGet(i, j) + d);
    }

    @Override
    public /* varargs */ INDArray broadcast(int ... targetShape) {
        int tdims = targetShape.length;
        if (tdims < 2) {
            throw new IllegalArgumentException(ErrorMessages.incompatibleBroadcast((INDArray)this, targetShape));
        }
        if (2 == tdims) {
            if (this.rowCount() == targetShape[0] && this.columnCount() == targetShape[1]) {
                return this;
            }
            throw new IllegalArgumentException(ErrorMessages.incompatibleBroadcast((INDArray)this, targetShape));
        }
        if (this.rowCount() != targetShape[tdims - 2] || this.columnCount() != targetShape[tdims - 1]) {
            throw new IllegalArgumentException(ErrorMessages.incompatibleBroadcast((INDArray)this, targetShape));
        }
        int n = targetShape[0];
        INDArray s = this.broadcast(Arrays.copyOfRange(targetShape, 1, tdims));
        return SliceArray.repeat(s, n);
    }

    @Override
    public INDArray broadcastLike(INDArray target) {
        if (target instanceof AMatrix) {
            return this.broadcastLike((AMatrix)target);
        }
        return this.broadcast(target.getShape());
    }

    @Override
    public AMatrix broadcastLike(AMatrix target) {
        if (this.rowCount() == target.rowCount() && this.columnCount() == target.columnCount()) {
            return this;
        }
        throw new IllegalArgumentException(ErrorMessages.incompatibleBroadcast((INDArray)this, target));
    }

    @Override
    public INDArray broadcastCloneLike(INDArray target) {
        INDArray r = this;
        if (target.dimensionality() > 2) {
            r = r.broadcastLike(target);
        }
        return r.clone();
    }

    @Override
    public boolean isZero() {
        int rc = this.rowCount();
        for (int i = 0; i < rc; ++i) {
            if (this.getRow(i).isZero()) continue;
            return false;
        }
        return true;
    }

    public boolean isPositiveDefinite() {
        return Definite.isPositiveDefinite(this);
    }

    public boolean isDiagonal() {
        int cc;
        int rc = this.rowCount();
        if (rc != (cc = this.columnCount())) {
            return false;
        }
        for (int i = 0; i < rc; ++i) {
            AVector r = this.getRow(i);
            if (!r.isRangeZero(0, i - 1)) {
                return false;
            }
            if (r.isRangeZero(i + 1, cc - i - 1)) continue;
            return false;
        }
        return true;
    }

    @Override
    public boolean isSameShape(INDArray a) {
        if (a instanceof AMatrix) {
            return this.isSameShape((AMatrix)a);
        }
        if (a.dimensionality() != 2) {
            return false;
        }
        if (this.getShape(0) != a.getShape(0)) {
            return false;
        }
        if (this.getShape(1) != a.getShape(1)) {
            return false;
        }
        return true;
    }

    public boolean isSameShape(AMatrix a) {
        return this.rowCount() == a.rowCount() && this.columnCount() == a.columnCount();
    }

    public boolean isRectangularDiagonal() {
        int rc = this.rowCount();
        int cc = this.columnCount();
        for (int i = 0; i < rc; ++i) {
            AVector r = this.getRow(i);
            if (i < cc) {
                if (!r.isRangeZero(0, i - 1)) {
                    return false;
                }
                if (r.isRangeZero(i + 1, cc - i - 1)) continue;
                return false;
            }
            if (r.isZero()) continue;
            return false;
        }
        return true;
    }

    @Override
    public boolean isSymmetric() {
        int cc;
        int rc = this.rowCount();
        if (rc != (cc = this.columnCount())) {
            return false;
        }
        for (int i = 0; i < rc; ++i) {
            if (this.getRow(i).equals(this.getColumn(i))) continue;
            return false;
        }
        return true;
    }

    public final boolean isHermitian() {
        return this.isSymmetric();
    }

    public boolean isUpperTriangular() {
        int rc = this.rowCount();
        int cc = this.columnCount();
        for (int i = 1; i < rc; ++i) {
            if (this.getRow(i).isRangeZero(0, Math.min(i, cc))) continue;
            return false;
        }
        return true;
    }

    public boolean isLowerTriangular() {
        int rc = this.rowCount();
        int cc = this.columnCount();
        for (int i = 0; i < rc; ++i) {
            int start = Math.min(cc, i + 1);
            if (this.getRow(i).isRangeZero(start, cc - start)) continue;
            return false;
        }
        return true;
    }

    public int upperBandwidthLimit() {
        return this.columnCount() - 1;
    }

    public int lowerBandwidthLimit() {
        return this.rowCount() - 1;
    }

    public int bandLength(int band) {
        return AMatrix.bandLength(this.rowCount(), this.columnCount(), band);
    }

    public final int bandStartRow(int band) {
        return band < 0 ? - band : 0;
    }

    public final int bandStartColumn(int band) {
        return band > 0 ? band : 0;
    }

    protected static final int bandLength(int rc, int cc, int band) {
        if (band > 0) {
            return band < cc ? Math.min(rc, cc - band) : 0;
        }
        return (band = - band) < rc ? Math.min(cc, rc - band) : 0;
    }

    public final int bandIndex(int i, int j) {
        return j - i;
    }

    public final int bandPosition(int i, int j) {
        return Math.min(i, j);
    }

    public int upperBandwidth() {
        for (int w = this.upperBandwidthLimit(); w > 0; --w) {
            if (this.getBand(w).isZero()) continue;
            return w;
        }
        return 0;
    }

    public int lowerBandwidth() {
        for (int w = this.lowerBandwidthLimit(); w > 0; --w) {
            if (this.getBand(- w).isZero()) continue;
            return w;
        }
        return 0;
    }

    @Override
    public AVector getBand(int band) {
        return MatrixBandView.create(this, band);
    }

    public AVector getBandWrapped(int band) {
        AVector result;
        int cc;
        result = Vector0.INSTANCE;
        int rc = this.rowCount();
        if (rc < (cc = this.columnCount())) {
            int si = band % rc;
            if (si > 0) {
                si -= rc;
            }
            while (si < cc) {
                result = result.join(this.getBand(si));
                si += rc;
            }
        } else {
            if (cc == 0) {
                return result;
            }
            int si = band % cc;
            if (si < 0) {
                si += cc;
            }
            while (si > - rc) {
                result = result.join(this.getBand(si));
                si -= cc;
            }
        }
        return result;
    }

    public void setRow(int i, AVector row) {
        this.getRowView(i).set(row);
    }

    public void replaceRow(int i, AVector row) {
        throw new UnsupportedOperationException("replaceRow not supported for " + this.getClass() + ". Consider using an AVectorMatrix or SparseRowMatrix instance instead.");
    }

    public void replaceColumn(int i, AVector row) {
        throw new UnsupportedOperationException("replaceColumn not supported for " + this.getClass() + ". Consider using a SparseColumnMatrix instance instead.");
    }

    public void setColumn(int i, AVector col) {
        this.getColumnView(i).set(col);
    }

    @Override
    public abstract AMatrix exactClone();

    @Override
    public INDArray immutable() {
        if (!this.isMutable()) {
            return this;
        }
        return ImmutableMatrix.create(this);
    }

    @Override
    public AMatrix mutable() {
        if (this.isFullyMutable()) {
            return this;
        }
        return this.clone();
    }

    @Override
    public AMatrix sparse() {
        if (this instanceof ISparse) {
            return this;
        }
        return Matrixx.createSparse(this);
    }

    @Override
    public AMatrix dense() {
        if (this instanceof IDense) {
            return this;
        }
        return Matrix.create(this);
    }

    @Override
    public final Matrix denseClone() {
        return Matrix.create(this);
    }

    @Override
    public void validate() {
    }

    public void copyRowTo(int i, double[] dest, int destOffset) {
        int cc = this.columnCount();
        for (int j = 0; j < cc; ++j) {
            dest[destOffset + j] = this.unsafeGet(i, j);
        }
    }

    public void copyColumnTo(int j, double[] dest, int destOffset) {
        int rc = this.rowCount();
        for (int i = 0; i < rc; ++i) {
            dest[destOffset + i] = this.unsafeGet(i, j);
        }
    }

    @Override
    public void addAt(int i, double d) {
        int cc = this.columnCount();
        this.addAt(i / cc, i % cc, d);
    }

    public void subAt(int i, double d) {
        int cc = this.columnCount();
        this.addAt(i / cc, i % cc, - d);
    }

    public void divideAt(int i, double d) {
        int cc = this.columnCount();
        int y = i / cc;
        int x = i % cc;
        this.unsafeSet(y, x, this.unsafeGet(y, x) / d);
    }

    public void multiplyAt(int i, double d) {
        int cc = this.columnCount();
        int y = i / cc;
        int x = i % cc;
        this.unsafeSet(y, x, this.unsafeGet(y, x) * d);
    }

    @Override
    public final INDArray addCopy(INDArray a) {
        if (a instanceof AMatrix) {
            return this.addCopy((AMatrix)a);
        }
        INDArray r = this.broadcastCloneLike(a);
        r.add(a);
        return r;
    }

    @Override
    public AMatrix addCopy(AMatrix a) {
        AMatrix m = this.clone();
        m.add(a);
        return m;
    }

    public Matrix addCopy(Matrix a) {
        this.checkSameShape(a);
        Matrix r = a.clone();
        this.addToArray(r.data, 0);
        return r;
    }

    @Override
    public boolean hasUncountable() {
        int rc = this.rowCount();
        for (int i = 0; i < rc; ++i) {
            if (!this.getRow(i).hasUncountable()) continue;
            return true;
        }
        return false;
    }

    public int checkSquare() {
        int rc = this.rowCount();
        if (rc != this.columnCount()) {
            throw new UnsupportedOperationException(ErrorMessages.nonSquareMatrix(this));
        }
        return rc;
    }

    protected int checkRowCount(int expected) {
        int rc = this.rowCount();
        if (rc != expected) {
            throw new IllegalArgumentException("Unexpected row count: " + rc + " expected: " + expected);
        }
        return rc;
    }

    protected int checkColumnCount(int expected) {
        int cc = this.columnCount();
        if (cc != expected) {
            throw new IllegalArgumentException("Unexpected column count: " + cc + " expected: " + expected);
        }
        return cc;
    }

    @Override
    protected final void checkDimension(int dimension) {
        if (dimension < 0 || dimension >= 2) {
            throw new IndexOutOfBoundsException(ErrorMessages.invalidDimension(this, dimension));
        }
    }

    protected void checkSameShape(AMatrix m) {
        int rc = this.rowCount();
        int cc = this.columnCount();
        if (rc != m.rowCount() || cc != m.columnCount()) {
            throw new IndexOutOfBoundsException(ErrorMessages.mismatch(this, m));
        }
    }

    protected void checkSameShape(ARectangularMatrix m) {
        int rc = this.rowCount();
        int cc = this.columnCount();
        if (rc != m.rowCount() || cc != m.columnCount()) {
            throw new IndexOutOfBoundsException(ErrorMessages.mismatch(this, m));
        }
    }

    protected void checkShape(int rows, int cols) {
        int rc = this.rowCount();
        int cc = this.columnCount();
        if (rc != rows || cc != cols) {
            throw new IllegalArgumentException("Unexpected shape: [" + cc + "," + rc + "] expected: [" + rows + "," + cols + "]");
        }
    }

    protected void checkIndex(int i, int j) {
        int rc = this.rowCount();
        int cc = this.columnCount();
        if (i < 0 || i >= rc || j < 0 || j >= cc) {
            throw new IndexOutOfBoundsException(ErrorMessages.invalidIndex((INDArray)this, i, j));
        }
    }

    @Override
    public void add2(AMatrix a, AMatrix b) {
        this.add(a);
        this.add(b);
    }
}

