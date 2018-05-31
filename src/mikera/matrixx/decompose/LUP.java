/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.decompose;

import mikera.matrixx.AMatrix;
import mikera.matrixx.decompose.ILUPResult;
import mikera.matrixx.decompose.impl.lu.AltLU;

public class LUP {
    public static ILUPResult decompose(AMatrix A) {
        return AltLU.decompose(A);
    }
}

