/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.solve.impl;

import mikera.matrixx.AMatrix;
import mikera.matrixx.Matrix;
import mikera.matrixx.decompose.ICholeskyLDUResult;
import mikera.matrixx.decompose.impl.chol.CholeskyLDU;
import mikera.matrixx.impl.ADiagonalMatrix;
import mikera.matrixx.solve.impl.TriangularSolver;
import mikera.vectorz.AVector;

public class CholeskyLDUSolver {
    protected Matrix A;
    protected int numRows;
    protected int numCols;
    private ICholeskyLDUResult ans;
    private int n;
    private double[] vv;
    private double[] el;
    private double[] d;

    public boolean setA(AMatrix _A) {
        this.A = Matrix.create(_A);
        this.numRows = this.A.rowCount();
        this.numCols = this.A.columnCount();
        this.ans = CholeskyLDU.decompose(this.A);
        if (this.ans != null) {
            this.n = this.A.columnCount();
            this.vv = new double[this.A.rowCount()];
            this.el = this.ans.getL().toMatrix().data;
            this.d = this.ans.getD().getLeadingDiagonal().toDoubleArray();
            return true;
        }
        return false;
    }

    public double quality() {
        return Math.abs(this.diagProd(this.ans.getL()));
    }

    private double diagProd(AMatrix m) {
        double prod = 1.0;
        int diagonalLength = m.rowCount();
        for (int i = 0; i < diagonalLength; ++i) {
            prod *= m.get(i, i);
        }
        return prod;
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
            this.solveInternal();
            for (i = 0; i < this.n; ++i) {
                dataX[i * numCols + j] = this.vv[i];
            }
        }
        return X;
    }

    private void solveInternal() {
        TriangularSolver.solveL(this.el, this.vv, this.n);
        for (int i = 0; i < this.n; ++i) {
            double[] arrd = this.vv;
            int n = i;
            arrd[n] = arrd[n] / this.d[i];
        }
        TriangularSolver.solveTranL(this.el, this.vv, this.n);
    }

    public AMatrix invert() {
        int k;
        double sum;
        int i;
        Matrix inv = Matrix.create(this.numRows, this.numCols);
        if (inv.rowCount() != this.n || inv.columnCount() != this.n) {
            throw new RuntimeException("Unexpected matrix dimension");
        }
        double[] a = inv.data;
        for (i = 0; i < this.n; ++i) {
            for (int j = 0; j <= i; ++j) {
                sum = i == j ? 1.0 : 0.0;
                for (k = i - 1; k >= j; --k) {
                    sum -= this.el[i * this.n + k] * a[j * this.n + k];
                }
                a[j * this.n + i] = sum;
            }
        }
        for (i = 0; i < this.n; ++i) {
            double inv_d = 1.0 / this.d[i];
            for (int j = 0; j <= i; ++j) {
                double[] arrd = a;
                int n = j * this.n + i;
                arrd[n] = arrd[n] * inv_d;
            }
        }
        for (i = this.n - 1; i >= 0; --i) {
            for (int j = 0; j <= i; ++j) {
                sum = i < j ? 0.0 : a[j * this.n + i];
                for (k = i + 1; k < this.n; ++k) {
                    sum -= this.el[k * this.n + i] * a[j * this.n + k];
                }
                double d = sum;
                a[j * this.n + i] = d;
                a[i * this.n + j] = d;
            }
        }
        return inv;
    }
}

