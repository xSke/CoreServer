/*
 * Decompiled with CFR 0_129.
 */
package mikera.randomz;

public class Probabilities {
    public static double logProbabilityChance(double lp0, double lp1) {
        if (lp0 > lp1) {
            double p1 = Math.exp(lp1 - lp0);
            return 1.0 / (1.0 + p1);
        }
        double p0 = Math.exp(lp0 - lp1);
        return p0 / (1.0 + p0);
    }
}

