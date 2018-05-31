/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.impl;

import java.util.Iterator;
import java.util.NoSuchElementException;
import mikera.matrixx.impl.AStridedMatrix;

public class StridedElementIterator
implements Iterator<Double> {
    private int col = 0;
    private int row = 0;
    private final int rows;
    private final int cols;
    private final int offset;
    private final int rowStride;
    private final int colStride;
    private final double[] source;

    public StridedElementIterator(AStridedMatrix a) {
        this(a.getArray(), a.rows, a.cols, a.getArrayOffset(), a.rowStride(), a.columnStride());
    }

    public StridedElementIterator(double[] array, int rows, int cols, int arrayOffset, int rowStride, int colStride) {
        this.source = array;
        this.rows = rows;
        this.cols = cols;
        this.rowStride = rowStride;
        this.colStride = colStride;
        this.offset = arrayOffset;
    }

    @Override
    public boolean hasNext() {
        return this.row < this.rows;
    }

    @Override
    public Double next() {
        if (this.row >= this.rows) {
            throw new NoSuchElementException();
        }
        int ox = this.col++;
        int oy = this.row++;
        if (this.col >= this.cols) {
            this.col = 0;
        }
        return this.source[this.offset + ox * this.colStride + oy * this.rowStride];
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Cannot remove from StridedElementIterator");
    }
}

