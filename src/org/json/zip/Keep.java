/*
 * Decompiled with CFR 0_129.
 */
package org.json.zip;

import org.json.zip.JSONzip;
import org.json.zip.None;
import org.json.zip.PostMortem;

abstract class Keep
implements None,
PostMortem {
    protected int capacity;
    protected int length;
    protected int power;
    protected long[] uses;

    public Keep(int bits) {
        this.capacity = JSONzip.twos[bits];
        this.length = 0;
        this.power = 0;
        this.uses = new long[this.capacity];
    }

    public static long age(long use) {
        return use >= 32L ? 16L : use / 2L;
    }

    public int bitsize() {
        while (JSONzip.twos[this.power] < this.length) {
            ++this.power;
        }
        return this.power;
    }

    public void tick(int integer) {
        long[] arrl = this.uses;
        int n = integer;
        arrl[n] = arrl[n] + 1L;
    }

    public abstract Object value(int var1);
}

