/*
 * Decompiled with CFR 0_129.
 */
package mikera.arrayz;

import java.io.Serializable;
import java.nio.DoubleBuffer;
import java.util.Iterator;
import java.util.List;
import mikera.arrayz.Array;
import mikera.indexz.AIndex;
import mikera.indexz.Index;
import mikera.matrixx.AMatrix;
import mikera.vectorz.AScalar;
import mikera.vectorz.AVector;
import mikera.vectorz.IOperator;
import mikera.vectorz.Op;
import mikera.vectorz.Vector;

public interface INDArray
extends Cloneable,
Serializable {
    public int dimensionality();

    public int[] getShape();

    public int[] getShapeClone();

    public int getShape(int var1);

    public long[] getLongShape();

    public double get(AIndex var1);

    public double get(Index var1);

    public double get();

    public double get(int var1);

    public double get(long var1);

    public double get(int var1, int var2);

    public double get(long var1, long var3);

    public /* varargs */ double get(int ... var1);

    public double get(long[] var1);

    public void set(double var1);

    public void set(int var1, double var2);

    public void set(int var1, int var2, double var3);

    public void set(int[] var1, double var2);

    public void set(INDArray var1);

    public void set(Object var1);

    public void add(double var1);

    public INDArray addCopy(INDArray var1);

    public void addToArray(double[] var1, int var2);

    public void sub(double var1);

    public INDArray subCopy(INDArray var1);

    public void add(INDArray var1);

    public void addAt(int var1, double var2);

    public void sub(INDArray var1);

    public void fill(double var1);

    public void negate();

    public INDArray negateCopy();

    public void reciprocal();

    public INDArray reciprocalCopy();

    public void clamp(double var1, double var3);

    public INDArray innerProduct(INDArray var1);

    public INDArray innerProduct(double var1);

    public INDArray innerProduct(AScalar var1);

    public INDArray outerProduct(INDArray var1);

    public AVector asVector();

    public List<Double> asElementList();

    public /* varargs */ INDArray reshape(int ... var1);

    public INDArray reorder(int var1, int[] var2);

    public INDArray reorder(int[] var1);

    public INDArray rotateView(int var1, int var2);

    public /* varargs */ INDArray broadcast(int ... var1);

    public INDArray broadcastLike(INDArray var1);

    public AMatrix broadcastLike(AMatrix var1);

    public AVector broadcastLike(AVector var1);

    public INDArray broadcastCloneLike(INDArray var1);

    public INDArray broadcastCopyLike(INDArray var1);

    public INDArray slice(int var1);

    public Object sliceValue(int var1);

    public INDArray join(INDArray var1, int var2);

    public INDArray join(INDArray var1);

    public INDArray slice(int var1, int var2);

    public INDArray subArray(int[] var1, int[] var2);

    public INDArray getTranspose();

    public INDArray getTransposeView();

    public INDArray getTransposeCopy();

    public int sliceCount();

    public long elementCount();

    public double elementSum();

    public double elementMax();

    public double elementMin();

    public double elementSquaredSum();

    public Iterator<Double> elementIterator();

    public void multiply(INDArray var1);

    public INDArray multiplyCopy(INDArray var1);

    public void divide(INDArray var1);

    public INDArray divideCopy(INDArray var1);

    public void divide(double var1);

    public long nonZeroCount();

    public boolean isMutable();

    public boolean isBoolean();

    public boolean isSparse();

    public boolean isDense();

    public boolean isFullyMutable();

    public boolean isElementConstrained();

    public boolean isSameShape(INDArray var1);

    public boolean isView();

    public boolean isZero();

    public INDArray clone();

    public INDArray copy();

    @Deprecated
    public INDArray ensureMutable();

    public void applyOp(Op var1);

    public INDArray applyOpCopy(Op var1);

    public void applyOp(IOperator var1);

    public boolean equals(INDArray var1);

    public boolean elementsEqual(double var1);

    public INDArray exactClone();

    public /* varargs */ void setElements(double ... var1);

    public void setElements(double[] var1, int var2);

    public void setElements(int var1, double[] var2, int var3, int var4);

    public void getElements(double[] var1, int var2);

    public void getElements(Object[] var1, int var2);

    public void scale(double var1);

    public INDArray scaleCopy(double var1);

    public void scaleAdd(double var1, double var3);

    public void multiply(double var1);

    public void pow(double var1);

    public void square();

    public INDArray squareCopy();

    public void sqrt();

    public void signum();

    public List<?> getSlices();

    public List<?> getSlices(int var1);

    public void validate();

    public void copyTo(double[] var1);

    public void toDoubleBuffer(DoubleBuffer var1);

    public double[] toDoubleArray();

    public INDArray[] toSliceArray();

    public double[] asDoubleArray();

    public List<INDArray> getSliceViews();

    public Vector toVector();

    public Array toArray();

    public boolean epsilonEquals(INDArray var1);

    public boolean epsilonEquals(INDArray var1, double var2);

    public void abs();

    public void log();

    public void exp();

    public INDArray immutable();

    public INDArray mutable();

    public INDArray mutableClone();

    public INDArray sparse();

    public INDArray dense();

    public INDArray sparseClone();

    public INDArray denseClone();

    public boolean equalsArray(double[] var1);

    public boolean equalsArray(double[] var1, int var2);

    public INDArray innerProduct(AVector var1);

    public INDArray absCopy();

    public INDArray signumCopy();

    public double[] getElements();

    public double elementProduct();

    public INDArray multiplyCopy(double var1);

    public boolean hasUncountable();

    public double elementPowSum(double var1);

    public double elementAbsPowSum(double var1);

    public int componentCount();

    public INDArray getComponent(int var1);

    public INDArray withComponents(INDArray[] var1);

    public INDArray[] getComponents();
}

