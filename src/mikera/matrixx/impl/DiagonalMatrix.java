/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.impl;

import mikera.arrayz.INDArray;
import mikera.matrixx.AMatrix;
import mikera.matrixx.impl.ADiagonalMatrix;
import mikera.vectorz.AVector;
import mikera.vectorz.Vector;
import mikera.vectorz.impl.ADenseArrayVector;
import mikera.vectorz.util.DoubleArrays;
import mikera.vectorz.util.ErrorMessages;
import mikera.vectorz.util.VectorzException;

public final class DiagonalMatrix
extends ADiagonalMatrix {
    private static final long serialVersionUID = -6721785163444613243L;
    final double[] data;
    private final Vector lead;

    public DiagonalMatrix(int dimensions) {
        super(dimensions);
        this.data = new double[dimensions];
        this.lead = Vector.wrap(this.data);
    }

    private /* varargs */ DiagonalMatrix(double ... values) {
        super(values.length);
        this.data = values;
        this.lead = Vector.wrap(this.data);
    }

    private DiagonalMatrix(Vector values) {
        super(values.length());
        this.data = values.getArray();
        this.lead = values;
    }

    public static DiagonalMatrix createDimensions(int dims) {
        return new DiagonalMatrix(dims);
    }

    public static /* varargs */ DiagonalMatrix create(double ... values) {
        int dimensions = values.length;
        double[] data = new double[dimensions];
        System.arraycopy(values, 0, data, 0, dimensions);
        return new DiagonalMatrix(data);
    }

    public static DiagonalMatrix create(AVector v) {
        return DiagonalMatrix.wrap(v.toDoubleArray());
    }

    public static DiagonalMatrix create(AMatrix m) {
        if (!m.isDiagonal()) {
            throw new IllegalArgumentException("Source is not a diagonal matrix!");
        }
        return DiagonalMatrix.wrap(m.getLeadingDiagonal().toDoubleArray());
    }

    public static DiagonalMatrix wrap(double[] data) {
        return new DiagonalMatrix(data);
    }

    public static DiagonalMatrix wrap(Vector data) {
        return new DiagonalMatrix(data);
    }

    @Override
    public double trace() {
        double result = 0.0;
        for (int i = 0; i < this.dimensions; ++i) {
            result += this.data[i];
        }
        return result;
    }

    @Override
    public double diagonalProduct() {
        double result = 1.0;
        for (int i = 0; i < this.dimensions; ++i) {
            result *= this.data[i];
        }
        return result;
    }

    @Override
    public double elementSum() {
        return this.lead.elementSum();
    }

    @Override
    public long nonZeroCount() {
        return this.lead.nonZeroCount();
    }

    @Override
    public double get(int row, int column) {
        if (row != column) {
            if (row < 0 || row >= this.dimensions) {
                throw new IndexOutOfBoundsException(ErrorMessages.position(row, column));
            }
            return 0.0;
        }
        return this.data[row];
    }

    @Override
    public double unsafeGet(int row, int column) {
        if (row != column) {
            return 0.0;
        }
        return this.data[row];
    }

    @Override
    public void set(int row, int column, double value) {
        if (row != column) {
            if (value != 0.0) {
                throw new UnsupportedOperationException(ErrorMessages.notFullyMutable(this, row, column));
            }
        } else {
            this.data[row] = value;
        }
    }

    @Override
    public void unsafeSet(int row, int column, double value) {
        this.data[row] = value;
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public boolean isFullyMutable() {
        return this.dimensions <= 1;
    }

    @Override
    public void multiply(double factor) {
        this.lead.multiply(factor);
    }

    @Override
    public DiagonalMatrix multiplyCopy(double factor) {
        double[] newData = DoubleArrays.copyOf(this.data);
        DoubleArrays.multiply(newData, factor);
        return DiagonalMatrix.wrap(newData);
    }

    @Override
    public double calculateElement(int i, AVector v) {
        return this.data[i] * v.unsafeGet(i);
    }

    @Override
    public double calculateElement(int i, Vector v) {
        return this.data[i] * v.unsafeGet(i);
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
        for (int i = 0; i < rc; ++i) {
            ddata[i] = sdata[i] * this.data[i];
        }
    }

    @Override
    public void transformInPlace(AVector v) {
        if (v instanceof ADenseArrayVector) {
            this.transformInPlace((ADenseArrayVector)v);
            return;
        }
        if (v.length() != this.dimensions) {
            throw new IllegalArgumentException("Wrong length vector: " + v.length());
        }
        for (int i = 0; i < this.dimensions; ++i) {
            v.unsafeSet(i, v.unsafeGet(i) * this.data[i]);
        }
    }

    @Override
    public void transformInPlace(ADenseArrayVector v) {
        double[] dest = v.getArray();
        int offset = v.getArrayOffset();
        DoubleArrays.arraymultiply(this.data, 0, dest, offset, this.dimensions);
    }

    @Override
    public boolean isIdentity() {
        for (int i = 0; i < this.dimensions; ++i) {
            if (this.data[i] == 1.0) continue;
            return false;
        }
        return true;
    }

    @Override
    public boolean isBoolean() {
        return DoubleArrays.isBoolean(this.data, 0, this.dimensions);
    }

    @Override
    public boolean isZero() {
        return DoubleArrays.isZero(this.data);
    }

    @Override
    public DiagonalMatrix clone() {
        DiagonalMatrix m = new DiagonalMatrix(this.data);
        return m;
    }

    @Override
    public double determinant() {
        return DoubleArrays.elementProduct(this.data, 0, this.dimensions);
    }

    @Override
    public DiagonalMatrix inverse() {
        double[] newData = new double[this.dimensions];
        DoubleArrays.reciprocal(newData);
        return new DiagonalMatrix(newData);
    }

    @Override
    public double getDiagonalValue(int i) {
        return this.data[i];
    }

    @Override
    public double unsafeGetDiagonalValue(int i) {
        return this.data[i];
    }

    @Override
    public Vector getLeadingDiagonal() {
        return this.lead;
    }

    @Override
    public AMatrix innerProduct(AMatrix a) {
        if (a instanceof ADiagonalMatrix) {
            return this.innerProduct((ADiagonalMatrix)a);
        }
        return super.innerProduct(a);
    }

    @Override
    public AMatrix innerProduct(ADiagonalMatrix a) {
        if (!(a instanceof DiagonalMatrix)) {
            return a.innerProduct(this);
        }
        if (this.dimensions != a.dimensions) {
            throw new IllegalArgumentException(ErrorMessages.mismatch(this, a));
        }
        DiagonalMatrix result = DiagonalMatrix.create(this.data);
        result.lead.multiply(a.getLeadingDiagonal());
        return result;
    }

    @Override
    public DiagonalMatrix exactClone() {
        return DiagonalMatrix.create(this.data);
    }

    @Override
    public void validate() {
        if (this.dimensions != this.data.length) {
            throw new VectorzException("dimension mismatch: " + this.dimensions);
        }
        super.validate();
    }
}

