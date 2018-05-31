/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.impl;

import mikera.arrayz.INDArray;
import mikera.matrixx.AMatrix;
import mikera.matrixx.impl.ARectangularMatrix;
import mikera.matrixx.impl.IFastRows;
import mikera.vectorz.AVector;
import mikera.vectorz.Op;
import mikera.vectorz.Vector;
import mikera.vectorz.Vectorz;
import mikera.vectorz.impl.Vector0;
import mikera.vectorz.util.ErrorMessages;

public abstract class AVectorMatrix<T extends AVector>
extends ARectangularMatrix
implements IFastRows {
    private static final long serialVersionUID = -6838429336358726743L;

    protected AVectorMatrix(int rows, int cols) {
        super(rows, cols);
    }

    @Override
    public abstract void replaceRow(int var1, AVector var2);

    public abstract T getRowView(int var1);

    @Override
    public double get(int row, int column) {
        return this.getRow(row).get(column);
    }

    @Override
    public double unsafeGet(int row, int column) {
        return this.getRow(row).unsafeGet(column);
    }

    @Override
    public boolean isFullyMutable() {
        int rc = this.rowCount();
        for (int i = 0; i < rc; ++i) {
            if (this.getRowView(i).isFullyMutable()) continue;
            return false;
        }
        return true;
    }

    @Override
    public void set(double value) {
        int rc = this.rowCount();
        for (int i = 0; i < rc; ++i) {
            this.getRowView(i).set(value);
        }
    }

    @Override
    public void fill(double value) {
        int rc = this.rowCount();
        for (int i = 0; i < rc; ++i) {
            this.getRowView(i).fill(value);
        }
    }

    @Override
    public void set(int row, int column, double value) {
        this.checkColumn(column);
        this.unsafeSet(row, column, value);
    }

    @Override
    public void unsafeSet(int row, int column, double value) {
        this.getRowView(row).unsafeSet(column, value);
    }

    @Override
    public void transform(AVector source, AVector dest) {
        if (source instanceof Vector && dest instanceof Vector) {
            this.transform((Vector)source, (Vector)dest);
            return;
        }
        int rc = this.rowCount();
        if (rc != dest.length()) {
            throw new IllegalArgumentException(ErrorMessages.wrongDestLength(dest));
        }
        for (int i = 0; i < rc; ++i) {
            dest.unsafeSet(i, this.getRow(i).dotProduct(source));
        }
    }

    @Override
    public void transform(Vector source, Vector dest) {
        int rc = this.rowCount();
        if (rc != dest.length()) {
            throw new IllegalArgumentException(ErrorMessages.wrongDestLength(dest));
        }
        for (int i = 0; i < rc; ++i) {
            dest.unsafeSet(i, this.getRow(i).dotProduct(source));
        }
    }

    @Override
    public double calculateElement(int i, AVector inputVector) {
        T row = this.getRowView(i);
        return row.dotProduct(inputVector);
    }

    @Override
    public void copyRowTo(int row, double[] dest, int destOffset) {
        this.getRow(row).getElements(dest, destOffset);
    }

    @Override
    public void copyColumnTo(int col, double[] dest, int destOffset) {
        this.checkColumn(col);
        int rc = this.rowCount();
        for (int i = 0; i < rc; ++i) {
            dest[destOffset++] = this.getRow(i).unsafeGet(col);
        }
    }

    @Override
    public final void getElements(double[] dest, int destOffset) {
        int rc = this.rowCount();
        int cc = this.columnCount();
        for (int i = 0; i < rc; ++i) {
            this.getRow(i).getElements(dest, destOffset);
            destOffset += cc;
        }
    }

    @Override
    public void applyOp(Op op) {
        int rc = this.rowCount();
        for (int i = 0; i < rc; ++i) {
            this.getRowView(i).applyOp(op);
        }
    }

    @Override
    public boolean isView() {
        return true;
    }

    @Override
    public boolean isZero() {
        int rc = this.rowCount();
        for (int i = 0; i < rc; ++i) {
            if (this.getRow(i).isZero()) continue;
            return false;
        }
        return true;
    }

    @Override
    public boolean isBoolean() {
        int rc = this.rowCount();
        for (int i = 0; i < rc; ++i) {
            if (this.getRow(i).isBoolean()) continue;
            return false;
        }
        return true;
    }

    @Override
    public double elementSum() {
        int rc = this.rowCount();
        double result = 0.0;
        for (int i = 0; i < rc; ++i) {
            result += this.getRow(i).elementSum();
        }
        return result;
    }

    @Override
    public double elementSquaredSum() {
        int rc = this.rowCount();
        double result = 0.0;
        for (int i = 0; i < rc; ++i) {
            result += this.getRow(i).elementSquaredSum();
        }
        return result;
    }

    @Override
    public long nonZeroCount() {
        int rc = this.rowCount();
        long result = 0L;
        for (int i = 0; i < rc; ++i) {
            result += this.getRow(i).nonZeroCount();
        }
        return result;
    }

    @Override
    public AVector asVector() {
        int rc = this.rowCount();
        if (rc == 0) {
            return Vector0.INSTANCE;
        }
        if (rc == 1) {
            return this.getRowView(0);
        }
        int cc = this.columnCount();
        if (cc == 1) {
            return this.getColumn(0);
        }
        Object v = this.getRowView(0);
        for (int i = 1; i < rc; ++i) {
            v = Vectorz.join(v, this.getRowView(i));
        }
        return v;
    }

    @Override
    public int componentCount() {
        return this.rows;
    }

    @Override
    public AVector getComponent(int i) {
        return this.getRow(i);
    }

    @Override
    public AVector innerProduct(AVector v) {
        int rc = this.rowCount();
        Vector r = Vector.createLength(rc);
        for (int i = 0; i < rc; ++i) {
            r.unsafeSet(i, this.getRow(i).dotProduct(v));
        }
        return r;
    }

    @Override
    public boolean equals(AMatrix m) {
        return this.equalsByRows(m);
    }

    @Override
    public AMatrix clone() {
        AMatrix avm = super.clone();
        return avm;
    }
}

