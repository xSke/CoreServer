/*
 * Decompiled with CFR 0_129.
 */
package mikera.transformz;

import mikera.matrixx.AMatrix;
import mikera.transformz.ALinearTransform;

public class MatrixTransform
extends ALinearTransform {
    private AMatrix mat;
    private int inputCount;
    private int outputCount;

    public MatrixTransform(AMatrix matrix) {
        this.mat = matrix;
        this.inputCount = this.mat.columnCount();
        this.outputCount = this.mat.rowCount();
    }

    @Override
    public AMatrix getMatrix() {
        return this.mat;
    }

    @Override
    public int inputDimensions() {
        return this.inputCount;
    }

    @Override
    public int outputDimensions() {
        return this.outputCount;
    }
}

