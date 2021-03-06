/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.decompose.impl.qr;

import mikera.matrixx.AMatrix;
import mikera.matrixx.decompose.IQRResult;

public class QRResult
implements IQRResult {
    private final AMatrix Q;
    private final AMatrix R;

    public QRResult(AMatrix Q, AMatrix R) {
        this.Q = Q;
        this.R = R;
    }

    @Override
    public AMatrix getQ() {
        return this.Q;
    }

    @Override
    public AMatrix getR() {
        return this.R;
    }
}

