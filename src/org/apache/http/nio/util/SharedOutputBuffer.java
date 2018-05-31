/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.util;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.ByteBuffer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.http.annotation.ThreadSafe;
import org.apache.http.nio.ContentEncoder;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.util.ByteBufferAllocator;
import org.apache.http.nio.util.ContentOutputBuffer;
import org.apache.http.nio.util.ExpandableBuffer;
import org.apache.http.nio.util.HeapByteBufferAllocator;
import org.apache.http.util.Args;
import org.apache.http.util.Asserts;

@ThreadSafe
public class SharedOutputBuffer
extends ExpandableBuffer
implements ContentOutputBuffer {
    private final ReentrantLock lock;
    private final Condition condition;
    private volatile IOControl ioctrl;
    private volatile boolean shutdown = false;
    private volatile boolean endOfStream = false;

    @Deprecated
    public SharedOutputBuffer(int buffersize, IOControl ioctrl, ByteBufferAllocator allocator) {
        super(buffersize, allocator);
        Args.notNull(ioctrl, "I/O content control");
        this.ioctrl = ioctrl;
        this.lock = new ReentrantLock();
        this.condition = this.lock.newCondition();
    }

    public SharedOutputBuffer(int buffersize, ByteBufferAllocator allocator) {
        super(buffersize, allocator);
        this.lock = new ReentrantLock();
        this.condition = this.lock.newCondition();
    }

    public SharedOutputBuffer(int buffersize) {
        this(buffersize, HeapByteBufferAllocator.INSTANCE);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void reset() {
        if (this.shutdown) {
            return;
        }
        this.lock.lock();
        try {
            this.clear();
            this.endOfStream = false;
        }
        finally {
            this.lock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean hasData() {
        this.lock.lock();
        try {
            boolean bl = super.hasData();
            return bl;
        }
        finally {
            this.lock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public int available() {
        this.lock.lock();
        try {
            int n = super.available();
            return n;
        }
        finally {
            this.lock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public int capacity() {
        this.lock.lock();
        try {
            int n = super.capacity();
            return n;
        }
        finally {
            this.lock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public int length() {
        this.lock.lock();
        try {
            int n = super.length();
            return n;
        }
        finally {
            this.lock.unlock();
        }
    }

    @Deprecated
    public int produceContent(ContentEncoder encoder) throws IOException {
        return this.produceContent(encoder, null);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public int produceContent(ContentEncoder encoder, IOControl ioctrl) throws IOException {
        if (this.shutdown) {
            return -1;
        }
        this.lock.lock();
        try {
            if (ioctrl != null) {
                this.ioctrl = ioctrl;
            }
            this.setOutputMode();
            int bytesWritten = 0;
            if (super.hasData()) {
                bytesWritten = encoder.write(this.buffer);
                if (encoder.isCompleted()) {
                    this.endOfStream = true;
                }
            }
            if (!super.hasData()) {
                if (this.endOfStream && !encoder.isCompleted()) {
                    encoder.complete();
                }
                if (!this.endOfStream && this.ioctrl != null) {
                    this.ioctrl.suspendOutput();
                }
            }
            this.condition.signalAll();
            int n = bytesWritten;
            return n;
        }
        finally {
            this.lock.unlock();
        }
    }

    public void close() {
        this.shutdown();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void shutdown() {
        if (this.shutdown) {
            return;
        }
        this.shutdown = true;
        this.lock.lock();
        try {
            this.condition.signalAll();
        }
        finally {
            this.lock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void write(byte[] b, int off, int len) throws IOException {
        if (b == null) {
            return;
        }
        int pos = off;
        this.lock.lock();
        try {
            Asserts.check(!this.shutdown && !this.endOfStream, "Buffer already closed for writing");
            this.setInputMode();
            int remaining = len;
            while (remaining > 0) {
                if (!this.buffer.hasRemaining()) {
                    this.flushContent();
                    this.setInputMode();
                }
                int chunk = Math.min(remaining, this.buffer.remaining());
                this.buffer.put(b, pos, chunk);
                remaining -= chunk;
                pos += chunk;
            }
        }
        finally {
            this.lock.unlock();
        }
    }

    public void write(byte[] b) throws IOException {
        if (b == null) {
            return;
        }
        this.write(b, 0, b.length);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void write(int b) throws IOException {
        this.lock.lock();
        try {
            Asserts.check(!this.shutdown && !this.endOfStream, "Buffer already closed for writing");
            this.setInputMode();
            if (!this.buffer.hasRemaining()) {
                this.flushContent();
                this.setInputMode();
            }
            this.buffer.put((byte)b);
        }
        finally {
            this.lock.unlock();
        }
    }

    public void flush() throws IOException {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void flushContent() throws IOException {
        this.lock.lock();
        try {
            try {
                while (super.hasData()) {
                    if (this.shutdown) {
                        throw new InterruptedIOException("Output operation aborted");
                    }
                    if (this.ioctrl != null) {
                        this.ioctrl.requestOutput();
                    }
                    this.condition.await();
                }
            }
            catch (InterruptedException ex) {
                throw new IOException("Interrupted while flushing the content buffer");
            }
        }
        finally {
            this.lock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void writeCompleted() throws IOException {
        this.lock.lock();
        try {
            if (this.endOfStream) {
                return;
            }
            this.endOfStream = true;
            if (this.ioctrl != null) {
                this.ioctrl.requestOutput();
            }
        }
        finally {
            this.lock.unlock();
        }
    }
}

