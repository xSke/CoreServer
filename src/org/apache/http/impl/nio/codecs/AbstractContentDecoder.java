/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.impl.nio.codecs;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.impl.io.HttpTransportMetricsImpl;
import org.apache.http.nio.ContentDecoder;
import org.apache.http.nio.reactor.SessionInputBuffer;
import org.apache.http.util.Args;

@NotThreadSafe
public abstract class AbstractContentDecoder
implements ContentDecoder {
    protected final ReadableByteChannel channel;
    protected final SessionInputBuffer buffer;
    protected final HttpTransportMetricsImpl metrics;
    protected boolean completed;

    public AbstractContentDecoder(ReadableByteChannel channel, SessionInputBuffer buffer, HttpTransportMetricsImpl metrics) {
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

    protected int readFromChannel(ByteBuffer dst) throws IOException {
        int bytesRead = this.channel.read(dst);
        if (bytesRead > 0) {
            this.metrics.incrementBytesTransferred(bytesRead);
        }
        return bytesRead;
    }

    protected int fillBufferFromChannel() throws IOException {
        int bytesRead = this.buffer.fill(this.channel);
        if (bytesRead > 0) {
            this.metrics.incrementBytesTransferred(bytesRead);
        }
        return bytesRead;
    }

    protected int readFromChannel(ByteBuffer dst, int limit) throws IOException {
        int bytesRead;
        if (dst.remaining() > limit) {
            int oldLimit = dst.limit();
            int newLimit = oldLimit - (dst.remaining() - limit);
            dst.limit(newLimit);
            bytesRead = this.channel.read(dst);
            dst.limit(oldLimit);
        } else {
            bytesRead = this.channel.read(dst);
        }
        if (bytesRead > 0) {
            this.metrics.incrementBytesTransferred(bytesRead);
        }
        return bytesRead;
    }
}

