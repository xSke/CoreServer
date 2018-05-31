/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.algo;

import mikera.matrixx.AMatrix;
import mikera.matrixx.decompose.Cholesky;
import mikera.matrixx.decompose.Eigen;
import mikera.matrixx.decompose.IEigenResult;
import mikera.vectorz.Vector2;

public class Definite {
    public static boolean isPositiveDefinite(AMatrix a) {
        return Cholesky.decompose(a) != null;
    }

    public static boolean isPositiveSemiDefinite(AMatrix a) {
        Vector2[] eigenValues;
        IEigenResult e = Eigen.decomposeSymmetric(a);
        if (e == null) {
            return false;
        }
        for (Vector2 v : eigenValues = e.getEigenvalues()) {
            if (v.x >= 0.0) continue;
            return false;
        }
        return true;
    }
}

