/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.decompose;

import mikera.matrixx.AMatrix;
import mikera.matrixx.decompose.IBidiagonalResult;
import mikera.matrixx.decompose.impl.bidiagonal.BidiagonalRow;

public class Bidiagonal {
    public static IBidiagonalResult decompose(AMatrix A) {
        return BidiagonalRow.decompose(A);
    }

    public static IBidiagonalResult decompose(AMatrix A, boolean compact) {
        return BidiagonalRow.decompose(A, compact);
    }

    public static IBidiagonalResult decomposeCompact(AMatrix A) {
        return Bidiagonal.decompose(A, true);
    }
}

