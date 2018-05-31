/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.impl;

import mikera.arrayz.INDArray;
import mikera.matrixx.AMatrix;
import mikera.vectorz.AVector;
import mikera.vectorz.impl.Vector0;

public abstract class ABlockMatrix
extends AMatrix {
    private static final long serialVersionUID = 5047577000801031158L;

    public abstract AMatrix getBlock(int var1, int var2);

    public abstract int getBlockColumnCount(int var1);

    public abstract int getBlockRowCount(int var1);

    public abstract int getBlockColumnStart(int var1);

    public abstract int getBlockRowStart(int var1);

    public abstract int getColumnBlockIndex(int var1);

    public abstract int getRowBlockIndex(int var1);

    public abstract int columnBlockCount();

    public abstract int rowBlockCount();

    @Override
    public int componentCount() {
        return this.columnBlockCount() * this.rowBlockCount();
    }

    @Override
    public void copyRowTo(int i, double[] dest, int destOffset) {
        this.getRow(i).copyTo(dest, destOffset);
    }

    @Override
    public void copyColumnTo(int j, double[] dest, int destOffset) {
        this.getColumn(j).copyTo(dest, destOffset);
    }

    @Override
    public AMatrix getComponent(int k) {
        int cbc = this.columnBlockCount();
        long i = k / cbc;
        long j = k % cbc;
        return this.getBlock((int)i, (int)j);
    }

    @Override
    public AVector getRowView(int row) {
        int blockIndex = this.getRowBlockIndex(row);
        int blockPos = this.getBlockRowStart(blockIndex);
        int n = this.columnBlockCount();
        AVector v = Vector0.INSTANCE;
        for (int i = 0; i < n; ++i) {
            v = v.join(this.getBlock(blockIndex, i).getRowView(row - blockPos));
        }
        return v;
    }

    @Override
    public boolean isZero() {
        int rbc = this.rowBlockCount();
        int cbc = this.columnBlockCount();
        for (int i = 0; i < rbc; ++i) {
            for (int j = 0; j < cbc; ++j) {
                if (this.getBlock(i, j).isZero()) continue;
                return false;
            }
        }
        return true;
    }

    @Override
    public AVector getColumnView(int col) {
        int blockIndex = this.getColumnBlockIndex(col);
        int blockPos = this.getBlockColumnStart(blockIndex);
        int n = this.rowBlockCount();
        AVector v = Vector0.INSTANCE;
        for (int i = 0; i < n; ++i) {
            v = v.join(this.getBlock(i, blockIndex).getColumnView(col - blockPos));
        }
        return v;
    }
}

