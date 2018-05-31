/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.decompose.impl.lu;

import mikera.matrixx.AMatrix;
import mikera.matrixx.Matrix;
import mikera.matrixx.Matrixx;
import mikera.vectorz.util.ErrorMessages;

public class DecomposeLUP {
    public static Matrix createLUPInverse(AMatrix m) {
        if (!m.isSquare()) {
            throw new IllegalArgumentException("Matrix must be square for inverse!");
        }
        int dims = m.rowCount();
        Matrix am = new Matrix(m);
        int[] rowPermutations = new int[dims];
        DecomposeLUP.decomposeLU(am, rowPermutations);
        return DecomposeLUP.backSubstituteLU(am, rowPermutations);
    }

    private static void decomposeLU(Matrix am, int[] permutations) {
        int dims = permutations.length;
        double[] data = am.data;
        double[] rowFactors = new double[dims];
        DecomposeLUP.calcRowFactors(data, rowFactors);
        for (int col = 0; col < dims; ++col) {
            for (int row = 0; row < col; ++row) {
                int dataIndex = dims * row + col;
                double acc = data[dataIndex];
                for (int i = 0; i < row; ++i) {
                    acc -= data[dims * row + i] * data[dims * i + col];
                }
                data[dataIndex] = acc;
            }
            int maxIndex = 0;
            double maxValue = Double.NEGATIVE_INFINITY;
            for (int row = col; row < dims; ++row) {
                int dataIndex = dims * row + col;
                double acc = data[dataIndex];
                for (int i = 0; i < col; ++i) {
                    acc -= data[dims * row + i] * data[dims * i + col];
                }
                data[dataIndex] = acc;
                double value = rowFactors[row] * Math.abs(acc);
                if (value <= maxValue) continue;
                maxValue = value;
                maxIndex = row;
            }
            if (col != maxIndex) {
                am.swapRows(col, maxIndex);
                rowFactors[maxIndex] = rowFactors[col];
            }
            permutations[col] = maxIndex;
            if (data[dims * col + col] == 0.0) {
                throw new IllegalArgumentException(ErrorMessages.singularMatrix());
            }
            double diagonalValue = data[dims * col + col];
            double factor = 1.0 / diagonalValue;
            int offset = dims * (col + 1) + col;
            for (int i = 0; i < dims - 1 - col; ++i) {
                double[] arrd = data;
                int n = dims * i + offset;
                arrd[n] = arrd[n] * factor;
            }
        }
    }

    private static void calcRowFactors(double[] data, double[] factorsOut) {
        int dims = factorsOut.length;
        for (int row = 0; row < dims; ++row) {
            double maxValue = 0.0;
            for (int col = 0; col < dims; ++col) {
                maxValue = Math.max(maxValue, Math.abs(data[row * dims + col]));
            }
            if (maxValue == 0.0) {
                throw new IllegalArgumentException(ErrorMessages.singularMatrix());
            }
            factorsOut[row] = 1.0 / maxValue;
        }
    }

    private static Matrix backSubstituteLU(Matrix am, int[] permutations) {
        int dims = permutations.length;
        double[] dataIn = am.data;
        Matrix result = new Matrix(Matrixx.createImmutableIdentityMatrix(dims));
        double[] dataOut = result.data;
        for (int col = 0; col < dims; ++col) {
            int row;
            int rowIndex = -1;
            for (row = 0; row < dims; ++row) {
                int pRow = permutations[row];
                double acc = dataOut[dims * pRow + col];
                dataOut[dims * pRow + col] = dataOut[dims * row + col];
                if (rowIndex >= 0) {
                    for (int i = rowIndex; i <= row - 1; ++i) {
                        acc -= dataIn[row * dims + i] * dataOut[dims * i + col];
                    }
                } else if (acc != 0.0) {
                    rowIndex = row;
                }
                dataOut[dims * row + col] = acc;
            }
            for (row = 0; row < dims; ++row) {
                int irow = dims - 1 - row;
                int offset = dims * irow;
                double total = 0.0;
                for (int i = 0; i < row; ++i) {
                    total += dataIn[offset + (dims - 1 - i)] * dataOut[dims * (dims - 1 - i) + col];
                }
                double diagonalValue = dataIn[offset + irow];
                dataOut[dims * irow + col] = (dataOut[dims * irow + col] - total) / diagonalValue;
            }
        }
        return result;
    }
}

