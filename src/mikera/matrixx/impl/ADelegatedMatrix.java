/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.impl;

import mikera.arrayz.INDArray;
import mikera.matrixx.AMatrix;
import mikera.matrixx.impl.ARectangularMatrix;
import mikera.vectorz.AVector;
import mikera.vectorz.IOperator;
import mikera.vectorz.Op;

abstract class ADelegatedMatrix
extends ARectangularMatrix {
    private static final long serialVersionUID = 7424713597425793457L;
    protected final AMatrix source;

    protected ADelegatedMatrix(int rows, int cols, AMatrix source) {
        super(rows, cols);
        this.source = source;
    }

    @Override
    public boolean isView() {
        return true;
    }

    @Override
    public boolean isZero() {
        return this.source.isZero();
    }

    @Override
    public boolean isBoolean() {
        return this.source.isBoolean();
    }

    @Override
    public abstract double get(int var1, int var2);

    @Override
    public abstract void set(int var1, int var2, double var3);

    @Override
    public abstract double unsafeGet(int var1, int var2);

    @Override
    public abstract void unsafeSet(int var1, int var2, double var3);

    @Override
    public int componentCount() {
        return this.source.componentCount();
    }

    @Override
    public INDArray getComponent(int k) {
        return this.source.getComponent(k);
    }

    @Override
    public INDArray[] getComponents() {
        return this.source.getComponents();
    }

    @Override
    public void applyOp(Op op) {
        this.source.applyOp(op);
    }

    @Override
    public void applyOp(IOperator op) {
        this.source.applyOp(op);
    }

    @Override
    public void multiply(double factor) {
        this.source.multiply(factor);
    }

    @Override
    public void set(double value) {
        this.source.set(value);
    }

    @Override
    public AVector getLeadingDiagonal() {
        return this.source.getLeadingDiagonal();
    }

    @Override
    public double elementSum() {
        return this.source.elementSum();
    }

    @Override
    public double elementMin() {
        return this.source.elementMin();
    }

    @Override
    public double elementMax() {
        return this.source.elementMax();
    }

    @Override
    public long nonZeroCount() {
        return this.source.nonZeroCount();
    }

    @Override
    public void abs() {
        this.source.abs();
    }

    @Override
    public void square() {
        this.source.square();
    }

    @Override
    public void sqrt() {
        this.source.sqrt();
    }

    @Override
    public void signum() {
        this.source.signum();
    }

    @Override
    public void negate() {
        this.source.negate();
    }

    @Override
    public void log() {
        this.source.log();
    }

    @Override
    public void exp() {
        this.source.exp();
    }

    @Override
    public void reciprocal() {
        this.source.reciprocal();
    }

    @Override
    public void fill(double value) {
        this.source.fill(value);
    }
}

