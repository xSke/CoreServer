/*
 * Decompiled with CFR 0_129.
 */
package mikera.indexz;

import java.util.ArrayList;
import java.util.List;
import mikera.indexz.AIndex;
import mikera.indexz.Index;
import mikera.util.Rand;

public class Indexz {
    public static Index create(List<Integer> data) {
        int length = data.size();
        Index result = new Index(length);
        for (int i = 0; i < length; ++i) {
            result.set(i, data.get(i));
        }
        return result;
    }

    public static Index create(AIndex source) {
        Index result = new Index(source.length());
        source.copyTo(result.getData(), 0);
        return result;
    }

    public static Index create(int[] source) {
        return Index.of(source);
    }

    public static Index createProgression(int start, int length, int skip) {
        Index result = new Index(length);
        int pos = start;
        for (int i = 0; i < length; ++i) {
            result.set(i, pos);
            pos += skip;
        }
        return result;
    }

    public static Index createSequence(int start, int length) {
        Index result = new Index(length);
        for (int i = 0; i < length; ++i) {
            result.set(i, start + i);
        }
        return result;
    }

    public static Index createSequence(int length) {
        return Indexz.createSequence(0, length);
    }

    public static Index createRandomPermutation(int length) {
        Index result = Indexz.createSequence(length);
        for (int j = length - 1; j > 0; --j) {
            result.swap(Rand.r(j + 1), j);
        }
        return result;
    }

    public static Index createRandomChoice(int count, int max) {
        Index result = Indexz.createSequence(count);
        Rand.chooseIntegers(result.data, 0, count, max);
        assert (result.isDistinct() && result.isSorted());
        return result;
    }

    public static Index createRandomChoice(int count, Index source) {
        Index result = Indexz.createSequence(count);
        Rand.chooseIntegers(result.data, 0, count, source.length());
        assert (result.isDistinct() && result.isSorted());
        result.lookupWith(source);
        return result;
    }

    public static Index createRandomSubset(AIndex index, double probability) {
        int len = index.length();
        int elen = (int)((double)len * probability + 3.0 * Math.sqrt((double)len * probability * (1.0 - probability)));
        ArrayList<Integer> al = new ArrayList<Integer>(elen);
        for (int i = 0; i < len; ++i) {
            if (!Rand.chance(probability)) continue;
            al.add(index.get(i));
        }
        return Indexz.create(al);
    }

    public static Index createRandomSubset(int length, double probability) {
        return Indexz.createRandomSubset(Indexz.createSequence(length), probability);
    }

    public static Index createRandomSelection(int length, int maxValue) {
        int[] sel = new int[length];
        for (int i = 0; i < length; ++i) {
            sel[i] = Rand.r(maxValue);
        }
        return new Index(length);
    }

    public static Index createLength(int length) {
        return new Index(length);
    }
}

