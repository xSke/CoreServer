/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.decompose;

import mikera.matrixx.AMatrix;
import mikera.matrixx.decompose.IQRResult;
import mikera.matrixx.decompose.impl.qr.HouseholderQR;
import mikera.matrixx.decompose.impl.qr.QRResult;

public class QR {
    public static IQRResult decompose(AMatrix matrix) {
        HouseholderQR alg = new HouseholderQR(false);
        return alg.decompose(matrix);
    }

    public static IQRResult decompose(AMatrix matrix, boolean compact) {
        HouseholderQR alg = new HouseholderQR(compact);
        return alg.decompose(matrix);
    }

    public static IQRResult decomposeCompact(AMatrix matrix) {
        HouseholderQR alg = new HouseholderQR(true);
        return alg.decompose(matrix);
    }
}

