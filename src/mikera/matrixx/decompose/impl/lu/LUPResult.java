/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.decompose.impl.lu;

import mikera.matrixx.AMatrix;
import mikera.matrixx.decompose.ILUPResult;
import mikera.matrixx.impl.PermutationMatrix;

public class LUPResult
implements ILUPResult {
    final AMatrix l;
    final AMatrix u;
    final PermutationMatrix p;

    public LUPResult(AMatrix l, AMatrix u, PermutationMatrix p) {
        this.l = l;
        this.u = u;
        this.p = p;
    }

    public LUPResult(AMatrix l, AMatrix u) {
        this(l, u, PermutationMatrix.createIdentity(l.rowCount()));
    }

    @Override
    public AMatrix getL() {
        return this.l;
    }

    @Override
    public AMatrix getU() {
        return this.u;
    }

    @Override
    public PermutationMatrix getP() {
        return this.p;
    }

    @Override
    public double computeDeterminant() {
        if (this.l.rowCount() != this.u.columnCount()) {
            throw new IllegalArgumentException("Input must be a square matrix");
        }
        return this.u.diagonalProduct() * this.p.determinant();
    }
}

