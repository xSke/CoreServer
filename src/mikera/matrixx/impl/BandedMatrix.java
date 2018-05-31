/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.impl;

import mikera.arrayz.INDArray;
import mikera.matrixx.AMatrix;
import mikera.matrixx.impl.ABandedMatrix;
import mikera.vectorz.AVector;
import mikera.vectorz.Vector;
import mikera.vectorz.Vectorz;
import mikera.vectorz.impl.ADenseArrayVector;
import mikera.vectorz.util.ErrorMessages;
import mikera.vectorz.util.VectorzException;

public class BandedMatrix
extends ABandedMatrix {
    private static final long serialVersionUID = -4014060138907872914L;
    private final int minBand;
    private final int maxBand;
    private final AVector[] bands;
    private final int rowCount;
    private final int columnCount;

    private BandedMatrix(int rc, int cc, int minBand, AVector[] bands) {
        this.rowCount = rc;
        this.columnCount = cc;
        this.bands = bands;
        this.minBand = minBand;
        this.maxBand = minBand + bands.length - 1;
    }

    public static BandedMatrix create(AMatrix m) {
        int rowCount = m.rowCount();
        int columnCount = m.columnCount();
        int minBand = - m.lowerBandwidth();
        int maxBand = m.upperBandwidth();
        AVector[] bands = new AVector[maxBand - minBand + 1];
        for (int i = minBand; i <= maxBand; ++i) {
            bands[i - minBand] = m.getBand(i).clone();
        }
        return new BandedMatrix(rowCount, columnCount, minBand, bands);
    }

    public static BandedMatrix create(int rowCount, int columnCount, int minBand, int maxBand) {
        if (- minBand >= rowCount) {
            minBand = - rowCount - 1;
        }
        if (maxBand >= columnCount) {
            maxBand = columnCount - 1;
        }
        AVector[] bands = new AVector[maxBand - minBand + 1];
        for (int i = minBand; i <= maxBand; ++i) {
            bands[i - minBand] = Vector.createLength(BandedMatrix.bandLength(rowCount, columnCount, i));
        }
        return new BandedMatrix(rowCount, columnCount, minBand, bands);
    }

    public static /* varargs */ BandedMatrix wrap(int rowCount, int columnCount, int minBand, int maxBand, AVector ... bands) {
        if (bands.length != maxBand - minBand + 1) {
            throw new IllegalArgumentException("Wrong number of bands: " + bands.length);
        }
        for (int i = minBand; i <= maxBand; ++i) {
            AVector b = bands[i - minBand];
            if (b.length() == BandedMatrix.bandLength(rowCount, columnCount, i)) continue;
            throw new IllegalArgumentException("Incorrect length of band " + i + ", was given: " + b.length());
        }
        return new BandedMatrix(rowCount, columnCount, minBand, bands);
    }

    @Override
    public int upperBandwidthLimit() {
        return this.maxBand;
    }

    @Override
    public int lowerBandwidthLimit() {
        return - this.minBand;
    }

    @Override
    public BandedMatrix getTranspose() {
        AVector[] nbands = new AVector[this.bands.length];
        for (int i = 0; i < - this.minBand + this.maxBand + 1; ++i) {
            nbands[i] = this.bands[this.bands.length - 1 - i];
        }
        return BandedMatrix.wrap(this.columnCount(), this.rowCount(), - this.maxBand, - this.minBand, nbands);
    }

    @Override
    public AVector getBand(int band) {
        if (band >= this.minBand && band <= this.maxBand) {
            return this.bands[band - this.minBand];
        }
        if (band >= - this.rowCount && band <= this.columnCount) {
            return Vectorz.createZeroVector(this.bandLength(band));
        }
        throw new IndexOutOfBoundsException(ErrorMessages.invalidBand(this, band));
    }

    @Override
    public int rowCount() {
        return this.rowCount;
    }

    @Override
    public int columnCount() {
        return this.columnCount;
    }

    @Override
    public boolean isView() {
        return true;
    }

    @Override
    public double get(int i, int j) {
        this.checkIndex(i, j);
        return this.unsafeGet(i, j);
    }

    @Override
    public void set(int i, int j, double value) {
        this.getBand(this.bandIndex(i, j)).set(this.bandPosition(i, j), value);
    }

    @Override
    public double unsafeGet(int i, int j) {
        return this.getBand(this.bandIndex(i, j)).unsafeGet(this.bandPosition(i, j));
    }

    @Override
    public void unsafeSet(int i, int j, double value) {
        this.getBand(this.bandIndex(i, j)).unsafeSet(this.bandPosition(i, j), value);
    }

    @Override
    public void addAt(int i, int j, double d) {
        int band = j - i;
        AVector b = this.getBand(band);
        b.addAt(this.bandIndex(i, j), d);
    }

    @Override
    public BandedMatrix exactClone() {
        BandedMatrix b = new BandedMatrix(this.rowCount, this.columnCount, this.minBand, (AVector[])this.bands.clone());
        for (int i = this.minBand; i <= this.maxBand; ++i) {
            b.bands[i - this.minBand] = b.bands[i - this.minBand].exactClone();
        }
        return b;
    }

    @Override
    public void transform(AVector source, AVector dest) {
        if (!(dest instanceof Vector)) {
            super.transform(source, dest);
        } else if (source instanceof Vector) {
            this.transform((Vector)source, (Vector)dest);
        } else {
            Vector t = (Vector)dest;
            t.fill(0.0);
            double[] data = t.getArray();
            for (int i = this.minBand; i <= this.maxBand; ++i) {
                AVector b = this.getBand(i);
                b.addProductToArray(1.0, 0, source, Math.max(i, 0), data, Math.max(- i, 0), this.bandLength(i));
            }
        }
    }

    @Override
    public void transform(Vector source, Vector dest) {
        Vector t = dest;
        t.fill(0.0);
        double[] data = dest.getArray();
        for (int i = this.minBand; i <= this.maxBand; ++i) {
            AVector b = this.getBand(i);
            b.addProductToArray(1.0, 0, source, Math.max(i, 0), data, Math.max(- i, 0), this.bandLength(i));
        }
    }

    @Override
    public void validate() {
        super.validate();
        if (this.minBand != - this.lowerBandwidthLimit()) {
            throw new VectorzException("Mismatched lower limit: " + this.minBand);
        }
        if (this.maxBand != this.upperBandwidthLimit()) {
            throw new VectorzException("Mismatched upper limit: " + this.maxBand);
        }
    }
}

