/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.decompose.impl.chol;

import mikera.matrixx.AMatrix;
import mikera.matrixx.Matrix;
import mikera.matrixx.decompose.ICholeskyResult;
import mikera.matrixx.decompose.impl.chol.CholeskyResult;

public abstract class CholeskyCommon {
    protected int n;
    protected Matrix T;
    protected double[] t;
    protected double[] vv;

    protected CholeskyCommon() {
    }

    protected ICholeskyResult _decompose(AMatrix mat) {
        if (mat.rowCount() != mat.columnCount()) {
            throw new IllegalArgumentException("Must be a square matrix.");
        }
        this.n = mat.rowCount();
        this.vv = new double[this.n];
        this.T = mat.toMatrix();
        this.t = this.T.data;
        return this.decomposeLower();
    }

    protected abstract CholeskyResult decomposeLower();
}

