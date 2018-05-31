/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.solve.impl.qr;

import mikera.arrayz.INDArray;
import mikera.matrixx.AMatrix;
import mikera.matrixx.Matrix;
import mikera.matrixx.decompose.impl.qr.HouseholderColQR;
import mikera.matrixx.decompose.impl.qr.QRHelperFunctions;
import mikera.matrixx.decompose.impl.qr.QRResult;

public class QRHouseColSolver {
    protected AMatrix A;
    protected int numRows;
    protected int numCols;
    private HouseholderColQR decomposer = new HouseholderColQR(true);
    private Matrix a;
    private Matrix temp;
    protected int maxRows = -1;
    protected int maxCols = -1;
    private double[][] QR;
    private Matrix R;
    private double[] gammas;
    private QRResult result;

    public AMatrix getA() {
        return this.A;
    }

    protected void _setA(AMatrix A) {
        this.A = A;
        this.numRows = A.rowCount();
        this.numCols = A.columnCount();
    }

    public void setMaxSize(int maxRows, int maxCols) {
        this.maxRows = maxRows;
        this.maxCols = maxCols;
    }

    public boolean setA(AMatrix A) {
        if (A.rowCount() > this.maxRows || A.columnCount() > this.maxCols) {
            this.setMaxSize(A.rowCount(), A.columnCount());
        }
        this.a = Matrix.create(A.rowCount(), 1);
        this.temp = Matrix.create(A.rowCount(), 1);
        this._setA(A);
        this.result = this.decomposer.decompose(A);
        if (this.result == null) {
            return false;
        }
        this.gammas = this.decomposer.getGammas();
        this.QR = this.decomposer.getQR();
        this.R = this.result.getR().toMatrix();
        return true;
    }

    public double quality() {
        return QRHouseColSolver.qualityTriangular(true, this.R);
    }

    public AMatrix solve(AMatrix B) {
        if (B.rowCount() != this.numRows) {
            throw new IllegalArgumentException("Unexpected dimensions for B");
        }
        Matrix X = Matrix.create(this.numCols, B.columnCount());
        int BnumCols = B.columnCount();
        for (int colB = 0; colB < BnumCols; ++colB) {
            for (int i = 0; i < this.numRows; ++i) {
                this.a.data[i] = B.unsafeGet(i, colB);
            }
            for (int n = 0; n < this.numCols; ++n) {
                double[] u = this.QR[n];
                double vv = u[n];
                u[n] = 1.0;
                QRHelperFunctions.rank1UpdateMultR(this.a, u, this.gammas[n], 0, n, this.numRows, this.temp.data);
                u[n] = vv;
            }
            this.solveU(this.R.asDoubleArray(), this.a.asDoubleArray(), this.numCols);
            double[] data = X.asDoubleArray();
            for (int i = 0; i < this.numCols; ++i) {
                data[i * X.columnCount() + colB] = this.a.data[i];
            }
        }
        return X;
    }

    private void solveU(double[] U, double[] b, int n) {
        for (int i = n - 1; i >= 0; --i) {
            double sum = b[i];
            int indexU = i * n + i + 1;
            for (int j = i + 1; j < n; ++j) {
                sum -= U[indexU++] * b[j];
            }
            b[i] = sum / U[i * n + i];
        }
    }

    public static double qualityTriangular(boolean upper, AMatrix T) {
        int N = Math.min(T.rowCount(), T.columnCount());
        double max = T.absCopy().elementMax();
        if (max == 0.0) {
            return 0.0;
        }
        double quality = 1.0;
        for (int i = 0; i < N; ++i) {
            quality *= T.unsafeGet(i, i) / max;
        }
        return Math.abs(quality);
    }
}

