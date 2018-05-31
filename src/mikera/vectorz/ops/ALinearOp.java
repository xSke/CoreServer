/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz.ops;

import mikera.arrayz.INDArray;
import mikera.vectorz.AVector;
import mikera.vectorz.Op;
import mikera.vectorz.ops.APolynomialOp;
import mikera.vectorz.ops.Constant;
import mikera.vectorz.ops.Linear;

public abstract class ALinearOp
extends APolynomialOp {
    public abstract double getFactor();

    public abstract double getConstant();

    @Override
    public boolean hasDerivative() {
        return true;
    }

    @Override
    public void applyTo(INDArray a) {
        a.scaleAdd(this.getFactor(), this.getConstant());
    }

    @Override
    public void applyTo(AVector v) {
        v.scaleAdd(this.getFactor(), this.getConstant());
    }

    @Override
    public double derivative(double x) {
        return this.getFactor();
    }

    @Override
    public double derivativeForOutput(double y) {
        return this.getFactor();
    }

    @Override
    public Op getDerivativeOp() {
        return Constant.create(this.getFactor());
    }

    @Override
    public boolean hasInverse() {
        return true;
    }

    @Override
    public Op compose(Op op) {
        if (this.getFactor() == 1.0 && this.getConstant() == 0.0) {
            return op;
        }
        return super.compose(op);
    }

    public Op sum(ALinearOp op) {
        return Linear.create(this.getFactor() + op.getFactor(), this.getConstant() + op.getConstant());
    }

    @Override
    public Op sum(Op op) {
        if (this.getFactor() == 0.0 && this.getConstant() == 0.0) {
            return op;
        }
        return super.sum(op);
    }
}

