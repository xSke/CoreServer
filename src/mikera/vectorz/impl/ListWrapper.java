/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz.impl;

import java.util.AbstractList;
import java.util.Iterator;
import mikera.arrayz.INDArray;
import mikera.vectorz.AVector;
import mikera.vectorz.util.ErrorMessages;

public final class ListWrapper
extends AbstractList<Double> {
    private final AVector wrappedVector;
    private final int length;
    private static final Double ZERO = 0.0;

    public ListWrapper(AVector v) {
        this.wrappedVector = v;
        this.length = v.length();
    }

    @Override
    public Double get(int index) {
        double v = this.wrappedVector.get(index);
        if (v == 0.0) {
            return ZERO;
        }
        return v;
    }

    @Override
    public Double set(int index, Double value) {
        if (index < 0 || index >= this.length) {
            throw new IndexOutOfBoundsException(ErrorMessages.invalidIndex((INDArray)this.wrappedVector, (long)index));
        }
        this.wrappedVector.unsafeSet(index, value);
        return null;
    }

    @Override
    public int size() {
        return this.length;
    }

    @Override
    public Iterator<Double> iterator() {
        return this.wrappedVector.iterator();
    }
}

