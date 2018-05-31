/*
 * Decompiled with CFR 0_129.
 */
package mikera.transformz.impl;

import mikera.transformz.ATranslation;
import mikera.transformz.Transformz;
import mikera.transformz.impl.AConstantTransform;
import mikera.vectorz.AVector;
import mikera.vectorz.Vector;

public final class ConstantTransform
extends AConstantTransform {
    private final int outputDimensions;
    private double[] constant;

    public ConstantTransform(int inputDimensions, AVector value) {
        super(inputDimensions);
        this.outputDimensions = value.length();
        this.constant = new double[this.outputDimensions];
        value.getElements(this.constant, 0);
    }

    @Override
    public double calculateElement(int i, AVector v) {
        return this.constant[i];
    }

    @Override
    public int outputDimensions() {
        return this.outputDimensions;
    }

    @Override
    public void transform(AVector source, AVector dest) {
        assert (source.length() == this.inputDimensions());
        dest.setElements(this.constant);
    }

    @Override
    public ATranslation getTranslation() {
        return Transformz.createTranslation(this.constant);
    }

    @Override
    public AVector getConstantValue() {
        return Vector.wrap(this.constant);
    }
}

