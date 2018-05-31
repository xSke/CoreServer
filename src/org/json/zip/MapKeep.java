/*
 * Decompiled with CFR 0_129.
 */
package org.json.zip;

import java.util.HashMap;
import org.json.Kim;
import org.json.zip.JSONzip;
import org.json.zip.Keep;
import org.json.zip.PostMortem;

class MapKeep
extends Keep {
    private Object[] list;
    private HashMap map;

    public MapKeep(int bits) {
        super(bits);
        this.list = new Object[this.capacity];
        this.map = new HashMap(this.capacity);
    }

    private void compact() {
        int to = 0;
        for (int from = 0; from < this.capacity; ++from) {
            Object key = this.list[from];
            long usage = MapKeep.age(this.uses[from]);
            if (usage > 0L) {
                this.uses[to] = usage;
                this.list[to] = key;
                this.map.put(key, new Integer(to));
                ++to;
                continue;
            }
            this.map.remove(key);
        }
        if (to < this.capacity) {
            this.length = to;
        } else {
            this.map.clear();
            this.length = 0;
        }
        this.power = 0;
    }

    public int find(Object key) {
        Object o = this.map.get(key);
        return o instanceof Integer ? (Integer)o : -1;
    }

    public boolean postMortem(PostMortem pm) {
        MapKeep that = (MapKeep)pm;
        if (this.length != that.length) {
            JSONzip.log("" + this.length + " <> " + that.length);
            return false;
        }
        for (int i = 0; i < this.length; ++i) {
            boolean b;
            if (this.list[i] instanceof Kim) {
                b = ((Kim)this.list[i]).equals(that.list[i]);
            } else {
                Object o = this.list[i];
                Object q = that.list[i];
                if (o instanceof Number) {
                    o = o.toString();
                }
                if (q instanceof Number) {
                    q = q.toString();
                }
                b = o.equals(q);
            }
            if (b) continue;
            JSONzip.log("\n[" + i + "]\n " + this.list[i] + "\n " + that.list[i] + "\n " + this.uses[i] + "\n " + that.uses[i]);
            return false;
        }
        return true;
    }

    public void register(Object value) {
        if (this.length >= this.capacity) {
            this.compact();
        }
        this.list[this.length] = value;
        this.map.put(value, new Integer(this.length));
        this.uses[this.length] = 1L;
        ++this.length;
    }

    public Object value(int integer) {
        return this.list[integer];
    }
}

