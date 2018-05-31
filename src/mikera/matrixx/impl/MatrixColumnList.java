/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.impl;

import java.util.AbstractList;
import mikera.matrixx.AMatrix;
import mikera.vectorz.AVector;

public class MatrixColumnList
extends AbstractList<AVector> {
    private AMatrix source;

    public MatrixColumnList(AMatrix m) {
        this.source = m;
    }

    @Override
    public AVector get(int index) {
        return this.source.getColumn(index);
    }

    @Override
    public int size() {
        return this.source.columnCount();
    }
}

