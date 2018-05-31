/*
 * Decompiled with CFR 0_129.
 */
package mikera.indexz.impl;

import mikera.indexz.AIndex;

public abstract class ComputedIndex
extends AIndex {
    protected final int length;

    public ComputedIndex(int length) {
        assert (length >= 0);
        this.length = length;
    }

    @Override
    public boolean isFullyMutable() {
        return false;
    }

    @Override
    public final int length() {
        return this.length;
    }

    @Override
    public int indexPosition(int x) {
        for (int i = 0; i < this.length; ++i) {
            if (this.get(i) != x) continue;
            return i;
        }
        return -1;
    }

    @Override
    public void set(int i, int value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public AIndex exactClone() {
        return this.clone();
    }
}

