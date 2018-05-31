/*
 * Decompiled with CFR 0_129.
 */
package mikera.arrayz.impl;

import mikera.arrayz.INDArray;

public interface IStridedArray
extends INDArray {
    public double[] getArray();

    public int getArrayOffset();

    @Override
    public int[] getShape();

    public int[] getStrides();

    public int getStride(int var1);

    public boolean isPackedArray();
}

