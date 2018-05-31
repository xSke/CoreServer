/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz.ops;

import mikera.vectorz.Op;
import mikera.vectorz.ops.ALinearOp;
import mikera.vectorz.ops.Constant;
import mikera.vectorz.ops.Linear;
import mikera.vectorz.ops.Quadratic;

public final class Product
extends Op {
    public final Op a;
    public final Op b;

    private Product(Op a, Op b) {
        this.a = a;
        this.b = b;
    }

    private static Op tryOptimisedCreate(Op a, Op b) {
        if (a instanceof Constant) {
            return Linear.create(((Constant)a).value, 0.0).compose(b);
        }
        if (a instanceof ALinearOp && b instanceof ALinearOp) {
            ALinearOp la = (ALinearOp)a;
            ALinearOp lb = (ALinearOp)b;
            double a1 = la.getFactor();
            double a2 = la.getConstant();
            double b1 = lb.getFactor();
            double b2 = lb.getConstant();
            return Quadratic.create(a1 * b1, a1 * b2 + b1 * a2, b2 * a2);
        }
        return null;
    }

    public static Op create(Op a, Op b) {
        Op t1 = Product.tryOptimisedCreate(a, b);
        if (t1 != null) {
            return t1;
        }
        Op t2 = Product.tryOptimisedCreate(b, a);
        if (t2 != null) {
            return t2;
        }
        return new Product(a, b);
    }

    @Override
    public double apply(double x) {
        return this.a.apply(x) * this.b.apply(x);
    }

    @Override
    public double averageValue() {
        return this.a.averageValue() * this.b.averageValue();
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
        return this.a.derivative(x) * by + this.b.derivative(x) * ay;
    }

    @Override
    public Op getDerivativeOp() {
        Op da = this.a.getDerivativeOp();
        Op db = this.b.getDerivativeOp();
        return da.product(this.b).sum(db.product(this.a));
    }

    @Override
    public boolean isStochastic() {
        return this.a.isStochastic() || this.b.isStochastic();
    }

    @Override
    public String toString() {
        return "Product(" + this.a + "," + this.b + ")";
    }
}

