/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz;

import mikera.transformz.ATransform;
import mikera.vectorz.AVector;
import mikera.vectorz.Op;

public interface IOperator {
    public double apply(double var1);

    public void applyTo(AVector var1);

    public void applyTo(AVector var1, int var2, int var3);

    public void applyTo(double[] var1, int var2, int var3);

    public ATransform getTransform(int var1);

    public Op getInverse();
}

