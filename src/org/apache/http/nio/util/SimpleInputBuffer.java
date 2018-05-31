/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.util;

import java.io.IOException;
import java.nio.ByteBuffer;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.nio.ContentDecoder;
import org.apache.http.nio.util.ByteBufferAllocator;
import org.apache.http.nio.util.ContentInputBuffer;
import org.apache.http.nio.util.ExpandableBuffer;
import org.apache.http.nio.util.HeapByteBufferAllocator;

@NotThreadSafe
public class SimpleInputBuffer
extends ExpandableBuffer
implements ContentInputBuffer {
    private boolean endOfStream = false;

    public SimpleInputBuffer(int buffersize, ByteBufferAllocator allocator) {
        super(buffersize, allocator);
    }

    public SimpleInputBuffer(int buffersize) {
        this(buffersize, HeapByteBufferAllocator.INSTANCE);
    }

    public void reset() {
        this.endOfStream = false;
        super.clear();
    }

    public int consumeContent(ContentDecoder decoder) throws IOException {
        int bytesRead;
        this.setInputMode();
        int totalRead = 0;
        while ((bytesRead = decoder.read(this.buffer)) != -1) {
            if (bytesRead == 0) {
                if (this.buffer.hasRemaining()) break;
                this.expand();
                continue;
            }
            totalRead += bytesRead;
        }
        if (bytesRead == -1 || decoder.isCompleted()) {
            this.endOfStream = true;
        }
        return totalRead;
    }

    public boolean isEndOfStream() {
        return !this.hasData() && this.endOfStream;
    }

    public int read() throws IOException {
        if (this.isEndOfStream()) {
            return -1;
        }
        this.setOutputMode();
        return this.buffer.get() & 255;
    }

    public int read(byte[] b, int off, int len) throws IOException {
        if (this.isEndOfStream()) {
            return -1;
        }
        if (b == null) {
            return 0;
        }
        this.setOutputMode();
        int chunk = len;
        if (chunk > this.buffer.remaining()) {
            chunk = this.buffer.remaining();
        }
        this.buffer.get(b, off, chunk);
        return chunk;
    }

    public int read(byte[] b) throws IOException {
        if (this.isEndOfStream()) {
            return -1;
        }
        if (b == null) {
            return 0;
        }
        return this.read(b, 0, b.length);
    }

    public void shutdown() {
        this.endOfStream = true;
    }
}

