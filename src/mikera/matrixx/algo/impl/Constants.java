/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.algo.impl;

public class Constants {
    public static final double EPS;

    static {
        double eps = 1.0;
        while (1.0 + eps > 1.0) {
            eps /= 2.0;
        }
        EPS = eps * 100.0;
    }
}

