/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.decompose.impl.chol;

import mikera.matrixx.AMatrix;
import mikera.matrixx.Matrix;
import mikera.matrixx.Matrixx;
import mikera.matrixx.decompose.ICholeskyResult;
import mikera.matrixx.decompose.impl.chol.CholeskyResult;
import mikera.util.Maths;

public class SimpleCholesky {
    public static final ICholeskyResult decompose(AMatrix a) {
        return SimpleCholesky.decompose(a.toMatrix());
    }

    public static final ICholeskyResult decompose(Matrix a) {
        if (!a.isSquare()) {
            throw new IllegalArgumentException("Matrix must be square for Cholesky decomposition");
        }
        int n = a.rowCount();
        Matrix u = Matrix.create(n, n);
        for (int i = 0; i < n; ++i) {
            double squareSum = 0.0;
            for (int j = 0; j < i; ++j) {
                double crossSum = 0.0;
                for (int k = 0; k < j; ++k) {
                    crossSum += u.get(i, k) * u.get(j, k);
                }
                double aij = a.get(i, j);
                double uij = aij - crossSum;
                double ujj = u.get(j, j);
                if (ujj == 0.0) {
                    return null;
                }
                u.set(i, j, uij /= ujj);
                squareSum += uij * uij;
            }
            double aii = a.get(i, i);
            double uii = Maths.sqrt(aii - squareSum);
            u.set(i, i, uii);
        }
        AMatrix L = Matrixx.extractLowerTriangular(u);
        return new CholeskyResult(L);
    }
}

