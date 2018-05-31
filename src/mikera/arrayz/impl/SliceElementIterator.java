/*
 * Decompiled with CFR 0_129.
 */
package mikera.arrayz.impl;

import java.util.Iterator;
import mikera.arrayz.INDArray;

public class SliceElementIterator
implements Iterator<Double> {
    private final INDArray source;
    private final int maxPos;
    private int pos;
    private Iterator<Double> inner;

    public SliceElementIterator(INDArray source) {
        this.pos = 0;
        this.source = source;
        this.maxPos = source.sliceCount();
        this.inner = source.slice(this.pos).elementIterator();
        if (!this.inner.hasNext()) {
            this.pos = this.maxPos;
        }
    }

    public SliceElementIterator(INDArray source, int start, int length) {
        this.pos = start;
        this.source = source;
        this.maxPos = start + length;
    }

    @Override
    public boolean hasNext() {
        return this.pos < this.maxPos && this.inner.hasNext();
    }

    @Override
    public Double next() {
        Double d = this.inner.next();
        if (!this.inner.hasNext()) {
            ++this.pos;
            if (this.pos < this.maxPos) {
                this.inner = this.source.slice(this.pos).elementIterator();
            }
        }
        return d;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Cannot remove from VectorIterator");
    }
}

