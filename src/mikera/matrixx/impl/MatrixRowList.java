/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.impl;

import java.util.AbstractList;
import mikera.matrixx.AMatrix;
import mikera.vectorz.AVector;

public class MatrixRowList
extends AbstractList<AVector> {
    private AMatrix source;

    public MatrixRowList(AMatrix m) {
        this.source = m;
    }

    @Override
    public AVector get(int index) {
        return this.source.getRow(index);
    }

    @Override
    public int size() {
        return this.source.rowCount();
    }
}

