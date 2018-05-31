/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz;

import java.nio.DoubleBuffer;
import java.util.Iterator;
import java.util.List;
import mikera.arrayz.INDArray;
import mikera.arrayz.impl.AbstractArray;
import mikera.arrayz.impl.IDense;
import mikera.matrixx.AMatrix;
import mikera.randomz.Hash;
import mikera.vectorz.AVector;
import mikera.vectorz.IOperator;
import mikera.vectorz.IScalar;
import mikera.vectorz.Op;
import mikera.vectorz.Scalar;
import mikera.vectorz.Vectorz;
import mikera.vectorz.impl.ImmutableScalar;
import mikera.vectorz.impl.RepeatedElementVector;
import mikera.vectorz.impl.SingleDoubleIterator;
import mikera.vectorz.impl.Vector0;
import mikera.vectorz.impl.WrappedScalarVector;
import mikera.vectorz.util.ErrorMessages;
import mikera.vectorz.util.IntArrays;
import mikera.vectorz.util.LongArrays;
import mikera.vectorz.util.VectorzException;

public abstract class AScalar
extends AbstractArray<Object>
implements IScalar,
IDense {
    private static final long serialVersionUID = -8285351135755012093L;
    private static final int[] SCALAR_SHAPE = IntArrays.EMPTY_INT_ARRAY;
    private static final long[] SCALAR_LONG_SHAPE = LongArrays.EMPTY_LONG_ARRAY;

    @Override
    public abstract double get();

    @Override
    public void set(double value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public double get(int x) {
        throw new IllegalArgumentException(ErrorMessages.invalidIndex((INDArray)this, (long)x));
    }

    @Override
    public double get(int x, int y) {
        throw new IllegalArgumentException(ErrorMessages.invalidIndex((INDArray)this, (long)x));
    }

    @Override
    public void setElements(int pos, double[] values, int offset, int length) {
        if (length == 0) {
            return;
        }
        if (length != 1) {
            throw new IllegalArgumentException("Length must be 0 or 1");
        }
        if (pos != 0) {
            throw new IllegalArgumentException("Element position must be zero for any scalar");
        }
        this.set(values[offset]);
    }

    @Override
    public void getElements(double[] dest, int offset) {
        dest[offset] = this.get();
    }

    @Override
    public AScalar getTranspose() {
        return this;
    }

    @Override
    public final AScalar getTransposeView() {
        return this;
    }

    @Override
    public int dimensionality() {
        return 0;
    }

    @Override
    public INDArray slice(int position) {
        throw new UnsupportedOperationException("Can't slice a scalar!");
    }

    @Override
    public INDArray slice(int dimension, int index) {
        throw new UnsupportedOperationException("Can't slice a scalar!");
    }

    @Override
    public int sliceCount() {
        return 0;
    }

    @Override
    public List<Object> getSlices() {
        throw new UnsupportedOperationException("Can't slice a scalar!");
    }

    @Override
    public AScalar subArray(int[] offsets, int[] shape) {
        if (offsets.length != 0) {
            throw new IllegalArgumentException(ErrorMessages.invalidIndex((INDArray)this, offsets));
        }
        if (shape.length != 0) {
            throw new IllegalArgumentException(ErrorMessages.invalidIndex((INDArray)this, offsets));
        }
        return this;
    }

    @Override
    public Iterator<Object> iterator() {
        throw new UnsupportedOperationException("Can't slice a scalar!");
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public boolean isFullyMutable() {
        return this.isMutable();
    }

    @Override
    public boolean isElementConstrained() {
        return false;
    }

    @Override
    public boolean isZero() {
        return this.get() == 0.0;
    }

    @Override
    public void add(double d) {
        this.set(this.get() + d);
    }

    @Override
    public void addAt(int i, double v) {
        this.add(v);
    }

    @Override
    public void addToArray(double[] data, int offset) {
        double[] arrd = data;
        int n = offset;
        arrd[n] = arrd[n] + this.get();
    }

    @Override
    public void sub(double d) {
        this.set(this.get() - d);
    }

    public void add(AScalar s) {
        this.set(this.get() + s.get());
    }

    @Override
    public void add(INDArray a) {
        if (a instanceof AScalar || a.dimensionality() == 0) {
            this.add(a.get());
        } else {
            super.add(a);
        }
    }

    @Override
    public void sub(INDArray a) {
        if (a instanceof AScalar || a.dimensionality() == 0) {
            this.sub(a.get());
        } else {
            super.sub(a);
        }
    }

    public void sub(AScalar s) {
        this.set(this.get() - s.get());
    }

    @Override
    public void negate() {
        this.set(- this.get());
    }

    @Override
    public void square() {
        double v = this.get();
        this.set(v * v);
    }

    @Override
    public void pow(double exponent) {
        double v = this.get();
        this.set(Math.pow(v, exponent));
    }

    @Override
    public void clamp(double min, double max) {
        double v = this.get();
        if (v < min) {
            this.set(min);
        } else if (v > max) {
            this.set(max);
        }
    }

    @Override
    public INDArray innerProduct(INDArray a) {
        return a.scaleCopy(this.get());
    }

    @Override
    public Scalar innerProduct(AScalar a) {
        return Scalar.create(this.get() * a.get());
    }

    @Override
    public Scalar innerProduct(double a) {
        return Scalar.create(this.get() * a);
    }

    @Override
    public AVector innerProduct(AVector a) {
        return a.multiplyCopy(this.get());
    }

    @Override
    public INDArray outerProduct(INDArray a) {
        return a.scaleCopy(this.get());
    }

    @Override
    public /* varargs */ double get(int ... indexes) {
        assert (indexes.length == 0);
        return this.get();
    }

    @Override
    public void set(int[] indexes, double value) {
        if (indexes.length != 0) {
            throw new VectorzException("" + indexes.length + "D set not supported on AScalar");
        }
        this.set(value);
    }

    @Override
    public int[] getShape() {
        return SCALAR_SHAPE;
    }

    @Override
    public int getShape(int dim) {
        throw new IndexOutOfBoundsException("Scalar does not have dimension: " + dim);
    }

    @Override
    public long[] getLongShape() {
        return SCALAR_LONG_SHAPE;
    }

    @Override
    public final long elementCount() {
        return 1L;
    }

    @Override
    public long nonZeroCount() {
        return this.get() == 0.0 ? 0L : 1L;
    }

    @Override
    public void copyTo(double[] arr) {
        arr[0] = this.get();
    }

    @Override
    public AVector asVector() {
        return new WrappedScalarVector(this);
    }

    @Override
    public /* varargs */ INDArray reshape(int ... dimensions) {
        return this.asVector().reshape(dimensions);
    }

    @Override
    public void applyOp(IOperator op) {
        this.set(op.apply(this.get()));
    }

    @Override
    public void applyOp(Op op) {
        this.set(op.apply(this.get()));
    }

    @Override
    public Scalar clone() {
        return Scalar.create(this.get());
    }

    @Override
    public void multiply(double factor) {
        this.set(factor * this.get());
    }

    @Override
    public void divide(double factor) {
        this.set(this.get() / factor);
    }

    @Override
    public void multiply(INDArray a) {
        this.multiply(a.get());
    }

    @Override
    public final double elementSum() {
        return this.get();
    }

    @Override
    public final double elementProduct() {
        return this.get();
    }

    @Override
    public double elementMax() {
        return this.get();
    }

    @Override
    public double elementMin() {
        return this.get();
    }

    @Override
    public final double elementSquaredSum() {
        double value = this.get();
        return value * value;
    }

    @Override
    public /* varargs */ INDArray broadcast(int ... targetShape) {
        int tdims = targetShape.length;
        if (tdims == 0) {
            return this;
        }
        int n = targetShape[tdims - 1];
        if (n == 0) {
            return Vector0.INSTANCE;
        }
        RepeatedElementVector v = RepeatedElementVector.create(n, this.get());
        return v.broadcast(targetShape);
    }

    @Override
    public INDArray broadcastLike(INDArray v) {
        int dims = v.dimensionality();
        if (dims == 0) {
            return this;
        }
        int lastShape = v.getShape(dims - 1);
        AVector rv = Vectorz.createRepeatedElement(lastShape, this.get());
        return rv.broadcastLike(v);
    }

    @Override
    public AVector broadcastLike(AVector v) {
        return Vectorz.createRepeatedElement(v.length(), this.get());
    }

    @Override
    public AMatrix broadcastLike(AMatrix v) {
        return Vectorz.createRepeatedElement(v.columnCount(), this.get()).broadcastLike(v);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof AScalar) {
            return this.equals((AScalar)o);
        }
        if (o instanceof INDArray) {
            return this.equals((INDArray)o);
        }
        return false;
    }

    @Override
    public boolean epsilonEquals(INDArray a) {
        return this.epsilonEquals(a, 1.0E-7);
    }

    @Override
    public boolean epsilonEquals(INDArray a, double epsilon) {
        if (a.dimensionality() != 0) {
            return false;
        }
        double d = this.get() - a.get();
        return Math.abs(d) <= epsilon;
    }

    @Override
    public boolean equals(INDArray o) {
        return o.dimensionality() == 0 && o.get() == this.get();
    }

    @Override
    public boolean equalsArray(double[] data, int offset) {
        return data[offset] == this.get();
    }

    public boolean equals(AScalar o) {
        return this.get() == o.get();
    }

    @Override
    public int hashCode() {
        return 31 + Hash.hashCode(this.get());
    }

    @Override
    public String toString() {
        return Double.toString(this.get());
    }

    @Override
    public Iterator<Double> elementIterator() {
        return new SingleDoubleIterator(this.get());
    }

    @Override
    public void toDoubleBuffer(DoubleBuffer dest) {
        dest.put(this.get());
    }

    @Override
    public double[] asDoubleArray() {
        return null;
    }

    @Override
    public double[] toDoubleArray() {
        return new double[]{this.get()};
    }

    @Override
    public INDArray[] toSliceArray() {
        throw new UnsupportedOperationException(ErrorMessages.noSlices(this));
    }

    @Override
    public abstract AScalar exactClone();

    @Override
    public AScalar mutable() {
        if (this.isFullyMutable()) {
            return this;
        }
        return Scalar.create(this.get());
    }

    @Override
    public boolean elementsEqual(double value) {
        return this.get() == value;
    }

    @Override
    public AScalar sparse() {
        double v = this.get();
        if (v == 0.0) {
            return ImmutableScalar.ZERO;
        }
        if (v == 1.0) {
            return ImmutableScalar.ONE;
        }
        return this;
    }

    @Override
    public INDArray dense() {
        return this;
    }

    @Override
    public final Scalar denseClone() {
        return Scalar.create(this.get());
    }

    @Override
    public AScalar sparseClone() {
        return Scalar.create(this.get());
    }

    @Override
    public AScalar immutable() {
        return ImmutableScalar.create(this.get());
    }

    @Override
    public void validate() {
        this.get();
        super.validate();
    }

    @Override
    protected final void checkDimension(int dimension) {
        throw new IndexOutOfBoundsException(ErrorMessages.invalidDimension(this, dimension));
    }

    @Override
    public void abs() {
        this.set(Math.abs(this.get()));
    }

    @Override
    public boolean hasUncountable() {
        return Vectorz.isUncountable(this.get());
    }
}

