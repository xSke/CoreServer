/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.decompose;

import mikera.matrixx.AMatrix;
import mikera.matrixx.decompose.ISVDResult;
import mikera.matrixx.decompose.impl.svd.SvdImplicitQr;

public class SVD {
    public static ISVDResult decompose(AMatrix A) {
        return SvdImplicitQr.decompose(A, false);
    }

    public static ISVDResult decompose(AMatrix A, boolean compact) {
        return SvdImplicitQr.decompose(A, compact);
    }

    public static ISVDResult decomposeCompact(AMatrix A) {
        return SvdImplicitQr.decompose(A, true);
    }
}

