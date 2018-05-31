/*
 * Decompiled with CFR 0_129.
 */
package mikera.arrayz.impl;

import java.util.Iterator;
import mikera.arrayz.INDArray;

public class SliceIterator<T>
implements Iterator<T> {
    private final INDArray source;
    private final int maxPos;
    private int pos;

    public SliceIterator(INDArray source) {
        this.pos = 0;
        this.source = source;
        this.maxPos = source.sliceCount();
    }

    public SliceIterator(INDArray source, int start, int length) {
        this.pos = start;
        this.source = source;
        this.maxPos = start + length;
    }

    @Override
    public boolean hasNext() {
        return this.pos < this.maxPos;
    }

    @Override
    public T next() {
        assert (this.pos < this.maxPos);
        return (T)this.source.slice(this.pos++);
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Cannot remove from VectorIterator");
    }
}

