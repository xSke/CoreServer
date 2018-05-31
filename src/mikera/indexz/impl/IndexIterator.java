/*
 * Decompiled with CFR 0_129.
 */
package mikera.indexz.impl;

import java.util.Iterator;
import mikera.indexz.AIndex;

public class IndexIterator
implements Iterator<Integer> {
    private final AIndex source;
    private final int length;
    private int pos = 0;

    public IndexIterator(AIndex source) {
        this.source = source;
        this.length = source.length();
    }

    @Override
    public boolean hasNext() {
        return this.pos < this.length;
    }

    @Override
    public Integer next() {
        assert (this.pos < this.length);
        return this.source.get(this.pos++);
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Cannot remove from IndexIterator");
    }
}

