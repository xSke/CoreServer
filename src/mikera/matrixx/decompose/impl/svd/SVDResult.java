/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.decompose.impl.svd;

import mikera.matrixx.AMatrix;
import mikera.matrixx.decompose.ISVDResult;
import mikera.vectorz.AVector;

public class SVDResult
implements ISVDResult {
    private final AMatrix U;
    private final AMatrix S;
    private final AMatrix V;
    private final AVector singularValues;

    public SVDResult(AMatrix U, AMatrix S, AMatrix V, AVector singularValues) {
        this.U = U;
        this.S = S;
        this.V = V;
        this.singularValues = singularValues;
    }

    @Override
    public AMatrix getU() {
        return this.U;
    }

    @Override
    public AMatrix getS() {
        return this.S;
    }

    @Override
    public AMatrix getV() {
        return this.V;
    }

    @Override
    public AVector getSingularValues() {
        return this.singularValues;
    }
}

