/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.impl;

import java.util.Iterator;
import java.util.NoSuchElementException;
import mikera.matrixx.AMatrix;

public class MatrixElementIterator
implements Iterator<Double> {
    private final AMatrix source;
    private int col = 0;
    private int row = 0;

    public MatrixElementIterator(AMatrix source) {
        this.source = source;
        if (source.elementCount() == 0L) {
            this.row = source.rowCount();
        }
    }

    @Override
    public boolean hasNext() {
        return this.row < this.source.rowCount();
    }

    @Override
    public Double next() {
        if (this.row >= this.source.rowCount()) {
            throw new NoSuchElementException();
        }
        int ox = this.col++;
        int oy = this.row++;
        if (this.col >= this.source.columnCount()) {
            this.col = 0;
        }
        return this.source.unsafeGet(oy, ox);
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Cannot remove from MatrixElementIterator");
    }
}

