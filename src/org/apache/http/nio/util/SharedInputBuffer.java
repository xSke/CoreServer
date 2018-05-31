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
import org.apache.http.nio.ContentDecoder;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.util.ByteBufferAllocator;
import org.apache.http.nio.util.ContentInputBuffer;
import org.apache.http.nio.util.ExpandableBuffer;
import org.apache.http.nio.util.HeapByteBufferAllocator;

@ThreadSafe
public class SharedInputBuffer
extends ExpandableBuffer
implements ContentInputBuffer {
    private final ReentrantLock lock;
    private final Condition condition;
    private volatile IOControl ioctrl;
    private volatile boolean shutdown = false;
    private volatile boolean endOfStream = false;

    @Deprecated
    public SharedInputBuffer(int buffersize, IOControl ioctrl, ByteBufferAllocator allocator) {
        super(buffersize, allocator);
        this.ioctrl = ioctrl;
        this.lock = new ReentrantLock();
        this.condition = this.lock.newCondition();
    }

    public SharedInputBuffer(int buffersize, ByteBufferAllocator allocator) {
        super(buffersize, allocator);
        this.lock = new ReentrantLock();
        this.condition = this.lock.newCondition();
    }

    public SharedInputBuffer(int buffersize) {
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

    @Deprecated
    public int consumeContent(ContentDecoder decoder) throws IOException {
        return this.consumeContent(decoder, null);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public int consumeContent(ContentDecoder decoder, IOControl ioctrl) throws IOException {
        if (this.shutdown) {
            return -1;
        }
        this.lock.lock();
        try {
            int bytesRead;
            if (ioctrl != null) {
                this.ioctrl = ioctrl;
            }
            this.setInputMode();
            int totalRead = 0;
            while ((bytesRead = decoder.read(this.buffer)) > 0) {
                totalRead += bytesRead;
            }
            if (bytesRead == -1 || decoder.isCompleted()) {
                this.endOfStream = true;
            }
            if (!this.buffer.hasRemaining() && this.ioctrl != null) {
                this.ioctrl.suspendInput();
            }
            this.condition.signalAll();
            if (totalRead > 0) {
                int n = totalRead;
                return n;
            }
            if (this.endOfStream) {
                int n = -1;
                return n;
            }
            int n = 0;
            return n;
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

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void waitForData() throws IOException {
        this.lock.lock();
        try {
            try {
                while (!super.hasData() && !this.endOfStream) {
                    if (this.shutdown) {
                        throw new InterruptedIOException("Input operation aborted");
                    }
                    if (this.ioctrl != null) {
                        this.ioctrl.requestInput();
                    }
                    this.condition.await();
                }
            }
            catch (InterruptedException ex) {
                throw new IOException("Interrupted while waiting for more data");
            }
        }
        finally {
            this.lock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void close() {
        if (this.shutdown) {
            return;
        }
        this.endOfStream = true;
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

    protected boolean isShutdown() {
        return this.shutdown;
    }

    protected boolean isEndOfStream() {
        return this.shutdown || !this.hasData() && this.endOfStream;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public int read() throws IOException {
        if (this.shutdown) {
            return -1;
        }
        this.lock.lock();
        try {
            if (!this.hasData()) {
                this.waitForData();
            }
            if (this.isEndOfStream()) {
                int n = -1;
                return n;
            }
            int n = this.buffer.get() & 255;
            return n;
        }
        finally {
            this.lock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public int read(byte[] b, int off, int len) throws IOException {
        if (this.shutdown) {
            return -1;
        }
        if (b == null) {
            return 0;
        }
        this.lock.lock();
        try {
            if (!this.hasData()) {
                this.waitForData();
            }
            if (this.isEndOfStream()) {
                int n = -1;
                return n;
            }
            this.setOutputMode();
            int chunk = len;
            if (chunk > this.buffer.remaining()) {
                chunk = this.buffer.remaining();
            }
            this.buffer.get(b, off, chunk);
            int n = chunk;
            return n;
        }
        finally {
            this.lock.unlock();
        }
    }

    public int read(byte[] b) throws IOException {
        if (this.shutdown) {
            return -1;
        }
        if (b == null) {
            return 0;
        }
        return this.read(b, 0, b.length);
    }
}

