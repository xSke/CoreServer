/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.impl;

import mikera.arrayz.INDArray;
import mikera.arrayz.ISparse;
import mikera.matrixx.AMatrix;
import mikera.matrixx.Matrix;
import mikera.matrixx.impl.IFastBands;
import mikera.vectorz.AVector;
import mikera.vectorz.Vector;
import mikera.vectorz.impl.ASizedVector;
import mikera.vectorz.impl.StridedVector;
import mikera.vectorz.util.VectorzException;

public abstract class ABandedMatrix
extends AMatrix
implements ISparse,
IFastBands {
    private static final long serialVersionUID = -229314208418131186L;

    @Override
    public abstract int upperBandwidthLimit();

    @Override
    public abstract int lowerBandwidthLimit();

    @Override
    public abstract AVector getBand(int var1);

    @Override
    public int upperBandwidth() {
        for (int i = this.upperBandwidthLimit(); i > 0; --i) {
            if (this.getBand(i).isZero()) continue;
            return i;
        }
        return 0;
    }

    @Override
    public int lowerBandwidth() {
        for (int i = - this.lowerBandwidthLimit(); i < 0; ++i) {
            if (this.getBand(i).isZero()) continue;
            return - i;
        }
        return 0;
    }

    @Override
    public boolean isMutable() {
        int lb = this.lowerBandwidthLimit();
        int ub = this.upperBandwidthLimit();
        for (int i = - lb; i <= ub; ++i) {
            if (!this.getBand(i).isMutable()) continue;
            return true;
        }
        return false;
    }

    @Override
    public boolean isFullyMutable() {
        return false;
    }

    @Override
    public boolean isSymmetric() {
        if (this.rowCount() != this.columnCount()) {
            return false;
        }
        int bs = Math.max(this.upperBandwidthLimit(), this.lowerBandwidthLimit());
        for (int i = 1; i <= bs; ++i) {
            if (this.getBand(i).equals(this.getBand(- i))) continue;
            return false;
        }
        return true;
    }

    @Override
    public boolean isUpperTriangular() {
        return this.lowerBandwidthLimit() == 0 || this.lowerBandwidth() == 0;
    }

    @Override
    public boolean isLowerTriangular() {
        return this.upperBandwidthLimit() == 0 || this.upperBandwidth() == 0;
    }

    @Override
    public AVector getRow(int row) {
        return new BandedMatrixRow(row);
    }

    @Override
    public long nonZeroCount() {
        long t = 0L;
        for (int i = - this.lowerBandwidthLimit(); i <= this.upperBandwidthLimit(); ++i) {
            t += this.getBand(i).nonZeroCount();
        }
        return t;
    }

    @Override
    public boolean isZero() {
        for (int i = - this.lowerBandwidthLimit(); i <= this.upperBandwidthLimit(); ++i) {
            if (this.getBand(i).isZero()) continue;
            return false;
        }
        return true;
    }

    @Override
    public double elementSum() {
        double t = 0.0;
        for (int i = - this.lowerBandwidthLimit(); i <= this.upperBandwidthLimit(); ++i) {
            t += this.getBand(i).elementSum();
        }
        return t;
    }

    @Override
    public double trace() {
        return this.getBand(0).elementSum();
    }

    @Override
    public double diagonalProduct() {
        return this.getBand(0).elementProduct();
    }

    @Override
    public double elementSquaredSum() {
        double t = 0.0;
        for (int i = - this.lowerBandwidthLimit(); i <= this.upperBandwidthLimit(); ++i) {
            t += this.getBand(i).elementSquaredSum();
        }
        return t;
    }

    @Override
    public void fill(double value) {
        for (int i = - this.rowCount() + 1; i < this.columnCount(); ++i) {
            this.getBand(i).fill(value);
        }
    }

    @Override
    public Matrix toMatrix() {
        int rc = this.rowCount();
        int cc = this.columnCount();
        Matrix m = Matrix.create(rc, cc);
        for (int i = - this.lowerBandwidthLimit(); i <= this.upperBandwidthLimit(); ++i) {
            m.getBand(i).set(this.getBand(i));
        }
        return m;
    }

    @Override
    public Matrix toMatrixTranspose() {
        int rc = this.rowCount();
        int cc = this.columnCount();
        Matrix m = Matrix.create(cc, rc);
        for (int i = - this.lowerBandwidthLimit(); i <= this.upperBandwidthLimit(); ++i) {
            m.getBand(- i).set(this.getBand(i));
        }
        return m;
    }

    @Override
    public void addToArray(double[] data, int offset) {
        int b1 = - this.lowerBandwidth();
        int b2 = this.upperBandwidth();
        int cc = this.columnCount();
        for (int b = b1; b <= b2; ++b) {
            AVector band = this.getBand(b);
            int di = offset + this.bandStartColumn(b) + cc * this.bandStartRow(b);
            band.addToArray(data, di, cc + 1);
        }
    }

    @Override
    public double[] toDoubleArray() {
        double[] result = Matrix.createStorage(this.rowCount(), this.columnCount());
        this.addToArray(result, 0);
        return result;
    }

    @Override
    public void validate() {
        super.validate();
        if (this.lowerBandwidthLimit() < 0) {
            throw new VectorzException("Negative lower bandwidth limit?!?");
        }
        int minBand = - this.lowerBandwidthLimit();
        int maxBand = this.upperBandwidthLimit();
        if (minBand <= - this.rowCount()) {
            throw new VectorzException("Invalid lower limit: " + minBand);
        }
        if (maxBand >= this.columnCount()) {
            throw new VectorzException("Invalid upper limit: " + maxBand);
        }
        for (int i = minBand; i <= maxBand; ++i) {
            AVector v = this.getBand(i);
            if (this.bandLength(i) == v.length()) continue;
            throw new VectorzException("Invalid band length: " + i);
        }
    }

    @Override
    public double density() {
        return (double)this.nonZeroCount() / (double)this.elementCount();
    }

    private final class BandedMatrixRow
    extends ASizedVector {
        final int row;
        final int lower;
        final int upper;

        public BandedMatrixRow(int row) {
            super(ABandedMatrix.this.columnCount());
            this.row = row;
            this.lower = - ABandedMatrix.this.lowerBandwidthLimit();
            this.upper = ABandedMatrix.this.upperBandwidthLimit();
        }

        @Override
        public double get(int i) {
            this.checkIndex(i);
            return this.unsafeGet(i);
        }

        @Override
        public double unsafeGet(int i) {
            int b = i - this.row;
            if (b < this.lower || b > this.upper) {
                return 0.0;
            }
            return ABandedMatrix.this.getBand(b).unsafeGet(Math.min(i, this.row));
        }

        @Override
        public double dotProduct(AVector v) {
            double result = 0.0;
            for (int i = Math.max((int)0, (int)(this.lower + this.row)); i <= Math.min(this.length - 1, this.row + this.upper); ++i) {
                result += ABandedMatrix.this.getBand(i - this.row).unsafeGet(Math.min(i, this.row)) * v.unsafeGet(i);
            }
            return result;
        }

        @Override
        public double dotProduct(Vector v) {
            double result = 0.0;
            for (int i = Math.max((int)0, (int)(this.lower + this.row)); i <= Math.min(this.length - 1, this.row + this.upper); ++i) {
                result += ABandedMatrix.this.getBand(i - this.row).unsafeGet(Math.min(i, this.row)) * v.unsafeGet(i);
            }
            return result;
        }

        @Override
        public void set(int i, double value) {
            this.checkIndex(i);
            this.unsafeSet(i, value);
        }

        @Override
        public void unsafeSet(int i, double value) {
            int b = i - this.row;
            ABandedMatrix.this.getBand(b).unsafeSet(Math.min(i, this.row), value);
        }

        @Override
        public AVector exactClone() {
            return ABandedMatrix.this.exactClone().getRow(this.row);
        }

        @Override
        public boolean isFullyMutable() {
            return ABandedMatrix.this.isFullyMutable();
        }

        @Override
        public double dotProduct(double[] data, int offset) {
            double result = 0.0;
            for (int i = 0; i < this.length; ++i) {
                result += data[offset + i] * this.unsafeGet(i);
            }
            return result;
        }
    }

}

