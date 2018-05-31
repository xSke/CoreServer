/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.algo;

import java.util.Iterator;
import mikera.matrixx.AMatrix;
import mikera.matrixx.decompose.ISVDResult;
import mikera.matrixx.decompose.SVD;
import mikera.vectorz.AVector;

public class Rank {
    private static double DEFAULT_THRESHOLD = 2.220446E-15;

    public static int compute(AMatrix A) {
        return Rank.compute(A, DEFAULT_THRESHOLD);
    }

    public static int compute(AMatrix A, double threshold) {
        ISVDResult ans = SVD.decompose(A, true);
        int rank = 0;
        AVector singularValues = ans.getSingularValues();
        int n = singularValues.length();
        for (int i = 0; i < n; ++i) {
            if (singularValues.unsafeGet(i) < threshold) continue;
            ++rank;
        }
        return rank;
    }

    public static int compute(ISVDResult result) {
        return Rank.compute(result, DEFAULT_THRESHOLD);
    }

    public static int compute(ISVDResult result, double threshold) {
        int rank = 0;
        AVector singularValues = result.getSingularValues();
        Iterator<Double> iterator = singularValues.iterator();
        while (iterator.hasNext()) {
            double s = iterator.next();
            if (s <= threshold) continue;
            ++rank;
        }
        return rank;
    }
}

