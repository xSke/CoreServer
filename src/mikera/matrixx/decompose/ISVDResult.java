/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.decompose;

import mikera.matrixx.AMatrix;
import mikera.vectorz.AVector;

public interface ISVDResult {
    public AMatrix getU();

    public AMatrix getS();

    public AMatrix getV();

    public AVector getSingularValues();
}

