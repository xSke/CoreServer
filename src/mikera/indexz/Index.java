/*
 * Decompiled with CFR 0_129.
 */
package mikera.indexz;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import mikera.indexz.AIndex;
import mikera.vectorz.AVector;
import mikera.vectorz.impl.IndexVector;
import mikera.vectorz.util.ErrorMessages;
import mikera.vectorz.util.IntArrays;
import mikera.vectorz.util.VectorzException;

public final class Index
extends AIndex {
    private static final long serialVersionUID = 8698831088064498284L;
    public static final Index EMPTY = new Index(0);
    public final int[] data;

    public Index(int length) {
        this(new int[length]);
    }

    private Index(int[] indexes) {
        this.data = indexes;
    }

    public static Index create(ArrayList<Integer> v) {
        int n = v.size();
        Index ind = new Index(n);
        for (int i = 0; i < n; ++i) {
            ind.data[i] = v.get(i);
        }
        return ind;
    }

    public static Index create(List<Integer> v) {
        int n = v.size();
        Index ind = new Index(n);
        for (int i = 0; i < n; ++i) {
            ind.data[i] = v.get(i);
        }
        return ind;
    }

    public static Index create(int[] indices) {
        return Index.wrap((int[])indices.clone());
    }

    public static Index create(AIndex index) {
        int[] data = index.toArray();
        return new Index(data);
    }

    public static Index createSorted(Set<Integer> keySet) {
        int n = keySet.size();
        int[] data = new int[n];
        int i = 0;
        Iterator<Integer> iterator = keySet.iterator();
        while (iterator.hasNext()) {
            int v = iterator.next();
            data[i++] = v;
        }
        Arrays.sort(data);
        return Index.wrap(data);
    }

    public static Index createSorted(SortedSet<Integer> keySet) {
        int[] rs = new int[keySet.size()];
        int i = 0;
        for (Integer x : keySet) {
            rs[i++] = x;
        }
        if (i != rs.length) {
            throw new VectorzException(ErrorMessages.impossible());
        }
        return new Index(rs);
    }

    public static Index create(AVector v) {
        int n = v.length();
        Index ind = new Index(n);
        for (int i = 0; i < n; ++i) {
            ind.data[i] = (int)v.unsafeGet(i);
        }
        return ind;
    }

    public static Index wrap(int[] indexes) {
        return new Index(indexes);
    }

    public static /* varargs */ Index of(int ... indexes) {
        return new Index((int[])indexes.clone());
    }

    public static Index createLength(int len) {
        return new Index(len);
    }

    @Override
    public void swap(int i, int j) {
        int t = this.data[i];
        this.data[i] = this.data[j];
        this.data[j] = t;
    }

    @Override
    public void reverse() {
        int len = this.length();
        int m = len / 2;
        for (int i = 0; i < m; ++i) {
            this.swap(i, len - 1 - i);
        }
    }

    @Override
    public boolean isDistinctSorted() {
        int len = this.length();
        for (int i = 1; i < len; ++i) {
            if (this.data[i - 1] < this.data[i]) continue;
            return false;
        }
        return true;
    }

    @Override
    public boolean isSorted() {
        int len = this.length();
        for (int i = 1; i < len; ++i) {
            if (this.data[i - 1] <= this.data[i]) continue;
            return false;
        }
        return true;
    }

    @Override
    public boolean isPermutation() {
        int n = this.length();
        if (n >= 64) {
            return this.isLongPermutation();
        }
        return this.isShortPermutation();
    }

    private boolean isShortPermutation() {
        int n = this.length();
        long chk = 0L;
        for (int i = 0; i < n; ++i) {
            int v = this.data[i];
            if (v < 0 || v >= n) {
                return false;
            }
            chk |= 1L << v;
        }
        return chk + 1L == 1L << n;
    }

    private boolean isLongPermutation() {
        int i;
        int n = this.length();
        boolean[] chk = new boolean[n];
        for (i = 0; i < n; ++i) {
            int v = this.data[i];
            if (v < 0 || v >= n || chk[v]) {
                return false;
            }
            chk[v] = true;
        }
        for (i = 0; i < n; ++i) {
            if (chk[i]) continue;
            return false;
        }
        return true;
    }

    public Index includeSorted(Set<Integer> is) {
        TreeSet<Integer> ss = new TreeSet<Integer>(this.toSet());
        for (Integer i : is) {
            ss.add(i);
        }
        return Index.createSorted(ss);
    }

    public Index includeSorted(Index ind) {
        TreeSet<Integer> ss = new TreeSet<Integer>(this.toSet());
        for (Integer i : ind) {
            ss.add(i);
        }
        return Index.createSorted(ss);
    }

    public Set<Integer> toSet() {
        TreeSet<Integer> ss = new TreeSet<Integer>();
        for (int i = 0; i < this.data.length; ++i) {
            ss.add(this.data[i]);
        }
        return ss;
    }

    public SortedSet<Integer> toSortedSet() {
        TreeSet<Integer> ss = new TreeSet<Integer>();
        for (int i = 0; i < this.data.length; ++i) {
            ss.add(this.data[i]);
        }
        return ss;
    }

    public int swapCount() {
        if (this.length() <= 64) {
            return this.swapCountSmall();
        }
        return this.swapCountLong();
    }

    private int swapCountLong() {
        int n = this.length();
        int swaps = 0;
        BitSet seen = new BitSet(n);
        for (int i = 0; i < n; ++i) {
            if (seen.get(i)) continue;
            seen.set(i);
            int j = this.data[i];
            while (!seen.get(j)) {
                seen.set(j);
                ++swaps;
                j = this.data[j];
            }
        }
        return swaps;
    }

    private int swapCountSmall() {
        int n = this.length();
        int swaps = 0;
        long seen = 0L;
        for (int i = 0; i < n; ++i) {
            long mask = 1L << i;
            if ((seen & mask) != 0L) continue;
            seen |= mask;
            int j = this.data[i];
            while ((seen & 1L << j) == 0L) {
                seen |= 1L << j;
                ++swaps;
                j = this.data[j];
            }
        }
        return swaps;
    }

    public boolean isOddPermutation() {
        return (this.swapCount() & 1) == 1;
    }

    public boolean isEvenPermutation() {
        return (this.swapCount() & 1) == 0;
    }

    @Override
    public int get(int i) {
        return this.data[i];
    }

    public int unsafeGet(int i) {
        return this.data[i];
    }

    @Override
    public void set(int i, int value) {
        this.data[i] = value;
    }

    @Override
    public int length() {
        return this.data.length;
    }

    @Override
    public Index clone() {
        return new Index(IntArrays.copyOf(this.data));
    }

    public void permute(Index permutationIndex) {
        int len = this.length();
        assert (len == permutationIndex.length());
        int[] temp = (int[])this.data.clone();
        for (int i = 0; i < len; ++i) {
            this.data[i] = temp[permutationIndex.get(i)];
        }
    }

    @Override
    public void sort() {
        Arrays.sort(this.data);
    }

    public AVector asVector() {
        return IndexVector.wrap(this);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Index) {
            return this.equals((Index)o);
        }
        return super.equals(o);
    }

    public boolean equals(Index o) {
        int len = this.length();
        if (len != o.length()) {
            return false;
        }
        for (int i = 0; i < len; ++i) {
            if (this.data[i] == o.data[i]) continue;
            return false;
        }
        return true;
    }

    public int[] getData() {
        return this.data;
    }

    @Override
    public int[] toArray() {
        return (int[])this.getData().clone();
    }

    @Override
    public boolean isFullyMutable() {
        return true;
    }

    public void lookupWith(Index source) {
        int len = this.length();
        for (int i = 0; i < len; ++i) {
            this.data[i] = source.data[this.data[i]];
        }
    }

    @Override
    public Index compose(AIndex a) {
        if (a instanceof Index) {
            return this.compose((Index)a);
        }
        return super.compose(a);
    }

    public Index compose(Index a) {
        int len = this.length();
        Index r = new Index(len);
        for (int i = 0; i < len; ++i) {
            r.data[i] = a.data[this.data[i]];
        }
        return r;
    }

    @Override
    public int indexPosition(int x) {
        return IntArrays.indexPosition(this.data, x);
    }

    public int findMissing() {
        int n = this.data.length;
        for (int i = 0; i < n; ++i) {
            if (this.data[i] == i) continue;
            return i;
        }
        return -1;
    }

    @Override
    public boolean containsSorted(int index) {
        return this.indexPosition(index) >= 0;
    }

    public Index insert(int position, int value) {
        return new Index(IntArrays.insert(this.data, position, value));
    }

    public int seekPosition(int i) {
        int min = 0;
        int max = this.data.length;
        while (min < max) {
            int mid = min + max >> 1;
            int mi = this.data[mid];
            if (i == mi) {
                return mid;
            }
            if (i < mi) {
                max = mid;
                continue;
            }
            min = mid + 1;
        }
        return min;
    }

    public int find(int value) {
        for (int i = 0; i < this.data.length; ++i) {
            if (this.data[i] != value) continue;
            return i;
        }
        return -1;
    }

    public Index invert() {
        int n = this.length();
        Index ni = new Index(n);
        for (int i = 0; i < n; ++i) {
            ni.set(this.get(i), i);
        }
        return ni;
    }

    public boolean allInRange(int start, int end) {
        for (int i = 0; i < this.data.length; ++i) {
            int a = this.data[i];
            if (a >= start && a < end) continue;
            return false;
        }
        return true;
    }

    public int[] getShape() {
        return new int[this.length()];
    }

    @Override
    public Index exactClone() {
        return Index.create(this);
    }
}

