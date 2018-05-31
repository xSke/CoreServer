/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import mikera.arrayz.INDArray;
import mikera.indexz.Index;
import mikera.matrixx.AMatrix;
import mikera.matrixx.IMatrix;
import mikera.matrixx.Matrix;
import mikera.matrixx.Matrix11;
import mikera.matrixx.Matrix22;
import mikera.matrixx.Matrix33;
import mikera.matrixx.impl.ADiagonalMatrix;
import mikera.matrixx.impl.AStridedMatrix;
import mikera.matrixx.impl.ColumnMatrix;
import mikera.matrixx.impl.DenseColumnMatrix;
import mikera.matrixx.impl.DiagonalMatrix;
import mikera.matrixx.impl.IdentityMatrix;
import mikera.matrixx.impl.ScalarMatrix;
import mikera.matrixx.impl.SparseColumnMatrix;
import mikera.matrixx.impl.SparseRowMatrix;
import mikera.matrixx.impl.StridedMatrix;
import mikera.matrixx.impl.ZeroMatrix;
import mikera.vectorz.AVector;
import mikera.vectorz.Tools;
import mikera.vectorz.Vector;
import mikera.vectorz.Vector3;
import mikera.vectorz.Vectorz;
import mikera.vectorz.impl.SparseIndexedVector;
import mikera.vectorz.util.ErrorMessages;
import mikera.vectorz.util.VectorzException;
import us.bpsm.edn.parser.Parseable;
import us.bpsm.edn.parser.Parser;
import us.bpsm.edn.parser.Parsers;

public class Matrixx {
    private static final long SPARSE_ELEMENT_THRESHOLD = 100000L;
    private static final Random rand = new Random();

    public static AMatrix createIdentityMatrix(int dimensions) {
        return Matrixx.createImmutableIdentityMatrix(dimensions);
    }

    public static IdentityMatrix createImmutableIdentityMatrix(int dimensions) {
        return IdentityMatrix.create(dimensions);
    }

    public static AMatrix createMutableIdentityMatrix(int dimensions) {
        AMatrix m = Matrixx.newMatrix(dimensions, dimensions);
        for (int i = 0; i < dimensions; ++i) {
            m.unsafeSet(i, i, 1.0);
        }
        return m;
    }

    public static AMatrix toMatrix(Object o) {
        if (o instanceof AMatrix) {
            return (AMatrix)o;
        }
        if (o instanceof AVector) {
            return ColumnMatrix.wrap((AVector)o);
        }
        if (o instanceof Iterable) {
            List<INDArray> al = Tools.toList((Iterable)o);
            return Matrixx.createFromVectors(al);
        }
        throw new UnsupportedOperationException("Can't convert to matrix: " + o.getClass());
    }

    public static AMatrix createSparse(AMatrix m) {
        int rc = m.rowCount();
        int cc = m.columnCount();
        if (rc == 0 || cc == 0) {
            return ZeroMatrix.create(rc, cc);
        }
        return SparseRowMatrix.create(m);
    }

    public static SparseRowMatrix createSparse(int rowCount, int columnCount) {
        return SparseRowMatrix.create(rowCount, columnCount);
    }

    public static SparseRowMatrix createSparseRows(Iterable<AVector> rows) {
        Iterator<AVector> rowIterator = rows.iterator();
        return Matrixx.createSparseRows(rowIterator);
    }

    public static ArrayList<AVector> createSparseArray(Iterator<AVector> vecIterator) {
        AVector v0 = vecIterator.next();
        int len = v0.length();
        ArrayList<AVector> vecList = new ArrayList<AVector>();
        vecList.add(v0);
        while (vecIterator.hasNext()) {
            AVector v = vecIterator.next();
            v = v == null || v.isZero() ? Vectorz.createZeroVector(len) : v.sparseClone();
            vecList.add(v.sparseClone());
        }
        return vecList;
    }

    public static SparseRowMatrix createSparseRows(Iterator<AVector> rowIterator) {
        return SparseRowMatrix.wrap(Matrixx.createSparseArray(rowIterator));
    }

    public static SparseColumnMatrix createSparseColumns(Iterator<AVector> colIterator) {
        return SparseColumnMatrix.wrap(Matrixx.createSparseArray(colIterator));
    }

    public static AMatrix createSparse(int columnCount, Index[] indexes, AVector[] weights) {
        int rowCount = indexes.length;
        if (rowCount != weights.length) {
            throw new IllegalArgumentException("Length of indexes array must match length of weights array");
        }
        SparseRowMatrix sm = SparseRowMatrix.create(rowCount, columnCount);
        for (int i = 0; i < rowCount; ++i) {
            sm.replaceRow(i, SparseIndexedVector.wrap(columnCount, indexes[i].clone(), weights[i].toDoubleArray()));
        }
        return sm;
    }

    public static SparseColumnMatrix createSparseColumns(AMatrix m) {
        int cc = m.columnCount();
        AVector[] cols = new AVector[cc];
        for (int i = 0; i < cc; ++i) {
            cols[i] = Vectorz.createSparse(m.getColumn(i));
        }
        return SparseColumnMatrix.wrap(cols);
    }

    public static AMatrix createSparseRows(AMatrix m) {
        if (m.rowCount() == 0) {
            return ZeroMatrix.create(0, m.columnCount());
        }
        return SparseRowMatrix.create(m);
    }

    public static SparseRowMatrix createSparseRows(INDArray a) {
        if (a.dimensionality() != 2) {
            throw new IllegalArgumentException(ErrorMessages.incompatibleShape(a));
        }
        int rc = a.getShape(0);
        int cc = a.getShape(1);
        SparseRowMatrix m = SparseRowMatrix.create(rc, cc);
        for (int i = 0; i < rc; ++i) {
            AVector v = a.slice(i).sparseClone().asVector();
            if (v.isZero()) continue;
            m.replaceRow(i, v);
        }
        return m;
    }

    public static ZeroMatrix createImmutableZeroMatrix(int rows, int columns) {
        return ZeroMatrix.create(rows, columns);
    }

    public static ADiagonalMatrix createScaleMatrix(int dimensions, double factor) {
        DiagonalMatrix im = new DiagonalMatrix(dimensions);
        for (int i = 0; i < dimensions; ++i) {
            im.unsafeSet(i, i, factor);
        }
        return im;
    }

    public static ADiagonalMatrix createScalarMatrix(int dimensions, double factor) {
        if (factor == 1.0) {
            return IdentityMatrix.create(dimensions);
        }
        return ScalarMatrix.create(dimensions, factor);
    }

    public static /* varargs */ DiagonalMatrix createScaleMatrix(double ... scalingFactors) {
        int dimensions = scalingFactors.length;
        DiagonalMatrix im = new DiagonalMatrix(dimensions);
        for (int i = 0; i < dimensions; ++i) {
            im.unsafeSet(i, i, scalingFactors[i]);
        }
        return im;
    }

    public static /* varargs */ DiagonalMatrix createDiagonalMatrix(double ... diagonalValues) {
        int dimensions = diagonalValues.length;
        DiagonalMatrix im = new DiagonalMatrix(dimensions);
        im.getLeadingDiagonal().setElements(diagonalValues);
        return im;
    }

    public static DiagonalMatrix createDiagonalMatrix(AVector diagonalValues) {
        return DiagonalMatrix.wrap(diagonalValues.toDoubleArray());
    }

    public static Matrix33 createRotationMatrix(Vector3 axis, double angle) {
        return Matrixx.createRotationMatrix(axis.x, axis.y, axis.z, angle);
    }

    public static Matrix33 createRotationMatrix(double x, double y, double z, double angle) {
        double d = Math.sqrt(x * x + y * y + z * z);
        if (d == 0.0) {
            return Matrix33.createIdentityMatrix();
        }
        double ca = Math.cos(angle);
        double u = x;
        double v = y;
        double w = z;
        if (d != 1.0) {
            double s = 1.0 / d;
            u = x * s;
            v = y * s;
            w = z * s;
        }
        double sa = Math.sin(angle);
        return new Matrix33(u * u + (1.0 - u * u) * ca, u * v * (1.0 - ca) - w * sa, u * w * (1.0 - ca) + v * sa, u * v * (1.0 - ca) + w * sa, v * v + (1.0 - v * v) * ca, v * w * (1.0 - ca) - u * sa, u * w * (1.0 - ca) - v * sa, v * w * (1.0 - ca) + u * sa, w * w + (1.0 - w * w) * ca);
    }

    public static Matrix33 createRotationMatrix(AVector v, double angle) {
        if (v.length() != 3) {
            throw new VectorzException("Rotation matrix requires a 3d axis vector");
        }
        return Matrixx.createRotationMatrix(v.unsafeGet(0), v.unsafeGet(1), v.unsafeGet(2), angle);
    }

    public static Matrix33 createXAxisRotationMatrix(double angle) {
        return Matrixx.createRotationMatrix(1.0, 0.0, 0.0, angle);
    }

    public static Matrix33 createYAxisRotationMatrix(double angle) {
        return Matrixx.createRotationMatrix(0.0, 1.0, 0.0, angle);
    }

    public static Matrix33 createZAxisRotationMatrix(double angle) {
        return Matrixx.createRotationMatrix(0.0, 0.0, 1.0, angle);
    }

    public static Matrix22 create2DRotationMatrix(double angle) {
        return Matrix22.createRotationMatrix(angle);
    }

    public static Matrix createRandomSquareMatrix(int dimensions) {
        Matrix m = Matrixx.createSquareMatrix(dimensions);
        Matrixx.fillRandomValues(m);
        return m;
    }

    public static Matrix createRandomSquareMatrix(int dimensions, Random rand) {
        Matrix m = Matrixx.createSquareMatrix(dimensions);
        Matrixx.fillRandomValues(m, rand);
        return m;
    }

    public static AMatrix createRandomMatrix(int rows, int columns) {
        AMatrix m = Matrixx.newMatrix(rows, columns);
        Matrixx.fillRandomValues(m);
        return m;
    }

    public static AMatrix createRandomMatrix(int rows, int columns, Random rand) {
        AMatrix m = Matrixx.newMatrix(rows, columns);
        Matrixx.fillRandomValues(m, rand);
        return m;
    }

    public static AMatrix newMatrix(int rows, int columns) {
        if (rows == columns) {
            if (rows == 1) {
                return new Matrix11();
            }
            if (rows == 2) {
                return new Matrix22();
            }
            if (rows == 3) {
                return new Matrix33();
            }
        }
        if ((long)rows * (long)columns > 100000L) {
            return Matrixx.createSparse(rows, columns);
        }
        return Matrix.create(rows, columns);
    }

    public static Matrix createFromVector(AVector data, int rows, int columns) {
        Matrix m = Matrix.create(rows, columns);
        int n = Math.min(rows * columns, data.length());
        data.copyTo(0, m.data, 0, n);
        return m;
    }

    public AMatrix createInverse(AMatrix m) {
        return m.inverse();
    }

    private static Matrix createSquareMatrix(int dimensions) {
        return Matrix.create(dimensions, dimensions);
    }

    public static AMatrix extractLowerTriangular(AMatrix a) {
        int rc = a.rowCount();
        if (rc > a.columnCount()) {
            throw new IllegalArgumentException("Too few columns in matrix");
        }
        AMatrix r = Matrixx.newMatrix(rc, rc);
        for (int i = 0; i < rc; ++i) {
            for (int j = 0; j <= i; ++j) {
                r.unsafeSet(i, j, a.unsafeGet(i, j));
            }
        }
        return r;
    }

    public static AMatrix extractUpperTriangular(AMatrix a) {
        int cc = a.rowCount();
        if (cc > a.rowCount()) {
            throw new IllegalArgumentException("Too few rows in matrix");
        }
        AMatrix r = Matrixx.newMatrix(cc, cc);
        for (int i = 0; i < cc; ++i) {
            for (int j = i; j < cc; ++j) {
                r.unsafeSet(i, j, a.unsafeGet(i, j));
            }
        }
        return r;
    }

    public static Matrix create(AMatrix m) {
        return new Matrix(m);
    }

    public static Matrix create(List<Object> rows) {
        int rc = rows.size();
        AVector firstRow = Vectorz.create(rows.get(0));
        int cc = firstRow.length();
        Matrix m = Matrix.create(rc, cc);
        m.setRow(0, firstRow);
        for (int i = 1; i < rc; ++i) {
            m.setRow(i, Vectorz.create(rows.get(i)));
        }
        return m;
    }

    public static AMatrix create(IMatrix m) {
        int rows = m.rowCount();
        int columns = m.columnCount();
        AMatrix result = Matrixx.newMatrix(rows, columns);
        result.set(m);
        return result;
    }

    public static void fillRandomValues(AMatrix m) {
        int rows = m.rowCount();
        int columns = m.columnCount();
        for (int i = 0; i < rows; ++i) {
            for (int j = 0; j < columns; ++j) {
                m.unsafeSet(i, j, rand.nextDouble());
            }
        }
    }

    public static void fillRandomValues(AMatrix m, Random rand) {
        int rows = m.rowCount();
        int columns = m.columnCount();
        for (int i = 0; i < rows; ++i) {
            for (int j = 0; j < columns; ++j) {
                m.unsafeSet(i, j, rand.nextDouble());
            }
        }
    }

    public static /* varargs */ AMatrix createFromVectors(INDArray ... data) {
        int rc = data.length;
        int cc = rc == 0 ? 0 : data[0].sliceCount();
        AMatrix m = Matrixx.newMatrix(rc, cc);
        for (int i = 0; i < rc; ++i) {
            m.setRow(i, data[i].asVector());
        }
        return m;
    }

    public static AMatrix createFromVectors(List<INDArray> data) {
        int rc = data.size();
        int cc = rc == 0 ? 0 : data.get(0).sliceCount();
        AMatrix m = Matrixx.newMatrix(rc, cc);
        for (int i = 0; i < rc; ++i) {
            m.setRow(i, data.get(i).asVector());
        }
        return m;
    }

    private static Parser.Config getMatrixParserConfig() {
        return Parsers.defaultConfiguration();
    }

    public static AMatrix parse(String ednString) {
        Parseable ps;
        Parser p = Parsers.newParser(Matrixx.getMatrixParserConfig());
        List data = (List)p.nextValue(ps = Parsers.newParseable(ednString));
        int rc = data.size();
        int cc = rc == 0 ? 0 : ((List)data.get(0)).size();
        AMatrix m = Matrixx.newMatrix(rc, cc);
        for (int i = 0; i < rc; ++i) {
            List row = (List)data.get(i);
            for (int j = 0; j < cc; ++j) {
                m.unsafeSet(i, j, Tools.toDouble(row.get(j)));
            }
        }
        return m;
    }

    public static Matrix deepCopy(AMatrix m) {
        return Matrixx.create(m);
    }

    public static /* varargs */ AMatrix create(Object ... vs) {
        return Matrixx.create(Arrays.asList(vs));
    }

    public static Matrix create(double[][] data) {
        return Matrix.create(data);
    }

    public static AStridedMatrix wrapStrided(double[] data, int rows, int cols, int offset, int rowStride, int colStride) {
        if (offset == 0 && data.length == rows * cols) {
            if (rows <= 1 || cols <= 1 || cols == rowStride && colStride == 1) {
                return Matrix.wrap(rows, cols, data);
            }
            if (rows == colStride && rowStride == 1) {
                return DenseColumnMatrix.wrap(rows, cols, data);
            }
        }
        return StridedMatrix.wrap(data, rows, cols, offset, rowStride, colStride);
    }

    public static AMatrix createSparse(Iterable<INDArray> slices) {
        INDArray slice1 = slices.iterator().next();
        int cc = slice1.sliceCount();
        ArrayList<AVector> al = new ArrayList<AVector>();
        for (INDArray a : slices) {
            if (a.dimensionality() != 1 || a.sliceCount() != cc) {
                throw new IllegalArgumentException(ErrorMessages.incompatibleShape(a));
            }
            al.add(a.sparse().asVector());
        }
        return SparseRowMatrix.create(al);
    }
}

