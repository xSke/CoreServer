/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.decompose.impl.qr;

import mikera.matrixx.Matrix;

public class QRHelperFunctions {
    public static double findMax(double[] u, int startU, int length) {
        double max = -1.0;
        int stopIndex = startU + length;
        for (int index = startU; index < stopIndex; ++index) {
            double val = u[index];
            double d = val = val < 0.0 ? - val : val;
            if (val <= max) continue;
            max = val;
        }
        return max;
    }

    public static void divideElements(int j, int numRows, double[] u, double u_0) {
        int i = j;
        while (i < numRows) {
            double[] arrd = u;
            int n = i++;
            arrd[n] = arrd[n] / u_0;
        }
    }

    public static void divideElements(int j, int numRows, double[] u, int startU, double u_0) {
        for (int i = j; i < numRows; ++i) {
            double[] arrd = u;
            int n = i + startU;
            arrd[n] = arrd[n] / u_0;
        }
    }

    public static void divideElements_Brow(int j, int numRows, double[] u, double[] b, int startB, double u_0) {
        for (int i = j; i < numRows; ++i) {
            double[] arrd = b;
            int n = i + startB;
            double d = arrd[n] / u_0;
            arrd[n] = d;
            u[i] = d;
        }
    }

    public static void divideElements_Bcol(int j, int numRows, int numCols, double[] u, double[] b, int startB, double u_0) {
        int indexB = j * numCols + startB;
        int i = j;
        while (i < numRows) {
            double[] arrd = u;
            int n = i++;
            double d = arrd[n] / u_0;
            arrd[n] = d;
            b[indexB] = d;
            indexB += numCols;
        }
    }

    public static double computeTauAndDivide(int j, int numRows, double[] u, int startU, double max) {
        double tau = 0.0;
        for (int i = j; i < numRows; ++i) {
            double[] arrd = u;
            int n = startU + i;
            double d = arrd[n] / max;
            arrd[n] = d;
            double d2 = d;
            tau += d2 * d2;
        }
        tau = Math.sqrt(tau);
        if (u[startU + j] < 0.0) {
            tau = - tau;
        }
        return tau;
    }

    public static double computeTauAndDivide(int j, int numRows, double[] u, double max) {
        double tau = 0.0;
        int i = j;
        while (i < numRows) {
            double[] arrd = u;
            int n = i++;
            double d = arrd[n] / max;
            arrd[n] = d;
            double d2 = d;
            tau += d2 * d2;
        }
        tau = Math.sqrt(tau);
        if (u[j] < 0.0) {
            tau = - tau;
        }
        return tau;
    }

    public static void rank1UpdateMultR(Matrix A, double[] u, double gamma, int colA0, int w0, int w1, double[] _temp) {
        int i;
        for (i = colA0; i < A.columnCount(); ++i) {
            _temp[i] = u[w0] * A.data[w0 * A.columnCount() + i];
        }
        for (int k = w0 + 1; k < w1; ++k) {
            int indexA = k * A.columnCount() + colA0;
            double valU = u[k];
            int i2 = colA0;
            while (i2 < A.columnCount()) {
                double[] arrd = _temp;
                int n = i2++;
                arrd[n] = arrd[n] + valU * A.data[indexA++];
            }
        }
        i = colA0;
        while (i < A.columnCount()) {
            double[] arrd = _temp;
            int n = i++;
            arrd[n] = arrd[n] * gamma;
        }
        for (i = w0; i < w1; ++i) {
            double valU = u[i];
            int indexA = i * A.columnCount() + colA0;
            for (int j = colA0; j < A.columnCount(); ++j) {
                double[] arrd = A.data;
                int n = indexA++;
                arrd[n] = arrd[n] - valU * _temp[j];
            }
        }
    }

    public static void rank1UpdateMultR(Matrix A, double[] u, int offsetU, double gamma, int colA0, int w0, int w1, double[] _temp) {
        int i;
        for (i = colA0; i < A.columnCount(); ++i) {
            _temp[i] = u[w0 + offsetU] * A.data[w0 * A.columnCount() + i];
        }
        for (int k = w0 + 1; k < w1; ++k) {
            int indexA = k * A.columnCount() + colA0;
            double valU = u[k + offsetU];
            int i2 = colA0;
            while (i2 < A.columnCount()) {
                double[] arrd = _temp;
                int n = i2++;
                arrd[n] = arrd[n] + valU * A.data[indexA++];
            }
        }
        i = colA0;
        while (i < A.columnCount()) {
            double[] arrd = _temp;
            int n = i++;
            arrd[n] = arrd[n] * gamma;
        }
        for (i = w0; i < w1; ++i) {
            double valU = u[i + offsetU];
            int indexA = i * A.columnCount() + colA0;
            for (int j = colA0; j < A.columnCount(); ++j) {
                double[] arrd = A.data;
                int n = indexA++;
                arrd[n] = arrd[n] - valU * _temp[j];
            }
        }
    }

    public static void rank1UpdateMultL(Matrix A, double[] u, double gamma, int colA0, int w0, int w1) {
        for (int i = colA0; i < A.rowCount(); ++i) {
            int j;
            int startIndex = i * A.columnCount() + w0;
            double sum = 0.0;
            int rowIndex = startIndex;
            for (j = w0; j < w1; ++j) {
                sum += A.data[rowIndex++] * u[j];
            }
            sum = (- gamma) * sum;
            rowIndex = startIndex;
            for (j = w0; j < w1; ++j) {
                double[] arrd = A.data;
                int n = rowIndex++;
                arrd[n] = arrd[n] + sum * u[j];
            }
        }
    }
}

