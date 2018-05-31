/*
 * Decompiled with CFR 0_129.
 */
package us.bpsm.edn;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

class Interner<K, V> {
    private final ConcurrentHashMap<K, Reference<V>> table = new ConcurrentHashMap();
    private final ReferenceQueue<V> refQueue = new ReferenceQueue();

    Interner() {
    }

    public V intern(K key, V value) {
        do {
            this.clearDeadEntries();
            WeakReference<V> newRef = new WeakReference<V>(value, this.refQueue);
            Reference existingRef = this.table.putIfAbsent(key, newRef);
            if (existingRef == null) {
                return value;
            }
            Object existingValue = existingRef.get();
            if (existingValue != null) {
                return (V)existingValue;
            }
            this.table.remove(key, existingRef);
        } while (true);
    }

    private void clearDeadEntries() {
        if (this.refQueue.poll() == null) {
            return;
        }
        while (this.refQueue.poll() != null) {
        }
        for (Map.Entry<K, Reference<V>> me : this.table.entrySet()) {
            Reference<V> ref = me.getValue();
            if (ref == null || ref.get() != null) continue;
            this.table.remove(me.getKey(), ref);
        }
    }
}

