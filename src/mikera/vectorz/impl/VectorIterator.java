/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz.impl;

import java.util.Iterator;
import java.util.NoSuchElementException;
import mikera.vectorz.AVector;

public final class VectorIterator
implements Iterator<Double> {
    private final AVector source;
    private final int maxPos;
    private int pos;

    public VectorIterator(AVector source) {
        this.pos = 0;
        this.source = source;
        this.maxPos = source.length();
    }

    public VectorIterator(AVector source, int start, int length) {
        this.pos = start;
        this.source = source;
        this.maxPos = start + length;
    }

    @Override
    public boolean hasNext() {
        return this.pos < this.maxPos;
    }

    @Override
    public Double next() {
        if (this.pos >= this.maxPos) {
            throw new NoSuchElementException();
        }
        return this.source.unsafeGet(this.pos++);
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Cannot remove from VectorIterator");
    }
}

