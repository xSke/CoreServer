/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz.impl;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class RepeatedElementIterator
implements Iterator<Double> {
    long n;
    final Double value;

    public RepeatedElementIterator(long count, Double value) {
        this.value = value;
        this.n = count;
    }

    @Override
    public boolean hasNext() {
        return this.n > 0L;
    }

    @Override
    public Double next() {
        if (this.n <= 0L) {
            throw new NoSuchElementException("Iterator has already been traversed!");
        }
        --this.n;
        return this.value;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}

