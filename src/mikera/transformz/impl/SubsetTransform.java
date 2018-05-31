/*
 * Decompiled with CFR 0_129.
 */
package mikera.transformz.impl;

import mikera.indexz.Index;
import mikera.transformz.ATransform;
import mikera.vectorz.AVector;

public final class SubsetTransform
extends ATransform {
    private ATransform source;
    private Index components;

    private SubsetTransform(ATransform trans, Index components) {
        this.source = trans;
        this.components = components;
    }

    public static SubsetTransform create(ATransform trans, Index components) {
        if (trans instanceof SubsetTransform) {
            return SubsetTransform.create((SubsetTransform)trans, components);
        }
        return new SubsetTransform(trans, components);
    }

    public static SubsetTransform create(SubsetTransform trans, Index components) {
        return new SubsetTransform(trans.source, components.compose(trans.components));
    }

    @Override
    public double calculateElement(int i, AVector source) {
        return this.source.calculateElement(this.components.get(i), source);
    }

    @Override
    public void transform(AVector source, AVector dest) {
        AVector v = this.source.transform(source);
        dest.set(v, this.components);
    }

    @Override
    public int inputDimensions() {
        return this.source.inputDimensions();
    }

    @Override
    public int outputDimensions() {
        return this.components.length();
    }
}

