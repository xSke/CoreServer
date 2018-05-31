/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz.impl;

import mikera.arrayz.INDArray;
import mikera.vectorz.AScalar;
import mikera.vectorz.impl.ASizedVector;
import mikera.vectorz.impl.ArrayIndexScalar;

public abstract class AArrayVector
extends ASizedVector {
    private static final long serialVersionUID = -6271828303431809681L;
    protected final double[] data;

    protected AArrayVector(int length, double[] data) {
        super(length);
        this.data = data;
    }

    @Override
    public AScalar slice(int i) {
        this.checkIndex(i);
        return ArrayIndexScalar.wrap(this.data, this.index(i));
    }

    protected abstract int index(int var1);
}

