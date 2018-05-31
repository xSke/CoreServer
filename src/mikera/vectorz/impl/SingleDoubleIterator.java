/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz.impl;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class SingleDoubleIterator
implements Iterator<Double> {
    boolean used = false;
    final double value;

    public SingleDoubleIterator(double value) {
        this.value = value;
    }

    @Override
    public boolean hasNext() {
        return !this.used;
    }

    @Override
    public Double next() {
        if (this.used) {
            throw new NoSuchElementException("Iterator has already been traversed!");
        }
        this.used = true;
        return this.value;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}

