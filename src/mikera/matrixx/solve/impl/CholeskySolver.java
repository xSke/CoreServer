/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.solve.impl;

import mikera.matrixx.AMatrix;
import mikera.matrixx.Matrix;
import mikera.matrixx.decompose.ICholeskyResult;
import mikera.matrixx.decompose.impl.chol.Cholesky;
import mikera.matrixx.solve.impl.TriangularSolver;

public class CholeskySolver {
    protected Matrix A;
    protected int numRows;
    protected int numCols;
    private ICholeskyResult ans;
    private int n;
    private double[] vv;
    private double[] t;

    public boolean setA(AMatrix _A) {
        this.A = Matrix.create(_A);
        this.numRows = this.A.rowCount();
        this.numCols = this.A.columnCount();
        this.ans = Cholesky.decompose(this.A);
        if (this.ans != null) {
            this.n = this.A.columnCount();
            this.vv = new double[this.A.rowCount()];
            this.t = this.ans.getL().toMatrix().data;
            return true;
        }
        return false;
    }

    public double quality() {
        return this.qualityTriangular(this.ans.getL().toMatrix());
    }

    private double qualityTriangular(Matrix T) {
        int N = Math.min(T.rowCount(), T.columnCount());
        double max = this.elementMaxAbs(T);
        if (max == 0.0) {
            return 0.0;
        }
        double quality = 1.0;
        for (int i = 0; i < N; ++i) {
            quality *= T.unsafeGet(i, i) / max;
        }
        return Math.abs(quality);
    }

    private double elementMaxAbs(Matrix a) {
        long size = a.elementCount();
        double[] el = a.data;
        double max = 0.0;
        int i = 0;
        while ((long)i < size) {
            double val = Math.abs(el[i]);
            if (val > max) {
                max = val;
            }
            ++i;
        }
        return max;
    }

    public AMatrix solve(AMatrix B) {
        Matrix X = Matrix.create(B.rowCount(), B.columnCount());
        if (B.columnCount() != X.columnCount() && B.rowCount() != this.n && X.rowCount() != this.n) {
            throw new IllegalArgumentException("Unexpected matrix size");
        }
        int numCols = B.columnCount();
        double[] dataB = B.toMatrix().data;
        double[] dataX = X.data;
        for (int j = 0; j < numCols; ++j) {
            int i;
            for (i = 0; i < this.n; ++i) {
                this.vv[i] = dataB[i * numCols + j];
            }
            this.solveInternalL();
            for (i = 0; i < this.n; ++i) {
                dataX[i * numCols + j] = this.vv[i];
            }
        }
        return X;
    }

    private void solveInternalL() {
        TriangularSolver.solveL(this.t, this.vv, this.n);
        TriangularSolver.solveTranL(this.t, this.vv, this.n);
    }

    public AMatrix invert() {
        Matrix inv = Matrix.create(this.numRows, this.numCols);
        double[] a = inv.data;
        this.setToInverseL(a);
        return inv;
    }

    public void setToInverseL(double[] a) {
        int k;
        double el_ii;
        int j;
        double sum;
        int i;
        for (i = 0; i < this.n; ++i) {
            el_ii = this.t[i * this.n + i];
            for (j = 0; j <= i; ++j) {
                sum = i == j ? 1.0 : 0.0;
                for (k = i - 1; k >= j; --k) {
                    sum -= this.t[i * this.n + k] * a[j * this.n + k];
                }
                a[j * this.n + i] = sum / el_ii;
            }
        }
        for (i = this.n - 1; i >= 0; --i) {
            el_ii = this.t[i * this.n + i];
            for (j = 0; j <= i; ++j) {
                sum = i < j ? 0.0 : a[j * this.n + i];
                for (k = i + 1; k < this.n; ++k) {
                    sum -= this.t[k * this.n + i] * a[j * this.n + k];
                }
                double d = sum / el_ii;
                a[j * this.n + i] = d;
                a[i * this.n + j] = d;
            }
        }
    }
}

