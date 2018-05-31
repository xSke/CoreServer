/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz.impl;

import mikera.arrayz.INDArray;
import mikera.vectorz.AVector;
import mikera.vectorz.impl.ASizedVector;

public abstract class AJoinedVector
extends ASizedVector {
    private static final long serialVersionUID = -1931862469605499077L;

    public AJoinedVector(int length) {
        super(length);
    }

    @Override
    public abstract int componentCount();

    @Override
    public abstract AVector getComponent(int var1);

    @Override
    public abstract AJoinedVector withComponents(INDArray[] var1);

    @Override
    public boolean isView() {
        return true;
    }

    @Override
    public void setElements(double[] values, int offset) {
        long n = this.componentCount();
        int i = 0;
        while ((long)i < n) {
            AVector v = this.getComponent(i);
            v.setElements(values, offset);
            offset += v.length();
            ++i;
        }
    }

    @Override
    public boolean equalsArray(double[] values, int offset) {
        long n = this.componentCount();
        int i = 0;
        while ((long)i < n) {
            AVector v = this.getComponent(i);
            if (!v.equalsArray(values, offset)) {
                return false;
            }
            offset += v.length();
            ++i;
        }
        return true;
    }
}

