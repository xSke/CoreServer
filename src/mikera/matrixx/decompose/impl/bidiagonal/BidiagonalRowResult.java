/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.decompose.impl.bidiagonal;

import mikera.matrixx.AMatrix;
import mikera.matrixx.decompose.IBidiagonalResult;

public class BidiagonalRowResult
implements IBidiagonalResult {
    private AMatrix B;
    private AMatrix U;
    private AMatrix V;

    public BidiagonalRowResult(AMatrix U, AMatrix B, AMatrix V) {
        this.U = U;
        this.B = B;
        this.V = V;
    }

    @Override
    public AMatrix getB() {
        return this.B;
    }

    @Override
    public AMatrix getU() {
        return this.U;
    }

    @Override
    public AMatrix getV() {
        return this.V;
    }
}

