/*
 * Decompiled with CFR 0_129.
 */
package mikera.util;

import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

public final class Rand {
    private static volatile long state = Rand.xorShift64(System.nanoTime() | -889275714L);
    private static final double DOUBLE_SCALE_FACTOR = 1.0 / Math.pow(2.0, 63.0);
    private static final float FLOAT_SCALE_FACTOR = (float)(1.0 / Math.pow(2.0, 63.0));

    public static final long nextLong() {
        long a = state;
        state = Rand.xorShift64(a);
        return a;
    }

    public static final long xorShift64(long a) {
        a ^= a << 21;
        a ^= a >>> 35;
        a ^= a << 4;
        return a;
    }

    public static final int xorShift32(int a) {
        a ^= a << 13;
        a ^= a >>> 17;
        a ^= a << 5;
        return a;
    }

    public static boolean chance(double d) {
        return Rand.nextDouble() < d;
    }

    public static boolean chance(float d) {
        return Rand.nextFloat() < d;
    }

    public static int best(int r, int n, int s) {
        boolean found;
        if (n <= 0 || r < 0 || r > n || s < 0) {
            return 0;
        }
        int[] rolls = new int[n];
        for (int i = 0; i < n; ++i) {
            rolls[i] = Rand.d(s);
        }
        do {
            found = false;
            for (int x = 0; x < n - 1; ++x) {
                if (rolls[x] >= rolls[x + 1]) continue;
                int t = rolls[x];
                rolls[x] = rolls[x + 1];
                rolls[x + 1] = t;
                found = true;
            }
        } while (found);
        int sum = 0;
        for (int i = 0; i < r; ++i) {
            sum += rolls[i];
        }
        return sum;
    }

    public static double sigmoid(double a) {
        double ea = Math.exp(- a);
        double df = 1.0 / (1.0 + ea);
        if (Double.isNaN(df)) {
            return a > 0.0 ? 1.0 : 0.0;
        }
        return df;
    }

    public static int sig(float x) {
        return Rand.chance(Rand.sigmoid(x)) ? 1 : 0;
    }

    public static int po(double mean) {
        if (mean <= 0.0) {
            if (mean < 0.0) {
                throw new IllegalArgumentException();
            }
            return 0;
        }
        if (mean > 400.0) {
            return Rand.poLarge(mean);
        }
        return Rand.poMedium(mean);
    }

    private static int poMedium(double mean) {
        int r = 0;
        double p = Math.exp(- mean);
        for (double a = Rand.nextDouble(); a > p; a -= p) {
            p = p * mean / (double)(++r);
        }
        return r;
    }

    private static int poLarge(double mean) {
        return (int)(0.5 + Rand.n(mean, Math.sqrt(mean)));
    }

    public static int po(int numerator, int denominator) {
        return Rand.po((double)numerator / (double)denominator);
    }

    public static int d(int number, int sides) {
        int total = 0;
        for (int i = 0; i < number; ++i) {
            total += Rand.d(sides);
        }
        return total;
    }

    public static double factorial(int n) {
        if (n < 2) {
            return 1.0;
        }
        double result = 1.0;
        for (int i = 2; i <= n; ++i) {
            result *= (double)i;
        }
        return result;
    }

    public static double factorialRatio(int n, int r) {
        if (n < 2) {
            return 1.0;
        }
        if (n < r) {
            return 1.0 / Rand.factorialRatio(r, n);
        }
        double result = 1.0;
        for (int i = r + 1; i <= n; ++i) {
            result *= (double)i;
        }
        return result;
    }

    public static double cumulativeBinomialChance(int r, int n, double p) {
        double prob = 0.0;
        for (int i = 0; i <= r; ++i) {
            prob += Rand.binomialChance(i, n, p);
        }
        return prob;
    }

    public static double combinations(int r, int n) {
        if (r > n - r) {
            return Rand.combinations(n - r, n);
        }
        return Rand.factorialRatio(n, n - r) / Rand.factorial(r);
    }

    public static double binomialChance(int r, int n, double p) {
        return Rand.combinations(r, n) * Math.pow(p, r) * Math.pow(1.0 - p, n - r);
    }

    public static double exp(double mean) {
        return (- Math.log(Rand.nextDouble())) * mean;
    }

    public static int geom(double p) {
        return (int)Math.floor(Math.log(Rand.nextDouble()) / Math.log(1.0 - p));
    }

    public static final int nextInt() {
        return (int)(Rand.nextLong() >> 32);
    }

    public static final short nextShort() {
        return (short)(Rand.nextLong() >> 32);
    }

    public static final char nextChar() {
        return (char)(Rand.nextLong() >> 32);
    }

    public static final String nextLetterString(int length) {
        char[] cs = new char[length];
        for (int i = 0; i < length; ++i) {
            cs[i] = Rand.nextLetter();
        }
        return new String(cs);
    }

    public static final byte nextByte() {
        return (byte)(Rand.nextLong() >> 32);
    }

    public static boolean nextBoolean() {
        return (Rand.nextLong() & 65536L) != 0L;
    }

    public static final char nextLetter() {
        return (char)Rand.range(97, 122);
    }

    public static final int r(int s) {
        if (s < 0) {
            throw new IllegalArgumentException();
        }
        long result = (Rand.nextLong() >>> 32) * (long)s >> 32;
        return (int)result;
    }

    public static final int otherIndex(int i, int max) {
        return (Rand.r(max - 1) + i) % max;
    }

    public static final double nextDouble() {
        return (double)(Rand.nextLong() >>> 1) * DOUBLE_SCALE_FACTOR;
    }

    public static final float nextFloat() {
        return (float)(Rand.nextLong() >>> 1) * FLOAT_SCALE_FACTOR;
    }

    public static final double u() {
        return Rand.nextDouble();
    }

    public static final double u(double max) {
        return Rand.u(0.0, max);
    }

    public static final double u(double min, double max) {
        return min + Rand.nextDouble() * (max - min);
    }

    public static final int round(double d) {
        int i = (int)Math.floor(d);
        int rem = Rand.nextDouble() < d - (double)i ? 1 : 0;
        return i + rem;
    }

    public static final int range(int n1, int n2) {
        if (n1 > n2) {
            int t = n1;
            n1 = n2;
            n2 = t;
        }
        return n1 + Rand.r(n2 - n1 + 1);
    }

    public static final int d(int sides) {
        return Rand.r(sides) + 1;
    }

    public static final int d3() {
        return Rand.d(3);
    }

    public static final int d4() {
        return Rand.d(4);
    }

    public static final int d6() {
        return Rand.d(6);
    }

    public static final int d8() {
        return Rand.d(8);
    }

    public static final int d10() {
        return Rand.d(10);
    }

    public static final int d12() {
        return Rand.d(12);
    }

    public static final int d20() {
        return Rand.d(20);
    }

    public static final int d100() {
        return Rand.d(100);
    }

    public static double n(double u, double sd) {
        return Rand.nextGaussian() * sd + u;
    }

    public static double nextGaussian() {
        double x;
        double d2;
        double y;
        while ((d2 = (x = 2.0 * Rand.nextDouble() - 1.0) * x + (y = 2.0 * Rand.nextDouble() - 1.0) * y) > 1.0 || d2 == 0.0) {
        }
        double radiusFactor = Math.sqrt(-2.0 * Math.log(d2) / d2);
        return x * radiusFactor;
    }

    public static String nextString() {
        char[] cs = new char[Rand.po(4.0)];
        for (int i = 0; i < cs.length; ++i) {
            cs[i] = Rand.nextLetter();
        }
        return String.valueOf(cs);
    }

    public static <T> void shuffle(T[] ts) {
        for (int i = 0; i < ts.length - 1; ++i) {
            int j = Rand.r(ts.length - i);
            if (i == j) continue;
            T t = ts[i];
            ts[i] = ts[j];
            ts[j] = t;
        }
    }

    public static void chooseIntegers(int[] dest, int destOffset, int length, int maxValue) {
        if (length > maxValue) {
            throw new Error("Cannot choose " + length + " items from a set of " + maxValue);
        }
        if (maxValue > 4 * length) {
            Rand.chooseIntegersBySampling(dest, destOffset, length, maxValue);
            return;
        }
        Rand.chooseIntegersByExclusion(dest, destOffset, length, maxValue);
    }

    public static void randIntegers(int[] dest, int destOffset, int length, int maxValue) {
        for (int i = destOffset; i < destOffset + length; ++i) {
            dest[i] = Rand.r(maxValue);
        }
    }

    private static void chooseIntegersByExclusion(int[] dest, int destOffset, int n, int maxValue) {
        while (n > 0) {
            if (n == maxValue || Rand.r(maxValue) < n) {
                dest[destOffset + n - 1] = maxValue - 1;
                --n;
            }
            --maxValue;
        }
    }

    private static void chooseIntegersByReservoirSampling(int[] dest, int destOffset, int n, int maxValue) {
        int found = 0;
        for (int i = 0; i < maxValue; ++i) {
            if (found < n) {
                dest[destOffset + found] = i;
                ++found;
                continue;
            }
            int ni = Rand.r(i + 1);
            if (ni >= n) continue;
            dest[destOffset + ni] = i;
        }
    }

    private static void chooseIntegersBySampling(int[] dest, int destOffset, int n, int maxValue) {
        TreeSet<Integer> s = new TreeSet<Integer>();
        while (s.size() < n) {
            int v = Rand.r(maxValue);
            s.add(v);
        }
        for (Integer i : s) {
            dest[destOffset++] = i;
        }
    }

    public static <T> T pick(T[] ts) {
        return ts[Rand.r(ts.length)];
    }

    public static <T> T pick(List<T> ts) {
        return ts.get(Rand.r(ts.size()));
    }

    public static <T> T pick(Collection<T> ts) {
        int n = ts.size();
        if (n == 0) {
            throw new Error("Empty collection!");
        }
        int p = Rand.r(n);
        for (T t : ts) {
            if (p-- != 0) continue;
            return t;
        }
        throw new Error("Shouldn't get here!");
    }

    public static void fillUniform(float[] d, int start, int length) {
        for (int i = 0; i < length; ++i) {
            d[start + i] = Rand.nextFloat();
        }
    }

    public static void fillBinary(float[] d, int start, int length) {
        for (int i = 0; i < length; ++i) {
            d[start + i] = Rand.r(2);
        }
    }

    public static void fillGaussian(float[] d, int start, int length, float u, float sd) {
        for (int i = 0; i < length; ++i) {
            d[start + i] = (float)Rand.n(u, sd);
        }
    }

    public static void fillGaussian(double[] d, int start, int length, double u, double sd) {
        for (int i = 0; i < length; ++i) {
            d[start + i] = Rand.n(u, sd);
        }
    }

    public static void fillBinary(double[] data, int start, int length, double mean) {
        for (int i = 0; i < length; ++i) {
            data[start + i] = Rand.binary(mean);
        }
    }

    public static double binary(double mean) {
        return Rand.nextDouble() < mean ? 1.0 : 0.0;
    }

    public static void binarySample(float[] temp, int offset, int length) {
        for (int i = offset; i < offset + length; ++i) {
            temp[i] = Rand.nextFloat() < temp[i] ? 1.0f : 0.0f;
        }
    }

    public static void binarySample(double[] temp, int offset, int length) {
        for (int i = offset; i < offset + length; ++i) {
            temp[i] = Rand.nextDouble() < temp[i] ? 1.0 : 0.0;
        }
    }

    public static int indexFromWeights(double[] probabilities) {
        double total = 0.0;
        for (int i = 0; i < probabilities.length; ++i) {
            total += probabilities[i];
        }
        double position = total * Rand.nextDouble();
        for (int i = 0; i < probabilities.length; ++i) {
            if ((position -= probabilities[i]) > 0.0) continue;
            return i;
        }
        throw new Error("Funny probabilities array!");
    }
}

