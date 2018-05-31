/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.decompose.impl.lu;

import mikera.matrixx.AMatrix;
import mikera.matrixx.Matrix;
import mikera.matrixx.decompose.ILUPResult;
import mikera.matrixx.decompose.impl.lu.LUPResult;
import mikera.matrixx.impl.PermutationMatrix;
import mikera.vectorz.AVector;
import mikera.vectorz.Vector;

public class SimpleLUP {
    public static ILUPResult decompose(AMatrix matrix) {
        return SimpleLUP.decomposeLUPInternal(Matrix.create(matrix));
    }

    public static ILUPResult decomposeLUP(Matrix matrix) {
        return SimpleLUP.decomposeLUPInternal(matrix.clone());
    }

    private static ILUPResult decomposeLUPInternal(Matrix m) {
        int i;
        if (!m.isSquare()) {
            throw new IllegalArgumentException("Wrong matrix size: not square");
        }
        int n = m.rowCount();
        PermutationMatrix p = PermutationMatrix.createIdentity(n);
        for (int j = 0; j < n; ++j) {
            int i2;
            Vector jcolumn = m.getColumn(j).toVector();
            for (i = 0; i < n; ++i) {
                int kmax = Math.min(i, j);
                double s = 0.0;
                for (int k = 0; k < kmax; ++k) {
                    s += m.get(i, k) * jcolumn.unsafeGet(k);
                }
                jcolumn.set(i, jcolumn.unsafeGet(i) - s);
                m.set(i, j, jcolumn.unsafeGet(i));
            }
            int biggest = j;
            for (i2 = j + 1; i2 < n; ++i2) {
                if (Math.abs(jcolumn.unsafeGet(i2)) <= Math.abs(jcolumn.unsafeGet(biggest))) continue;
                biggest = i2;
            }
            if (biggest != j) {
                m.swapRows(biggest, j);
                p.swapRows(biggest, j);
            }
            if (j >= n || m.get(j, j) == 0.0) continue;
            for (i2 = j + 1; i2 < n; ++i2) {
                m.set(i2, j, m.get(i2, j) / m.get(j, j));
            }
        }
        Matrix l = Matrix.create(n, n);
        for (int i3 = 0; i3 < n; ++i3) {
            for (int j = 0; j < i3; ++j) {
                l.unsafeSet(i3, j, m.get(i3, j));
            }
            l.unsafeSet(i3, i3, 1.0);
        }
        Matrix u = m;
        for (i = 0; i < n; ++i) {
            for (int j = 0; j < i; ++j) {
                u.unsafeSet(i, j, 0.0);
            }
        }
        return new LUPResult(l, u, p);
    }
}

