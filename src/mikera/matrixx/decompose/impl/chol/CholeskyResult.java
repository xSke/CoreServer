/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.decompose.impl.chol;

import mikera.matrixx.AMatrix;
import mikera.matrixx.decompose.ICholeskyLDUResult;
import mikera.matrixx.impl.ADiagonalMatrix;
import mikera.matrixx.impl.IdentityMatrix;

public class CholeskyResult
implements ICholeskyLDUResult {
    private final AMatrix L;
    private final ADiagonalMatrix D;
    private final AMatrix U;

    public CholeskyResult(AMatrix L) {
        this(L, IdentityMatrix.create(L.rowCount()), L.getTranspose());
    }

    public CholeskyResult(AMatrix L, ADiagonalMatrix D, AMatrix U) {
        this.L = L;
        this.D = D;
        this.U = U;
    }

    @Override
    public AMatrix getL() {
        return this.L;
    }

    @Override
    public AMatrix getU() {
        return this.U;
    }

    @Override
    public ADiagonalMatrix getD() {
        return this.D;
    }
}

