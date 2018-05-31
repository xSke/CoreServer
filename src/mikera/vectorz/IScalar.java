/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz;

import mikera.arrayz.INDArray;
import mikera.vectorz.AScalar;

public interface IScalar
extends INDArray {
    @Override
    public double get();

    @Override
    public void set(double var1);

    @Override
    public AScalar mutable();

    @Override
    public AScalar immutable();
}

