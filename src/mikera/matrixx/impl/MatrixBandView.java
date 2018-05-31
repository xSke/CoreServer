/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.impl;

import mikera.arrayz.INDArray;
import mikera.matrixx.AMatrix;
import mikera.vectorz.AScalar;
import mikera.vectorz.AVector;
import mikera.vectorz.impl.AMatrixViewVector;
import mikera.vectorz.impl.MatrixIndexScalar;
import mikera.vectorz.impl.Vector0;
import mikera.vectorz.util.ErrorMessages;

public final class MatrixBandView
extends AMatrixViewVector {
    private int band;

    private MatrixBandView(AMatrix source, int band) {
        super(source, source.bandLength(band));
        this.band = band;
    }

    public static AVector create(AMatrix source, int band) {
        int rc = source.rowCount();
        int cc = source.columnCount();
        if (band > cc || band < - rc) {
            throw new IllegalArgumentException(ErrorMessages.invalidBand(source, band));
        }
        if (band == cc || band == - rc) {
            return Vector0.INSTANCE;
        }
        return new MatrixBandView(source, band);
    }

    @Override
    public void addToArray(double[] data, int offset) {
        int r = this.source.bandStartRow(this.band);
        int c = this.source.bandStartColumn(this.band);
        for (int i = 0; i < this.length; ++i) {
            double[] arrd = data;
            int n = offset + i;
            arrd[n] = arrd[n] + this.source.unsafeGet(r + i, c + i);
        }
    }

    @Override
    public void getElements(double[] data, int offset) {
        int r = this.source.bandStartRow(this.band);
        int c = this.source.bandStartColumn(this.band);
        for (int i = 0; i < this.length; ++i) {
            data[offset + i] = this.source.unsafeGet(r + i, c + i);
        }
    }

    @Override
    protected int calcRow(int i) {
        return this.band < 0 ? i - this.band : i;
    }

    @Override
    protected int calcCol(int i) {
        return this.band > 0 ? i + this.band : i;
    }

    @Override
    public MatrixIndexScalar slice(int i) {
        if (i < 0 || i >= this.length) {
            throw new IndexOutOfBoundsException(ErrorMessages.invalidIndex((INDArray)this, (long)i));
        }
        return MatrixIndexScalar.wrap(this.source, this.calcRow(i), this.calcCol(i));
    }

    @Override
    public AVector exactClone() {
        return new MatrixBandView(this.source.exactClone(), this.band);
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

