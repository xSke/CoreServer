/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.algo;

import mikera.matrixx.AMatrix;
import mikera.matrixx.Matrix;
import mikera.matrixx.impl.ImmutableMatrix;
import mikera.vectorz.util.DoubleArrays;
import mikera.vectorz.util.ErrorMessages;

public class Multiplications {
    protected static final int WORKING_SET_TARGET = 8192;

    public static Matrix multiply(AMatrix a, AMatrix b) {
        if (a instanceof Matrix) {
            return Multiplications.multiply((Matrix)a, b);
        }
        if (a instanceof ImmutableMatrix) {
            return Multiplications.multiply(Matrix.wrap(a.rowCount(), a.columnCount(), ((ImmutableMatrix)a).getInternalData()), b);
        }
        return Multiplications.blockedMultiply(a.toMatrix(), b);
    }

    public static Matrix multiply(Matrix a, AMatrix b) {
        return Multiplications.blockedMultiply(a, b);
    }

    public static Matrix blockedMultiply(Matrix a, AMatrix b) {
        int rc = a.rowCount();
        int cc = b.columnCount();
        int ic = a.columnCount();
        if (ic != b.rowCount()) {
            throw new IllegalArgumentException(ErrorMessages.incompatibleShapes(a, b));
        }
        Matrix result = Matrix.create(rc, cc);
        if (ic == 0) {
            return result;
        }
        int block = 8192 / ic + 1;
        Matrix wsb = Matrix.create(Math.min(block, cc), ic);
        for (int bj = 0; bj < cc; bj += block) {
            int bjsize = Math.min(block, cc - bj);
            for (int t = 0; t < bjsize; ++t) {
                b.copyColumnTo(bj + t, wsb.data, t * ic);
            }
            for (int bi = 0; bi < rc; bi += block) {
                int bisize = Math.min(block, rc - bi);
                for (int i = bi; i < bi + bisize; ++i) {
                    int aDataOffset = i * ic;
                    for (int j = bj; j < bj + bjsize; ++j) {
                        double val = DoubleArrays.dotProduct(a.data, aDataOffset, wsb.data, ic * (j - bj), ic);
                        result.unsafeSet(i, j, val);
                    }
                }
            }
        }
        return result;
    }

    public static Matrix doubleBlockedMultiply(AMatrix a, AMatrix b) {
        int rc = a.rowCount();
        int cc = b.columnCount();
        int ic = a.columnCount();
        if (ic != b.rowCount()) {
            throw new IllegalArgumentException(ErrorMessages.incompatibleShapes(a, b));
        }
        Matrix result = Matrix.create(rc, cc);
        if (ic == 0) {
            return result;
        }
        int block = 8192 / ic + 1;
        Matrix wsa = Matrix.create(Math.min(block, rc), ic);
        Matrix wsb = Matrix.create(Math.min(block, cc), ic);
        for (int bj = 0; bj < cc; bj += block) {
            int bjsize = Math.min(block, cc - bj);
            for (int t = 0; t < bjsize; ++t) {
                b.copyColumnTo(bj + t, wsb.data, t * ic);
            }
            for (int bi = 0; bi < rc; bi += block) {
                int bisize = Math.min(block, rc - bi);
                for (int t = 0; t < bisize; ++t) {
                    b.copyRowTo(bi + t, wsa.data, t * ic);
                }
                for (int i = bi; i < bi + bisize; ++i) {
                    for (int j = bj; j < bj + bjsize; ++j) {
                        double val = DoubleArrays.dotProduct(wsa.data, ic * (i - bi), wsb.data, ic * (j - bj), ic);
                        result.unsafeSet(i, j, val);
                    }
                }
            }
        }
        return result;
    }

    public static Matrix directMultiply(Matrix a, AMatrix b) {
        int rc = a.rowCount();
        int cc = b.columnCount();
        int ic = a.columnCount();
        if (ic != b.rowCount()) {
            throw new IllegalArgumentException(ErrorMessages.incompatibleShapes(a, b));
        }
        Matrix result = Matrix.create(rc, cc);
        double[] tmp = new double[ic];
        for (int j = 0; j < cc; ++j) {
            b.copyColumnTo(j, tmp, 0);
            for (int i = 0; i < rc; ++i) {
                double acc = DoubleArrays.dotProduct(a.data, i * ic, tmp, 0, ic);
                result.unsafeSet(i, j, acc);
            }
        }
        return result;
    }

    public static AMatrix naiveMultiply(AMatrix a, AMatrix b) {
        int rc = a.rowCount();
        int cc = b.columnCount();
        int ic = a.columnCount();
        if (ic != b.rowCount()) {
            throw new IllegalArgumentException(ErrorMessages.incompatibleShapes(a, b));
        }
        Matrix result = Matrix.create(rc, cc);
        for (int i = 0; i < rc; ++i) {
            for (int j = 0; j < cc; ++j) {
                double acc = 0.0;
                for (int k = 0; k < ic; ++k) {
                    acc += a.unsafeGet(i, k) * b.unsafeGet(k, j);
                }
                result.unsafeSet(i, j, acc);
            }
        }
        return result;
    }
}

