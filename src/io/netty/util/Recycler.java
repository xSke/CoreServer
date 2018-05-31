/*
 * Decompiled with CFR 0_129.
 */
package io.netty.util;

import io.netty.util.concurrent.FastThreadLocal;
import io.netty.util.internal.SystemPropertyUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class Recycler<T> {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(Recycler.class);
    private static final AtomicInteger ID_GENERATOR = new AtomicInteger(Integer.MIN_VALUE);
    private static final int OWN_THREAD_ID = ID_GENERATOR.getAndIncrement();
    private static final int DEFAULT_MAX_CAPACITY;
    private static final int INITIAL_CAPACITY;
    private final int maxCapacity;
    private final FastThreadLocal<Stack<T>> threadLocal = new FastThreadLocal<Stack<T>>(){

        @Override
        protected Stack<T> initialValue() {
            return new Stack(Recycler.this, Thread.currentThread(), Recycler.this.maxCapacity);
        }
    };
    private static final FastThreadLocal<Map<Stack<?>, WeakOrderQueue>> DELAYED_RECYCLED;

    protected Recycler() {
        this(DEFAULT_MAX_CAPACITY);
    }

    protected Recycler(int maxCapacity) {
        this.maxCapacity = Math.max(0, maxCapacity);
    }

    public final T get() {
        Stack<T> stack = this.threadLocal.get();
        DefaultHandle handle = stack.pop();
        if (handle == null) {
            handle = stack.newHandle();
            handle.value = this.newObject(handle);
        }
        return (T)handle.value;
    }

    public final boolean recycle(T o, Handle handle) {
        DefaultHandle h = (DefaultHandle)handle;
        if (DefaultHandle.access$200((DefaultHandle)h).parent != this) {
            return false;
        }
        if (o != h.value) {
            throw new IllegalArgumentException("o does not belong to handle");
        }
        h.recycle();
        return true;
    }

    protected abstract T newObject(Handle var1);

    final int threadLocalCapacity() {
        return this.threadLocal.get().elements.length;
    }

    final int threadLocalSize() {
        return this.threadLocal.get().size;
    }

    static /* synthetic */ AtomicInteger access$600() {
        return ID_GENERATOR;
    }

    static {
        int maxCapacity = SystemPropertyUtil.getInt("io.netty.recycler.maxCapacity.default", 0);
        if (maxCapacity <= 0) {
            maxCapacity = 262144;
        }
        DEFAULT_MAX_CAPACITY = maxCapacity;
        if (logger.isDebugEnabled()) {
            logger.debug("-Dio.netty.recycler.maxCapacity.default: {}", (Object)DEFAULT_MAX_CAPACITY);
        }
        INITIAL_CAPACITY = Math.min(DEFAULT_MAX_CAPACITY, 256);
        DELAYED_RECYCLED = new FastThreadLocal<Map<Stack<?>, WeakOrderQueue>>(){

            @Override
            protected Map<Stack<?>, WeakOrderQueue> initialValue() {
                return new WeakHashMap();
            }
        };
    }

    static final class Stack<T> {
        final Recycler<T> parent;
        final Thread thread;
        private DefaultHandle[] elements;
        private final int maxCapacity;
        private int size;
        private volatile WeakOrderQueue head;
        private WeakOrderQueue cursor;
        private WeakOrderQueue prev;

        Stack(Recycler<T> parent, Thread thread, int maxCapacity) {
            this.parent = parent;
            this.thread = thread;
            this.maxCapacity = maxCapacity;
            this.elements = new DefaultHandle[Math.min(INITIAL_CAPACITY, maxCapacity)];
        }

        int increaseCapacity(int expectedCapacity) {
            int newCapacity = this.elements.length;
            int maxCapacity = this.maxCapacity;
            while ((newCapacity <<= 1) < expectedCapacity && newCapacity < maxCapacity) {
            }
            if ((newCapacity = Math.min(newCapacity, maxCapacity)) != this.elements.length) {
                this.elements = Arrays.copyOf(this.elements, newCapacity);
            }
            return newCapacity;
        }

        DefaultHandle pop() {
            DefaultHandle ret;
            int size = this.size;
            if (size == 0) {
                if (!this.scavenge()) {
                    return null;
                }
                size = this.size;
            }
            if ((ret = this.elements[--size]).lastRecycledId != ret.recycleId) {
                throw new IllegalStateException("recycled multiple times");
            }
            ret.recycleId = 0;
            ret.lastRecycledId = 0;
            this.size = size;
            return ret;
        }

        boolean scavenge() {
            if (this.scavengeSome()) {
                return true;
            }
            this.prev = null;
            this.cursor = this.head;
            return false;
        }

        boolean scavengeSome() {
            WeakOrderQueue next;
            WeakOrderQueue cursor = this.cursor;
            if (cursor == null && (cursor = this.head) == null) {
                return false;
            }
            boolean success = false;
            WeakOrderQueue prev = this.prev;
            do {
                if (cursor.transfer(this)) {
                    success = true;
                    break;
                }
                next = cursor.next;
                if (cursor.owner.get() == null) {
                    if (cursor.hasFinalData()) {
                        while (cursor.transfer(this)) {
                            success = true;
                        }
                    }
                    if (prev == null) continue;
                    prev.next = next;
                    continue;
                }
                prev = cursor;
            } while ((cursor = next) != null && !success);
            this.prev = prev;
            this.cursor = cursor;
            return success;
        }

        void push(DefaultHandle item) {
            if ((item.recycleId | item.lastRecycledId) != 0) {
                throw new IllegalStateException("recycled already");
            }
            item.recycleId = (item.lastRecycledId = OWN_THREAD_ID);
            int size = this.size;
            if (size >= this.maxCapacity) {
                return;
            }
            if (size == this.elements.length) {
                this.elements = Arrays.copyOf(this.elements, Math.min(size << 1, this.maxCapacity));
            }
            this.elements[size] = item;
            this.size = size + 1;
        }

        DefaultHandle newHandle() {
            return new DefaultHandle(this);
        }
    }

    private static final class WeakOrderQueue {
        private static final int LINK_CAPACITY = 16;
        private Link head = this.tail = new Link();
        private Link tail;
        private WeakOrderQueue next;
        private final WeakReference<Thread> owner;
        private final int id = Recycler.access$600().getAndIncrement();

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        WeakOrderQueue(Stack<?> stack, Thread thread) {
            this.owner = new WeakReference<Thread>(thread);
            Stack stack2 = stack;
            synchronized (stack2) {
                this.next = stack.head;
                stack.head = this;
            }
        }

        void add(DefaultHandle handle) {
            handle.lastRecycledId = this.id;
            Link tail = this.tail;
            int writeIndex = tail.get();
            if (writeIndex == 16) {
                this.tail = tail = (tail.next = new Link());
                writeIndex = tail.get();
            }
            Link.access$1100((Link)tail)[writeIndex] = handle;
            handle.stack = null;
            tail.lazySet(writeIndex + 1);
        }

        boolean hasFinalData() {
            return this.tail.readIndex != this.tail.get();
        }

        boolean transfer(Stack<?> dst) {
            Link head = this.head;
            if (head == null) {
                return false;
            }
            if (head.readIndex == 16) {
                if (head.next == null) {
                    return false;
                }
                this.head = head = head.next;
            }
            int srcStart = head.readIndex;
            int srcEnd = head.get();
            int srcSize = srcEnd - srcStart;
            if (srcSize == 0) {
                return false;
            }
            int dstSize = dst.size;
            int expectedCapacity = dstSize + srcSize;
            if (expectedCapacity > dst.elements.length) {
                int actualCapacity = dst.increaseCapacity(expectedCapacity);
                srcEnd = Math.min(srcStart + actualCapacity - dstSize, srcEnd);
            }
            if (srcStart != srcEnd) {
                DefaultHandle[] srcElems = head.elements;
                DefaultHandle[] dstElems = dst.elements;
                int newDstSize = dstSize;
                for (int i = srcStart; i < srcEnd; ++i) {
                    DefaultHandle element = srcElems[i];
                    if (element.recycleId == 0) {
                        element.recycleId = element.lastRecycledId;
                    } else if (element.recycleId != element.lastRecycledId) {
                        throw new IllegalStateException("recycled already");
                    }
                    element.stack = dst;
                    dstElems[newDstSize++] = element;
                    srcElems[i] = null;
                }
                dst.size = newDstSize;
                if (srcEnd == 16 && head.next != null) {
                    this.head = head.next;
                }
                head.readIndex = srcEnd;
                return true;
            }
            return false;
        }

        private static final class Link
        extends AtomicInteger {
            private final DefaultHandle[] elements = new DefaultHandle[16];
            private int readIndex;
            private Link next;

            private Link() {
            }
        }

    }

    static final class DefaultHandle
    implements Handle {
        private int lastRecycledId;
        private int recycleId;
        private Stack<?> stack;
        private Object value;

        DefaultHandle(Stack<?> stack) {
            this.stack = stack;
        }

        public void recycle() {
            Thread thread = Thread.currentThread();
            if (thread == this.stack.thread) {
                this.stack.push(this);
                return;
            }
            Map delayedRecycled = (Map)DELAYED_RECYCLED.get();
            WeakOrderQueue queue = (WeakOrderQueue)delayedRecycled.get(this.stack);
            if (queue == null) {
                queue = new WeakOrderQueue(this.stack, thread);
                delayedRecycled.put(this.stack, queue);
            }
            queue.add(this);
        }

        static /* synthetic */ Stack access$200(DefaultHandle x0) {
            return x0.stack;
        }
    }

    public static interface Handle {
    }

}

