/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.solve;

import mikera.matrixx.AMatrix;
import mikera.matrixx.decompose.impl.lu.LUPResult;
import mikera.matrixx.impl.ADenseArrayMatrix;
import mikera.matrixx.impl.ColumnMatrix;
import mikera.matrixx.solve.impl.lu.LUSolver;
import mikera.matrixx.solve.impl.qr.QRHouseColSolver;
import mikera.vectorz.AVector;

public class Linear {
    public static AVector solveLeastSquares(AMatrix A, AVector b) {
        QRHouseColSolver solver = new QRHouseColSolver();
        solver.setA(A);
        ColumnMatrix B = ColumnMatrix.wrap(b);
        AMatrix X = solver.solve(B);
        return X.asVector();
    }

    public static AMatrix solveLeastSquares(AMatrix A, AMatrix B) {
        QRHouseColSolver solver = new QRHouseColSolver();
        solver.setA(A);
        AMatrix x = solver.solve(B);
        return x;
    }

    public static AVector solve(AMatrix A, AVector b) {
        if (A.isSquare()) {
            return Linear.solveSquare(A, b);
        }
        return Linear.solveLeastSquares(A, b);
    }

    public static AMatrix solve(AMatrix A, AMatrix B) {
        if (A.isSquare()) {
            return Linear.solveSquare(A, B);
        }
        return Linear.solveLeastSquares(A, B);
    }

    private static AVector solveSquare(AMatrix A, AVector b) {
        A.checkSquare();
        LUSolver solver = new LUSolver();
        solver.setA(A);
        ColumnMatrix B = ColumnMatrix.wrap(b);
        ADenseArrayMatrix X = solver.solve(B);
        if (X == null) {
            return null;
        }
        return X.asVector();
    }

    private static AMatrix solveSquare(AMatrix A, AMatrix B) {
        A.checkSquare();
        LUSolver solver = new LUSolver();
        solver.setA(A);
        return solver.solve(B);
    }
}

