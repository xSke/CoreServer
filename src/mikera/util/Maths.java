/*
 * Decompiled with CFR 0_129.
 */
package mikera.util;

public final class Maths {
    public static final double ROOT_TWO = Math.sqrt(2.0);
    public static final double ROOT_THREE = Math.sqrt(3.0);
    public static final double E = 2.718281828459045;
    public static final double PI = 3.141592653589793;
    public static final double TWO_PI = 6.283185307179586;
    public static final double TAU = 6.283185307179586;
    public static final double HALF_PI = 1.5707963267948966;
    public static final double QUARTER_PI = 0.7853981633974483;
    private static final double EPSILON = 1.0E-5;

    public static float sqrt(float a) {
        return (float)Math.sqrt(a);
    }

    public static double sqrt(double a) {
        return Math.sqrt(a);
    }

    public static int clampToInteger(double value, int min, int max) {
        int v = (int)value;
        if (v < min) {
            return min;
        }
        if (v > max) {
            return max;
        }
        return v;
    }

    public static int clampToInteger(float value, int min, int max) {
        int v = (int)value;
        if (v < min) {
            return min;
        }
        if (v > max) {
            return max;
        }
        return v;
    }

    public static final double lerp(double t, double a, double b) {
        return (1.0 - t) * a + t * b;
    }

    public static int middle(int a, int b, int c) {
        if (a < b) {
            if (b < c) {
                return b;
            }
            return a < c ? c : a;
        }
        if (a < c) {
            return a;
        }
        return b < c ? c : b;
    }

    public static float middle(float a, float b, float c) {
        if (a < b) {
            if (b < c) {
                return b;
            }
            return a < c ? c : a;
        }
        if (a < c) {
            return a;
        }
        return b < c ? c : b;
    }

    public static int sign(double x) {
        if (x == 0.0) {
            return 0;
        }
        return x > 0.0 ? 1 : -1;
    }

    public static int sign(float x) {
        if (x == 0.0f) {
            return 0;
        }
        return x > 0.0f ? 1 : -1;
    }

    public static final int sign(int a) {
        return a == 0 ? 0 : (a > 0 ? 1 : -1);
    }

    public static int sign(long a) {
        if (a == 0L) {
            return 0;
        }
        return a > 0L ? 1 : -1;
    }

    public static int mod(int number, int divisor) {
        int r = number % divisor;
        if (r < 0) {
            r += divisor;
        }
        return r;
    }

    public static long mod(long number, long divisor) {
        long r = number % divisor;
        if (r < 0L) {
            r += divisor;
        }
        return r;
    }

    public static long quantize(long increase, long boundary, long base) {
        return (base + increase) / boundary - base / boundary;
    }

    public static double min(double a, double b, double c) {
        double result = a;
        if (b < result) {
            result = b;
        }
        if (c < result) {
            result = c;
        }
        return result;
    }

    public static double max(double a, double b, double c) {
        double result = a;
        if (b > result) {
            result = b;
        }
        if (c > result) {
            result = c;
        }
        return result;
    }

    public static float min(float a, float b, float c) {
        float result = a;
        if (b < result) {
            result = b;
        }
        if (c < result) {
            result = c;
        }
        return result;
    }

    public static float max(float a, float b, float c) {
        float result = a;
        if (b > result) {
            result = b;
        }
        if (c > result) {
            result = c;
        }
        return result;
    }

    public static final float min(float a, float b, float c, float d) {
        float result = a;
        if (result > b) {
            result = b;
        }
        if (result > c) {
            result = c;
        }
        if (result > d) {
            result = d;
        }
        return result;
    }

    public static final float max(float a, float b, float c, float d) {
        float result = a;
        if (result < b) {
            result = b;
        }
        if (result < c) {
            result = c;
        }
        if (result < d) {
            result = d;
        }
        return result;
    }

    public static int min(int a, int b) {
        return a < b ? a : b;
    }

    public static int max(int a, int b) {
        return a > b ? a : b;
    }

    public static float min(float a, float b) {
        return a < b ? a : b;
    }

    public static float max(float a, float b) {
        return a > b ? a : b;
    }

    public static int min(int a, int b, int c) {
        int result = a;
        if (b < result) {
            result = b;
        }
        if (c < result) {
            result = c;
        }
        return result;
    }

    public static int max(int a, int b, int c) {
        int result = a;
        if (b > result) {
            result = b;
        }
        if (c > result) {
            result = c;
        }
        return result;
    }

    public static double logistic(double x) {
        double ea = Math.exp(- x);
        double df = 1.0 / (1.0 + ea);
        if (Double.isNaN(df)) {
            return x > 0.0 ? 1.0 : 0.0;
        }
        return df;
    }

    public static double softplus(double x) {
        if (x > 100.0) {
            return x;
        }
        if (x < -100.0) {
            return 0.0;
        }
        return Math.log(1.0 + Math.exp(x));
    }

    public static double tanhScaled(double x) {
        return 1.7159 * Math.tanh(0.6666666666666666 * x);
    }

    public static double tanhScaledDerivative(double x) {
        double ta = Math.tanh(0.6666666666666666 * x);
        return 1.1439333333333332 * (ta * (1.0 - ta));
    }

    public static double inverseLogistic(double y) {
        if (y >= 1.0) {
            return 800.0;
        }
        if (y <= 0.0) {
            return -800.0;
        }
        double ea = y / (1.0 - y);
        return Math.log(ea);
    }

    public static double logisticDerivative(double x) {
        double sa = Maths.logistic(x);
        return sa * (1.0 - sa);
    }

    public static double tanhDerivative(double x) {
        double sa = Math.tanh(x);
        return 1.0 - sa * sa;
    }

    public static float frac(float a) {
        return a - (float)Maths.roundDown(a);
    }

    public static double frac(double a) {
        return a - Math.floor(a);
    }

    public static int square(byte b) {
        return b * b;
    }

    public static int square(int x) {
        return x * x;
    }

    public static float square(float x) {
        return x * x;
    }

    public static double square(double x) {
        return x * x;
    }

    public static int roundUp(double x) {
        int i = (int)x;
        return (double)i == x ? i : i + 1;
    }

    public static int roundUp(Number x) {
        return Maths.roundUp(x.doubleValue());
    }

    public static int roundUp(float x) {
        int i = (int)x;
        return (float)i == x ? i : i + 1;
    }

    public static int roundDown(double x) {
        if (x >= 0.0) {
            return (int)x;
        }
        int r = (int)x;
        return x == (double)r ? r : r - 1;
    }

    public static int roundDown(float x) {
        if (x >= 0.0f) {
            return (int)x;
        }
        int r = (int)x;
        return x == (float)r ? r : r - 1;
    }

    public static double softMaximum(double x, double y) {
        double max = Math.max(x, y);
        double min = Math.min(x, y);
        return max + Math.log(1.0 + Math.exp(max - min));
    }

    public static final double bound(double v, double min, double max) {
        if (v < min) {
            return min;
        }
        if (v > max) {
            return max;
        }
        return v;
    }

    public static final float bound(float v, float min, float max) {
        if (v < min) {
            return min;
        }
        if (v > max) {
            return max;
        }
        return v;
    }

    public static final int bound(int v, int min, int max) {
        if (v < min) {
            return min;
        }
        if (v > max) {
            return max;
        }
        return v;
    }

    public static int modPower32Bit(int x, int pow) {
        int result = 1;
        for (int n = pow; n > 0; n >>>= 1) {
            if ((n & 1) != 0) {
                result *= x;
            }
            x *= x;
        }
        return result;
    }

    public static boolean notNearZero(double x) {
        return x < -1.0E-5 || x > 1.0E-5;
    }

    public static double mod(double num, double div) {
        double result = num % div;
        if (result < 0.0) {
            result += div;
        }
        return result;
    }

    public static double triangleWave(double x) {
        return (x -= Math.floor(x)) < 0.5 ? x * 2.0 : 2.0 - x * 2.0;
    }
}

