/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz.impl;

import mikera.vectorz.impl.ASizedVector;
import mikera.vectorz.util.VectorzException;

abstract class BaseIndexedVector
extends ASizedVector {
    protected final int[] indexes;

    protected BaseIndexedVector(int length) {
        super(length);
        this.indexes = new int[length];
    }

    public BaseIndexedVector(int[] indexes) {
        super(indexes.length);
        this.indexes = indexes;
    }

    @Override
    public boolean isView() {
        return true;
    }

    @Override
    public void validate() {
        if (this.length != this.indexes.length) {
            throw new VectorzException("Wrong index length");
        }
        super.validate();
    }
}

