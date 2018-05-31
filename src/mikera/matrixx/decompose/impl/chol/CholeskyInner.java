/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.decompose.impl.chol;

import mikera.matrixx.AMatrix;
import mikera.matrixx.Matrix;
import mikera.matrixx.decompose.ICholeskyResult;
import mikera.matrixx.decompose.impl.chol.CholeskyCommon;
import mikera.matrixx.decompose.impl.chol.CholeskyResult;

public class CholeskyInner
extends CholeskyCommon {
    public static ICholeskyResult decompose(AMatrix mat) {
        CholeskyInner temp = new CholeskyInner();
        return temp._decompose(mat);
    }

    @Override
    protected CholeskyResult decomposeLower() {
        int i;
        int j;
        double div_el_ii = 0.0;
        for (i = 0; i < this.n; ++i) {
            for (j = i; j < this.n; ++j) {
                double sum = this.t[i * this.n + j];
                int iEl = i * this.n;
                int jEl = j * this.n;
                int end = iEl + i;
                while (iEl < end) {
                    sum -= this.t[iEl] * this.t[jEl];
                    ++iEl;
                    ++jEl;
                }
                if (i == j) {
                    double el_ii;
                    if (sum <= 0.0) {
                        return null;
                    }
                    this.t[i * this.n + i] = el_ii = Math.sqrt(sum);
                    div_el_ii = 1.0 / el_ii;
                    continue;
                }
                this.t[j * this.n + i] = sum * div_el_ii;
            }
        }
        for (i = 0; i < this.n; ++i) {
            for (j = i + 1; j < this.n; ++j) {
                this.t[i * this.n + j] = 0.0;
            }
        }
        return new CholeskyResult(this.T);
    }
}

