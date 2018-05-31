/*
 * Decompiled with CFR 0_129.
 */
package mikera.arrayz.impl;

import mikera.arrayz.INDArray;
import mikera.arrayz.impl.AbstractArray;
import mikera.vectorz.util.IntArrays;

public abstract class BaseShapedArray
extends AbstractArray<INDArray> {
    private static final long serialVersionUID = -1486048632091493890L;
    protected final int[] shape;

    public BaseShapedArray(int[] shape) {
        this.shape = shape;
    }

    @Override
    public int dimensionality() {
        return this.shape.length;
    }

    @Override
    public int[] getShape() {
        return this.shape;
    }

    @Override
    public int[] getShapeClone() {
        return (int[])this.shape.clone();
    }

    @Override
    public int sliceCount() {
        return this.shape[0];
    }

    @Override
    public int getShape(int dim) {
        return this.shape[dim];
    }

    @Override
    public long elementCount() {
        return IntArrays.arrayProduct(this.shape);
    }
}

