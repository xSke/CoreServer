/*
 * Decompiled with CFR 0_129.
 */
package mikera.transformz;

import mikera.vectorz.AVector;

public interface ITransform {
    public void transform(AVector var1, AVector var2);

    public int inputDimensions();

    public int outputDimensions();
}

