/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.decompose.impl.hessenberg;

import mikera.matrixx.AMatrix;
import mikera.matrixx.Matrix;
import mikera.matrixx.decompose.impl.qr.QRHelperFunctions;

public class TridiagonalDecompositionHouseholder {
    private Matrix QT;
    private int N = 1;
    private double[] w = new double[this.N];
    private double[] gammas = new double[this.N];
    private double[] b = new double[this.N];

    public AMatrix getQT() {
        return this.QT;
    }

    public void getDiagonal(double[] diag, double[] off) {
        for (int i = 0; i < this.N; ++i) {
            diag[i] = this.QT.data[i * this.N + i];
            if (i + 1 >= this.N) continue;
            off[i] = this.QT.data[i * this.N + i + 1];
        }
    }

    public AMatrix getT() {
        Matrix T = Matrix.create(this.N, this.N);
        T.data[0] = this.QT.data[0];
        for (int i = 1; i < this.N; ++i) {
            T.set(i, i, this.QT.get(i, i));
            double a = this.QT.get(i - 1, i);
            T.set(i - 1, i, a);
            T.set(i, i - 1, a);
        }
        if (this.N > 1) {
            T.data[(this.N - 1) * this.N + this.N - 1] = this.QT.data[(this.N - 1) * this.N + this.N - 1];
            T.data[(this.N - 1) * this.N + this.N - 2] = this.QT.data[(this.N - 2) * this.N + this.N - 1];
        }
        return T;
    }

    public AMatrix getQ(boolean transposed) {
        Matrix Q;
        int j;
        Q = Matrix.createIdentity(this.N);
        for (int i = 0; i < this.N; ++i) {
            this.w[i] = 0.0;
        }
        if (transposed) {
            for (j = this.N - 2; j >= 0; --j) {
                this.w[j + 1] = 1.0;
                for (int i = j + 2; i < this.N; ++i) {
                    this.w[i] = this.QT.data[j * this.N + i];
                }
                QRHelperFunctions.rank1UpdateMultL(Q, this.w, this.gammas[j + 1], j + 1, j + 1, this.N);
            }
        } else {
            for (j = this.N - 2; j >= 0; --j) {
                this.w[j + 1] = 1.0;
                for (int i = j + 2; i < this.N; ++i) {
                    this.w[i] = this.QT.get(j, i);
                }
                QRHelperFunctions.rank1UpdateMultR(Q, this.w, this.gammas[j + 1], j + 1, j + 1, this.N, this.b);
            }
        }
        return Q;
    }

    public boolean decompose(AMatrix A) {
        this.init(A);
        for (int k = 1; k < this.N; ++k) {
            this.similarTransform(k);
        }
        return true;
    }

    private void similarTransform(int k) {
        double[] t = this.QT.data;
        double max = 0.0;
        int rowU = (k - 1) * this.N;
        for (int i = k; i < this.N; ++i) {
            double val = Math.abs(t[rowU + i]);
            if (val <= max) continue;
            max = val;
        }
        if (max > 0.0) {
            double gamma;
            double tau = QRHelperFunctions.computeTauAndDivide(k, this.N, t, rowU, max);
            double nu = t[rowU + k] + tau;
            QRHelperFunctions.divideElements(k + 1, this.N, t, rowU, nu);
            t[rowU + k] = 1.0;
            this.gammas[k] = gamma = nu / tau;
            this.householderSymmetric(k, gamma);
            t[rowU + k] = (- tau) * max;
        } else {
            this.gammas[k] = 0.0;
        }
    }

    public void householderSymmetric(int row, double gamma) {
        int i;
        int startU = (row - 1) * this.N;
        for (int i2 = row; i2 < this.N; ++i2) {
            int j;
            double total = 0.0;
            for (j = row; j < i2; ++j) {
                total += this.QT.data[j * this.N + i2] * this.QT.data[startU + j];
            }
            for (j = i2; j < this.N; ++j) {
                total += this.QT.data[i2 * this.N + j] * this.QT.data[startU + j];
            }
            this.w[i2] = (- gamma) * total;
        }
        double alpha = 0.0;
        for (i = row; i < this.N; ++i) {
            alpha += this.QT.data[startU + i] * this.w[i];
        }
        alpha *= -0.5 * gamma;
        for (i = row; i < this.N; ++i) {
            double[] arrd = this.w;
            int n = i;
            arrd[n] = arrd[n] + alpha * this.QT.data[startU + i];
        }
        for (i = row; i < this.N; ++i) {
            double ww = this.w[i];
            double uu = this.QT.data[startU + i];
            int rowA = i * this.N;
            for (int j = i; j < this.N; ++j) {
                double[] arrd = this.QT.data;
                int n = rowA + j;
                arrd[n] = arrd[n] + (ww * this.QT.data[startU + j] + this.w[j] * uu);
            }
        }
    }

    public void init(AMatrix A) {
        if (A.rowCount() != A.columnCount()) {
            throw new IllegalArgumentException("Must be square");
        }
        if (A.columnCount() != this.N) {
            this.N = A.columnCount();
            if (this.w.length < this.N) {
                this.w = new double[this.N];
                this.gammas = new double[this.N];
                this.b = new double[this.N];
            }
        }
        this.QT = Matrix.create(A);
    }
}

