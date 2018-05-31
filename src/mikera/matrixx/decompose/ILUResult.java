/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.decompose;

import mikera.matrixx.AMatrix;

public interface ILUResult {
    public AMatrix getL();

    public AMatrix getU();

    public double computeDeterminant();
}

