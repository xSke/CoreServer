/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.decompose.impl.svd;

import mikera.matrixx.AMatrix;
import mikera.matrixx.Matrix;
import mikera.matrixx.decompose.Bidiagonal;
import mikera.matrixx.decompose.IBidiagonalResult;
import mikera.matrixx.decompose.impl.svd.SVDResult;
import mikera.matrixx.decompose.impl.svd.SvdImplicitQrAlgorithm;
import mikera.matrixx.impl.DenseColumnMatrix;
import mikera.vectorz.AVector;
import mikera.vectorz.Vector;

public class SvdImplicitQr {
    private int numRows;
    private int numCols;
    private int numRowsT;
    private int numColsT;
    private IBidiagonalResult bidiagResult;
    private SvdImplicitQrAlgorithm qralg = new SvdImplicitQrAlgorithm();
    double[] diag;
    double[] off;
    private Matrix Ut;
    private Matrix Vt;
    private double[] singularValues;
    private int numSingular;
    private boolean compact;
    private boolean transposed;
    private Matrix A_mod = Matrix.create(1, 1);

    public static SVDResult decompose(AMatrix A, boolean compact) {
        SvdImplicitQr svd = new SvdImplicitQr(compact);
        return svd._decompose(A);
    }

    public SvdImplicitQr(boolean compact) {
        this.compact = compact;
    }

    public AVector getSingularValues() {
        return Vector.wrap(this.singularValues);
    }

    public int numberOfSingularValues() {
        return this.numSingular;
    }

    public boolean isCompact() {
        return this.compact;
    }

    public AMatrix getU() {
        return this.Ut.getTranspose();
    }

    public AMatrix getV() {
        return this.Vt.getTranspose();
    }

    public AMatrix getS() {
        int m = this.compact ? this.numSingular : this.numRows;
        int n = this.compact ? this.numSingular : this.numCols;
        Matrix S = Matrix.create(m, n);
        for (int i = 0; i < this.numSingular; ++i) {
            S.unsafeSet(i, i, this.singularValues[i]);
        }
        return S;
    }

    public SVDResult _decompose(AMatrix _orig) {
        Matrix orig = _orig.copy().toMatrix();
        this.setup(orig);
        if (this.bidiagonalization(orig)) {
            return null;
        }
        if (this.computeUSV()) {
            return null;
        }
        this.makeSingularPositive();
        this.undoTranspose();
        return new SVDResult(this.getU(), this.getS(), this.getV(), this.getSingularValues());
    }

    private boolean bidiagonalization(Matrix orig) {
        this.A_mod = this.transposed ? orig.getTransposeCopy().toMatrix() : orig.copy().toMatrix();
        this.bidiagResult = Bidiagonal.decompose(this.A_mod, this.compact);
        return this.bidiagResult == null;
    }

    private void undoTranspose() {
        if (this.transposed) {
            Matrix temp = this.Vt;
            this.Vt = this.Ut;
            this.Ut = temp;
        }
    }

    private boolean computeUSV() {
        this.diag = this.bidiagResult.getB().getBand(0).toDoubleArray();
        this.off = this.bidiagResult.getB().getBand(1).toDoubleArray();
        this.qralg.setMatrix(this.numRowsT, this.numColsT, this.diag, this.off);
        this.Ut = this.bidiagResult.getU().getTranspose().toMatrix();
        this.Vt = this.bidiagResult.getV().getTranspose().toMatrix();
        this.qralg.setFastValues(false);
        this.qralg.setUt(this.Ut);
        this.qralg.setVt(this.Vt);
        boolean ret = !this.qralg.process();
        return ret;
    }

    private void setup(Matrix orig) {
        boolean bl = this.transposed = orig.columnCount() > orig.rowCount();
        if (this.transposed) {
            this.numRowsT = orig.columnCount();
            this.numColsT = orig.rowCount();
        } else {
            this.numRowsT = orig.rowCount();
            this.numColsT = orig.columnCount();
        }
        this.numRows = orig.rowCount();
        this.numCols = orig.columnCount();
        this.diag = new double[this.numColsT];
        this.off = new double[this.numColsT - 1];
    }

    private void makeSingularPositive() {
        this.numSingular = this.qralg.getNumberOfSingularValues();
        this.singularValues = this.qralg.getSingularValues();
        double[] UtData = this.Ut.asDoubleArray();
        for (int i = 0; i < this.numSingular; ++i) {
            double val = this.qralg.getSingularValue(i);
            if (val < 0.0) {
                this.singularValues[i] = 0.0 - val;
                int start = i * this.Ut.columnCount();
                int stop = start + this.Ut.columnCount();
                for (int j = start; j < stop; ++j) {
                    UtData[j] = 0.0 - UtData[j];
                }
                continue;
            }
            this.singularValues[i] = val;
        }
    }

    public int numRows() {
        return this.numRows;
    }

    public int numCols() {
        return this.numCols;
    }
}

