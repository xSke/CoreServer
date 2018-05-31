/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.util;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.nio.util.BufferInfo;
import org.apache.http.nio.util.ByteBufferAllocator;
import org.apache.http.util.Args;

@NotThreadSafe
public class ExpandableBuffer
implements org.apache.http.io.BufferInfo,
BufferInfo {
    public static final int INPUT_MODE = 0;
    public static final int OUTPUT_MODE = 1;
    private final ByteBufferAllocator allocator;
    private int mode;
    protected ByteBuffer buffer = null;

    public ExpandableBuffer(int buffersize, ByteBufferAllocator allocator) {
        Args.notNull(allocator, "ByteBuffer allocator");
        this.allocator = allocator;
        this.buffer = allocator.allocate(buffersize);
        this.mode = 0;
    }

    protected int getMode() {
        return this.mode;
    }

    protected void setOutputMode() {
        if (this.mode != 1) {
            this.buffer.flip();
            this.mode = 1;
        }
    }

    protected void setInputMode() {
        if (this.mode != 0) {
            if (this.buffer.hasRemaining()) {
                this.buffer.compact();
            } else {
                this.buffer.clear();
            }
            this.mode = 0;
        }
    }

    private void expandCapacity(int capacity) {
        ByteBuffer oldbuffer = this.buffer;
        this.buffer = this.allocator.allocate(capacity);
        oldbuffer.flip();
        this.buffer.put(oldbuffer);
    }

    protected void expand() {
        int newcapacity = this.buffer.capacity() + 1 << 1;
        if (newcapacity < 0) {
            newcapacity = Integer.MAX_VALUE;
        }
        this.expandCapacity(newcapacity);
    }

    protected void ensureCapacity(int requiredCapacity) {
        if (requiredCapacity > this.buffer.capacity()) {
            this.expandCapacity(requiredCapacity);
        }
    }

    public int capacity() {
        return this.buffer.capacity();
    }

    public boolean hasData() {
        this.setOutputMode();
        return this.buffer.hasRemaining();
    }

    public int length() {
        this.setOutputMode();
        return this.buffer.remaining();
    }

    public int available() {
        this.setInputMode();
        return this.buffer.remaining();
    }

    protected void clear() {
        this.buffer.clear();
        this.mode = 0;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[mode=");
        if (this.getMode() == 0) {
            sb.append("in");
        } else {
            sb.append("out");
        }
        sb.append(" pos=");
        sb.append(this.buffer.position());
        sb.append(" lim=");
        sb.append(this.buffer.limit());
        sb.append(" cap=");
        sb.append(this.buffer.capacity());
        sb.append("]");
        return sb.toString();
    }
}

