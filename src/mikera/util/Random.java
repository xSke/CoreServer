/*
 * Decompiled with CFR 0_129.
 */
package mikera.util;

import mikera.randomz.Hash;
import mikera.util.Rand;

public final class Random
extends java.util.Random {
    private static final long serialVersionUID = 6868944865706425166L;
    private long state = Random.ensureState(System.nanoTime());

    public Random() {
    }

    public Random(long state) {
        this.state = Random.ensureState(state);
    }

    private static final long ensureState(long l) {
        if (l == 0L) {
            return 54384849948L;
        }
        return l;
    }

    @Override
    protected int next(int bits) {
        return (int)(this.nextLong() >>> 64 - bits);
    }

    @Override
    public long nextLong() {
        long a = this.state;
        this.state = Rand.xorShift64(a);
        return a;
    }

    @Override
    public void setSeed(long seed) {
        this.state = Random.ensureState(seed);
    }

    public long getSeed() {
        return this.state;
    }

    public boolean equals(Object o) {
        if (o instanceof Random) {
            return this.equals((Random)o);
        }
        return Object.super.equals(o);
    }

    public Random clone() {
        return new Random(this.state);
    }

    public boolean equals(Random o) {
        return this.state == o.state;
    }

    public int hashCode() {
        return Hash.hashCode(this.state);
    }
}

