/*
 * Decompiled with CFR 0_129.
 */
package mikera.indexz.impl;

import mikera.indexz.impl.ComputedIndex;

public class SequentialIndex
extends ComputedIndex {
    private static final long serialVersionUID = 8586796655048075367L;
    private final int start;

    public SequentialIndex(int start, int length) {
        super(length);
        this.start = start;
    }

    @Override
    public int minIndex() {
        return this.start;
    }

    @Override
    public int maxIndex() {
        return this.start + this.length - 1;
    }

    @Override
    public int get(int i) {
        assert (i >= 0 && i < this.length);
        return this.start + i;
    }
}

