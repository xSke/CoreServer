/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.decompose.impl.bidiagonal;

import mikera.matrixx.AMatrix;
import mikera.matrixx.Matrix;
import mikera.matrixx.decompose.IBidiagonalResult;
import mikera.matrixx.decompose.impl.bidiagonal.BidiagonalRowResult;
import mikera.matrixx.decompose.impl.qr.QRHelperFunctions;

public class BidiagonalRow {
    private Matrix UBV;
    private int m;
    private int n;
    private int min;
    private double[] gammasU;
    private double[] gammasV;
    private double[] b;
    private double[] u;
    private boolean compact;

    private BidiagonalRow() {
    }

    public static IBidiagonalResult decompose(AMatrix A) {
        BidiagonalRow temp = new BidiagonalRow();
        return temp._decompose(A, false);
    }

    public static IBidiagonalResult decompose(AMatrix A, boolean compact) {
        BidiagonalRow temp = new BidiagonalRow();
        return temp._decompose(A, compact);
    }

    private IBidiagonalResult _decompose(AMatrix A, boolean compact) {
        this.compact = compact;
        this.UBV = Matrix.create(A);
        this.m = this.UBV.rowCount();
        this.n = this.UBV.columnCount();
        this.min = Math.min(this.m, this.n);
        int max = Math.max(this.m, this.n);
        this.b = new double[max + 1];
        this.u = new double[max + 1];
        this.gammasU = new double[this.m];
        this.gammasV = new double[this.n];
        for (int k = 0; k < this.min; ++k) {
            this.computeU(k);
            this.computeV(k);
        }
        return new BidiagonalRowResult(this.getU(), this.getB(), this.getV());
    }

    private AMatrix getB() {
        Matrix B = this.handleB(this.m, this.n, this.min);
        B.set(0, 0, this.UBV.get(0, 0));
        for (int i = 1; i < this.min; ++i) {
            B.set(i, i, this.UBV.get(i, i));
            B.set(i - 1, i, this.UBV.get(i - 1, i));
        }
        if (this.n > this.m) {
            B.set(this.min - 1, this.min, this.UBV.get(this.min - 1, this.min));
        }
        return B;
    }

    private Matrix handleB(int m, int n, int min) {
        int w;
        int n2 = w = n > m ? min + 1 : min;
        if (this.compact) {
            return Matrix.create(min, w);
        }
        return Matrix.create(m, n);
    }

    private AMatrix getU() {
        Matrix U = this.handleU(this.m, this.n, this.min);
        for (int i = 0; i < this.m; ++i) {
            this.u[i] = 0.0;
        }
        for (int j = this.min - 1; j >= 0; --j) {
            this.u[j] = 1.0;
            for (int i = j + 1; i < this.m; ++i) {
                this.u[i] = this.UBV.get(i, j);
            }
            QRHelperFunctions.rank1UpdateMultR(U, this.u, this.gammasU[j], j, j, this.m, this.b);
        }
        return U;
    }

    private Matrix handleU(int m, int n, int min) {
        if (this.compact) {
            return Matrix.createIdentity(m, min);
        }
        return Matrix.createIdentity(m, m);
    }

    private AMatrix getV() {
        Matrix V = this.handleV(this.m, this.n, this.min);
        for (int j = this.min - 1; j >= 0; --j) {
            this.u[j + 1] = 1.0;
            for (int i = j + 2; i < this.n; ++i) {
                this.u[i] = this.UBV.get(j, i);
            }
            QRHelperFunctions.rank1UpdateMultR(V, this.u, this.gammasV[j], j + 1, j + 1, this.n, this.b);
        }
        return V;
    }

    private Matrix handleV(int m, int n, int min) {
        int w;
        int n2 = w = n > m ? min + 1 : min;
        if (this.compact) {
            return Matrix.createIdentity(n, w);
        }
        return Matrix.createIdentity(n, n);
    }

    protected void computeU(int k) {
        double[] b = this.UBV.data;
        double max = 0.0;
        for (int i = k; i < this.m; ++i) {
            double val = this.u[i] = b[i * this.n + k];
            if ((val = Math.abs(val)) <= max) continue;
            max = val;
        }
        if (max > 0.0) {
            double gamma;
            double tau = QRHelperFunctions.computeTauAndDivide(k, this.m, this.u, max);
            double nu = this.u[k] + tau;
            QRHelperFunctions.divideElements_Bcol(k + 1, this.m, this.n, this.u, b, k, nu);
            this.u[k] = 1.0;
            this.gammasU[k] = gamma = nu / tau;
            QRHelperFunctions.rank1UpdateMultR(this.UBV, this.u, gamma, k + 1, k, this.m, this.b);
            b[k * this.n + k] = (- tau) * max;
        } else {
            this.gammasU[k] = 0.0;
        }
    }

    protected void computeV(int k) {
        double[] b = this.UBV.data;
        int row = k * this.n;
        double max = QRHelperFunctions.findMax(b, row + k + 1, this.n - k - 1);
        if (max > 0.0) {
            double gamma;
            double tau = QRHelperFunctions.computeTauAndDivide(k + 1, this.n, b, row, max);
            double nu = b[row + k + 1] + tau;
            QRHelperFunctions.divideElements_Brow(k + 2, this.n, this.u, b, row, nu);
            this.u[k + 1] = 1.0;
            this.gammasV[k] = gamma = nu / tau;
            QRHelperFunctions.rank1UpdateMultL(this.UBV, this.u, gamma, k + 1, k + 1, this.n);
            b[row + k + 1] = (- tau) * max;
        } else {
            this.gammasV[k] = 0.0;
        }
    }
}

