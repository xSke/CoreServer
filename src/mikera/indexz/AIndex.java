/*
 * Decompiled with CFR 0_129.
 */
package mikera.indexz;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import mikera.indexz.Index;
import mikera.indexz.impl.IndexIterator;
import mikera.vectorz.Tools;

public abstract class AIndex
implements Serializable,
Cloneable,
Comparable<AIndex>,
Iterable<Integer> {
    public abstract int get(int var1);

    public abstract int length();

    public abstract void set(int var1, int var2);

    public boolean isFullyMutable() {
        return false;
    }

    public void copyTo(int[] array, int offset) {
        int len = this.length();
        for (int i = 0; i < len; ++i) {
            array[offset + i] = this.get(i);
        }
    }

    public int[] toArray() {
        int len = this.length();
        int[] arr = new int[len];
        this.copyTo(arr, 0);
        return arr;
    }

    public Index toIndex() {
        int len = this.length();
        int[] arr = new int[len];
        this.copyTo(arr, 0);
        return Index.wrap(arr);
    }

    public List<Integer> toList() {
        int len = this.length();
        ArrayList<Integer> al = new ArrayList<Integer>(len);
        for (int i = 0; i < len; ++i) {
            al.add(this.get(i));
        }
        return al;
    }

    public void swap(int i, int j) {
        int t = this.get(i);
        this.set(i, this.get(j));
        this.set(j, t);
    }

    public void reverse() {
        int len = this.length();
        int m = len / 2;
        for (int i = 0; i < m; ++i) {
            this.swap(i, len - 1 - i);
        }
    }

    public int minIndex() {
        int len = this.length();
        int min = this.get(0);
        for (int i = 1; i < len; ++i) {
            int x = this.get(i);
            if (x >= min) continue;
            min = x;
        }
        return min;
    }

    public int maxIndex() {
        int len = this.length();
        int max = this.get(0);
        for (int i = 1; i < len; ++i) {
            int x = this.get(i);
            if (x <= max) continue;
            max = x;
        }
        return max;
    }

    public boolean isSorted() {
        int len = this.length();
        for (int i = 1; i < len; ++i) {
            if (this.get(i - 1) <= this.get(i)) continue;
            return false;
        }
        return true;
    }

    public boolean isDistinctSorted() {
        int len = this.length();
        for (int i = 1; i < len; ++i) {
            if (this.get(i - 1) < this.get(i)) continue;
            return false;
        }
        return true;
    }

    public void sort() {
        throw new UnsupportedOperationException();
    }

    public boolean isDistinct() {
        HashSet<Integer> hs = new HashSet<Integer>();
        int len = this.length();
        for (int i = 0; i < len; ++i) {
            Integer v = this.get(i);
            if (hs.contains(v)) {
                return false;
            }
            hs.add(v);
        }
        return true;
    }

    public boolean isPermutation() {
        if (!this.isWithinRange(0, this.length())) {
            return false;
        }
        if (!this.isDistinct()) {
            return false;
        }
        return true;
    }

    private boolean isWithinRange(int start, int length) {
        int len = this.length();
        for (int i = 0; i < len; ++i) {
            int v = this.get(i) - start;
            if (v >= 0 && v < length) continue;
            return false;
        }
        return true;
    }

    public boolean contains(int index) {
        int len = this.length();
        for (int i = 0; i < len; ++i) {
            if (this.get(i) != index) continue;
            return true;
        }
        return false;
    }

    public boolean containsSorted(int index) {
        return this.contains(index);
    }

    public boolean contains(Index inds) {
        int len = inds.length();
        for (int i = 0; i < len; ++i) {
            if (this.contains(inds.get(i))) continue;
            return false;
        }
        return true;
    }

    public boolean equals(Object o) {
        if (o instanceof AIndex) {
            return this.equals((AIndex)o);
        }
        return false;
    }

    public boolean equals(AIndex o) {
        int len = this.length();
        if (len != o.length()) {
            return false;
        }
        for (int i = 0; i < len; ++i) {
            if (this.get(i) == o.get(i)) continue;
            return false;
        }
        return true;
    }

    public int hashCode() {
        int hashCode = 1;
        int len = this.length();
        for (int i = 0; i < len; ++i) {
            hashCode = 31 * hashCode + Tools.hashCode(this.get(i));
        }
        return hashCode;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        int length = this.length();
        sb.append('[');
        if (length > 0) {
            sb.append(this.get(0));
            for (int i = 1; i < length; ++i) {
                sb.append(',');
                sb.append(this.get(i));
            }
        }
        sb.append(']');
        return sb.toString();
    }

    public AIndex clone() {
        try {
            return (AIndex)super.clone();
        }
        catch (CloneNotSupportedException e) {
            throw new Error(e);
        }
    }

    @Override
    public int compareTo(AIndex a) {
        int alen;
        int len = this.length();
        if (len != (alen = a.length())) {
            return len - alen;
        }
        for (int i = 0; i < len; ++i) {
            int d = this.get(i) - a.get(i);
            if (d == 0) continue;
            return d;
        }
        return 0;
    }

    public IndexIterator iterator() {
        return new IndexIterator(this);
    }

    public Index compose(AIndex a) {
        int len = this.length();
        Index r = new Index(len);
        for (int i = 0; i < len; ++i) {
            r.data[i] = a.get(this.get(i));
        }
        return r;
    }

    public abstract int indexPosition(int var1);

    public abstract AIndex exactClone();
}

