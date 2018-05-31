/*
 * Decompiled with CFR 0_129.
 */
package io.netty.buffer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PoolArenaMetric;
import io.netty.buffer.PoolChunk;
import io.netty.buffer.PoolChunkList;
import io.netty.buffer.PoolChunkListMetric;
import io.netty.buffer.PoolSubpage;
import io.netty.buffer.PoolSubpageMetric;
import io.netty.buffer.PoolThreadCache;
import io.netty.buffer.PooledByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.PooledDirectByteBuf;
import io.netty.buffer.PooledHeapByteBuf;
import io.netty.buffer.PooledUnsafeDirectByteBuf;
import io.netty.util.internal.LongCounter;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.StringUtil;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

abstract class PoolArena<T>
implements PoolArenaMetric {
    static final int numTinySubpagePools = 32;
    final PooledByteBufAllocator parent;
    private final int maxOrder;
    final int pageSize;
    final int pageShifts;
    final int chunkSize;
    final int subpageOverflowMask;
    final int numSmallSubpagePools;
    private final PoolSubpage<T>[] tinySubpagePools;
    private final PoolSubpage<T>[] smallSubpagePools;
    private final PoolChunkList<T> q050;
    private final PoolChunkList<T> q025;
    private final PoolChunkList<T> q000;
    private final PoolChunkList<T> qInit;
    private final PoolChunkList<T> q075;
    private final PoolChunkList<T> q100;
    private final List<PoolChunkListMetric> chunkListMetrics;
    private long allocationsTiny;
    private long allocationsSmall;
    private long allocationsNormal;
    private final LongCounter allocationsHuge = PlatformDependent.newLongCounter();
    private long deallocationsTiny;
    private long deallocationsSmall;
    private long deallocationsNormal;
    private final LongCounter deallocationsHuge = PlatformDependent.newLongCounter();

    protected PoolArena(PooledByteBufAllocator parent, int pageSize, int maxOrder, int pageShifts, int chunkSize) {
        int i;
        this.parent = parent;
        this.pageSize = pageSize;
        this.maxOrder = maxOrder;
        this.pageShifts = pageShifts;
        this.chunkSize = chunkSize;
        this.subpageOverflowMask = ~ (pageSize - 1);
        this.tinySubpagePools = this.newSubpagePoolArray(32);
        for (i = 0; i < this.tinySubpagePools.length; ++i) {
            this.tinySubpagePools[i] = this.newSubpagePoolHead(pageSize);
        }
        this.numSmallSubpagePools = pageShifts - 9;
        this.smallSubpagePools = this.newSubpagePoolArray(this.numSmallSubpagePools);
        for (i = 0; i < this.smallSubpagePools.length; ++i) {
            this.smallSubpagePools[i] = this.newSubpagePoolHead(pageSize);
        }
        this.q100 = new PoolChunkList(null, 100, Integer.MAX_VALUE);
        this.q075 = new PoolChunkList<T>(this.q100, 75, 100);
        this.q050 = new PoolChunkList<T>(this.q075, 50, 100);
        this.q025 = new PoolChunkList<T>(this.q050, 25, 75);
        this.q000 = new PoolChunkList<T>(this.q025, 1, 50);
        this.qInit = new PoolChunkList<T>(this.q000, Integer.MIN_VALUE, 25);
        this.q100.prevList(this.q075);
        this.q075.prevList(this.q050);
        this.q050.prevList(this.q025);
        this.q025.prevList(this.q000);
        this.q000.prevList(null);
        this.qInit.prevList(this.qInit);
        ArrayList<PoolChunkList<T>> metrics = new ArrayList<PoolChunkList<T>>(6);
        metrics.add(this.qInit);
        metrics.add(this.q000);
        metrics.add(this.q025);
        metrics.add(this.q050);
        metrics.add(this.q075);
        metrics.add(this.q100);
        this.chunkListMetrics = Collections.unmodifiableList(metrics);
    }

    private PoolSubpage<T> newSubpagePoolHead(int pageSize) {
        PoolSubpage head = new PoolSubpage(pageSize);
        head.prev = head;
        head.next = head;
        return head;
    }

    private PoolSubpage<T>[] newSubpagePoolArray(int size) {
        return new PoolSubpage[size];
    }

    abstract boolean isDirect();

    PooledByteBuf<T> allocate(PoolThreadCache cache, int reqCapacity, int maxCapacity) {
        PooledByteBuf<T> buf = this.newByteBuf(maxCapacity);
        this.allocate(cache, buf, reqCapacity);
        return buf;
    }

    static int tinyIdx(int normCapacity) {
        return normCapacity >>> 4;
    }

    static int smallIdx(int normCapacity) {
        int tableIdx = 0;
        int i = normCapacity >>> 10;
        while (i != 0) {
            i >>>= 1;
            ++tableIdx;
        }
        return tableIdx;
    }

    boolean isTinyOrSmall(int normCapacity) {
        return (normCapacity & this.subpageOverflowMask) == 0;
    }

    static boolean isTiny(int normCapacity) {
        return (normCapacity & -512) == 0;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void allocate(PoolThreadCache cache, PooledByteBuf<T> buf, int reqCapacity) {
        int normCapacity = this.normalizeCapacity(reqCapacity);
        if (this.isTinyOrSmall(normCapacity)) {
            PoolSubpage<T>[] table;
            PoolSubpage<T> head;
            int tableIdx;
            boolean tiny = PoolArena.isTiny(normCapacity);
            if (tiny) {
                if (cache.allocateTiny(this, buf, reqCapacity, normCapacity)) {
                    return;
                }
                tableIdx = PoolArena.tinyIdx(normCapacity);
                table = this.tinySubpagePools;
            } else {
                if (cache.allocateSmall(this, buf, reqCapacity, normCapacity)) {
                    return;
                }
                tableIdx = PoolArena.smallIdx(normCapacity);
                table = this.smallSubpagePools;
            }
            PoolSubpage<T> poolSubpage = head = table[tableIdx];
            synchronized (poolSubpage) {
                PoolSubpage<T> s = head.next;
                if (s != head) {
                    assert (s.doNotDestroy && s.elemSize == normCapacity);
                    long handle = s.allocate();
                    assert (handle >= 0L);
                    s.chunk.initBufWithSubpage(buf, handle, reqCapacity);
                    if (tiny) {
                        ++this.allocationsTiny;
                    } else {
                        ++this.allocationsSmall;
                    }
                    return;
                }
            }
            this.allocateNormal(buf, reqCapacity, normCapacity);
            return;
        }
        if (normCapacity <= this.chunkSize) {
            if (cache.allocateNormal(this, buf, reqCapacity, normCapacity)) {
                return;
            }
            this.allocateNormal(buf, reqCapacity, normCapacity);
        } else {
            this.allocateHuge(buf, reqCapacity);
        }
    }

    private synchronized void allocateNormal(PooledByteBuf<T> buf, int reqCapacity, int normCapacity) {
        ++this.allocationsNormal;
        if (this.q050.allocate(buf, reqCapacity, normCapacity) || this.q025.allocate(buf, reqCapacity, normCapacity) || this.q000.allocate(buf, reqCapacity, normCapacity) || this.qInit.allocate(buf, reqCapacity, normCapacity) || this.q075.allocate(buf, reqCapacity, normCapacity) || this.q100.allocate(buf, reqCapacity, normCapacity)) {
            return;
        }
        PoolChunk<T> c = this.newChunk(this.pageSize, this.maxOrder, this.pageShifts, this.chunkSize);
        long handle = c.allocate(normCapacity);
        assert (handle > 0L);
        c.initBuf(buf, handle, reqCapacity);
        this.qInit.add(c);
    }

    private void allocateHuge(PooledByteBuf<T> buf, int reqCapacity) {
        this.allocationsHuge.increment();
        buf.initUnpooled(this.newUnpooledChunk(reqCapacity), reqCapacity);
    }

    void free(PoolChunk<T> chunk, long handle, int normCapacity, PoolThreadCache cache) {
        if (chunk.unpooled) {
            this.allocationsHuge.decrement();
            this.destroyChunk(chunk);
        } else {
            SizeClass sizeClass = this.sizeClass(normCapacity);
            if (cache != null && cache.add(this, chunk, handle, normCapacity, sizeClass)) {
                return;
            }
            this.freeChunk(chunk, handle, sizeClass);
        }
    }

    private SizeClass sizeClass(int normCapacity) {
        if (!this.isTinyOrSmall(normCapacity)) {
            return SizeClass.Normal;
        }
        return PoolArena.isTiny(normCapacity) ? SizeClass.Tiny : SizeClass.Small;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void freeChunk(PoolChunk<T> chunk, long handle, SizeClass sizeClass) {
        boolean destroyChunk;
        PoolArena poolArena = this;
        synchronized (poolArena) {
            switch (sizeClass) {
                case Normal: {
                    ++this.deallocationsNormal;
                    break;
                }
                case Small: {
                    ++this.deallocationsSmall;
                    break;
                }
                case Tiny: {
                    ++this.deallocationsTiny;
                    break;
                }
                default: {
                    throw new Error();
                }
            }
            destroyChunk = !chunk.parent.free(chunk, handle);
        }
        if (destroyChunk) {
            this.destroyChunk(chunk);
        }
    }

    PoolSubpage<T> findSubpagePoolHead(int elemSize) {
        int tableIdx;
        PoolSubpage<T>[] table;
        if (PoolArena.isTiny(elemSize)) {
            tableIdx = elemSize >>> 4;
            table = this.tinySubpagePools;
        } else {
            tableIdx = 0;
            elemSize >>>= 10;
            while (elemSize != 0) {
                elemSize >>>= 1;
                ++tableIdx;
            }
            table = this.smallSubpagePools;
        }
        return table[tableIdx];
    }

    int normalizeCapacity(int reqCapacity) {
        if (reqCapacity < 0) {
            throw new IllegalArgumentException("capacity: " + reqCapacity + " (expected: 0+)");
        }
        if (reqCapacity >= this.chunkSize) {
            return reqCapacity;
        }
        if (!PoolArena.isTiny(reqCapacity)) {
            int normalizedCapacity = reqCapacity;
            --normalizedCapacity;
            normalizedCapacity |= normalizedCapacity >>> 1;
            normalizedCapacity |= normalizedCapacity >>> 2;
            normalizedCapacity |= normalizedCapacity >>> 4;
            normalizedCapacity |= normalizedCapacity >>> 8;
            normalizedCapacity |= normalizedCapacity >>> 16;
            if (++normalizedCapacity < 0) {
                normalizedCapacity >>>= 1;
            }
            return normalizedCapacity;
        }
        if ((reqCapacity & 15) == 0) {
            return reqCapacity;
        }
        return (reqCapacity & -16) + 16;
    }

    void reallocate(PooledByteBuf<T> buf, int newCapacity, boolean freeOldMemory) {
        if (newCapacity < 0 || newCapacity > buf.maxCapacity()) {
            throw new IllegalArgumentException("newCapacity: " + newCapacity);
        }
        int oldCapacity = buf.length;
        if (oldCapacity == newCapacity) {
            return;
        }
        PoolChunk<T> oldChunk = buf.chunk;
        long oldHandle = buf.handle;
        T oldMemory = buf.memory;
        int oldOffset = buf.offset;
        int oldMaxLength = buf.maxLength;
        int readerIndex = buf.readerIndex();
        int writerIndex = buf.writerIndex();
        this.allocate(this.parent.threadCache(), buf, newCapacity);
        if (newCapacity > oldCapacity) {
            this.memoryCopy(oldMemory, oldOffset, buf.memory, buf.offset, oldCapacity);
        } else if (newCapacity < oldCapacity) {
            if (readerIndex < newCapacity) {
                if (writerIndex > newCapacity) {
                    writerIndex = newCapacity;
                }
                this.memoryCopy(oldMemory, oldOffset + readerIndex, buf.memory, buf.offset + readerIndex, writerIndex - readerIndex);
            } else {
                readerIndex = writerIndex = newCapacity;
            }
        }
        buf.setIndex(readerIndex, writerIndex);
        if (freeOldMemory) {
            this.free(oldChunk, oldHandle, oldMaxLength, buf.cache);
        }
    }

    @Override
    public int numTinySubpages() {
        return this.tinySubpagePools.length;
    }

    @Override
    public int numSmallSubpages() {
        return this.smallSubpagePools.length;
    }

    @Override
    public int numChunkLists() {
        return this.chunkListMetrics.size();
    }

    @Override
    public List<PoolSubpageMetric> tinySubpages() {
        return PoolArena.subPageMetricList(this.tinySubpagePools);
    }

    @Override
    public List<PoolSubpageMetric> smallSubpages() {
        return PoolArena.subPageMetricList(this.smallSubpagePools);
    }

    @Override
    public List<PoolChunkListMetric> chunkLists() {
        return this.chunkListMetrics;
    }

    private static List<PoolSubpageMetric> subPageMetricList(PoolSubpage<?>[] pages) {
        ArrayList<PoolSubpageMetric> metrics = new ArrayList<PoolSubpageMetric>();
        for (int i = 1; i < pages.length; ++i) {
            PoolSubpage<?> head = pages[i];
            if (head.next == head) continue;
            PoolSubpage<T> s = head.next;
            do {
                metrics.add(s);
            } while ((s = s.next) != head);
        }
        return metrics;
    }

    @Override
    public long numAllocations() {
        return this.allocationsTiny + this.allocationsSmall + this.allocationsNormal + this.allocationsHuge.value();
    }

    @Override
    public long numTinyAllocations() {
        return this.allocationsTiny;
    }

    @Override
    public long numSmallAllocations() {
        return this.allocationsSmall;
    }

    @Override
    public long numNormalAllocations() {
        return this.allocationsNormal;
    }

    @Override
    public long numDeallocations() {
        return this.deallocationsTiny + this.deallocationsSmall + this.allocationsNormal + this.deallocationsHuge.value();
    }

    @Override
    public long numTinyDeallocations() {
        return this.deallocationsTiny;
    }

    @Override
    public long numSmallDeallocations() {
        return this.deallocationsSmall;
    }

    @Override
    public long numNormalDeallocations() {
        return this.deallocationsNormal;
    }

    @Override
    public long numHugeAllocations() {
        return this.allocationsHuge.value();
    }

    @Override
    public long numHugeDeallocations() {
        return this.deallocationsHuge.value();
    }

    @Override
    public long numActiveAllocations() {
        long val = this.numAllocations() - this.numDeallocations();
        return val >= 0L ? val : 0L;
    }

    @Override
    public long numActiveTinyAllocations() {
        long val = this.numTinyAllocations() - this.numTinyDeallocations();
        return val >= 0L ? val : 0L;
    }

    @Override
    public long numActiveSmallAllocations() {
        long val = this.numSmallAllocations() - this.numSmallDeallocations();
        return val >= 0L ? val : 0L;
    }

    @Override
    public long numActiveNormalAllocations() {
        long val = this.numNormalAllocations() - this.numNormalDeallocations();
        return val >= 0L ? val : 0L;
    }

    @Override
    public long numActiveHugeAllocations() {
        long val = this.numHugeAllocations() - this.numHugeDeallocations();
        return val >= 0L ? val : 0L;
    }

    protected abstract PoolChunk<T> newChunk(int var1, int var2, int var3, int var4);

    protected abstract PoolChunk<T> newUnpooledChunk(int var1);

    protected abstract PooledByteBuf<T> newByteBuf(int var1);

    protected abstract void memoryCopy(T var1, int var2, T var3, int var4, int var5);

    protected abstract void destroyChunk(PoolChunk<T> var1);

    public synchronized String toString() {
        PoolSubpage<T> head;
        PoolSubpage<T> s;
        int i;
        StringBuilder buf = new StringBuilder().append("Chunk(s) at 0~25%:").append(StringUtil.NEWLINE).append(this.qInit).append(StringUtil.NEWLINE).append("Chunk(s) at 0~50%:").append(StringUtil.NEWLINE).append(this.q000).append(StringUtil.NEWLINE).append("Chunk(s) at 25~75%:").append(StringUtil.NEWLINE).append(this.q025).append(StringUtil.NEWLINE).append("Chunk(s) at 50~100%:").append(StringUtil.NEWLINE).append(this.q050).append(StringUtil.NEWLINE).append("Chunk(s) at 75~100%:").append(StringUtil.NEWLINE).append(this.q075).append(StringUtil.NEWLINE).append("Chunk(s) at 100%:").append(StringUtil.NEWLINE).append(this.q100).append(StringUtil.NEWLINE).append("tiny subpages:");
        for (i = 1; i < this.tinySubpagePools.length; ++i) {
            head = this.tinySubpagePools[i];
            if (head.next == head) continue;
            buf.append(StringUtil.NEWLINE).append(i).append(": ");
            s = head.next;
            do {
                buf.append(s);
            } while ((s = s.next) != head);
        }
        buf.append(StringUtil.NEWLINE).append("small subpages:");
        for (i = 1; i < this.smallSubpagePools.length; ++i) {
            head = this.smallSubpagePools[i];
            if (head.next == head) continue;
            buf.append(StringUtil.NEWLINE).append(i).append(": ");
            s = head.next;
            do {
                buf.append(s);
            } while ((s = s.next) != head);
        }
        buf.append(StringUtil.NEWLINE);
        return buf.toString();
    }

    static final class DirectArena
    extends PoolArena<ByteBuffer> {
        private static final boolean HAS_UNSAFE = PlatformDependent.hasUnsafe();

        DirectArena(PooledByteBufAllocator parent, int pageSize, int maxOrder, int pageShifts, int chunkSize) {
            super(parent, pageSize, maxOrder, pageShifts, chunkSize);
        }

        @Override
        boolean isDirect() {
            return true;
        }

        @Override
        protected PoolChunk<ByteBuffer> newChunk(int pageSize, int maxOrder, int pageShifts, int chunkSize) {
            return new PoolChunk<ByteBuffer>(this, ByteBuffer.allocateDirect(chunkSize), pageSize, maxOrder, pageShifts, chunkSize);
        }

        @Override
        protected PoolChunk<ByteBuffer> newUnpooledChunk(int capacity) {
            return new PoolChunk<ByteBuffer>(this, ByteBuffer.allocateDirect(capacity), capacity);
        }

        @Override
        protected void destroyChunk(PoolChunk<ByteBuffer> chunk) {
            PlatformDependent.freeDirectBuffer((ByteBuffer)chunk.memory);
        }

        @Override
        protected PooledByteBuf<ByteBuffer> newByteBuf(int maxCapacity) {
            if (HAS_UNSAFE) {
                return PooledUnsafeDirectByteBuf.newInstance(maxCapacity);
            }
            return PooledDirectByteBuf.newInstance(maxCapacity);
        }

        @Override
        protected void memoryCopy(ByteBuffer src, int srcOffset, ByteBuffer dst, int dstOffset, int length) {
            if (length == 0) {
                return;
            }
            if (HAS_UNSAFE) {
                PlatformDependent.copyMemory(PlatformDependent.directBufferAddress(src) + (long)srcOffset, PlatformDependent.directBufferAddress(dst) + (long)dstOffset, length);
            } else {
                src = src.duplicate();
                dst = dst.duplicate();
                src.position(srcOffset).limit(srcOffset + length);
                dst.position(dstOffset);
                dst.put(src);
            }
        }
    }

    static final class HeapArena
    extends PoolArena<byte[]> {
        HeapArena(PooledByteBufAllocator parent, int pageSize, int maxOrder, int pageShifts, int chunkSize) {
            super(parent, pageSize, maxOrder, pageShifts, chunkSize);
        }

        @Override
        boolean isDirect() {
            return false;
        }

        @Override
        protected PoolChunk<byte[]> newChunk(int pageSize, int maxOrder, int pageShifts, int chunkSize) {
            return new PoolChunk<byte[]>(this, new byte[chunkSize], pageSize, maxOrder, pageShifts, chunkSize);
        }

        @Override
        protected PoolChunk<byte[]> newUnpooledChunk(int capacity) {
            return new PoolChunk<byte[]>(this, new byte[capacity], capacity);
        }

        @Override
        protected void destroyChunk(PoolChunk<byte[]> chunk) {
        }

        @Override
        protected PooledByteBuf<byte[]> newByteBuf(int maxCapacity) {
            return PooledHeapByteBuf.newInstance(maxCapacity);
        }

        @Override
        protected void memoryCopy(byte[] src, int srcOffset, byte[] dst, int dstOffset, int length) {
            if (length == 0) {
                return;
            }
            System.arraycopy(src, srcOffset, dst, dstOffset, length);
        }
    }

    static enum SizeClass {
        Tiny,
        Small,
        Normal;
        

        private SizeClass() {
        }
    }

}

