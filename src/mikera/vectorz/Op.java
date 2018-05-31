/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz;

import mikera.arrayz.INDArray;
import mikera.matrixx.AMatrix;
import mikera.transformz.ATransform;
import mikera.transformz.impl.AOpTransform;
import mikera.vectorz.AScalar;
import mikera.vectorz.AVector;
import mikera.vectorz.IOperator;
import mikera.vectorz.Ops;
import mikera.vectorz.impl.ADenseArrayVector;
import mikera.vectorz.ops.Composed;
import mikera.vectorz.ops.Derivative;
import mikera.vectorz.ops.Division;
import mikera.vectorz.ops.Inverse;
import mikera.vectorz.ops.Product;
import mikera.vectorz.ops.Sum;

public abstract class Op
implements IOperator {
    @Override
    public abstract double apply(double var1);

    public double applyInverse(double y) {
        throw new UnsupportedOperationException("Inverse not defined for operator: " + this.toString());
    }

    @Override
    public void applyTo(AVector v) {
        if (v instanceof ADenseArrayVector) {
            this.applyTo((ADenseArrayVector)v);
        } else {
            v.applyOp(this);
        }
    }

    public void applyTo(AMatrix m) {
        m.applyOp(this);
    }

    @Override
    public void applyTo(AVector v, int start, int length) {
        if (start < 0) {
            throw new IllegalArgumentException("Negative start position: " + start);
        }
        if (start == 0 && length == v.length()) {
            v.applyOp(this);
        } else {
            v.subVector(start, length).applyOp(this);
        }
    }

    public void applyTo(AScalar s) {
        s.set(this.apply(s.get()));
    }

    public void applyTo(ADenseArrayVector v) {
        this.applyTo(v.getArray(), v.getArrayOffset(), v.length());
    }

    public void applyTo(INDArray a) {
        if (a instanceof AVector) {
            this.applyTo((AVector)a);
        } else if (a instanceof AMatrix) {
            this.applyTo((AMatrix)a);
        } else if (a instanceof AScalar) {
            this.applyTo((AScalar)a);
        } else {
            a.applyOp(this);
        }
    }

    @Override
    public void applyTo(double[] data, int start, int length) {
        for (int i = 0; i < length; ++i) {
            double x = data[start + i];
            data[start + i] = this.apply(x);
        }
    }

    public void applyTo(double[] data) {
        this.applyTo(data, 0, data.length);
    }

    @Override
    public ATransform getTransform(int dims) {
        return new AOpTransform(this, dims);
    }

    @Override
    public Op getInverse() {
        if (this.hasInverse()) {
            return new Inverse(this);
        }
        throw new UnsupportedOperationException("No inverse available: " + this.getClass());
    }

    public boolean hasDerivative() {
        return false;
    }

    public boolean hasDerivativeForOutput() {
        return this.hasDerivative();
    }

    public boolean hasInverse() {
        return false;
    }

    public double derivativeForOutput(double y) {
        assert (!this.hasDerivative());
        throw new UnsupportedOperationException("No derivative defined for " + this.toString());
    }

    public double derivative(double x) {
        assert (!this.hasDerivative());
        return this.derivativeForOutput(this.apply(x));
    }

    public boolean isStochastic() {
        return false;
    }

    public abstract double averageValue();

    public double minValue() {
        return Double.NEGATIVE_INFINITY;
    }

    public double maxValue() {
        return Double.POSITIVE_INFINITY;
    }

    public double minDomain() {
        return Double.NEGATIVE_INFINITY;
    }

    public double maxDomain() {
        return Double.POSITIVE_INFINITY;
    }

    public boolean isDomainBounded() {
        return this.minDomain() >= -1.7976931348623157E308 || this.maxDomain() <= Double.MAX_VALUE;
    }

    public boolean validateOutput(double[] output) {
        double min = this.minValue();
        double max = this.maxValue();
        for (double d : output) {
            if (d >= min && d <= max) continue;
            return false;
        }
        return true;
    }

    public void constrainValues(double[] src, double[] dest, int offset, int length) {
        if (!this.isBounded()) {
            System.arraycopy(src, 0, dest, offset, length);
        }
        double min = this.minValue();
        double max = this.maxValue();
        for (int i = offset; i < offset + length; ++i) {
            double v = src[i];
            dest[i] = v > max ? max : (v < min ? min : v);
        }
    }

    public boolean isBounded() {
        return this.minValue() >= -1.7976931348623157E308 || this.maxValue() <= Double.MAX_VALUE;
    }

    public Op getDerivativeOp() {
        return new Derivative(this);
    }

    public static Op compose(Op op1, Op op2) {
        return Ops.compose(op1, op2);
    }

    public Op compose(Op op) {
        return Composed.create(this, op);
    }

    public Op product(Op op) {
        return Product.create(this, op);
    }

    public Op divide(Op op) {
        return Division.create(this, op);
    }

    public Op sum(Op op) {
        return Sum.create(this, op);
    }

    public String toString() {
        return this.getClass().toString();
    }
}

