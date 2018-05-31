/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.decompose.impl.qr;

import mikera.matrixx.AMatrix;
import mikera.matrixx.Matrix;
import mikera.matrixx.decompose.IQRResult;
import mikera.matrixx.decompose.impl.qr.QRDecomposition;
import mikera.matrixx.decompose.impl.qr.QRHelperFunctions;
import mikera.matrixx.decompose.impl.qr.QRResult;

public class HouseholderColQR
implements QRDecomposition {
    protected double[][] dataQR;
    protected double[] v;
    protected int numCols;
    protected int numRows;
    protected int minLength;
    protected double[] gammas;
    protected double gamma;
    protected double tau;
    protected boolean error;
    private boolean compact;
    private AMatrix Q;
    private AMatrix R;

    public HouseholderColQR(boolean compact) {
        this.compact = compact;
    }

    public double[][] getQR() {
        return this.dataQR;
    }

    public AMatrix getQ() {
        if (this.Q == null) {
            this.Q = this.computeQ();
        }
        return this.Q;
    }

    public AMatrix getR() {
        if (this.R == null) {
            this.R = this.computeR();
        }
        return this.R;
    }

    protected AMatrix computeQ() {
        Matrix Q = Matrix.createIdentity(this.numRows);
        for (int j = this.minLength - 1; j >= 0; --j) {
            double[] u = this.dataQR[j];
            double vv = u[j];
            u[j] = 1.0;
            QRHelperFunctions.rank1UpdateMultR(Q, u, this.gammas[j], j, j, this.numRows, this.v);
            u[j] = vv;
        }
        return Q;
    }

    protected AMatrix computeR() {
        Matrix R = this.compact ? Matrix.create(this.minLength, this.numCols) : Matrix.create(this.numRows, this.numCols);
        for (int j = 0; j < this.numCols; ++j) {
            double[] colR = this.dataQR[j];
            int l = Math.min(j, this.numRows - 1);
            for (int i = 0; i <= l; ++i) {
                double val = colR[i];
                R.set(i, j, val);
            }
        }
        return R;
    }

    @Override
    public QRResult decompose(AMatrix A) {
        this.numCols = A.columnCount();
        this.numRows = A.rowCount();
        this.minLength = Math.min(this.numCols, this.numRows);
        int maxLength = Math.max(this.numCols, this.numRows);
        this.dataQR = new double[this.numCols][this.numRows];
        this.v = new double[maxLength];
        this.gammas = new double[this.minLength];
        this.convertToColumnMajor(A);
        this.error = false;
        for (int j = 0; j < this.minLength; ++j) {
            this.householder(j);
            this.updateA(j);
        }
        return new QRResult(this.getQ(), this.getR());
    }

    protected void convertToColumnMajor(AMatrix A) {
        double[] data = A.asDoubleArray();
        for (int x = 0; x < this.numCols; ++x) {
            double[] colQ = this.dataQR[x];
            for (int y = 0; y < this.numRows; ++y) {
                colQ[y] = data[y * this.numCols + x];
            }
        }
    }

    protected void householder(int j) {
        double[] u = this.dataQR[j];
        double max = QRHelperFunctions.findMax(u, j, this.numRows - j);
        if (max == 0.0) {
            this.gamma = 0.0;
            this.error = true;
        } else {
            this.tau = QRHelperFunctions.computeTauAndDivide(j, this.numRows, u, max);
            double u_0 = u[j] + this.tau;
            QRHelperFunctions.divideElements(j + 1, this.numRows, u, u_0);
            this.gamma = u_0 / this.tau;
            this.tau *= max;
            u[j] = - this.tau;
        }
        this.gammas[j] = this.gamma;
    }

    protected void updateA(int w) {
        double[] u = this.dataQR[w];
        for (int j = w + 1; j < this.numCols; ++j) {
            double[] colQ = this.dataQR[j];
            double val = colQ[w];
            for (int k = w + 1; k < this.numRows; ++k) {
                val += u[k] * colQ[k];
            }
            double[] arrd = colQ;
            int n = w;
            arrd[n] = arrd[n] - (val *= this.gamma);
            for (int i = w + 1; i < this.numRows; ++i) {
                double[] arrd2 = colQ;
                int n2 = i;
                arrd2[n2] = arrd2[n2] - u[i] * val;
            }
        }
    }

    public double[] getGammas() {
        return this.gammas;
    }
}

