/*
 * Decompiled with CFR 0_129.
 */
package mikera.indexz;

import mikera.indexz.AIndex;
import mikera.vectorz.util.IntArrays;

public class GrowableIndex
extends AIndex {
    private static final long serialVersionUID = 4466899007444582094L;
    private int[] data;
    private int count = 0;

    public GrowableIndex() {
        this(4);
    }

    public GrowableIndex(int initialCapacity) {
        this.data = new int[initialCapacity];
    }

    public static GrowableIndex create(AIndex source) {
        int n = source.length();
        GrowableIndex g = new GrowableIndex(n);
        source.copyTo(g.data, 0);
        g.count = n;
        return g;
    }

    @Override
    public int get(int i) {
        if (i < 0 || i >= this.count) {
            throw new IndexOutOfBoundsException("Index: " + i);
        }
        return this.data[i];
    }

    @Override
    public int length() {
        return this.count;
    }

    @Override
    public void set(int i, int value) {
        if (i < 0 || i >= this.count) {
            if (i == this.count) {
                this.append(i);
                return;
            }
            throw new IndexOutOfBoundsException("Index: " + i);
        }
        this.data[i] = value;
    }

    @Override
    public void copyTo(int[] array, int offset) {
        System.arraycopy(this.data, 0, array, offset, this.count);
    }

    public void append(int i) {
        this.ensureCapacity(this.count + 1);
        this.data[this.count++] = i;
    }

    public void checkedAppend(int i) {
        if (this.count > 0 && this.data[this.count - 1] >= i) {
            throw new IllegalArgumentException("Trying to append non-increasing index value: " + i);
        }
        this.append(i);
    }

    private void ensureCapacity(int capacity) {
        if (this.data.length >= capacity) {
            return;
        }
        int nLen = Math.max(capacity, this.data.length * 2 + 4);
        int[] ndata = new int[nLen];
        System.arraycopy(this.data, 0, ndata, 0, this.count);
        this.data = ndata;
    }

    @Override
    public int indexPosition(int x) {
        return IntArrays.indexPosition(this.data, x, 0, this.count);
    }

    @Override
    public GrowableIndex exactClone() {
        return GrowableIndex.create(this);
    }
}

