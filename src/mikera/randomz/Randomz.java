/*
 * Decompiled with CFR 0_129.
 */
package mikera.randomz;

import java.util.Random;

public class Randomz {
    public static Random getGenerator() {
        return new mikera.util.Random();
    }

    public static Random getGenerator(long seed) {
        return new mikera.util.Random(seed);
    }
}

