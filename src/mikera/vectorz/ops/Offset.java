/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz.ops;

import mikera.arrayz.INDArray;
import mikera.matrixx.AMatrix;
import mikera.vectorz.AVector;
import mikera.vectorz.Op;
import mikera.vectorz.ops.ALinearOp;
import mikera.vectorz.ops.Constant;
import mikera.vectorz.ops.Linear;

public final class Offset
extends ALinearOp {
    private final double constant;

    private Offset(double constant) {
        this.constant = constant;
    }

    public static Offset create(double offset) {
        return new Offset(offset);
    }

    @Override
    public double apply(double x) {
        return x + this.constant;
    }

    @Override
    public double applyInverse(double y) {
        return y - this.constant;
    }

    @Override
    public void applyTo(INDArray v) {
        v.add(this.constant);
    }

    @Override
    public void applyTo(AMatrix v) {
        v.add(this.constant);
    }

    @Override
    public void applyTo(AVector v) {
        v.add(this.constant);
    }

    @Override
    public void applyTo(double[] data) {
        int i = 0;
        while (i < data.length) {
            double[] arrd = data;
            int n = i++;
            arrd[n] = arrd[n] + this.constant;
        }
    }

    @Override
    public void applyTo(double[] data, int start, int length) {
        for (int i = 0; i < length; ++i) {
            double[] arrd = data;
            int n = i + start;
            arrd[n] = arrd[n] + this.constant;
        }
    }

    @Override
    public double getFactor() {
        return 1.0;
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
        return 1.0;
    }

    @Override
    public double derivativeForOutput(double y) {
        return 1.0;
    }

    @Override
    public Op getDerivativeOp() {
        return Constant.ONE;
    }

    @Override
    public boolean hasInverse() {
        return true;
    }

    @Override
    public Offset getInverse() {
        return Offset.create(- this.constant);
    }

    public Op compose(ALinearOp op) {
        return Linear.create(op.getFactor(), this.constant + op.getConstant());
    }

    @Override
    public Op compose(Op op) {
        if (op instanceof ALinearOp) {
            return this.compose((ALinearOp)op);
        }
        return super.compose(op);
    }
}

