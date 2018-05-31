/*
 * Decompiled with CFR 0_129.
 */
package mikera.vectorz;

import java.nio.DoubleBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import mikera.arrayz.INDArray;
import mikera.util.Rand;
import mikera.vectorz.AVector;
import mikera.vectorz.GrowableVector;
import mikera.vectorz.IVector;
import mikera.vectorz.Scalar;
import mikera.vectorz.Tools;
import mikera.vectorz.Vector;
import mikera.vectorz.Vector1;
import mikera.vectorz.Vector2;
import mikera.vectorz.Vector3;
import mikera.vectorz.Vector4;
import mikera.vectorz.impl.ADenseArrayVector;
import mikera.vectorz.impl.AStridedVector;
import mikera.vectorz.impl.ArraySubVector;
import mikera.vectorz.impl.AxisVector;
import mikera.vectorz.impl.RangeVector;
import mikera.vectorz.impl.RepeatedElementVector;
import mikera.vectorz.impl.SingleElementVector;
import mikera.vectorz.impl.SparseIndexedVector;
import mikera.vectorz.impl.StridedVector;
import mikera.vectorz.impl.Vector0;
import mikera.vectorz.impl.ZeroVector;
import mikera.vectorz.util.VectorzException;
import us.bpsm.edn.parser.CollectionBuilder;
import us.bpsm.edn.parser.Parseable;
import us.bpsm.edn.parser.Parser;
import us.bpsm.edn.parser.Parsers;

public class Vectorz {
    public static final double TEST_EPSILON = 1.0E-7;
    public static final int MIN_SPARSE_LENGTH = 50;
    public static final int BIG_SPARSE_LENGTH = 1000000;
    private static final double SPARSE_DENSITY_THRESHOLD = 0.2;

    public static /* varargs */ AVector create(double ... data) {
        int n = data.length;
        switch (n) {
            case 0: {
                return Vector0.INSTANCE;
            }
            case 1: {
                return Vector1.of(data);
            }
            case 2: {
                return Vector2.of(data);
            }
            case 3: {
                return Vector3.of(data);
            }
            case 4: {
                return Vector4.of(data);
            }
        }
        return Vector.of(data);
    }

    public static AVector join(AVector first, AVector second) {
        AVector result = first.join(second);
        assert (result.length() == first.length() + second.length());
        return result;
    }

    public static /* varargs */ AVector join(AVector ... vectors) {
        AVector result = vectors[0];
        for (int i = 1; i < vectors.length; ++i) {
            result = result.join(vectors[i]);
        }
        return result;
    }

    public static AVector join(List<AVector> vectors) {
        int count = vectors.size();
        AVector v = Vector0.INSTANCE;
        for (int i = 0; i < count; ++i) {
            v = v.join(vectors.get(i));
        }
        return v;
    }

    public static AVector createZeroVector(long l) {
        if (l == 0L) {
            return Vector0.INSTANCE;
        }
        if (l >= Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Requested zero vector length too large: " + l);
        }
        return ZeroVector.create((int)l);
    }

    public static /* varargs */ Vector wrap(double ... data) {
        return Vector.wrap(data);
    }

    public static AVector wrap(double[][] data) {
        if (data.length == 0) {
            return Vector0.INSTANCE;
        }
        AVector v = Vector0.INSTANCE;
        for (int i = 0; i < data.length; ++i) {
            v = Vectorz.join(v, (AVector)Vectorz.wrap(data[i]));
        }
        return v;
    }

    public static ADenseArrayVector wrap(double[] data, int offset, int length) {
        if (offset == 0 && length == data.length) {
            return Vectorz.wrap(data);
        }
        return ArraySubVector.wrap(data, offset, length);
    }

    public static AStridedVector wrapStrided(double[] data, int offset, int length, int stride) {
        if (stride == 1) {
            if (offset == 0 && length == data.length) {
                return Vector.wrap(data);
            }
            return ArraySubVector.wrap(data, offset, length);
        }
        return StridedVector.wrapStrided(data, offset, length, stride);
    }

    public static AVector newVector(int length) {
        switch (length) {
            case 0: {
                return Vector0.INSTANCE;
            }
            case 1: {
                return new Vector1();
            }
            case 2: {
                return new Vector2();
            }
            case 3: {
                return new Vector3();
            }
            case 4: {
                return new Vector4();
            }
        }
        if (length >= 1000000) {
            return Vectorz.createSparseMutable(length);
        }
        return Vector.createLength(length);
    }

    public static AVector createSparse(AVector v) {
        if (v.isSparse()) {
            return v.copy();
        }
        int len = v.length();
        if (len < 50) {
            return v.copy();
        }
        int[] ixs = v.nonZeroIndices();
        int n = ixs.length;
        if (n == 0) {
            return Vectorz.createZeroVector(len);
        }
        if (n == 1) {
            int i = ixs[0];
            double val = v.unsafeGet(i);
            if (val != 0.0) {
                if (val == 1.0) {
                    return AxisVector.create(i, len);
                }
                return SingleElementVector.create(val, i, len);
            }
            throw new VectorzException("non-zero element not found!!");
        }
        if ((double)n > (double)len * 0.2) {
            return Vector.create(v);
        }
        return SparseIndexedVector.createWithIndices(v, ixs);
    }

    public static AVector createSparseMutable(int length) {
        if (length < 50) {
            return Vector.createLength(length);
        }
        return SparseIndexedVector.createLength(length);
    }

    public static AVector createSparseMutable(AVector v) {
        int len = v.length();
        long n = v.nonZeroCount();
        if (len < 50 || (double)n > (double)len * 0.2) {
            return Vector.create(v);
        }
        return SparseIndexedVector.create(v);
    }

    public static AVector createSameSize(AVector v) {
        return Vectorz.newVector(v.length());
    }

    public static AVector create(AVector vector) {
        return vector.clone();
    }

    public static AVector create(IVector vector) {
        return (AVector)vector.clone();
    }

    public static Scalar createScalar(double value) {
        return new Scalar(value);
    }

    static void copy(AVector source, int srcOffset, AVector dest, int destOffset, int length) {
        source.copyTo(srcOffset, dest, destOffset, length);
    }

    public static AVector createUniformRandomVector(int dimensions) {
        AVector v = Vectorz.newVector(dimensions);
        for (int i = 0; i < dimensions; ++i) {
            v.unsafeSet(i, Rand.nextDouble());
        }
        return v;
    }

    public static AVector createMutableVector(AVector v) {
        return v.clone();
    }

    public static AVector immutableZeroVector(int dimensions) {
        return ZeroVector.create(dimensions);
    }

    private static Parser.Config getVectorParserConfig() {
        return ParserConfigHolder.parserConfig;
    }

    public static AVector parse(String ednString) {
        Parser p = Parsers.newParser(Vectorz.getVectorParserConfig());
        return (AVector)p.nextValue(Parsers.newParseable(ednString));
    }

    public static AVector create(List<Object> d) {
        int length = d.size();
        AVector v = Vectorz.newVector(length);
        for (int i = 0; i < length; ++i) {
            v.set(i, Tools.toDouble(d.get(i)));
        }
        return v;
    }

    public static AVector create(DoubleBuffer d) {
        int length = d.remaining();
        Vector v = Vector.createLength(length);
        double[] data = v.getArray();
        d.get(data, 0, length);
        return v;
    }

    public static AVector create(Iterable<Object> d) {
        ArrayList<Object> al = new ArrayList<Object>();
        for (Object o : d) {
            al.add(o);
        }
        return Vectorz.create(al);
    }

    @Deprecated
    public static double minValue(AVector v) {
        return v.elementMin();
    }

    @Deprecated
    public static int indexOfMinValue(AVector v) {
        int len = v.length();
        double min = v.get(0);
        int ind = 0;
        for (int i = 1; i < len; ++i) {
            double d = v.unsafeGet(i);
            if (d >= min) continue;
            min = d;
            ind = i;
        }
        return ind;
    }

    @Deprecated
    public static double maxValue(AVector v) {
        return v.elementMax();
    }

    @Deprecated
    public static int indexOfMaxValue(AVector v) {
        int len = v.length();
        double max = v.unsafeGet(0);
        int ind = 0;
        for (int i = 1; i < len; ++i) {
            double d = v.get(i);
            if (d <= max) continue;
            max = d;
            ind = i;
        }
        return ind;
    }

    public static void invSqrt(AVector v) {
        if (v instanceof Vector) {
            Vectorz.invSqrt((Vector)v);
            return;
        }
        int len = v.length();
        for (int i = 0; i < len; ++i) {
            double d = 1.0 / Math.sqrt(v.unsafeGet(i));
            v.set(i, d);
        }
    }

    public static void invSqrt(Vector v) {
        int len = v.length();
        double[] data = v.getArray();
        for (int i = 0; i < len; ++i) {
            double d;
            data[i] = d = 1.0 / Math.sqrt(data[i]);
        }
    }

    @Deprecated
    public static double totalValue(AVector v) {
        return v.elementSum();
    }

    @Deprecated
    public static double averageValue(AVector v) {
        int len = v.length();
        double result = v.elementSum();
        return result / (double)len;
    }

    @Deprecated
    public static double averageSquaredDifference(AVector a, AVector b) {
        int len = a.length();
        if (len != b.length()) {
            throw new IllegalArgumentException("Vector size mismatch");
        }
        double result = 0.0;
        for (int i = 0; i < len; ++i) {
            double d = a.unsafeGet(i) - b.unsafeGet(i);
            result += d * d;
        }
        return result / (double)len;
    }

    public static double rmsDifference(AVector a, AVector b) {
        return Math.sqrt(Vectorz.averageSquaredDifference(a, b));
    }

    public static void fillRandom(AVector v) {
        int len = v.length();
        for (int i = 0; i < len; ++i) {
            v.unsafeSet(i, Rand.nextDouble());
        }
    }

    public static void fillIndexes(AVector v) {
        int n = v.length();
        for (int i = 0; i < n; ++i) {
            v.unsafeSet(i, i);
        }
    }

    public static void fillGaussian(AVector v) {
        Vectorz.fillGaussian(v, 0.0, 1.0);
    }

    public static void fillGaussian(AVector v, Random r) {
        Vectorz.fillGaussian(v, 0.0, 1.0);
    }

    public static void fillGaussian(AVector v, double mean, double sd) {
        int len = v.length();
        for (int i = 0; i < len; ++i) {
            v.unsafeSet(i, mean + Rand.nextGaussian() * sd);
        }
    }

    public static void fillGaussian(AVector v, double mean, double sd, Random r) {
        int len = v.length();
        for (int i = 0; i < len; ++i) {
            v.unsafeSet(i, mean + r.nextGaussian() * sd);
        }
    }

    public static void fillBinaryRandom(AVector v) {
        Vectorz.fillBinaryRandom(v, 0.5);
    }

    public static AVector axisVector(int axisIndex, int dimensions) {
        return AxisVector.create(axisIndex, dimensions);
    }

    public static void fillBinaryRandom(AVector v, double prob) {
        int len = v.length();
        for (int i = 0; i < len; ++i) {
            v.unsafeSet(i, Rand.binary(prob));
        }
    }

    public static AVector toVector(Object o) {
        if (o instanceof AVector) {
            return (AVector)o;
        }
        if (o instanceof double[]) {
            return Vectorz.create((double[])o);
        }
        if (o instanceof INDArray) {
            INDArray a = (INDArray)o;
            if (a.dimensionality() != 1) {
                throw new IllegalArgumentException("Cannot coerce INDArray with shape " + a.getShape().toString() + " to a vector");
            }
            return a.asVector();
        }
        if (o instanceof List) {
            return Vectorz.create((List)o);
        }
        if (o instanceof Iterable) {
            return Vectorz.create((Iterable)o);
        }
        throw new UnsupportedOperationException("Cannot coerce to AVector: " + o.getClass());
    }

    public static AVector create(Object o) {
        if (o instanceof Double) {
            return Vector1.of(Tools.toDouble(o));
        }
        return Vectorz.toVector(o);
    }

    public static AVector createMutableRange(int length) {
        AVector v = Vectorz.newVector(length);
        for (int i = 0; i < length; ++i) {
            v.unsafeSet(i, i);
        }
        return v;
    }

    public static AVector createRange(int length) {
        return RangeVector.create(0, length);
    }

    public static AVector createRepeatedElement(int length, double value) {
        if (length == 0) {
            return Vector0.INSTANCE;
        }
        if (value == 0.0) {
            return ZeroVector.create(length);
        }
        return RepeatedElementVector.create(length, value);
    }

    public static int safeLongToInt(long value) {
        int result = (int)value;
        if ((long)result != value) {
            throw new IllegalArgumentException("Can't cast safely to int: " + value);
        }
        return result;
    }

    public static void fillRandom(AVector v, long seed) {
        Vectorz.fillRandom(v, new Random(seed));
    }

    public static void fillRandom(AVector v, Random random) {
        int n = v.length();
        for (int i = 0; i < n; ++i) {
            v.unsafeSet(i, random.nextDouble());
        }
    }

    public static void fillNormal(AVector v, long seed) {
        Vectorz.fillNormal(v, new Random(seed));
    }

    public static void fillNormal(AVector v, Random random) {
        int n = v.length();
        for (int i = 0; i < n; ++i) {
            v.unsafeSet(i, random.nextGaussian());
        }
    }

    public static boolean isUncountable(double value) {
        return Double.isNaN(value) || Double.isInfinite(value);
    }

    private static class ParserConfigHolder {
        static final Parser.Config parserConfig;

        private ParserConfigHolder() {
        }

        static {
            Parser.Config.Builder b = Parsers.newParserConfigBuilder();
            b.setVectorFactory(new CollectionBuilder.Factory(){

                @Override
                public CollectionBuilder builder() {
                    return new CollectionBuilder(){
                        GrowableVector b = new GrowableVector();

                        @Override
                        public void add(Object o) {
                            double d;
                            if (o instanceof Double) {
                                d = (Double)o;
                            } else if (o instanceof Number) {
                                d = ((Number)o).doubleValue();
                            } else {
                                throw new VectorzException("Cannot parse double value from class: " + o.getClass());
                            }
                            this.b.append(d);
                        }

                        @Override
                        public Object build() {
                            return this.b.toVector();
                        }
                    };
                }

            });
            parserConfig = b.build();
        }

    }

}

