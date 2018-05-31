/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz.ops;

import mikera.vectorz.Op;
import mikera.vectorz.Ops;
import mikera.vectorz.ops.Product;

public final class Division
extends Op {
    public final Op a;
    public final Op b;

    private Division(Op a, Op b) {
        this.a = a;
        this.b = b;
    }

    private static Op tryOptimisedCreate(Op a, Op b) {
        if (a instanceof Division) {
            Division d = (Division)a;
            return Division.create(d.a, Product.create(a, b));
        }
        if (b instanceof Division) {
            Division d = (Division)b;
            return Division.create(Product.create(a, d.b), d.a);
        }
        return null;
    }

    public static Op create(Op a, Op b) {
        Op t1 = Division.tryOptimisedCreate(a, b);
        if (t1 != null) {
            return t1;
        }
        return new Division(a, b);
    }

    @Override
    public boolean isStochastic() {
        return this.a.isStochastic() || this.b.isStochastic();
    }

    @Override
    public double apply(double x) {
        return this.a.apply(x) / this.b.apply(x);
    }

    @Override
    public double averageValue() {
        return this.a.averageValue() / this.b.averageValue();
    }

    @Override
    public boolean hasDerivative() {
        return this.a.hasDerivative() && this.b.hasDerivative();
    }

    @Override
    public boolean hasDerivativeForOutput() {
        return false;
    }

    @Override
    public double derivative(double x) {
        double ay = this.a.apply(x);
        double by = this.b.apply(x);
        return this.a.derivative(x) * by - ay / this.b.derivative(x);
    }

    @Override
    public Op getDerivativeOp() {
        return Ops.sum(this.a.getDerivativeOp().product(this.b), Ops.divide(Ops.NEGATE.compose(this.a), this.b.getDerivativeOp()));
    }

    @Override
    public String toString() {
        return "Division(" + this.a + "," + this.b + ")";
    }
}

