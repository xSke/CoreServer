/*
 * Decompiled with CFR 0_129.
 */
package mikera.transformz.impl;

import mikera.transformz.ATranslation;
import mikera.transformz.Translation3;
import mikera.transformz.impl.AConstantTransform;
import mikera.transformz.marker.ISpecialisedTransform;
import mikera.vectorz.AVector;
import mikera.vectorz.Vector;
import mikera.vectorz.Vector3;

public final class ConstantTransform3
extends AConstantTransform
implements ISpecialisedTransform {
    private double x;
    private double y;
    private double z;

    public ConstantTransform3(int inputDimensions, AVector value) {
        super(inputDimensions);
        this.x = value.get(0);
        this.y = value.get(1);
        this.z = value.get(2);
    }

    @Override
    public double calculateElement(int i, AVector inputVector) {
        switch (i) {
            case 0: {
                return this.x;
            }
            case 1: {
                return this.y;
            }
            case 2: {
                return this.z;
            }
        }
        throw new IndexOutOfBoundsException("Index: " + i);
    }

    @Override
    public int outputDimensions() {
        return 3;
    }

    @Override
    public void transform(AVector source, AVector dest) {
        if (dest instanceof Vector3) {
            this.transform(source, (Vector3)dest);
            return;
        }
        assert (source.length() == this.inputDimensions());
        dest.set(0, this.x);
        dest.set(1, this.y);
        dest.set(2, this.z);
    }

    @Override
    public Vector transform(AVector source) {
        return Vector.of(this.x, this.y, this.z);
    }

    public Vector3 transform(Vector3 source) {
        return Vector3.of(this.x, this.y, this.z);
    }

    public void transform(AVector source, Vector3 dest) {
        assert (source.length() == this.inputDimensions());
        dest.x = this.x;
        dest.y = this.y;
        dest.z = this.z;
    }

    @Override
    public ATranslation getTranslation() {
        return new Translation3(this.x, this.y, this.z);
    }

    @Override
    public AVector getConstantValue() {
        return new Vector3(this.x, this.y, this.z);
    }
}

