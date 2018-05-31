/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx.impl;

import java.util.Arrays;
import mikera.arrayz.INDArray;
import mikera.arrayz.ISparse;
import mikera.indexz.Index;
import mikera.indexz.Indexz;
import mikera.matrixx.AMatrix;
import mikera.matrixx.Matrix;
import mikera.matrixx.impl.ABooleanMatrix;
import mikera.matrixx.impl.IFastColumns;
import mikera.matrixx.impl.IFastRows;
import mikera.vectorz.AVector;
import mikera.vectorz.Vector;
import mikera.vectorz.impl.AxisVector;
import mikera.vectorz.util.ErrorMessages;
import mikera.vectorz.util.VectorzException;

public final class PermutationMatrix
extends ABooleanMatrix
implements IFastRows,
IFastColumns,
ISparse {
    private static final long serialVersionUID = 8098287603508120428L;
    private final Index perm;
    private final int size;

    private PermutationMatrix(Index perm) {
        if (!perm.isPermutation()) {
            throw new IllegalArgumentException("Not a valid permutation: " + perm);
        }
        this.perm = perm;
        this.size = perm.length();
    }

    public static PermutationMatrix createIdentity(int length) {
        return new PermutationMatrix(Indexz.createSequence(length));
    }

    public static PermutationMatrix createSwap(int i, int j, int length) {
        PermutationMatrix p = PermutationMatrix.createIdentity(length);
        p.swapRows(i, j);
        return p;
    }

    public static PermutationMatrix create(Index rowPermutations) {
        return new PermutationMatrix(rowPermutations.clone());
    }

    public static PermutationMatrix wrap(Index rowPermutations) {
        return new PermutationMatrix(rowPermutations);
    }

    public static /* varargs */ PermutationMatrix create(int ... rowPermutations) {
        Index index = Index.of(rowPermutations);
        return PermutationMatrix.wrap(index);
    }

    public static PermutationMatrix wrap(int[] rowPermutations) {
        return PermutationMatrix.wrap(Index.wrap(rowPermutations));
    }

    public static PermutationMatrix createRandomPermutation(int length) {
        Index index = Indexz.createRandomPermutation(length);
        return new PermutationMatrix(index);
    }

    @Override
    public void addToArray(double[] data, int offset) {
        for (int i = 0; i < this.size; ++i) {
            double[] arrd = data;
            int n = offset + i * this.size + this.perm.get(i);
            arrd[n] = arrd[n] + 1.0;
        }
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public boolean isSymmetric() {
        return this.isIdentity();
    }

    @Override
    public double determinant() {
        return this.perm.isEvenPermutation() ? 1.0 : -1.0;
    }

    @Override
    public int rank() {
        return this.size;
    }

    @Override
    public boolean isIdentity() {
        int[] data = this.perm.data;
        for (int i = 0; i < this.size; ++i) {
            if (data[i] == i) continue;
            return false;
        }
        return true;
    }

    @Override
    public boolean isOrthogonal() {
        return true;
    }

    @Override
    public boolean isOrthogonal(double tolerance) {
        return true;
    }

    @Override
    public boolean hasOrthonormalColumns() {
        return true;
    }

    @Override
    public boolean hasOrthonormalRows() {
        return true;
    }

    @Override
    public boolean isDiagonal() {
        return this.isIdentity();
    }

    @Override
    public boolean isBoolean() {
        return true;
    }

    @Override
    public boolean isUpperTriangular() {
        return this.isIdentity();
    }

    @Override
    public boolean isLowerTriangular() {
        return this.isIdentity();
    }

    @Override
    public boolean isSquare() {
        return true;
    }

    @Override
    public int rowCount() {
        return this.size;
    }

    @Override
    public int columnCount() {
        return this.size;
    }

    @Override
    public double elementSum() {
        return this.size;
    }

    @Override
    public double elementSquaredSum() {
        return this.size;
    }

    @Override
    public long nonZeroCount() {
        return this.size;
    }

    @Override
    public double trace() {
        int result = 0;
        for (int i = 0; i < this.size; ++i) {
            if (this.perm.data[i] != i) continue;
            ++result;
        }
        return result;
    }

    @Override
    public PermutationMatrix inverse() {
        return this.getTranspose();
    }

    @Override
    public PermutationMatrix getTranspose() {
        return new PermutationMatrix(this.perm.invert());
    }

    @Override
    public double get(int row, int column) {
        if (column < 0 || column >= this.size) {
            throw new IndexOutOfBoundsException(ErrorMessages.invalidIndex((INDArray)this, row, column));
        }
        return this.perm.get(row) == column ? 1.0 : 0.0;
    }

    @Override
    public void unsafeSet(int row, int column, double value) {
        if (this.get(row, column) == value) {
            return;
        }
        throw new UnsupportedOperationException(ErrorMessages.notFullyMutable(this, row, column));
    }

    @Override
    public double unsafeGet(int row, int column) {
        return this.perm.unsafeGet(row) == column ? 1.0 : 0.0;
    }

    @Override
    public void set(int row, int column, double value) {
        throw new UnsupportedOperationException(ErrorMessages.notFullyMutable(this, row, column));
    }

    @Override
    public AxisVector getRow(int i) {
        return AxisVector.create(this.perm.get(i), this.size);
    }

    @Override
    public AxisVector getColumn(int j) {
        return AxisVector.create(this.perm.find(j), this.size);
    }

    @Override
    public void copyRowTo(int row, double[] dest, int destOffset) {
        Arrays.fill(dest, destOffset, destOffset + this.size, 0.0);
        dest[destOffset + this.perm.get((int)row)] = 1.0;
    }

    @Override
    public void copyColumnTo(int col, double[] dest, int destOffset) {
        Arrays.fill(dest, destOffset, destOffset + this.size, 0.0);
        dest[destOffset + this.perm.find((int)col)] = 1.0;
    }

    @Override
    public void swapRows(int i, int j) {
        if (i != j) {
            this.perm.swap(i, j);
        }
    }

    @Override
    public void swapColumns(int i, int j) {
        if (i != j) {
            int a = this.perm.find(i);
            int b = this.perm.find(j);
            this.perm.swap(a, b);
        }
    }

    @Override
    public void transform(AVector source, AVector dest) {
        if (source instanceof Vector && dest instanceof Vector) {
            this.transform((Vector)source, (Vector)dest);
            return;
        }
        if (this.rowCount() != dest.length()) {
            throw new IllegalArgumentException(ErrorMessages.wrongDestLength(dest));
        }
        if (this.columnCount() != source.length()) {
            throw new IllegalArgumentException(ErrorMessages.wrongSourceLength(dest));
        }
        for (int i = 0; i < this.size; ++i) {
            dest.unsafeSet(i, source.unsafeGet(this.perm.unsafeGet(i)));
        }
    }

    @Override
    public void transform(Vector source, Vector dest) {
        int rc = this.rowCount();
        int cc = this.columnCount();
        if (source.length() != cc) {
            throw new IllegalArgumentException(ErrorMessages.wrongSourceLength(source));
        }
        if (dest.length() != rc) {
            throw new IllegalArgumentException(ErrorMessages.wrongDestLength(dest));
        }
        for (int i = 0; i < this.size; ++i) {
            dest.unsafeSet(i, source.unsafeGet(this.perm.unsafeGet(i)));
        }
    }

    @Override
    public double calculateElement(int i, AVector inputVector) {
        return inputVector.unsafeGet(this.perm.get(i));
    }

    @Override
    public double calculateElement(int i, Vector inputVector) {
        return inputVector.unsafeGet(this.perm.get(i));
    }

    @Override
    public Matrix innerProduct(AMatrix a) {
        if (a instanceof Matrix) {
            return this.innerProduct((Matrix)a);
        }
        if (a.rowCount() != this.size) {
            throw new IllegalArgumentException(ErrorMessages.mismatch(this, a));
        }
        int cc = a.columnCount();
        Matrix result = Matrix.create(this.size, cc);
        for (int i = 0; i < this.size; ++i) {
            int dstIndex = i * cc;
            int srcRow = this.perm.get(i);
            a.copyRowTo(srcRow, result.data, dstIndex);
        }
        return result;
    }

    @Override
    public Matrix innerProduct(Matrix a) {
        if (a.rowCount() != this.size) {
            throw new IllegalArgumentException(ErrorMessages.mismatch(this, a));
        }
        int cc = a.columnCount();
        Matrix result = Matrix.create(this.size, cc);
        for (int i = 0; i < this.size; ++i) {
            int srcIndex = this.perm.get(i) * cc;
            int dstIndex = i * cc;
            System.arraycopy(a.data, srcIndex, result.data, dstIndex, cc);
        }
        return result;
    }

    @Override
    public Matrix transposeInnerProduct(Matrix s) {
        return this.getTranspose().innerProduct(s);
    }

    @Override
    public void validate() {
        super.validate();
        if (this.size != this.perm.length()) {
            throw new VectorzException("Whoops!");
        }
    }

    @Override
    public double density() {
        return 1.0 / (double)this.size;
    }

    @Override
    public PermutationMatrix exactClone() {
        return new PermutationMatrix(this.perm.clone());
    }

    @Override
    public boolean hasUncountable() {
        return false;
    }

    @Override
    public double elementPowSum(double p) {
        return this.size;
    }

    @Override
    public double elementAbsPowSum(double p) {
        return this.elementPowSum(p);
    }

    @Override
    public boolean isZero() {
        return false;
    }
}

