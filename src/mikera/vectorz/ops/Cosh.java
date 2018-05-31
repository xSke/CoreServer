/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz.ops;

import mikera.vectorz.ops.AFunctionOp;

public class Cosh
extends AFunctionOp {
    public static final Cosh INSTANCE = new Cosh();

    @Override
    public double apply(double x) {
        return Math.cosh(x);
    }
}

