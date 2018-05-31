/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.impl.nio.codecs;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.impl.io.HttpTransportMetricsImpl;
import org.apache.http.nio.ContentEncoder;
import org.apache.http.nio.reactor.SessionOutputBuffer;
import org.apache.http.util.Args;
import org.apache.http.util.Asserts;

@NotThreadSafe
public abstract class AbstractContentEncoder
implements ContentEncoder {
    protected final WritableByteChannel channel;
    protected final SessionOutputBuffer buffer;
    protected final HttpTransportMetricsImpl metrics;
    protected boolean completed;

    public AbstractContentEncoder(WritableByteChannel channel, SessionOutputBuffer buffer, HttpTransportMetricsImpl metrics) {
        Args.notNull(channel, "Channel");
        Args.notNull(buffer, "Session input buffer");
        Args.notNull(metrics, "Transport metrics");
        this.buffer = buffer;
        this.channel = channel;
        this.metrics = metrics;
    }

    public boolean isCompleted() {
        return this.completed;
    }

    public void complete() throws IOException {
        this.completed = true;
    }

    protected void assertNotCompleted() {
        Asserts.check(!this.completed, "Encoding process already completed");
    }

    protected int flushToChannel() throws IOException {
        if (!this.buffer.hasData()) {
            return 0;
        }
        int bytesWritten = this.buffer.flush(this.channel);
        if (bytesWritten > 0) {
            this.metrics.incrementBytesTransferred(bytesWritten);
        }
        return bytesWritten;
    }

    protected int writeToChannel(ByteBuffer src) throws IOException {
        if (!src.hasRemaining()) {
            return 0;
        }
        int bytesWritten = this.channel.write(src);
        if (bytesWritten > 0) {
            this.metrics.incrementBytesTransferred(bytesWritten);
        }
        return bytesWritten;
    }

    protected int writeToChannel(ByteBuffer src, int limit) throws IOException {
        return this.doWriteChunk(src, limit, true);
    }

    protected int writeToBuffer(ByteBuffer src, int limit) throws IOException {
        return this.doWriteChunk(src, limit, false);
    }

    private int doWriteChunk(ByteBuffer src, int chunk, boolean direct) throws IOException {
        int bytesWritten;
        if (src.remaining() > chunk) {
            int oldLimit = src.limit();
            int newLimit = oldLimit - (src.remaining() - chunk);
            src.limit(newLimit);
            bytesWritten = this.doWriteChunk(src, direct);
            src.limit(oldLimit);
        } else {
            bytesWritten = this.doWriteChunk(src, direct);
        }
        return bytesWritten;
    }

    private int doWriteChunk(ByteBuffer src, boolean direct) throws IOException {
        if (direct) {
            int bytesWritten = this.channel.write(src);
            if (bytesWritten > 0) {
                this.metrics.incrementBytesTransferred(bytesWritten);
            }
            return bytesWritten;
        }
        int chunk = src.remaining();
        this.buffer.write(src);
        return chunk;
    }
}

