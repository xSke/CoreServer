/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz.ops;

import mikera.vectorz.AVector;
import mikera.vectorz.Op;
import mikera.vectorz.impl.ADenseArrayVector;
import mikera.vectorz.ops.ALinearOp;
import mikera.vectorz.ops.APolynomialOp;
import mikera.vectorz.ops.Linear;
import mikera.vectorz.ops.Square;

public final class Quadratic
extends APolynomialOp {
    private final double a;
    private final double b;
    private final double c;

    private Quadratic(double a, double b, double c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }

    public static Op create(double a, double b, double c) {
        if (a == 0.0) {
            return Linear.create(b, c);
        }
        if (a == 1.0 && b == 0.0 && c == 0.0) {
            return Square.INSTANCE;
        }
        return new Quadratic(a, b, c);
    }

    @Override
    public final double apply(double x) {
        return this.a * x * x + this.b * x + this.c;
    }

    @Override
    public double applyInverse(double y) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void applyTo(AVector v) {
        if (v instanceof ADenseArrayVector) {
            ADenseArrayVector av = (ADenseArrayVector)v;
            this.applyTo(av.getArray(), av.getArrayOffset(), av.length());
            return;
        }
        int len = v.length();
        for (int i = 0; i < len; ++i) {
            double x = v.unsafeGet(i);
            v.unsafeSet(i, this.apply(x));
        }
    }

    @Override
    public void applyTo(double[] data) {
        for (int i = 0; i < data.length; ++i) {
            double x = data[i];
            data[i] = this.apply(x);
        }
    }

    @Override
    public void applyTo(double[] data, int start, int length) {
        for (int i = 0; i < length; ++i) {
            double x = data[i + start];
            data[i + start] = this.apply(x);
        }
    }

    @Override
    public double averageValue() {
        return this.apply(-2.0 * this.b / this.a) + this.a;
    }

    @Override
    public boolean hasDerivative() {
        return true;
    }

    @Override
    public double derivative(double x) {
        return 2.0 * this.a * x + this.b;
    }

    @Override
    public double derivativeForOutput(double y) {
        return this.b;
    }

    @Override
    public Op getDerivativeOp() {
        return Linear.create(2.0 * this.a, this.b);
    }

    @Override
    public boolean hasInverse() {
        return false;
    }

    public Op compose(ALinearOp op) {
        double f = op.getFactor();
        double g = op.getConstant();
        return Quadratic.create(this.a * f * f, 2.0 * this.a * f * g + f * this.b, this.a * g * g + this.b * g + this.c);
    }

    @Override
    public Op compose(Op op) {
        if (op instanceof ALinearOp) {
            return this.compose((ALinearOp)op);
        }
        return super.compose(op);
    }
}

