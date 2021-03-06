/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.decompose.impl.hessenberg;

import mikera.matrixx.AMatrix;
import mikera.matrixx.decompose.IHessenbergResult;

public class HessenbergResult
implements IHessenbergResult {
    private final AMatrix H;
    private final AMatrix Q;

    public HessenbergResult(AMatrix H, AMatrix Q) {
        this.H = H;
        this.Q = Q;
    }

    @Override
    public AMatrix getH() {
        return this.H;
    }

    @Override
    public AMatrix getQ() {
        return this.Q;
    }
}

