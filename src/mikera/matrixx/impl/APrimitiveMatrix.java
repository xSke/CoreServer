/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.impl;

import mikera.arrayz.INDArray;
import mikera.matrixx.AMatrix;
import mikera.vectorz.AVector;

public abstract class APrimitiveMatrix
extends AMatrix {
    private static final long serialVersionUID = -6061660451592522674L;

    @Override
    public boolean isSquare() {
        return true;
    }

    @Override
    public abstract int checkSquare();

    @Override
    public boolean isFullyMutable() {
        return true;
    }

    @Override
    public AVector getRow(int i) {
        return this.getRowClone(i);
    }

    @Override
    public AVector getColumn(int j) {
        return this.getColumnClone(j);
    }

    @Override
    public AMatrix copy() {
        return this.clone();
    }
}

