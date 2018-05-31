/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.decompose;

import mikera.matrixx.AMatrix;
import mikera.matrixx.decompose.IHessenbergResult;
import mikera.matrixx.decompose.impl.hessenberg.HessenbergSimilarDecomposition;

public class Hessenberg {
    public static IHessenbergResult decompose(AMatrix A) {
        return HessenbergSimilarDecomposition.decompose(A);
    }
}

