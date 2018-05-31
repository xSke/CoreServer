/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz;

import mikera.arrayz.INDArray;
import mikera.vectorz.AVector;

public interface IVector
extends INDArray {
    public int length();

    @Override
    public double get(int var1);

    @Override
    public void set(int var1, double var2);

    @Override
    public AVector immutable();

    @Override
    public AVector mutable();

    public AVector addCopy(AVector var1);

    public AVector subCopy(AVector var1);

    public AVector multiplyCopy(AVector var1);

    public AVector divideCopy(AVector var1);

    public AVector sqrtCopy();

    public boolean epsilonEquals(AVector var1, double var2);

    public double normalise();

    public AVector normaliseCopy();
}

