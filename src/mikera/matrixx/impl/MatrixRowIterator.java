/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.impl;

import java.util.Iterator;
import mikera.matrixx.AMatrix;
import mikera.vectorz.AVector;

public class MatrixRowIterator
implements Iterator<AVector> {
    private final AMatrix source;
    private final int maxPos;
    private int pos = 0;

    public MatrixRowIterator(AMatrix source) {
        this.source = source;
        this.maxPos = source.rowCount();
    }

    @Override
    public boolean hasNext() {
        return this.pos < this.maxPos;
    }

    @Override
    public AVector next() {
        assert (this.pos < this.maxPos);
        return this.source.getRow(this.pos++);
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Cannot remove from MatrixIterator");
    }
}

