/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.algo;

import mikera.matrixx.AMatrix;
import mikera.matrixx.Matrix;
import mikera.matrixx.Matrix33;
import mikera.matrixx.decompose.ILUPResult;
import mikera.matrixx.decompose.impl.lu.SimpleLUP;
import mikera.matrixx.impl.PermutationMatrix;
import mikera.vectorz.util.IntArrays;

public class Determinant {
    public static double calculate(AMatrix m) {
        int rc = m.checkSquare();
        if (rc <= 4) {
            if (rc <= 3) {
                return Determinant.calculateSmallDeterminant(m, rc);
            }
            return Determinant.naiveDeterminant(m.toMatrix(), rc);
        }
        return Determinant.calculateLUPDeterminant(m);
    }

    static double calculateLUPDeterminant(AMatrix m) {
        ILUPResult lup = SimpleLUP.decompose(m);
        double det = lup.getL().diagonalProduct() * lup.getU().diagonalProduct() * lup.getP().determinant();
        return det;
    }

    static double calculateSmallDeterminant(AMatrix m, int rc) {
        if (rc == 1) {
            return m.unsafeGet(0, 0);
        }
        if (rc == 2) {
            return m.unsafeGet(0, 0) * m.unsafeGet(1, 1) - m.unsafeGet(1, 0) * m.unsafeGet(0, 1);
        }
        if (rc == 3) {
            return new Matrix33(m).determinant();
        }
        throw new UnsupportedOperationException("Small determinant calculation on size " + rc + " not possible");
    }

    static double naiveDeterminant(Matrix m) {
        return Determinant.naiveDeterminant(m, m.rowCount());
    }

    static double naiveDeterminant(AMatrix m, int rc) {
        int[] inds = new int[rc];
        int i = 0;
        while (i < rc) {
            inds[i] = i++;
        }
        return Determinant.calcDeterminant(m.toMatrix(), inds, 0);
    }

    private static double calcDeterminant(Matrix m, int[] inds, int offset) {
        int rc = m.rowCount();
        if (offset == rc - 1) {
            return m.unsafeGet(offset, inds[offset]);
        }
        double v0 = m.unsafeGet(offset, inds[offset]);
        double det = v0 == 0.0 ? 0.0 : v0 * Determinant.calcDeterminant(m, inds, offset + 1);
        for (int i = 1; i < rc - offset; ++i) {
            IntArrays.swap(inds, offset, offset + i);
            double v = m.unsafeGet(offset, inds[offset]);
            if (v != 0.0) {
                det -= v * Determinant.calcDeterminant(m, inds, offset + 1);
            }
            IntArrays.swap(inds, offset, offset + i);
        }
        return det;
    }
}

