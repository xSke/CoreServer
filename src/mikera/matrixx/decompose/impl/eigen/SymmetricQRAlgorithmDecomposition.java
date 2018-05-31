/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.decompose.impl.eigen;

import mikera.matrixx.AMatrix;
import mikera.matrixx.Matrix;
import mikera.matrixx.decompose.impl.eigen.EigenResult;
import mikera.matrixx.decompose.impl.eigen.SymmetricQREigenHelper;
import mikera.matrixx.decompose.impl.eigen.SymmetricQrAlgorithm;
import mikera.matrixx.decompose.impl.hessenberg.TridiagonalDecompositionHouseholder;
import mikera.vectorz.AVector;
import mikera.vectorz.Vector2;

public class SymmetricQRAlgorithmDecomposition {
    private TridiagonalDecompositionHouseholder decomp;
    private SymmetricQREigenHelper helper;
    private SymmetricQrAlgorithm vector;
    private boolean computeVectorsWithValues = false;
    private double[] values;
    private double[] diag;
    private double[] off;
    private double[] diagSaved;
    private double[] offSaved;
    private Matrix V;
    private Matrix eigenvectors;
    boolean computeVectors;

    public SymmetricQRAlgorithmDecomposition(TridiagonalDecompositionHouseholder decomp, boolean computeVectors) {
        this.decomp = decomp;
        this.computeVectors = computeVectors;
        this.helper = new SymmetricQREigenHelper();
        this.vector = new SymmetricQrAlgorithm(this.helper);
    }

    public SymmetricQRAlgorithmDecomposition(boolean computeVectors) {
        this(new TridiagonalDecompositionHouseholder(), computeVectors);
    }

    public void setComputeVectorsWithValues(boolean computeVectorsWithValues) {
        if (!this.computeVectors) {
            throw new IllegalArgumentException("Compute eigenvalues has been set to false");
        }
        this.computeVectorsWithValues = computeVectorsWithValues;
    }

    public void setMaxIterations(int max) {
        this.vector.setMaxIterations(max);
    }

    public int getNumberOfEigenvalues() {
        return this.helper.getMatrixSize();
    }

    public Vector2 getEigenvalue(int index) {
        return new Vector2(this.values[index], 0.0);
    }

    public AVector getEigenVector(int index) {
        return this.eigenvectors.getRow(index);
    }

    public EigenResult decompose(AMatrix orig) {
        if (orig.columnCount() != orig.rowCount()) {
            throw new IllegalArgumentException("Matrix must be square.");
        }
        if (!orig.isSymmetric()) {
            throw new IllegalArgumentException("Matrix must be symmetric.");
        }
        int N = orig.rowCount();
        if (!this.decomp.decompose(orig)) {
            return null;
        }
        if (this.diag == null || this.diag.length < N) {
            this.diag = new double[N];
            this.off = new double[N - 1];
        }
        this.decomp.getDiagonal(this.diag, this.off);
        this.helper.init(this.diag, this.off, N);
        if (this.computeVectors) {
            if (this.computeVectorsWithValues) {
                if (this.extractTogether()) {
                    return new EigenResult(this.allEigenValues(), this.allEigenVectors());
                }
                return null;
            }
            if (this.extractSeparate(N)) {
                return new EigenResult(this.allEigenValues(), this.allEigenVectors());
            }
            return null;
        }
        if (this.computeEigenValues()) {
            return new EigenResult(this.allEigenValues());
        }
        return null;
    }

    private AVector[] allEigenVectors() {
        AVector[] eig_vecs = new AVector[this.getNumberOfEigenvalues()];
        for (int i = 0; i < eig_vecs.length; ++i) {
            eig_vecs[i] = this.getEigenVector(i);
        }
        return eig_vecs;
    }

    private Vector2[] allEigenValues() {
        Vector2[] eig_vals = new Vector2[this.getNumberOfEigenvalues()];
        for (int i = 0; i < eig_vals.length; ++i) {
            eig_vals[i] = this.getEigenvalue(i);
        }
        return eig_vals;
    }

    private boolean extractTogether() {
        AMatrix temp = this.decomp.getQ(true);
        this.V = Matrix.wrap(temp.rowCount(), temp.columnCount(), temp.asDoubleArray());
        this.helper.setQ(this.V);
        this.vector.setFastEigenvalues(false);
        if (!this.vector.process(-1, null, null)) {
            return false;
        }
        this.eigenvectors = Matrix.create(this.V);
        this.values = this.helper.copyEigenvalues(this.values);
        return true;
    }

    private boolean extractSeparate(int numCols) {
        if (!this.computeEigenValues()) {
            return false;
        }
        this.helper.reset(numCols);
        this.diagSaved = this.helper.swapDiag(this.diagSaved);
        this.offSaved = this.helper.swapOff(this.offSaved);
        AMatrix temp = this.decomp.getQ(true);
        this.V = Matrix.wrap(temp.rowCount(), temp.columnCount(), temp.asDoubleArray());
        this.vector.setQ(this.V);
        if (!this.vector.process(-1, null, null, this.values)) {
            return false;
        }
        this.values = this.helper.copyEigenvalues(this.values);
        this.eigenvectors = Matrix.create(this.V);
        return true;
    }

    private boolean computeEigenValues() {
        this.diagSaved = this.helper.copyDiag(this.diagSaved);
        this.offSaved = this.helper.copyOff(this.offSaved);
        this.vector.setQ(null);
        this.vector.setFastEigenvalues(true);
        if (!this.vector.process(-1, null, null)) {
            return false;
        }
        this.values = this.helper.copyEigenvalues(this.values);
        return true;
    }
}

