/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz.ops;

import java.io.ObjectStreamException;
import mikera.vectorz.AVector;
import mikera.vectorz.ops.ABoundedOp;

public final class Logistic
extends ABoundedOp {
    public static final Logistic INSTANCE = new Logistic();

    public static double logisticFunction(double a) {
        double ea = Math.exp(- a);
        double df = 1.0 / (1.0 + ea);
        if (Double.isNaN(df)) {
            return a > 0.0 ? 1.0 : 0.0;
        }
        return df;
    }

    private static double inverseLogistic(double a) {
        if (a >= 1.0) {
            return 800.0;
        }
        if (a <= 0.0) {
            return -800.0;
        }
        double ea = a / (1.0 - a);
        return Math.log(ea);
    }

    @Override
    public double apply(double x) {
        return Logistic.logisticFunction(x);
    }

    @Override
    public double applyInverse(double y) {
        return Logistic.inverseLogistic(y);
    }

    @Override
    public void applyTo(double[] data, int start, int length) {
        for (int i = 0; i < length; ++i) {
            data[i + start] = Logistic.logisticFunction(data[i + start]);
        }
    }

    @Override
    public void applyTo(AVector v) {
        v.logistic();
    }

    @Override
    public boolean hasDerivative() {
        return true;
    }

    @Override
    public double derivativeForOutput(double y) {
        return y * (1.0 - y);
    }

    @Override
    public double derivative(double x) {
        double y = Logistic.logisticFunction(x);
        return y * (1.0 - y);
    }

    @Override
    public double minValue() {
        return 0.0;
    }

    @Override
    public double maxValue() {
        return 1.0;
    }

    @Override
    public double averageValue() {
        return 0.5;
    }

    private Object readResolve() throws ObjectStreamException {
        return INSTANCE;
    }
}

