/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz.ops;

import mikera.vectorz.Op;

public abstract class AFunctionOp
extends Op {
    @Override
    public double averageValue() {
        return this.apply(0.0);
    }
}

