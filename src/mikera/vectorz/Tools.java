/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import mikera.arrayz.INDArray;
import mikera.vectorz.AScalar;

public final class Tools {
    public static void debugBreak(Object o) {
        o.toString();
    }

    public static int hashCode(int value) {
        return value;
    }

    public static int hashCode(double d) {
        return Tools.hashCode(Double.doubleToLongBits(d));
    }

    public static int hashCode(long l) {
        return (int)(l ^ l >>> 32);
    }

    public static <E> List<E> toList(Iterable<E> iter) {
        ArrayList<E> list = new ArrayList<E>();
        for (E item : iter) {
            list.add(item);
        }
        return list;
    }

    public static <T> ArrayList<T> toList(Iterator<T> iter) {
        ArrayList<T> result = new ArrayList<T>();
        while (iter.hasNext()) {
            result.add(iter.next());
        }
        return result;
    }

    public static int toInt(Object object) {
        if (object instanceof Number) {
            if (object instanceof Integer) {
                return (Integer)object;
            }
            if (object instanceof Long) {
                return Tools.toInt((Long)object);
            }
            double d = ((Number)object).doubleValue();
            return Tools.toInt(d);
        }
        if (object instanceof AScalar) {
            return Tools.toInt(((AScalar)object).get());
        }
        throw new IllegalArgumentException("Cannot convert to int: " + object.toString());
    }

    public static int toInt(long d) {
        int r = (int)d;
        if ((long)r != d) {
            throw new IllegalArgumentException("Out of range when converting to int");
        }
        return r;
    }

    public static int toInt(double d) {
        long n = Math.round(d);
        if ((double)n != d) {
            throw new IllegalArgumentException("Cannot convert to int: " + d);
        }
        return Tools.toInt(n);
    }

    public static int toInt(int d) {
        return d;
    }

    public static int toInt(Number d) {
        return Tools.toInt(d.doubleValue());
    }

    public static double toDouble(Object object) {
        if (object instanceof Double) {
            return (Double)object;
        }
        if (object instanceof Number) {
            return ((Number)object).doubleValue();
        }
        if (object instanceof AScalar) {
            return ((AScalar)object).get();
        }
        throw new IllegalArgumentException("Cannot convert to double: " + object.toString());
    }

    public static double toDouble(Double d) {
        return d;
    }

    public static double toDouble(Number d) {
        return d.doubleValue();
    }

    public static boolean epsilonEquals(double a, double b) {
        return Tools.epsilonEquals(a, b, 1.0E-7);
    }

    public static boolean epsilonEquals(double a, double b, double tolerance) {
        double diff = a - b;
        if (diff > tolerance || diff < - tolerance) {
            return false;
        }
        return true;
    }

    public static boolean isBoolean(double d) {
        return d == 0.0 || d == 1.0;
    }

    public static double[] getElements(INDArray a) {
        int n = (int)a.elementCount();
        double[] data = new double[n];
        a.getElements(data, 0);
        return data;
    }
}

