/*
 * Decompiled with CFR 0_129.
 */
package mikera.randomz;

import java.util.List;

public class Hash {
    private static final double LONG_SCALE_FACTOR = 1.0842021724855044E-19;
    private static final int[] ZERO_HASHES = new int[20];

    public static final long longHash(long a) {
        a ^= a << 21;
        a ^= a >>> 35;
        a ^= a << 4;
        return a;
    }

    public static final long hash(double x) {
        return Hash.longHash(Hash.longHash(32768L + Long.rotateLeft(Hash.longHash(Double.doubleToRawLongBits(x)), 17)));
    }

    public static final long hash(double x, double y) {
        return Hash.longHash(Hash.longHash(Hash.hash(x) + Long.rotateLeft(Hash.longHash(Double.doubleToRawLongBits(y)), 17)));
    }

    public static final long hash(double x, double y, double z) {
        return Hash.longHash(Hash.longHash(Hash.hash(x, y) + Long.rotateLeft(Hash.longHash(Double.doubleToRawLongBits(z)), 17)));
    }

    public static final long hash(double x, double y, double z, double t) {
        return Hash.longHash(Hash.longHash(Hash.hash(x, y, z) + Long.rotateLeft(Hash.longHash(Double.doubleToRawLongBits(t)), 17)));
    }

    public static final double dhash(double x) {
        long h = Hash.hash(x);
        return (double)(h & Long.MAX_VALUE) * 1.0842021724855044E-19;
    }

    public static final double dhash(double x, double y) {
        long h = Hash.hash(x, y);
        return (double)(h & Long.MAX_VALUE) * 1.0842021724855044E-19;
    }

    public static final double dhash(double x, double y, double z) {
        long h = Hash.hash(x, y, z);
        return (double)(h & Long.MAX_VALUE) * 1.0842021724855044E-19;
    }

    public static final double dhash(double x, double y, double z, double t) {
        long h = Hash.hash(x, y, z, t);
        return (double)(h & Long.MAX_VALUE) * 1.0842021724855044E-19;
    }

    public static final int hashCode(int value) {
        return value;
    }

    public static final int hashCode(double d) {
        return Hash.hashCode(Double.doubleToLongBits(d));
    }

    public static final int hashCode(long l) {
        return (int)(l ^ l >>> 32);
    }

    public static <T> int hashCode(List<T> list) {
        int length = list.size();
        int hashCode = 1;
        for (int i = 0; i < length; ++i) {
            hashCode = 31 * hashCode + list.get(i).hashCode();
        }
        return hashCode;
    }

    public static int zeroVectorHash(int length) {
        if (length < ZERO_HASHES.length) {
            return ZERO_HASHES[length];
        }
        int hashCode = ZERO_HASHES[ZERO_HASHES.length - 1];
        for (int i = 0; i <= length - ZERO_HASHES.length; ++i) {
            hashCode = 31 * hashCode;
        }
        return hashCode;
    }

    static {
        int hashCode = 1;
        for (int i = 0; i < ZERO_HASHES.length; ++i) {
            Hash.ZERO_HASHES[i] = hashCode;
            hashCode = 31 * hashCode;
        }
    }
}

