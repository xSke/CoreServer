/*
 * Decompiled with CFR 0_129.
 */
package mikera.transformz.impl;

import mikera.transformz.ATranslation;
import mikera.transformz.Translation;
import mikera.transformz.impl.AConstantTransform;
import mikera.transformz.marker.ISpecialisedTransform;
import mikera.vectorz.AVector;
import mikera.vectorz.Vector4;

public final class ConstantTransform4
extends AConstantTransform
implements ISpecialisedTransform {
    private double x;
    private double y;
    private double z;
    private double t;

    public ConstantTransform4(int inputDimensions, AVector value) {
        super(inputDimensions);
        this.x = value.get(0);
        this.y = value.get(1);
        this.z = value.get(2);
        this.t = value.get(3);
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
            case 3: {
                return this.t;
            }
        }
        throw new IndexOutOfBoundsException("Index: " + i);
    }

    @Override
    public int outputDimensions() {
        return 4;
    }

    @Override
    public void transform(AVector source, AVector dest) {
        if (dest instanceof Vector4) {
            this.transform(source, (Vector4)dest);
            return;
        }
        assert (source.length() == this.inputDimensions());
        dest.set(0, this.x);
        dest.set(1, this.y);
        dest.set(2, this.z);
        dest.set(3, this.t);
    }

    public void transform(AVector source, Vector4 dest) {
        assert (source.length() == this.inputDimensions());
        dest.x = this.x;
        dest.y = this.y;
        dest.z = this.z;
        dest.t = this.t;
    }

    @Override
    public ATranslation getTranslation() {
        return new Translation(new double[]{this.x, this.y, this.z, this.t});
    }

    @Override
    public AVector getConstantValue() {
        return new Vector4(this.x, this.y, this.z, this.y);
    }
}

