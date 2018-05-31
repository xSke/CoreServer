/*
 * Decompiled with CFR 0_129.
 */
package mikera.transformz.impl;

import mikera.transformz.ATransform;
import mikera.vectorz.AVector;
import mikera.vectorz.Vectorz;

public class CompoundTransform
extends ATransform {
    private ATransform outer;
    private ATransform inner;

    public CompoundTransform(ATransform outer, ATransform inner) {
        if (inner.outputDimensions() != outer.inputDimensions()) {
            throw new IllegalArgumentException("Transform dimensionality not compatible");
        }
        this.outer = outer;
        this.inner = inner;
    }

    @Override
    public boolean isLinear() {
        return this.inner.isLinear() && this.outer.isLinear();
    }

    @Override
    public void transform(AVector source, AVector dest) {
        AVector temp = Vectorz.newVector(this.inner.outputDimensions());
        this.inner.transform(source, temp);
        this.outer.transform(temp, dest);
    }

    @Override
    public int inputDimensions() {
        return this.inner.inputDimensions();
    }

    @Override
    public CompoundTransform compose(ATransform trans) {
        return new CompoundTransform(this.outer, this.inner.compose(trans));
    }

    @Override
    public int outputDimensions() {
        return this.outer.outputDimensions();
    }
}

