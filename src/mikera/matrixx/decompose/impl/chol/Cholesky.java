/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.decompose.impl.chol;

import mikera.matrixx.AMatrix;
import mikera.matrixx.Matrix;
import mikera.matrixx.decompose.ICholeskyResult;
import mikera.matrixx.decompose.impl.chol.CholeskyCommon;
import mikera.matrixx.decompose.impl.chol.CholeskyHelper;
import mikera.matrixx.decompose.impl.chol.CholeskyResult;

public class Cholesky
extends CholeskyCommon {
    private int blockWidth;
    private Matrix B;
    private CholeskyHelper chol;
    private static final int BLOCK_WIDTH = 60;

    private Cholesky() {
        this.blockWidth = 60;
    }

    private Cholesky(int blockWidth) {
        this.blockWidth = blockWidth;
    }

    public static ICholeskyResult decompose(AMatrix mat) {
        return Cholesky.decompose(mat, 60);
    }

    public static ICholeskyResult decompose(AMatrix mat, int blockWidth) {
        Cholesky temp = new Cholesky(blockWidth);
        return temp._decompose(mat);
    }

    @Override
    protected ICholeskyResult _decompose(AMatrix mat) {
        int cc;
        int rc = mat.rowCount();
        if (rc != (cc = mat.columnCount())) {
            throw new IllegalArgumentException("Must be a square matrix.");
        }
        this.n = mat.rowCount();
        this.vv = new double[this.n];
        this.t = mat.toDoubleArray();
        this.T = Matrix.wrap(rc, cc, this.t);
        this.B = mat.rowCount() < this.blockWidth ? Matrix.create(0, 0) : Matrix.create(this.blockWidth, this.n);
        this.chol = new CholeskyHelper(this.blockWidth);
        return this.decomposeLower();
    }

    @Override
    protected CholeskyResult decomposeLower() {
        int i;
        this.B = this.n < this.blockWidth ? Matrix.create(0, 0) : Matrix.create(this.blockWidth, this.n - this.blockWidth);
        int numBlocks = this.n / this.blockWidth;
        int remainder = this.n % this.blockWidth;
        if (remainder > 0) {
            ++numBlocks;
        }
        int b_numCols = this.n;
        for (i = 0; i < numBlocks; ++i) {
            int width;
            if ((b_numCols -= this.blockWidth) > 0) {
                if (!this.chol.decompose(this.T, i * this.blockWidth * this.T.columnCount() + i * this.blockWidth, this.blockWidth)) {
                    return null;
                }
                int indexSrc = i * this.blockWidth * this.T.columnCount() + (i + 1) * this.blockWidth;
                int indexDst = (i + 1) * this.blockWidth * this.T.columnCount() + i * this.blockWidth;
                Cholesky.solveL_special(this.chol.getL().toMatrix().data, this.T, indexSrc, indexDst, this.B, b_numCols);
                int indexL = (i + 1) * this.blockWidth * this.n + (i + 1) * this.blockWidth;
                Cholesky.symmRankTranA_sub(this.B, this.T, indexL, b_numCols);
                continue;
            }
            int n = width = remainder > 0 ? remainder : this.blockWidth;
            if (this.chol.decompose(this.T, i * this.blockWidth * this.T.columnCount() + i * this.blockWidth, width)) continue;
            return null;
        }
        for (i = 0; i < this.n; ++i) {
            for (int j = i + 1; j < this.n; ++j) {
                this.t[i * this.n + j] = 0.0;
            }
        }
        return new CholeskyResult(this.T);
    }

    private static void solveL_special(double[] L, Matrix b_src, int indexSrc, int indexDst, Matrix B, int b_numCols) {
        double[] dataSrc = b_src.data;
        double[] b = B.data;
        int m = B.rowCount();
        int n = b_numCols;
        int widthL = m;
        for (int j = 0; j < n; ++j) {
            int indexb = j;
            int rowL = 0;
            int i = 0;
            while (i < widthL) {
                double val;
                double sum = dataSrc[indexSrc + i * b_src.columnCount() + j];
                int indexL = rowL;
                int endL = indexL + i;
                int indexB = j;
                while (indexL != endL) {
                    sum -= L[indexL++] * b[indexB];
                    indexB += n;
                }
                dataSrc[indexDst + j * b_src.columnCount() + i] = val = sum / L[i * widthL + i];
                b[indexb] = val;
                ++i;
                indexb += n;
                rowL += widthL;
            }
        }
    }

    private static void symmRankTranA_sub(Matrix a, Matrix c, int startIndexC, int b_numCols) {
        double[] dataA = a.data;
        double[] dataC = c.data;
        int strideC = c.columnCount() + 1;
        for (int i = 0; i < b_numCols; ++i) {
            int indexA = i;
            int endR = b_numCols;
            int k = 0;
            while (k < a.rowCount()) {
                int indexC = startIndexC;
                double valA = dataA[indexA];
                int indexR = indexA;
                while (indexR < endR) {
                    double[] arrd = dataC;
                    int n = indexC++;
                    arrd[n] = arrd[n] - valA * dataA[indexR++];
                }
                ++k;
                indexA += b_numCols;
                endR += b_numCols;
            }
            startIndexC += strideC;
        }
    }
}

