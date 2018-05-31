/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.decompose.impl.lu;

import java.util.Arrays;
import mikera.indexz.Index;
import mikera.matrixx.AMatrix;
import mikera.matrixx.Matrix;
import mikera.matrixx.decompose.impl.lu.LUPResult;
import mikera.matrixx.impl.PermutationMatrix;
import mikera.matrixx.solve.impl.TriangularSolver;
import mikera.vectorz.AVector;

public class AltLU {
    private static final double EPS = Math.pow(2.0, -52.0);
    protected Matrix LU;
    protected int maxWidth = -1;
    protected int m;
    protected int n;
    protected double[] dataLU;
    protected double[] vv;
    protected int[] indx;
    protected int[] pivot;

    public static LUPResult decompose(AMatrix A) {
        AltLU alg = new AltLU();
        return alg._decompose(A);
    }

    public AMatrix getLU() {
        return this.LU;
    }

    public int[] getIndx() {
        return this.indx;
    }

    public int[] getPivot() {
        return this.pivot;
    }

    private AMatrix computeL() {
        int i;
        int j;
        int numRows = this.LU.rowCount();
        int numCols = Math.min(this.LU.rowCount(), this.LU.columnCount());
        Matrix lower = Matrix.create(numRows, numCols);
        for (i = 0; i < numCols; ++i) {
            lower.set(i, i, 1.0);
            for (j = 0; j < i; ++j) {
                lower.set(i, j, this.LU.get(i, j));
            }
        }
        if (numRows > numCols) {
            for (i = numCols; i < numRows; ++i) {
                for (j = 0; j < numCols; ++j) {
                    lower.set(i, j, this.LU.get(i, j));
                }
            }
        }
        return lower;
    }

    private AMatrix computeU() {
        int numRows = Math.min(this.LU.rowCount(), this.LU.columnCount());
        int numCols = this.LU.columnCount();
        Matrix upper = Matrix.create(numRows, numCols);
        for (int i = 0; i < numRows; ++i) {
            for (int j = i; j < numCols; ++j) {
                upper.set(i, j, this.LU.get(i, j));
            }
        }
        return upper;
    }

    private PermutationMatrix getPivotMatrix() {
        return PermutationMatrix.create(Index.wrap(Arrays.copyOf(this.pivot, this.LU.rowCount()))).getTranspose();
    }

    private void decomposeCommonInit(AMatrix a) {
        this.m = a.rowCount();
        this.n = a.columnCount();
        this.LU = Matrix.create(a);
        this.dataLU = this.LU.data;
        this.maxWidth = Math.max(this.m, this.n);
        this.vv = new double[this.maxWidth];
        this.indx = new int[this.maxWidth];
        this.pivot = new int[this.maxWidth];
        int i = 0;
        while (i < this.m) {
            this.pivot[i] = i++;
        }
    }

    public double quality() {
        int N = Math.min(this.LU.rowCount(), this.LU.columnCount());
        double max = this.LU.getLeadingDiagonal().maxAbsElement();
        if (Math.abs(max - 0.0) < 1.0E-8) {
            return 0.0;
        }
        return this.LU.diagonalProduct() / Math.pow(max, N);
    }

    public LUPResult _decompose(AMatrix a) {
        this.decomposeCommonInit(a);
        double[] LUcolj = this.vv;
        for (int j = 0; j < this.n; ++j) {
            int i;
            double lujj;
            for (i = 0; i < this.m; ++i) {
                LUcolj[i] = this.dataLU[i * this.n + j];
            }
            i = 0;
            while (i < this.m) {
                int rowIndex = i * this.n;
                int kmax = i < j ? i : j;
                double s = 0.0;
                for (int k = 0; k < kmax; ++k) {
                    s += this.dataLU[rowIndex + k] * LUcolj[k];
                }
                double[] arrd = LUcolj;
                int n = i++;
                double d = arrd[n] - s;
                arrd[n] = d;
                this.dataLU[rowIndex + j] = d;
            }
            int p = j;
            double max = Math.abs(LUcolj[p]);
            for (int i2 = j + 1; i2 < this.m; ++i2) {
                double v = Math.abs(LUcolj[i2]);
                if (v <= max) continue;
                p = i2;
                max = v;
            }
            if (p != j) {
                int rowP = p * this.n;
                int rowJ = j * this.n;
                int endP = rowP + this.n;
                while (rowP < endP) {
                    double t = this.dataLU[rowP];
                    this.dataLU[rowP] = this.dataLU[rowJ];
                    this.dataLU[rowJ] = t;
                    ++rowP;
                    ++rowJ;
                }
                int k = this.pivot[p];
                this.pivot[p] = this.pivot[j];
                this.pivot[j] = k;
            }
            this.indx[j] = p;
            if (j >= this.m || (lujj = this.dataLU[j * this.n + j]) == 0.0) continue;
            for (int i3 = j + 1; i3 < this.m; ++i3) {
                double[] arrd = this.dataLU;
                int n = i3 * this.n + j;
                arrd[n] = arrd[n] / lujj;
            }
        }
        return new LUPResult(this.computeL(), this.computeU(), this.getPivotMatrix());
    }

    public void _solveVectorInternal(double[] vv) {
        int ii = 0;
        for (int i = 0; i < this.n; ++i) {
            int ip = this.indx[i];
            double sum = vv[ip];
            vv[ip] = vv[i];
            if (ii != 0) {
                int index = i * this.n + ii - 1;
                for (int j = ii - 1; j < i; ++j) {
                    sum -= this.dataLU[index++] * vv[j];
                }
            } else if (sum != 0.0) {
                ii = i + 1;
            }
            vv[i] = sum;
        }
        TriangularSolver.solveU(this.dataLU, vv, this.n);
    }

    public double[] _getVV() {
        return this.vv;
    }

    public boolean isSingular() {
        for (int i = 0; i < this.m; ++i) {
            if (Math.abs(this.dataLU[i * this.n + i]) >= EPS) continue;
            return true;
        }
        return false;
    }
}

