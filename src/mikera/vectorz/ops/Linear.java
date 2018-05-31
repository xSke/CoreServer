/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz.ops;

import mikera.arrayz.INDArray;
import mikera.vectorz.AVector;
import mikera.vectorz.Op;
import mikera.vectorz.ops.ALinearOp;
import mikera.vectorz.ops.Constant;
import mikera.vectorz.ops.Identity;
import mikera.vectorz.ops.Offset;
import mikera.vectorz.util.DoubleArrays;

public final class Linear
extends ALinearOp {
    private final double factor;
    private final double constant;

    private Linear(double factor, double constant) {
        this.factor = factor;
        this.constant = constant;
    }

    public static ALinearOp create(double factor, double constant) {
        if (factor == 0.0) {
            return Constant.create(constant);
        }
        if (factor == 1.0) {
            if (constant == 0.0) {
                return Identity.INSTANCE;
            }
            return Offset.create(constant);
        }
        if (factor != -1.0 || constant == 0.0) {
            // empty if block
        }
        return new Linear(factor, constant);
    }

    @Override
    public double apply(double x) {
        return this.factor * x + this.constant;
    }

    @Override
    public double applyInverse(double y) {
        return (y - this.constant) / this.factor;
    }

    @Override
    public void applyTo(INDArray v) {
        v.scaleAdd(this.factor, this.constant);
    }

    @Override
    public void applyTo(AVector v) {
        v.scaleAdd(this.factor, this.constant);
    }

    @Override
    public void applyTo(double[] data) {
        DoubleArrays.scaleAdd(data, this.factor, this.constant);
    }

    @Override
    public void applyTo(double[] data, int start, int length) {
        DoubleArrays.scaleAdd(data, start, length, this.factor, this.constant);
    }

    @Override
    public double getFactor() {
        return this.factor;
    }

    @Override
    public double getConstant() {
        return this.constant;
    }

    @Override
    public double averageValue() {
        return this.constant;
    }

    @Override
    public boolean hasDerivative() {
        return true;
    }

    @Override
    public double derivative(double x) {
        return this.factor;
    }

    @Override
    public double derivativeForOutput(double y) {
        return this.factor;
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
    public ALinearOp getInverse() {
        return Linear.create(1.0 / this.factor, (- this.constant) / this.factor);
    }

    public Op compose(ALinearOp op) {
        return Linear.create(this.factor * op.getFactor(), this.factor * op.getConstant() + this.constant);
    }

    @Override
    public Op compose(Op op) {
        if (op instanceof ALinearOp) {
            return this.compose((ALinearOp)op);
        }
        return super.compose(op);
    }
}

