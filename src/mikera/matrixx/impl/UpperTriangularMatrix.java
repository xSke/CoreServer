/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.impl;

import mikera.arrayz.INDArray;
import mikera.matrixx.AMatrix;
import mikera.matrixx.impl.ADenseArrayMatrix;
import mikera.matrixx.impl.ATriangularMatrix;
import mikera.matrixx.impl.IFastColumns;
import mikera.matrixx.impl.LowerTriangularMatrix;
import mikera.vectorz.AVector;
import mikera.vectorz.Vectorz;
import mikera.vectorz.impl.ArraySubVector;
import mikera.vectorz.impl.IndexedArrayVector;

public final class UpperTriangularMatrix
extends ATriangularMatrix
implements IFastColumns {
    private static final long serialVersionUID = 4438118586237354484L;

    private UpperTriangularMatrix(double[] data, int rows, int cols) {
        super(data, rows, cols);
    }

    private UpperTriangularMatrix(int rows, int cols) {
        this(new double[cols * (cols + 1) >> 1], rows, cols);
    }

    static UpperTriangularMatrix wrap(double[] data, int rows, int cols) {
        return new UpperTriangularMatrix(data, rows, cols);
    }

    public static UpperTriangularMatrix createFrom(AMatrix m) {
        int rc = m.rowCount();
        int cc = m.columnCount();
        UpperTriangularMatrix r = new UpperTriangularMatrix(rc, cc);
        for (int i = 0; i < rc; ++i) {
            for (int j = i; j < cc; ++j) {
                r.unsafeSet(i, j, m.unsafeGet(i, j));
            }
        }
        return r;
    }

    @Override
    public boolean isUpperTriangular() {
        return true;
    }

    @Override
    public int lowerBandwidthLimit() {
        return 0;
    }

    @Override
    public int lowerBandwidth() {
        return 0;
    }

    @Override
    public AVector getBand(int band) {
        int n = this.bandLength(band);
        if (n == 0 || band < 0) {
            return Vectorz.createZeroVector(this.bandLength(band));
        }
        if (n == 1) {
            return ArraySubVector.wrap(this.data, this.internalIndex(0, band), 1);
        }
        int[] ixs = new int[n];
        for (int i = 0; i < n; ++i) {
            ixs[i] = this.internalIndex(i, i + band);
        }
        return IndexedArrayVector.wrap(this.data, ixs);
    }

    @Override
    protected int index(int i, int j) {
        if (i <= j) {
            return this.internalIndex(i, j);
        }
        throw new IndexOutOfBoundsException("Can't compute array index for sparse entry!");
    }

    private int internalIndex(int i, int j) {
        return i + (j * (j + 1) >> 1);
    }

    @Override
    public double get(int i, int j) {
        this.checkIndex(i, j);
        if (i > j) {
            return 0.0;
        }
        return this.data[this.internalIndex(i, j)];
    }

    @Override
    public double unsafeGet(int i, int j) {
        if (i > j) {
            return 0.0;
        }
        return this.data[this.internalIndex(i, j)];
    }

    @Override
    public void unsafeSet(int i, int j, double value) {
        this.data[this.internalIndex((int)i, (int)j)] = value;
    }

    @Override
    public AVector getColumnView(int j) {
        int end = Math.min(j + 1, this.rows);
        return ArraySubVector.wrap(this.data, this.internalIndex(0, j), end).join(Vectorz.createZeroVector(this.rows - end));
    }

    @Override
    public LowerTriangularMatrix getTranspose() {
        return LowerTriangularMatrix.wrap(this.data, this.cols, this.rows);
    }

    @Override
    public boolean equals(AMatrix a) {
        if (a == this) {
            return true;
        }
        if (a instanceof ADenseArrayMatrix) {
            return this.equals((ADenseArrayMatrix)a);
        }
        if (!this.isSameShape(a)) {
            return false;
        }
        for (int j = 0; j < this.cols; ++j) {
            int i;
            int end = Math.min(j, this.rows - 1);
            for (i = 0; i <= end; ++i) {
                if (this.data[this.internalIndex(i, j)] == a.unsafeGet(i, j)) continue;
                return false;
            }
            for (i = j + 1; i < this.rows; ++i) {
                if (a.unsafeGet(i, j) == 0.0) continue;
                return false;
            }
        }
        return true;
    }

    @Override
    public AMatrix exactClone() {
        return new UpperTriangularMatrix((double[])this.data.clone(), this.rows, this.cols);
    }
}

