/*
 * Decompiled with CFR 0_129.
 */
package mikera.matrixx;

import java.util.List;
import mikera.arrayz.INDArray;
import mikera.matrixx.AMatrix;
import mikera.vectorz.AVector;

public interface IMatrix
extends INDArray {
    public int rowCount();

    public int columnCount();

    @Override
    public double get(int var1, int var2);

    @Override
    public void set(int var1, int var2, double var3);

    public AVector getRow(int var1);

    public AVector getColumn(int var1);

    public AVector getBand(int var1);

    public boolean isSquare();

    public boolean isInvertible();

    public AVector transform(AVector var1);

    public void transform(AVector var1, AVector var2);

    public void transformInPlace(AVector var1);

    public AMatrix inverse();

    public AMatrix addCopy(AMatrix var1);

    public double diagonalProduct();

    public List<AVector> getRows();

    public List<AVector> getColumns();

    public boolean isSymmetric();

    public void add2(AMatrix var1, AMatrix var2);
}

