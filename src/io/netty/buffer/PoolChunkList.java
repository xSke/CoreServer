/*
 * Decompiled with CFR 0_129.
 */
package io.netty.buffer;

import io.netty.buffer.PoolChunk;
import io.netty.buffer.PoolChunkListMetric;
import io.netty.buffer.PoolChunkMetric;
import io.netty.buffer.PooledByteBuf;
import io.netty.util.internal.StringUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

final class PoolChunkList<T>
implements PoolChunkListMetric {
    private static final Iterator<PoolChunkMetric> EMPTY_METRICS = Collections.emptyList().iterator();
    private final PoolChunkList<T> nextList;
    private final int minUsage;
    private final int maxUsage;
    private PoolChunk<T> head;
    private PoolChunkList<T> prevList;

    PoolChunkList(PoolChunkList<T> nextList, int minUsage, int maxUsage) {
        this.nextList = nextList;
        this.minUsage = minUsage;
        this.maxUsage = maxUsage;
    }

    void prevList(PoolChunkList<T> prevList) {
        assert (this.prevList == null);
        this.prevList = prevList;
    }

    boolean allocate(PooledByteBuf<T> buf, int reqCapacity, int normCapacity) {
        long handle;
        if (this.head == null) {
            return false;
        }
        PoolChunk<T> cur = this.head;
        while ((handle = cur.allocate(normCapacity)) < 0L) {
            cur = cur.next;
            if (cur != null) continue;
            return false;
        }
        cur.initBuf(buf, handle, reqCapacity);
        if (cur.usage() >= this.maxUsage) {
            this.remove(cur);
            this.nextList.add(cur);
        }
        return true;
    }

    boolean free(PoolChunk<T> chunk, long handle) {
        chunk.free(handle);
        if (chunk.usage() < this.minUsage) {
            this.remove(chunk);
            if (this.prevList == null) {
                assert (chunk.usage() == 0);
                return false;
            }
            this.prevList.add(chunk);
            return true;
        }
        return true;
    }

    void add(PoolChunk<T> chunk) {
        if (chunk.usage() >= this.maxUsage) {
            this.nextList.add(chunk);
            return;
        }
        chunk.parent = this;
        if (this.head == null) {
            this.head = chunk;
            chunk.prev = null;
            chunk.next = null;
        } else {
            chunk.prev = null;
            chunk.next = this.head;
            this.head.prev = chunk;
            this.head = chunk;
        }
    }

    private void remove(PoolChunk<T> cur) {
        if (cur == this.head) {
            this.head = cur.next;
            if (this.head != null) {
                this.head.prev = null;
            }
        } else {
            PoolChunk next;
            cur.prev.next = next = cur.next;
            if (next != null) {
                next.prev = cur.prev;
            }
        }
    }

    @Override
    public int minUsage() {
        return this.minUsage;
    }

    @Override
    public int maxUsage() {
        return this.maxUsage;
    }

    @Override
    public Iterator<PoolChunkMetric> iterator() {
        ArrayList<PoolChunk<T>> metrics;
        if (this.head == null) {
            return EMPTY_METRICS;
        }
        metrics = new ArrayList<PoolChunk<T>>();
        PoolChunk<T> cur = this.head;
        do {
            metrics.add(cur);
        } while ((cur = cur.next) != null);
        return metrics.iterator();
    }

    public String toString() {
        if (this.head == null) {
            return "none";
        }
        StringBuilder buf = new StringBuilder();
        PoolChunk<T> cur = this.head;
        do {
            buf.append(cur);
            cur = cur.next;
            if (cur == null) break;
            buf.append(StringUtil.NEWLINE);
        } while (true);
        return buf.toString();
    }
}

