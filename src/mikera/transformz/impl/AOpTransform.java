/*
 * Decompiled with CFR 0_129.
 */
package mikera.transformz.impl;

import mikera.transformz.ATransform;
import mikera.vectorz.AVector;
import mikera.vectorz.Op;
import mikera.vectorz.util.ErrorMessages;

public class AOpTransform
extends ATransform {
    private final Op op;
    private final int dims;

    public AOpTransform(Op op, int dims) {
        this.op = op;
        this.dims = dims;
    }

    @Override
    public void transform(AVector source, AVector dest) {
        if (source.length() != this.dims) {
            throw new IllegalArgumentException(ErrorMessages.wrongSourceLength(source));
        }
        if (dest.length() != this.dims) {
            throw new IllegalArgumentException(ErrorMessages.wrongDestLength(source));
        }
        for (int i = 0; i < this.dims; ++i) {
            dest.unsafeSet(i, this.op.apply(source.unsafeGet(i)));
        }
    }

    @Override
    public double calculateElement(int i, AVector inputVector) {
        return this.op.apply(inputVector.get(i));
    }

    @Override
    public int inputDimensions() {
        return this.dims;
    }

    @Override
    public int outputDimensions() {
        return this.dims;
    }
}

