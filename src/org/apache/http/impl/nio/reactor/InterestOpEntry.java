/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.impl.nio.reactor;

import java.nio.channels.SelectionKey;
import org.apache.http.util.Args;

class InterestOpEntry {
    private final SelectionKey key;
    private final int eventMask;

    public InterestOpEntry(SelectionKey key, int eventMask) {
        Args.notNull(key, "Selection key");
        this.key = key;
        this.eventMask = eventMask;
    }

    public SelectionKey getSelectionKey() {
        return this.key;
    }

    public int getEventMask() {
        return this.eventMask;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof InterestOpEntry) {
            InterestOpEntry that = (InterestOpEntry)obj;
            return this.key.equals(that.key);
        }
        return false;
    }

    public int hashCode() {
        return this.key.hashCode();
    }
}

