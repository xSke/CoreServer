/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.decompose.impl.chol;

import mikera.matrixx.AMatrix;
import mikera.matrixx.Matrix;
import mikera.matrixx.decompose.ICholeskyLDUResult;
import mikera.matrixx.decompose.impl.chol.CholeskyResult;
import mikera.matrixx.impl.ADiagonalMatrix;
import mikera.matrixx.impl.DenseColumnMatrix;
import mikera.matrixx.impl.DiagonalMatrix;

public class CholeskyLDU {
    private int n;
    private Matrix L;
    private double[] el;
    private double[] d;
    double[] vv;

    public static ICholeskyLDUResult decompose(AMatrix mat) {
        CholeskyLDU temp = new CholeskyLDU();
        return temp._decompose(mat);
    }

    private ICholeskyLDUResult _decompose(AMatrix mat) {
        int i;
        int j;
        if (mat.rowCount() != mat.columnCount()) {
            throw new RuntimeException("Can only decompose square matrices");
        }
        this.n = mat.rowCount();
        this.vv = new double[this.n];
        this.d = new double[this.n];
        this.L = mat.toMatrix();
        this.el = this.L.data;
        double d_inv = 0.0;
        for (i = 0; i < this.n; ++i) {
            for (j = i; j < this.n; ++j) {
                double sum = this.el[i * this.n + j];
                for (int k = 0; k < i; ++k) {
                    sum -= this.el[i * this.n + k] * this.el[j * this.n + k] * this.d[k];
                }
                if (i == j) {
                    if (sum <= 0.0) {
                        return null;
                    }
                    this.d[i] = sum;
                    d_inv = 1.0 / sum;
                    this.el[i * this.n + i] = 1.0;
                    continue;
                }
                this.el[j * this.n + i] = sum * d_inv;
            }
        }
        for (i = 0; i < this.n; ++i) {
            for (j = i + 1; j < this.n; ++j) {
                this.el[i * this.n + j] = 0.0;
            }
        }
        return new CholeskyResult(this.L, DiagonalMatrix.create(this.d), this.L.getTranspose());
    }
}

