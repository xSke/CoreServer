/*
 * Decompiled with CFR 0_129.
 */
package mikera.transformz;

import mikera.matrixx.AMatrix;
import mikera.transformz.AAffineTransform;
import mikera.transformz.ATransform;
import mikera.transformz.ATranslation;
import mikera.transformz.Transformz;
import mikera.vectorz.AVector;

public final class AffineMN
extends AAffineTransform {
    private final AMatrix matrix;
    private final ATranslation translation;
    private final int inputDimensions;
    private final int outputDimensions;

    public AffineMN(AMatrix matrix, ATranslation translation) {
        this.matrix = matrix;
        this.translation = translation;
        this.inputDimensions = matrix.columnCount();
        this.outputDimensions = matrix.rowCount();
        if (this.outputDimensions != translation.inputDimensions()) {
            throw new IllegalArgumentException("matrix and translation have incompatible dimensionality");
        }
    }

    public AffineMN(AAffineTransform at) {
        this(at.getMatrix().toMutableMatrix(), at.getTranslation().toMutableTranslation());
    }

    public AffineMN(AMatrix m, AVector v) {
        this(m, Transformz.createTranslation(v));
    }

    @Override
    public void composeWith(ATransform trans) {
        if (trans instanceof AffineMN) {
            this.composeWith((AffineMN)trans);
            return;
        }
        super.composeWith(trans);
    }

    public void composeWith(AffineMN trans) {
        super.composeWith(trans);
    }

    @Override
    public AMatrix getMatrix() {
        return this.matrix;
    }

    @Override
    public ATranslation getTranslation() {
        return this.translation;
    }

    @Override
    public void transform(AVector source, AVector dest) {
        this.matrix.transform(source, dest);
        this.translation.transformInPlace(dest);
    }

    @Override
    public int inputDimensions() {
        return this.inputDimensions;
    }

    @Override
    public int outputDimensions() {
        return this.outputDimensions;
    }
}

