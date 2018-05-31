/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.decompose;

import mikera.matrixx.AMatrix;
import mikera.matrixx.decompose.ICholeskyResult;

public class Cholesky {
    public static final ICholeskyResult decompose(AMatrix a) {
        return mikera.matrixx.decompose.impl.chol.Cholesky.decompose(a);
    }
}

