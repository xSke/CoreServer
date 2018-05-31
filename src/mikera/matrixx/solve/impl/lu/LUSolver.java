/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.solve.impl.lu;

import mikera.matrixx.AMatrix;
import mikera.matrixx.Matrix;
import mikera.matrixx.decompose.impl.lu.AltLU;
import mikera.matrixx.decompose.impl.lu.LUPResult;
import mikera.matrixx.impl.ADenseArrayMatrix;

public class LUSolver {
    protected AltLU decomp;
    private LUPResult result;
    boolean doImprove = false;
    protected AMatrix A;
    protected int numRows;
    protected int numCols;

    public AMatrix getA() {
        return this.A;
    }

    public LUSolver(boolean improve) {
        this.doImprove = improve;
    }

    public LUSolver() {
        this.doImprove = false;
    }

    public LUPResult setA(AMatrix A) {
        if (!A.isSquare()) {
            throw new IllegalArgumentException("Input must be a square matrix.");
        }
        this.A = A;
        this.numRows = A.rowCount();
        this.numCols = A.columnCount();
        this.decomp = new AltLU();
        this.result = this.decomp._decompose(A);
        return this.result;
    }

    public double quality() {
        return this.decomp.quality();
    }

    public AMatrix invert() {
        if (!this.A.isSquare()) {
            throw new IllegalArgumentException("Matrix must be square for inverse!");
        }
        double[] vv = this.decomp._getVV();
        AMatrix LU = this.decomp.getLU();
        Matrix A_inv = Matrix.create(LU.rowCount(), LU.columnCount());
        int n = this.A.columnCount();
        double[] dataInv = A_inv.data;
        for (int j = 0; j < n; ++j) {
            for (int i = 0; i < n; ++i) {
                vv[i] = i == j ? 1.0 : 0.0;
            }
            this.decomp._solveVectorInternal(vv);
            int index = j;
            int i = 0;
            while (i < n) {
                dataInv[index] = vv[i];
                ++i;
                index += n;
            }
        }
        return A_inv;
    }

    public ADenseArrayMatrix solve(AMatrix b) {
        if (b.rowCount() != this.numCols) {
            throw new IllegalArgumentException("Unexpected matrix size");
        }
        if (Math.abs(this.result.computeDeterminant()) < 1.0E-10) {
            return null;
        }
        Matrix x = Matrix.create(this.numCols, b.columnCount());
        int numCols = b.columnCount();
        double[] dataB = b.asDoubleArray();
        if (dataB == null) {
            dataB = b.toDoubleArray();
        }
        double[] dataX = x.data;
        double[] vv = this.decomp._getVV();
        for (int j = 0; j < numCols; ++j) {
            int index = j;
            int i = 0;
            while (i < this.numCols) {
                vv[i] = dataB[index];
                ++i;
                index += numCols;
            }
            this.decomp._solveVectorInternal(vv);
            index = j;
            i = 0;
            while (i < this.numCols) {
                dataX[index] = vv[i];
                ++i;
                index += numCols;
            }
        }
        if (this.doImprove) {
            this.improveSol(b, x);
        }
        return x;
    }

    public void improveSol(AMatrix b, AMatrix x) {
        if (b.columnCount() != x.columnCount()) {
            throw new IllegalArgumentException("bad shapes");
        }
        double[] dataA = this.A.asDoubleArray();
        double[] dataB = b.asDoubleArray();
        double[] dataX = x.asDoubleArray();
        int nc = b.columnCount();
        int n = b.columnCount();
        double[] vv = this.decomp._getVV();
        for (int k = 0; k < nc; ++k) {
            int i;
            for (i = 0; i < n; ++i) {
                double sdp = - dataB[i * nc + k];
                for (int j = 0; j < n; ++j) {
                    sdp += dataA[i * n + j] * dataX[j * nc + k];
                }
                vv[i] = sdp;
            }
            this.decomp._solveVectorInternal(vv);
            for (i = 0; i < n; ++i) {
                double[] arrd = dataX;
                int n2 = i * nc + k;
                arrd[n2] = arrd[n2] - vv[i];
            }
        }
    }
}

