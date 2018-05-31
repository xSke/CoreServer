/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.decompose.impl.chol;

import mikera.matrixx.AMatrix;
import mikera.matrixx.Matrix;

public class CholeskyHelper {
    private Matrix L;
    private double[] el;

    public CholeskyHelper(int widthMax) {
        this.L = Matrix.create(widthMax, widthMax);
        this.el = this.L.data;
    }

    public boolean decompose(AMatrix mat, int indexStart, int n) {
        double[] m = mat.toMatrix().data;
        double div_el_ii = 0.0;
        for (int i = 0; i < n; ++i) {
            for (int j = i; j < n; ++j) {
                double v;
                double sum = m[indexStart + i * mat.rowCount() + j];
                int iEl = i * n;
                int jEl = j * n;
                int end = iEl + i;
                while (iEl < end) {
                    sum -= this.el[iEl] * this.el[jEl];
                    ++iEl;
                    ++jEl;
                }
                if (i == j) {
                    double el_ii;
                    if (sum <= 0.0) {
                        return false;
                    }
                    this.el[i * n + i] = el_ii = Math.sqrt(sum);
                    m[indexStart + i * mat.columnCount() + i] = el_ii;
                    div_el_ii = 1.0 / el_ii;
                    continue;
                }
                this.el[j * n + i] = v = sum * div_el_ii;
                m[indexStart + j * mat.columnCount() + i] = v;
            }
        }
        return true;
    }

    public AMatrix getL() {
        return this.L;
    }
}

