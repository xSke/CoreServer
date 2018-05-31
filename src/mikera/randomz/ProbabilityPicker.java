/*
 * Decompiled with CFR 0_129.
 */
package mikera.randomz;

import mikera.util.Rand;

public final class ProbabilityPicker<O> {
    private static final double[] NULLCHANCES = new double[0];
    private static final Object[] NULLOBJECTS = new Object[0];
    private int count = 0;
    private double[] chances = NULLCHANCES;
    private double[] belows = NULLCHANCES;
    private Object[] objects = NULLOBJECTS;
    private double total = 0.0;

    public O pick() {
        double r = Rand.nextDouble() * this.total;
        int i = 0;
        int ir = 1;
        double ci;
        while (r >= (ci = this.chances[i])) {
            double bi = this.belows[i];
            if ((r -= ci) < bi) {
                i = ProbabilityPicker.childIndex(i, ir, 0);
            } else {
                i = ProbabilityPicker.childIndex(i, ir, 1);
                r -= bi;
            }
            ir *= 2;
        }
        return (O)this.objects[i];
    }

    private void ensureSize(int n) {
        if (n <= this.count) {
            return;
        }
        if (n > this.chances.length) {
            int nn = Math.max(n, this.chances.length * 2);
            double[] newChances = new double[nn];
            double[] newBelows = new double[nn];
            Object[] newObjects = new Object[nn];
            System.arraycopy(this.chances, 0, newChances, 0, this.count);
            System.arraycopy(this.belows, 0, newBelows, 0, this.count);
            System.arraycopy(this.objects, 0, newObjects, 0, this.count);
            this.chances = newChances;
            this.objects = newObjects;
            this.belows = newBelows;
        }
    }

    public void add(O object, double probability) {
        int i = this.getIndex(object);
        if (i < 0) {
            this.addNew(object, probability);
            return;
        }
        this.setChance(i, probability + this.chances[i]);
    }

    private void addNew(O object, double probability) {
        if (probability < 0.0) {
            return;
        }
        this.ensureSize(this.count + 1);
        int i = this.count++;
        this.objects[i] = object;
        this.setChance(i, probability);
    }

    public int getIndex(O o) {
        for (int i = 0; i < this.count; ++i) {
            if (!this.objects[i].equals(o)) continue;
            return i;
        }
        return -1;
    }

    public double get(O o) {
        int i = this.getIndex(o);
        if (i < 0) {
            return 0.0;
        }
        return this.chances[i];
    }

    protected void update(O o, double p) {
        for (int i = 0; i < this.count; ++i) {
            if (!this.objects[i].equals(o)) continue;
            this.setChance(i, p);
            return;
        }
    }

    private void setChance(int i, double p) {
        double d = p - this.chances[i];
        this.total += d;
        this.chances[i] = p;
        int r = ProbabilityPicker.order(i);
        while (r > 1) {
            int pi = ProbabilityPicker.parentIndex(i, r);
            int pr = r / 2;
            if ((i + 1 & pr) <= 0) {
                double[] arrd = this.belows;
                int n = pi;
                arrd[n] = arrd[n] + d;
            }
            r = pr;
            i = pi;
        }
    }

    public double getTotal() {
        return this.total;
    }

    public int getCount() {
        return this.count;
    }

    public void remove(Object object) {
        Object o = object;
        int i = this.getIndex(o);
        if (i < 0) {
            throw new Error("Object not found: " + object.toString());
        }
        this.remove(i);
    }

    private void remove(int i) {
        this.setChance(i, 0.0);
        int last = this.count - 1;
        if (i < last) {
            double cd = this.chances[last];
            this.setChance(last, 0.0);
            this.objects[i] = this.objects[last];
            this.setChance(i, cd);
            this.objects[last] = null;
        }
        --this.count;
    }

    protected void swap(int a, int b) {
        double ca = this.chances[a];
        double cb = this.chances[b];
        Object oa = this.objects[a];
        this.setChance(a, cb);
        this.setChance(b, ca);
        this.objects[a] = this.objects[b];
        this.objects[b] = oa;
    }

    public static int parentIndex(int i) {
        int po = ProbabilityPicker.order(i);
        return ProbabilityPicker.parentIndex(i, po);
    }

    private static int parentIndex(int i, int po) {
        return (i + 1 & ~ po | po >> 1) - 1;
    }

    public static int childIndex(int i, int branch) {
        return ProbabilityPicker.childIndex(i, ProbabilityPicker.order(i), branch);
    }

    private static int childIndex(int i, int ir, int branch) {
        return i + (1 + branch) * ir;
    }

    private static int fillBitsRight(int n) {
        n |= n >> 1;
        n |= n >> 2;
        n |= n >> 4;
        n |= n >> 8;
        n |= n >> 16;
        return n;
    }

    private static int roundUpToPowerOfTwo(int n) {
        --n;
        n = ProbabilityPicker.fillBitsRight(n);
        return ++n;
    }

    public static int order(int i) {
        return ProbabilityPicker.roundUpToPowerOfTwo(i + 2) / 2;
    }
}

