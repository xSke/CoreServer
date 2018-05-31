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

public class HouseholderQR
implements QRDecomposition {
    protected Matrix QR;
    protected double[] u;
    protected double[] v;
    protected int numCols;
    protected int numRows;
    protected int minLength;
    protected double[] dataQR;
    protected double[] gammas;
    protected double gamma;
    protected double tau;
    protected boolean error;
    private boolean compact;
    private AMatrix Q;
    private AMatrix R;

    public HouseholderQR(boolean compact) {
        this.compact = compact;
    }

    public AMatrix getQR() {
        return this.QR;
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
            this.u[j] = 1.0;
            for (int i = j + 1; i < this.numRows; ++i) {
                this.u[i] = this.QR.get(i, j);
            }
            QRHelperFunctions.rank1UpdateMultR(Q, this.u, this.gammas[j], j, j, this.numRows, this.v);
        }
        return Q;
    }

    protected AMatrix computeR() {
        Matrix R = this.compact ? Matrix.create(this.minLength, this.numCols) : Matrix.create(this.numRows, this.numCols);
        for (int i = 0; i < this.minLength; ++i) {
            for (int j = i; j < this.numCols; ++j) {
                double val = this.QR.get(i, j);
                R.set(i, j, val);
            }
        }
        return R;
    }

    @Override
    public QRResult decompose(AMatrix A) {
        this.error = false;
        this.numCols = A.columnCount();
        this.numRows = A.rowCount();
        this.minLength = Math.min(this.numRows, this.numCols);
        int maxLength = Math.max(this.numRows, this.numCols);
        this.QR = Matrix.create(A);
        this.u = new double[maxLength];
        this.v = new double[maxLength];
        this.dataQR = this.QR.data;
        this.gammas = new double[this.minLength];
        for (int j = 0; j < this.minLength; ++j) {
            this.householder(j);
            this.updateA(j);
        }
        return new QRResult(this.getQ(), this.getR());
    }

    protected void householder(int j) {
        double d;
        int i;
        int index = j + j * this.numCols;
        double max = 0.0;
        for (i = j; i < this.numRows; ++i) {
            this.u[i] = this.dataQR[index];
            d = this.u[i];
            if (d < 0.0) {
                d = - d;
            }
            if (max < d) {
                max = d;
            }
            index += this.numCols;
        }
        if (max == 0.0) {
            this.gamma = 0.0;
            this.error = true;
        } else {
            this.tau = 0.0;
            for (i = j; i < this.numRows; ++i) {
                double[] arrd = this.u;
                int n = i;
                arrd[n] = arrd[n] / max;
                d = this.u[i];
                this.tau += d * d;
            }
            this.tau = Math.sqrt(this.tau);
            if (this.u[j] < 0.0) {
                this.tau = - this.tau;
            }
            double u_0 = this.u[j] + this.tau;
            this.gamma = u_0 / this.tau;
            int i2 = j + 1;
            while (i2 < this.numRows) {
                double[] arrd = this.u;
                int n = i2++;
                arrd[n] = arrd[n] / u_0;
            }
            this.u[j] = 1.0;
            this.tau *= max;
        }
        this.gammas[j] = this.gamma;
    }

    protected void updateA(int w) {
        int i;
        for (i = w + 1; i < this.numCols; ++i) {
            this.v[i] = this.u[w] * this.dataQR[w * this.numCols + i];
        }
        for (int k = w + 1; k < this.numRows; ++k) {
            int indexQR = k * this.numCols + w + 1;
            int i2 = w + 1;
            while (i2 < this.numCols) {
                double[] arrd = this.v;
                int n = i2++;
                arrd[n] = arrd[n] + this.u[k] * this.dataQR[indexQR++];
            }
        }
        i = w + 1;
        while (i < this.numCols) {
            double[] arrd = this.v;
            int n = i++;
            arrd[n] = arrd[n] * this.gamma;
        }
        for (i = w; i < this.numRows; ++i) {
            double valU = this.u[i];
            int indexQR = i * this.numCols + w + 1;
            for (int j = w + 1; j < this.numCols; ++j) {
                double[] arrd = this.dataQR;
                int n = indexQR++;
                arrd[n] = arrd[n] - valU * this.v[j];
            }
        }
        if (w < this.numCols) {
            this.dataQR[w + w * this.numCols] = - this.tau;
        }
        for (i = w + 1; i < this.numRows; ++i) {
            this.dataQR[w + i * this.numCols] = this.u[i];
        }
    }

    public double[] getGammas() {
        return this.gammas;
    }
}

