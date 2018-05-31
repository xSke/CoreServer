/*
 * Decompiled with CFR 0_129.
 */
package mikera.util.mathz;

import mikera.util.Maths;

public final class FloatMaths {
    public static float round(float f, int dp) {
        float factor = (float)Math.pow(10.0, - dp);
        return (float)Math.round(f / factor) * factor;
    }

    public static final float fastPower(float a, float b) {
        float x = Float.floatToRawIntBits(a);
        x *= 1.1920929E-7f;
        float y = x - (float)((int)Math.floor(x -= 127.0f));
        y = b - (float)((int)Math.floor(b *= x + (y - y * y) * 0.346607f));
        y = (y - y * y) * 0.33971f;
        return Float.intBitsToFloat((int)((b + 127.0f - y) * 8388608.0f));
    }

    public static final float lerp(float t, float a, float b) {
        return (1.0f - t) * a + t * b;
    }

    public static final float smoothFactor(float x) {
        return x * x * (3.0f - 2.0f * x);
    }

    public static float mod(float n, float d) {
        float x = n / d;
        return n - (float)Maths.roundDown(x) * d;
    }

    public static final float smoothStep(float a, float b, float x) {
        if (x <= a) {
            return 0.0f;
        }
        if (x >= b) {
            return 1.0f;
        }
        float t = FloatMaths.bound((x - a) / (b - a), 0.0f, 1.0f);
        return t * t * (3.0f - 2.0f * t);
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

    public static float abs(float a) {
        if (a < 0.0f) {
            return - a;
        }
        return a;
    }

    public static float square(float x) {
        return x * x;
    }

    public static float sqrt(float a) {
        return (float)Math.sqrt(a);
    }

    public static float alternateSqrt(float x) {
        if (x < 0.0f) {
            return 0.0f;
        }
        float r = FloatMaths.approxSqrt(x);
        r -= 0.5f * (r * r - x) / r;
        r -= 0.5f * (r * r - x) / r;
        return r;
    }

    public static float approxSqrt(float x) {
        int i = Float.floatToRawIntBits(x);
        i = i + 1065353216 >>> 1;
        return Float.intBitsToFloat(i);
    }

    public static float fastInverseSqrt(float x) {
        float xhalf = 0.5f * x;
        int i = Float.floatToRawIntBits(x);
        i = 1597463007 - (i >> 1);
        x = Float.intBitsToFloat(i);
        x *= 1.5f - xhalf * x * x;
        return x;
    }

    public static float sin(double a) {
        return (float)Math.sin(a %= 6.283185307179586);
    }

    public static float cos(double a) {
        return (float)Math.cos(a %= 6.283185307179586);
    }

    public static float sin(float a) {
        return (float)Math.sin(a %= 6.2831855f);
    }

    public static float tanh(float x) {
        double ex = Math.exp(2.0f * x);
        float df = (float)((ex - 1.0) / (ex + 1.0));
        if (Float.isNaN(df)) {
            return x > 0.0f ? 1.0f : -1.0f;
        }
        return df;
    }

    public static float tanhDerivative(float x) {
        double sa = Math.tanh(x);
        return 1.0f - (float)(sa * sa);
    }

    public static float cos(float a) {
        return (float)Math.cos(a %= 6.2831855f);
    }
}

