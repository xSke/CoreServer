/*
 * Decompiled with CFR 0_129.
 */
package mikera.transformz;

import mikera.indexz.Index;
import mikera.indexz.Indexz;
import mikera.transformz.AAffineTransform;
import mikera.transformz.ITransform;
import mikera.transformz.Transformz;
import mikera.transformz.impl.AConstantTransform;
import mikera.transformz.impl.CompoundTransform;
import mikera.transformz.impl.SubsetTransform;
import mikera.vectorz.AVector;
import mikera.vectorz.Vector;

public abstract class ATransform
implements Cloneable,
ITransform {
    @Override
    public abstract void transform(AVector var1, AVector var2);

    @Override
    public abstract int inputDimensions();

    @Override
    public abstract int outputDimensions();

    public ATransform clone() {
        try {
            return (ATransform)super.clone();
        }
        catch (CloneNotSupportedException e) {
            throw new Error("Clone should be supported!!");
        }
    }

    public ATransform compose(ATransform trans) {
        if (trans instanceof AConstantTransform) {
            return Transformz.constantTransform(trans.inputDimensions(), this.transform(((AConstantTransform)trans).getConstantValue()));
        }
        return new CompoundTransform(this, trans);
    }

    public void composeWith(ATransform trans) {
        throw new UnsupportedOperationException(this.getClass() + " cannot compose with " + trans.getClass());
    }

    public boolean isLinear() {
        return false;
    }

    public boolean isSquare() {
        return this.inputDimensions() == this.outputDimensions();
    }

    public AVector transform(AVector v) {
        Vector temp = Vector.createLength(this.outputDimensions());
        this.transform(v, temp);
        return temp;
    }

    public Vector transform(Vector v) {
        Vector temp = Vector.createLength(this.outputDimensions());
        this.transform(v, temp);
        return temp;
    }

    public double calculateElement(int i, AVector inputVector) {
        AVector r = this.transform(inputVector);
        return r.unsafeGet(i);
    }

    public void transformInPlace(AVector v) {
        throw new UnsupportedOperationException("" + this.getClass() + " does not support transform in place");
    }

    public boolean isIdentity() {
        throw new UnsupportedOperationException();
    }

    public ATransform takeComponents(int length) {
        return this.takeComponents(Indexz.createSequence(length));
    }

    public ATransform takeComponents(int start, int length) {
        return this.takeComponents(Indexz.createSequence(start, length));
    }

    public ATransform takeComponents(Index components) {
        return SubsetTransform.create(this, components);
    }

    public AAffineTransform inverse() {
        throw new UnsupportedOperationException("inverse not supported by " + this.getClass());
    }

    public boolean isInvertible() {
        return false;
    }
}

