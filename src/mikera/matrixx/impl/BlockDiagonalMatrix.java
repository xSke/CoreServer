/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.impl;

import java.util.Arrays;
import mikera.arrayz.INDArray;
import mikera.arrayz.ISparse;
import mikera.matrixx.AMatrix;
import mikera.matrixx.impl.ABlockMatrix;
import mikera.matrixx.impl.ZeroMatrix;
import mikera.vectorz.util.IntArrays;

public class BlockDiagonalMatrix
extends ABlockMatrix
implements ISparse {
    private static final long serialVersionUID = -8569790012901451992L;
    private final AMatrix[] mats;
    private final int[] sizes;
    private final int[] offsets;
    private final int blockCount;
    private final int size;

    private BlockDiagonalMatrix(AMatrix[] newMats) {
        this.blockCount = newMats.length;
        this.mats = newMats;
        this.sizes = new int[this.blockCount];
        this.offsets = new int[this.blockCount + 1];
        int totalSize = 0;
        for (int i = 0; i < this.blockCount; ++i) {
            int size;
            this.sizes[i] = size = this.mats[i].rowCount();
            this.offsets[i] = totalSize;
            totalSize += size;
        }
        this.offsets[this.blockCount] = this.size = totalSize;
    }

    public static /* varargs */ BlockDiagonalMatrix create(AMatrix ... blocks) {
        return new BlockDiagonalMatrix((AMatrix[])blocks.clone());
    }

    @Override
    public boolean isFullyMutable() {
        return false;
    }

    @Override
    public boolean isMutable() {
        for (int i = 0; i < this.blockCount; ++i) {
            if (!this.mats[i].isMutable()) continue;
            return true;
        }
        return true;
    }

    @Override
    public AMatrix getBlock(int rowBlock, int colBlock) {
        if (rowBlock != colBlock) {
            return ZeroMatrix.create(this.getBlockRowCount(rowBlock), this.getBlockColumnCount(colBlock));
        }
        return this.mats[rowBlock];
    }

    @Override
    public int getBlockColumnStart(int colBlock) {
        return this.offsets[colBlock];
    }

    @Override
    public int getBlockRowStart(int rowBlock) {
        return this.offsets[rowBlock];
    }

    @Override
    public int getBlockColumnCount(int colBlock) {
        return this.sizes[colBlock];
    }

    @Override
    public int getBlockRowCount(int rowBlock) {
        return this.sizes[rowBlock];
    }

    @Override
    public int getColumnBlockIndex(int col) {
        if (col < 0 || col >= this.size) {
            throw new IndexOutOfBoundsException("Column: " + col);
        }
        int i = IntArrays.indexLookup(this.offsets, col);
        if (i < 0) {
            throw new IndexOutOfBoundsException("Column: " + col);
        }
        return i;
    }

    @Override
    public int getRowBlockIndex(int row) {
        if (row < 0 || row >= this.size) {
            throw new IndexOutOfBoundsException("Row: " + row);
        }
        int i = IntArrays.indexLookup(this.offsets, row);
        if (i < 0) {
            throw new IndexOutOfBoundsException("Row: " + row);
        }
        return i;
    }

    @Override
    public int rowCount() {
        return this.size;
    }

    @Override
    public int columnCount() {
        return this.size;
    }

    @Override
    public double get(int row, int column) {
        int bj;
        int bi = this.getRowBlockIndex(row);
        if (bi != (bj = this.getColumnBlockIndex(column))) {
            return 0.0;
        }
        int i = row - this.offsets[bi];
        int j = column - this.offsets[bi];
        return this.mats[bi].unsafeGet(i, j);
    }

    @Override
    public void set(int row, int column, double value) {
        int bj;
        int bi = this.getRowBlockIndex(row);
        if (bi != (bj = this.getColumnBlockIndex(column))) {
            throw new UnsupportedOperationException("Block Diagonal Matrix immutable at this position");
        }
        int i = row - this.offsets[bi];
        int j = column - this.offsets[bi];
        this.mats[bi].unsafeSet(i, j, value);
    }

    @Override
    public AMatrix exactClone() {
        AMatrix[] newMats = (AMatrix[])this.mats.clone();
        for (int i = 0; i < this.blockCount; ++i) {
            newMats[i] = newMats[i].exactClone();
        }
        return new BlockDiagonalMatrix(newMats);
    }

    @Override
    public int columnBlockCount() {
        return this.blockCount;
    }

    @Override
    public int rowBlockCount() {
        return this.blockCount;
    }

    @Override
    public void copyColumnTo(int col, double[] dest, int destOffset) {
        int i = this.getColumnBlockIndex(col);
        int si = this.offsets[i];
        int di = this.offsets[i + 1];
        Arrays.fill(dest, destOffset, si + destOffset, 0.0);
        this.mats[i].copyColumnTo(col - si, dest, destOffset + si);
        Arrays.fill(dest, di + destOffset, this.size + destOffset, 0.0);
    }

    @Override
    public void copyRowTo(int row, double[] dest, int destOffset) {
        int i = this.getRowBlockIndex(row);
        int si = this.offsets[i];
        int di = this.offsets[i + 1];
        Arrays.fill(dest, destOffset, si + destOffset, 0.0);
        this.mats[i].copyRowTo(row - si, dest, destOffset + si);
        Arrays.fill(dest, di + destOffset, this.size + destOffset, 0.0);
    }

    @Override
    public double density() {
        long nzero = 0L;
        for (int i = 0; i < this.blockCount; ++i) {
            nzero += this.mats[i].nonZeroCount();
        }
        return (double)nzero / (double)this.elementCount();
    }

    @Override
    public boolean hasUncountable() {
        for (int i = 0; i < this.blockCount; ++i) {
            if (!this.mats[i].hasUncountable()) continue;
            return true;
        }
        return false;
    }

    @Override
    public double elementPowSum(double p) {
        double result = 0.0;
        for (int i = 0; i < this.blockCount; ++i) {
            result += this.mats[i].elementPowSum(p);
        }
        return result;
    }

    @Override
    public double elementAbsPowSum(double p) {
        double result = 0.0;
        for (int i = 0; i < this.blockCount; ++i) {
            result += this.mats[i].elementAbsPowSum(p);
        }
        return result;
    }
}

