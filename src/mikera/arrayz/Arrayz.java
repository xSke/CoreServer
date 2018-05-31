/*
 * Decompiled with CFR 0_129.
 */
package mikera.arrayz;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import mikera.arrayz.Array;
import mikera.arrayz.INDArray;
import mikera.arrayz.NDArray;
import mikera.arrayz.impl.SliceArray;
import mikera.arrayz.impl.ZeroArray;
import mikera.matrixx.Matrix;
import mikera.matrixx.Matrixx;
import mikera.matrixx.impl.StridedMatrix;
import mikera.matrixx.impl.ZeroMatrix;
import mikera.vectorz.AScalar;
import mikera.vectorz.AVector;
import mikera.vectorz.Scalar;
import mikera.vectorz.Vector;
import mikera.vectorz.Vectorz;
import mikera.vectorz.impl.ArrayIndexScalar;
import mikera.vectorz.impl.ArraySubVector;
import mikera.vectorz.impl.ImmutableScalar;
import mikera.vectorz.impl.SparseIndexedVector;
import mikera.vectorz.impl.Vector0;
import mikera.vectorz.impl.ZeroVector;
import mikera.vectorz.util.ErrorMessages;
import mikera.vectorz.util.IntArrays;
import mikera.vectorz.util.VectorzException;
import us.bpsm.edn.parser.Parseable;
import us.bpsm.edn.parser.Parser;
import us.bpsm.edn.parser.Parsers;

public class Arrayz {
    public static INDArray create(Object object) {
        if (object instanceof INDArray) {
            return Arrayz.create((INDArray)object);
        }
        if (object instanceof double[]) {
            return Vector.of((double[])object);
        }
        if (object instanceof List) {
            List list = (List)object;
            int n = list.size();
            if (n == 0) {
                return Vector0.INSTANCE;
            }
            Object o1 = list.get(0);
            if (o1 instanceof AScalar || o1 instanceof Number) {
                return Vectorz.create((List)object);
            }
            if (o1 instanceof AVector) {
                return Matrixx.create((List)object);
            }
            if (o1 instanceof INDArray) {
                return SliceArray.create((List)object);
            }
            ArrayList<INDArray> al = new ArrayList<INDArray>(n);
            for (Object o : list) {
                al.add(Arrayz.create(o));
            }
            return Arrayz.create(al);
        }
        if (object instanceof Number) {
            return Scalar.create(((Number)object).doubleValue());
        }
        if (object.getClass().isArray()) {
            return Arrayz.create(Arrays.asList((Object[])object));
        }
        throw new VectorzException("Don't know how to create array from: " + object.getClass());
    }

    public static /* varargs */ INDArray newArray(int ... shape) {
        int dims = shape.length;
        switch (dims) {
            case 0: {
                return Scalar.create(0.0);
            }
            case 1: {
                return Vector.createLength(shape[0]);
            }
            case 2: {
                return Matrix.create(shape[0], shape[1]);
            }
        }
        return Array.newArray(shape);
    }

    public static INDArray create(INDArray a) {
        int dims = a.dimensionality();
        switch (dims) {
            case 0: {
                return Scalar.create(a.get());
            }
            case 1: {
                return Vector.wrap(a.toDoubleArray());
            }
            case 2: {
                return Matrix.wrap(a.getShape(0), a.getShape(1), a.toDoubleArray());
            }
        }
        return Array.wrap(a.toDoubleArray(), a.getShape());
    }

    public static /* varargs */ INDArray create(Object ... data) {
        return Arrayz.create((Object)data);
    }

    public static INDArray wrap(double[] data, int[] shape) {
        int dlength = data.length;
        switch (shape.length) {
            case 0: {
                return ArrayIndexScalar.wrap(data, 0);
            }
            case 1: {
                int n = shape[0];
                if (dlength < n) {
                    throw new IllegalArgumentException(ErrorMessages.insufficientElements(dlength));
                }
                if (n == dlength) {
                    return Vector.wrap(data);
                }
                return ArraySubVector.wrap(data, 0, n);
            }
            case 2: {
                int rc = shape[0];
                int cc = shape[1];
                int ec = rc * cc;
                if (dlength < ec) {
                    throw new IllegalArgumentException(ErrorMessages.insufficientElements(dlength));
                }
                if (ec == dlength) {
                    return Matrix.wrap(rc, cc, data);
                }
                return StridedMatrix.wrap(data, shape[0], shape[1], 0, shape[1], 1);
            }
        }
        long eec = IntArrays.arrayProduct(shape);
        if ((long)dlength < eec) {
            throw new IllegalArgumentException(ErrorMessages.insufficientElements(dlength));
        }
        if (eec == (long)dlength) {
            return Array.wrap(data, shape);
        }
        return NDArray.wrap(data, shape);
    }

    public static /* varargs */ INDArray createFromVector(AVector a, int ... shape) {
        int dims = shape.length;
        if (dims == 0) {
            return Scalar.createFromVector(a);
        }
        if (dims == 1) {
            return Vector.createFromVector(a, shape[0]);
        }
        if (dims == 2) {
            return Matrixx.createFromVector(a, shape[0], shape[1]);
        }
        return Array.createFromVector(a, shape);
    }

    public static INDArray load(Reader reader) {
        Parseable pbr = Parsers.newParseable(reader);
        Parser p = Parsers.newParser(Parsers.defaultConfiguration());
        return Arrayz.create(p.nextValue(pbr));
    }

    public static INDArray parse(String ednString) {
        return Arrayz.load(new StringReader(ednString));
    }

    public static INDArray wrapStrided(double[] data, int offset, int[] shape, int[] strides) {
        int dims = shape.length;
        if (dims == 0) {
            return ArrayIndexScalar.wrap(data, offset);
        }
        if (dims == 1) {
            return Vectorz.wrapStrided(data, offset, shape[0], strides[0]);
        }
        if (dims == 2) {
            return Matrixx.wrapStrided(data, shape[0], shape[1], offset, strides[0], strides[1]);
        }
        if (Arrayz.isPackedLayout(data, offset, shape, strides)) {
            return Array.wrap(data, shape);
        }
        return NDArray.wrapStrided(data, offset, shape, strides);
    }

    public static boolean isPackedLayout(double[] data, int offset, int[] shape, int[] strides) {
        if (offset != 0) {
            return false;
        }
        int dims = shape.length;
        int st = 1;
        for (int i = dims - 1; i >= 0; --i) {
            if (strides[i] != st) {
                return false;
            }
            st *= shape[i];
        }
        return st == data.length;
    }

    public static boolean isPackedStrides(int[] shape, int[] strides) {
        int dims = shape.length;
        int st = 1;
        for (int i = dims - 1; i >= 0; --i) {
            if (strides[i] != st) {
                return false;
            }
            st *= shape[i];
        }
        return true;
    }

    public static INDArray createSparse(INDArray a) {
        int dims = a.dimensionality();
        if (dims == 0) {
            return Scalar.create(a.get());
        }
        if (dims == 1) {
            return Vectorz.createSparse(a.asVector());
        }
        if (dims == 2) {
            return Matrixx.createSparse(Matrixx.toMatrix(a));
        }
        int n = a.sliceCount();
        List<INDArray> slices = a.getSliceViews();
        for (int i = 0; i < n; ++i) {
            slices.set(i, slices.get(i).sparseClone());
        }
        return SliceArray.create(slices);
    }

    public static /* varargs */ INDArray createZeroArray(int ... shape) {
        switch (shape.length) {
            case 0: {
                return ImmutableScalar.ZERO;
            }
            case 1: {
                return ZeroVector.create(shape[0]);
            }
            case 2: {
                return ZeroMatrix.create(shape[0], shape[1]);
            }
        }
        return ZeroArray.create(shape);
    }

    public static /* varargs */ INDArray createSparseArray(int ... shape) {
        switch (shape.length) {
            case 0: {
                return Scalar.create(0.0);
            }
            case 1: {
                return SparseIndexedVector.createLength(shape[0]);
            }
            case 2: {
                return Matrixx.createSparse(shape[0], shape[1]);
            }
        }
        int sliceCount = shape[0];
        int[] subshape = IntArrays.tailArray(shape);
        ArrayList<INDArray> slices = new ArrayList<INDArray>(sliceCount);
        INDArray sa = Arrayz.createSparseArray(subshape);
        slices.add(sa);
        for (int i = 1; i < sliceCount; ++i) {
            slices.add(sa.sparseClone());
        }
        SliceArray<INDArray> m = SliceArray.create(slices);
        return m;
    }

    public static void fillRandom(INDArray a, long seed) {
        Vectorz.fillRandom(a.asVector(), seed);
    }

    public static void fillRandom(INDArray a, Random random) {
        Vectorz.fillRandom(a.asVector(), random);
    }

    public static void fillNormal(INDArray a, long seed) {
        Vectorz.fillNormal(a.asVector(), seed);
    }

    public static void fillNormal(INDArray a, Random random) {
        Vectorz.fillNormal(a.asVector(), random);
    }
}

