/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.impl.nio.codecs;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.impl.io.HttpTransportMetricsImpl;
import org.apache.http.impl.nio.codecs.AbstractContentEncoder;
import org.apache.http.nio.FileContentEncoder;
import org.apache.http.nio.reactor.SessionOutputBuffer;
import org.apache.http.util.Args;

@NotThreadSafe
public class LengthDelimitedEncoder
extends AbstractContentEncoder
implements FileContentEncoder {
    private final long contentLength;
    private final int fragHint;
    private long remaining;

    public LengthDelimitedEncoder(WritableByteChannel channel, SessionOutputBuffer buffer, HttpTransportMetricsImpl metrics, long contentLength, int fragementSizeHint) {
        super(channel, buffer, metrics);
        Args.notNegative(contentLength, "Content length");
        this.contentLength = contentLength;
        this.fragHint = fragementSizeHint > 0 ? fragementSizeHint : 0;
        this.remaining = contentLength;
    }

    public LengthDelimitedEncoder(WritableByteChannel channel, SessionOutputBuffer buffer, HttpTransportMetricsImpl metrics, long contentLength) {
        this(channel, buffer, metrics, contentLength, 0);
    }

    private int nextChunk(ByteBuffer src) {
        return (int)Math.min(Math.min(this.remaining, Integer.MAX_VALUE), (long)src.remaining());
    }

    public int write(ByteBuffer src) throws IOException {
        if (src == null) {
            return 0;
        }
        this.assertNotCompleted();
        int total = 0;
        while (src.hasRemaining() && this.remaining > 0L) {
            int bytesWritten;
            int chunk;
            int capacity;
            if ((this.buffer.hasData() || this.fragHint > 0) && (chunk = this.nextChunk(src)) <= this.fragHint && (capacity = this.fragHint - this.buffer.length()) > 0) {
                int limit = Math.min(capacity, chunk);
                int bytesWritten2 = this.writeToBuffer(src, limit);
                this.remaining -= (long)bytesWritten2;
                total += bytesWritten2;
            }
            if (this.buffer.hasData()) {
                chunk = this.nextChunk(src);
                if ((this.buffer.length() >= this.fragHint || chunk > 0) && (bytesWritten = this.flushToChannel()) == 0) break;
            }
            if (this.buffer.hasData() || (chunk = this.nextChunk(src)) <= this.fragHint) continue;
            bytesWritten = this.writeToChannel(src, chunk);
            this.remaining -= (long)bytesWritten;
            total += bytesWritten;
            if (bytesWritten != 0) continue;
            break;
        }
        if (this.remaining <= 0L) {
            super.complete();
        }
        return total;
    }

    public long transfer(FileChannel src, long position, long count) throws IOException {
        if (src == null) {
            return 0L;
        }
        this.assertNotCompleted();
        this.flushToChannel();
        if (this.buffer.hasData()) {
            return 0L;
        }
        long chunk = Math.min(this.remaining, count);
        long bytesWritten = src.transferTo(position, chunk, this.channel);
        if (bytesWritten > 0L) {
            this.metrics.incrementBytesTransferred(bytesWritten);
        }
        this.remaining -= bytesWritten;
        if (this.remaining <= 0L) {
            super.complete();
        }
        return bytesWritten;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[content length: ");
        sb.append(this.contentLength);
        sb.append("; pos: ");
        sb.append(this.contentLength - this.remaining);
        sb.append("; completed: ");
        sb.append(this.isCompleted());
        sb.append("]");
        return sb.toString();
    }
}

