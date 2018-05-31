/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.algo;

import mikera.matrixx.AMatrix;
import mikera.matrixx.Matrix;
import mikera.matrixx.Matrix11;
import mikera.matrixx.Matrix22;
import mikera.matrixx.Matrix33;
import mikera.matrixx.decompose.impl.lu.LUPResult;
import mikera.matrixx.solve.impl.lu.LUSolver;
import mikera.vectorz.util.ErrorMessages;

public final class Inverse {
    public static AMatrix calculate(AMatrix a) {
        int rc = a.checkSquare();
        if (rc <= 3) {
            return Inverse.calculateSmall(a, rc);
        }
        return Inverse.createLUPInverse(a);
    }

    public static AMatrix calculateSymmetric(AMatrix a) {
        return Inverse.calculate(a);
    }

    private static AMatrix calculateSmall(AMatrix m, int rc) {
        if (rc == 1) {
            return new Matrix11(m).inverse();
        }
        if (rc == 2) {
            return new Matrix22(m).inverse();
        }
        if (rc == 3) {
            return new Matrix33(m).inverse();
        }
        throw new IllegalArgumentException(ErrorMessages.incompatibleShape(m));
    }

    static Matrix createLUPInverse(AMatrix m) {
        LUSolver lus = new LUSolver();
        lus.setA(m);
        return lus.invert().toMatrix();
    }
}

