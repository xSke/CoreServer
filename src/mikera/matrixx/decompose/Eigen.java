/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.decompose;

import mikera.matrixx.AMatrix;
import mikera.matrixx.decompose.IEigenResult;
import mikera.matrixx.decompose.impl.eigen.EigenResult;
import mikera.matrixx.decompose.impl.eigen.SymmetricQRAlgorithmDecomposition;

public class Eigen {
    public static IEigenResult decompose(AMatrix A, boolean computeVectors) {
        throw new UnsupportedOperationException("This has not yet been implemented");
    }

    public static IEigenResult decompose(AMatrix A) {
        return Eigen.decompose(A, true);
    }

    public static IEigenResult decomposeSymmetric(AMatrix A, boolean computeVectors) {
        SymmetricQRAlgorithmDecomposition alg = new SymmetricQRAlgorithmDecomposition(computeVectors);
        return alg.decompose(A);
    }

    public static IEigenResult decomposeSymmetric(AMatrix A) {
        return Eigen.decomposeSymmetric(A, true);
    }
}

