/*
 * Decompiled with CFR 0_129.
 */
package mikera.util.mathz;

public class AlternativeMaths {
    public static int min2(int a, int b) {
        return a ^ (a ^ b) & b - a >> 31;
    }

    public static int max2(int a, int b) {
        return a ^ (a ^ b) & a - b >> 31;
    }

    public static int sign2fast(int a) {
        return 1 + (a >> 31) + (a - 1 >> 31);
    }

    public static double tanh(double x) {
        double ex = Math.exp(2.0 * x);
        double df = (ex - 1.0) / (ex + 1.0);
        if (Double.isNaN(df)) {
            return x > 0.0 ? 1.0 : -1.0;
        }
        return df;
    }

    public static final int sign2(int a) {
        return (a >> 31) + (a > 0 ? 1 : 0);
    }
}

