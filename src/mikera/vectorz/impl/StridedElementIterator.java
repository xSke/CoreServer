/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz.impl;

import java.util.Iterator;
import java.util.NoSuchElementException;

public final class StridedElementIterator
implements Iterator<Double> {
    private final double[] source;
    private final int offset;
    private final int maxPos;
    private final int stride;
    private int pos = 0;

    public StridedElementIterator(double[] source, int offset, int length, int stride) {
        this.offset = offset;
        this.source = source;
        this.maxPos = length;
        this.stride = stride;
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
        return this.source[this.offset + this.pos++ * this.stride];
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Cannot remove from StridedElementIterator");
    }
}

