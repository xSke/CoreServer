/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.decompose.impl.hessenberg;

import mikera.matrixx.AMatrix;
import mikera.matrixx.Matrix;
import mikera.matrixx.decompose.impl.hessenberg.HessenbergResult;
import mikera.matrixx.decompose.impl.qr.QRHelperFunctions;

public class HessenbergSimilarDecomposition {
    private Matrix QH;
    private int N;
    private double[] gammas;
    private double[] b;
    private double[] u;

    private HessenbergSimilarDecomposition() {
    }

    public static HessenbergResult decompose(AMatrix A) {
        HessenbergSimilarDecomposition alg = new HessenbergSimilarDecomposition();
        return alg._decompose(A);
    }

    public AMatrix getQH() {
        return this.QH;
    }

    private AMatrix getH() {
        Matrix H = Matrix.create(this.N, this.N);
        System.arraycopy(this.QH.data, 0, H.data, 0, this.N);
        for (int i = 1; i < this.N; ++i) {
            for (int j = i - 1; j < this.N; ++j) {
                H.set(i, j, this.QH.get(i, j));
            }
        }
        return H;
    }

    private AMatrix getQ() {
        Matrix Q = Matrix.createIdentity(this.N);
        for (int j = this.N - 2; j >= 0; --j) {
            this.u[j + 1] = 1.0;
            for (int i = j + 2; i < this.N; ++i) {
                this.u[i] = this.QH.get(i, j);
            }
            QRHelperFunctions.rank1UpdateMultR(Q, this.u, this.gammas[j], j + 1, j + 1, this.N, this.b);
        }
        return Q;
    }

    private HessenbergResult _decompose(AMatrix A) {
        if (A.rowCount() != A.columnCount()) {
            throw new IllegalArgumentException("A must be square.");
        }
        this.QH = A.copy().toMatrix();
        this.N = A.columnCount();
        this.b = new double[this.N];
        this.gammas = new double[this.N];
        this.u = new double[this.N];
        double[] h = this.QH.data;
        for (int k = 0; k < this.N - 2; ++k) {
            double max = 0.0;
            for (int i = k + 1; i < this.N; ++i) {
                double val = this.u[i] = h[i * this.N + k];
                if ((val = Math.abs(val)) <= max) continue;
                max = val;
            }
            if (max > 0.0) {
                double gamma;
                double tau = 0.0;
                int i = k + 1;
                while (i < this.N) {
                    double[] arrd = this.u;
                    int n = i++;
                    double d = arrd[n] / max;
                    arrd[n] = d;
                    double val = d;
                    tau += val * val;
                }
                tau = Math.sqrt(tau);
                if (this.u[k + 1] < 0.0) {
                    tau = - tau;
                }
                double nu = this.u[k + 1] + tau;
                this.u[k + 1] = 1.0;
                int i2 = k + 2;
                while (i2 < this.N) {
                    double[] arrd = this.u;
                    int n = i2++;
                    double d = arrd[n] / nu;
                    arrd[n] = d;
                    h[i2 * this.N + k] = d;
                }
                this.gammas[k] = gamma = nu / tau;
                QRHelperFunctions.rank1UpdateMultR(this.QH, this.u, gamma, k + 1, k + 1, this.N, this.b);
                QRHelperFunctions.rank1UpdateMultL(this.QH, this.u, gamma, 0, k + 1, this.N);
                h[(k + 1) * this.N + k] = (- tau) * max;
                continue;
            }
            this.gammas[k] = 0.0;
        }
        return new HessenbergResult(this.getH(), this.getQ());
    }

    public double[] getGammas() {
        return this.gammas;
    }
}

