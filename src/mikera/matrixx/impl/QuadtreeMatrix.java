/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.impl;

import mikera.arrayz.INDArray;
import mikera.arrayz.ISparse;
import mikera.matrixx.AMatrix;
import mikera.matrixx.impl.ABlockMatrix;
import mikera.vectorz.AVector;

public class QuadtreeMatrix
extends ABlockMatrix
implements ISparse {
    private static final long serialVersionUID = -7267626771473908891L;
    private final AMatrix c00;
    private final AMatrix c01;
    private final AMatrix c10;
    private final AMatrix c11;
    private final int rowSplit;
    private final int columnSplit;
    private final int rows;
    private final int cols;

    private QuadtreeMatrix(AMatrix c00, AMatrix c01, AMatrix c10, AMatrix c11) {
        this.c00 = c00;
        this.c01 = c01;
        this.c10 = c10;
        this.c11 = c11;
        this.rowSplit = c00.rowCount();
        this.columnSplit = c00.columnCount();
        this.rows = this.rowSplit + c10.rowCount();
        this.cols = this.columnSplit + c01.columnCount();
    }

    public static QuadtreeMatrix wrap(AMatrix c00, AMatrix c01, AMatrix c10, AMatrix c11) {
        if (c00.rowCount() != c01.rowCount()) {
            throw new IllegalArgumentException("Mismtached submatrix size");
        }
        if (c10.rowCount() != c11.rowCount()) {
            throw new IllegalArgumentException("Mismtached submatrix size");
        }
        if (c00.columnCount() != c10.columnCount()) {
            throw new IllegalArgumentException("Mismtached submatrix size");
        }
        if (c01.columnCount() != c11.columnCount()) {
            throw new IllegalArgumentException("Mismtached submatrix size");
        }
        return new QuadtreeMatrix(c00, c01, c10, c11);
    }

    public static QuadtreeMatrix create(AMatrix c00, AMatrix c01, AMatrix c10, AMatrix c11) {
        return QuadtreeMatrix.wrap(c00.copy(), c01.copy(), c10.copy(), c11.copy());
    }

    @Override
    public boolean isFullyMutable() {
        return this.c00.isFullyMutable() && this.c01.isFullyMutable() && this.c10.isFullyMutable() && this.c11.isFullyMutable();
    }

    @Override
    public boolean isMutable() {
        return this.c00.isMutable() || this.c01.isMutable() || this.c10.isMutable() || this.c11.isMutable();
    }

    @Override
    public boolean isZero() {
        return this.c00.isZero() && this.c01.isZero() && this.c10.isZero() && this.c11.isZero();
    }

    @Override
    public boolean isDiagonal() {
        if (!this.isSquare()) {
            return false;
        }
        if (this.columnSplit == this.rowSplit) {
            return this.c01.isZero() && this.c10.isZero() && this.c00.isDiagonal() && this.c11.isDiagonal();
        }
        return super.isDiagonal();
    }

    @Override
    public int rowCount() {
        return this.rows;
    }

    @Override
    public int columnCount() {
        return this.cols;
    }

    @Override
    public double get(int row, int column) {
        this.checkIndex(row, column);
        return this.unsafeGet(row, column);
    }

    @Override
    public void set(int row, int column, double value) {
        this.checkIndex(row, column);
        this.unsafeSet(row, column, value);
    }

    @Override
    public double unsafeGet(int row, int column) {
        if (row < this.rowSplit) {
            if (column < this.columnSplit) {
                return this.c00.unsafeGet(row, column);
            }
            return this.c01.unsafeGet(row, column - this.columnSplit);
        }
        if (column < this.columnSplit) {
            return this.c10.unsafeGet(row - this.rowSplit, column);
        }
        return this.c11.unsafeGet(row - this.rowSplit, column - this.columnSplit);
    }

    @Override
    public void unsafeSet(int row, int column, double value) {
        if (row < this.rowSplit) {
            if (column < this.columnSplit) {
                this.c00.unsafeSet(row, column, value);
            } else {
                this.c01.unsafeSet(row, column - this.columnSplit, value);
            }
        } else if (column < this.columnSplit) {
            this.c10.unsafeSet(row - this.rowSplit, column, value);
        } else {
            this.c11.unsafeSet(row - this.rowSplit, column - this.columnSplit, value);
        }
    }

    @Override
    public void addAt(int row, int column, double value) {
        if (row < this.rowSplit) {
            if (column < this.columnSplit) {
                this.c00.addAt(row, column, value);
            } else {
                this.c01.addAt(row, column - this.columnSplit, value);
            }
        } else if (column < this.columnSplit) {
            this.c10.addAt(row - this.rowSplit, column, value);
        } else {
            this.c11.addAt(row - this.rowSplit, column - this.columnSplit, value);
        }
    }

    @Override
    public void copyRowTo(int row, double[] data, int offset) {
        if (row < this.rowSplit) {
            this.c00.copyRowTo(row, data, offset);
            this.c01.copyRowTo(row, data, offset + this.columnSplit);
        } else {
            this.c10.copyRowTo(row - this.rowSplit, data, offset);
            this.c11.copyRowTo(row - this.rowSplit, data, offset + this.columnSplit);
        }
    }

    @Override
    public void copyColumnTo(int col, double[] data, int offset) {
        if (col < this.columnSplit) {
            this.c00.copyColumnTo(col, data, offset);
            this.c10.copyColumnTo(col, data, offset + this.rowSplit);
        } else {
            this.c01.copyColumnTo(col - this.columnSplit, data, offset);
            this.c11.copyColumnTo(col - this.columnSplit, data, offset + this.rowSplit);
        }
    }

    @Override
    public long nonZeroCount() {
        return this.c00.nonZeroCount() + this.c01.nonZeroCount() + this.c10.nonZeroCount() + this.c11.nonZeroCount();
    }

    @Override
    public double elementSum() {
        return this.c00.elementSum() + this.c01.elementSum() + this.c10.elementSum() + this.c11.elementSum();
    }

    @Override
    public double elementMin() {
        return Math.min(Math.min(this.c00.elementMin(), this.c01.elementMin()), Math.min(this.c10.elementMin(), this.c11.elementMin()));
    }

    @Override
    public double elementMax() {
        return Math.max(Math.max(this.c00.elementMax(), this.c01.elementMax()), Math.max(this.c10.elementMax(), this.c11.elementMax()));
    }

    @Override
    public void fill(double v) {
        this.c00.fill(v);
        this.c01.fill(v);
        this.c10.fill(v);
        this.c11.fill(v);
    }

    @Override
    public void add(double v) {
        this.c00.add(v);
        this.c01.add(v);
        this.c10.add(v);
        this.c11.add(v);
    }

    @Override
    public void add(AVector v) {
        AVector v0 = v.subVector(0, this.columnSplit);
        AVector v1 = v.subVector(this.columnSplit, this.cols - this.columnSplit);
        this.c00.add(v0);
        this.c01.add(v1);
        this.c10.add(v0);
        this.c11.add(v1);
    }

    @Override
    public AVector getRowView(int row) {
        if (row < this.rowSplit) {
            return this.c00.getRowView(row).join(this.c01.getRowView(row));
        }
        return this.c10.getRowView(row).join(this.c11.getRowView(row -= this.rowSplit));
    }

    @Override
    public AVector getColumnView(int col) {
        if (col < this.columnSplit) {
            return this.c00.getColumnView(col).join(this.c10.getColumnView(col));
        }
        return this.c01.getColumnView(col).join(this.c11.getColumnView(col -= this.columnSplit));
    }

    @Override
    public AMatrix exactClone() {
        return new QuadtreeMatrix(this.c00.exactClone(), this.c01.exactClone(), this.c10.exactClone(), this.c11.exactClone());
    }

    @Override
    public double density() {
        return (double)this.nonZeroCount() / (double)((long)this.rows * (long)this.cols);
    }

    @Override
    public AMatrix getBlock(int rowBlock, int colBlock) {
        switch (rowBlock) {
            case 0: {
                switch (colBlock) {
                    case 0: {
                        return this.c00;
                    }
                    case 1: {
                        return this.c01;
                    }
                }
                throw new IndexOutOfBoundsException("Column Block: " + colBlock);
            }
            case 1: {
                switch (colBlock) {
                    case 0: {
                        return this.c10;
                    }
                    case 1: {
                        return this.c11;
                    }
                }
                throw new IndexOutOfBoundsException("Column Block: " + colBlock);
            }
        }
        throw new IndexOutOfBoundsException("Row Block: " + rowBlock);
    }

    @Override
    public int getBlockColumnStart(int colBlock) {
        switch (colBlock) {
            case 0: {
                return 0;
            }
            case 1: {
                return this.columnSplit;
            }
        }
        throw new IndexOutOfBoundsException("Column Block: " + colBlock);
    }

    @Override
    public int getBlockRowStart(int rowBlock) {
        switch (rowBlock) {
            case 0: {
                return 0;
            }
            case 1: {
                return this.rowSplit;
            }
        }
        throw new IndexOutOfBoundsException("Row Block: " + rowBlock);
    }

    @Override
    public int getBlockColumnCount(int colBlock) {
        return colBlock == 0 ? this.columnSplit : this.cols - this.columnSplit;
    }

    @Override
    public int getBlockRowCount(int rowBlock) {
        return rowBlock == 0 ? this.rowSplit : this.rows - this.rowSplit;
    }

    @Override
    public int getColumnBlockIndex(int col) {
        return col < this.columnSplit ? 0 : 1;
    }

    @Override
    public int getRowBlockIndex(int row) {
        return row < this.rowSplit ? 0 : 1;
    }

    @Override
    public int columnBlockCount() {
        return 2;
    }

    @Override
    public int rowBlockCount() {
        return 2;
    }
}

