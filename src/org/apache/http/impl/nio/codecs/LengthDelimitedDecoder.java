/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.impl.nio.codecs;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import org.apache.http.ConnectionClosedException;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.impl.io.HttpTransportMetricsImpl;
import org.apache.http.impl.nio.codecs.AbstractContentDecoder;
import org.apache.http.nio.FileContentDecoder;
import org.apache.http.nio.reactor.SessionInputBuffer;
import org.apache.http.util.Args;

@NotThreadSafe
public class LengthDelimitedDecoder
extends AbstractContentDecoder
implements FileContentDecoder {
    private final long contentLength;
    private long len;

    public LengthDelimitedDecoder(ReadableByteChannel channel, SessionInputBuffer buffer, HttpTransportMetricsImpl metrics, long contentLength) {
        super(channel, buffer, metrics);
        Args.notNegative(contentLength, "Content length");
        this.contentLength = contentLength;
    }

    public int read(ByteBuffer dst) throws IOException {
        int bytesRead;
        Args.notNull(dst, "Byte buffer");
        if (this.completed) {
            return -1;
        }
        int chunk = (int)Math.min(this.contentLength - this.len, Integer.MAX_VALUE);
        if (this.buffer.hasData()) {
            int maxLen = Math.min(chunk, this.buffer.length());
            bytesRead = this.buffer.read(dst, maxLen);
        } else {
            bytesRead = this.readFromChannel(dst, chunk);
        }
        if (bytesRead == -1) {
            this.completed = true;
            if (this.len < this.contentLength) {
                throw new ConnectionClosedException("Premature end of Content-Length delimited message body (expected: " + this.contentLength + "; received: " + this.len);
            }
        }
        this.len += (long)bytesRead;
        if (this.len >= this.contentLength) {
            this.completed = true;
        }
        if (this.completed && bytesRead == 0) {
            return -1;
        }
        return bytesRead;
    }

    public long transfer(FileChannel dst, long position, long count) throws IOException {
        long bytesRead;
        if (dst == null) {
            return 0L;
        }
        if (this.completed) {
            return -1L;
        }
        int chunk = (int)Math.min(this.contentLength - this.len, Integer.MAX_VALUE);
        if (this.buffer.hasData()) {
            int maxLen = Math.min(chunk, this.buffer.length());
            dst.position(position);
            bytesRead = this.buffer.read(dst, maxLen);
        } else {
            if (this.channel.isOpen()) {
                if (position > dst.size()) {
                    throw new IOException("Position past end of file [" + position + " > " + dst.size() + "]");
                }
                bytesRead = dst.transferFrom(this.channel, position, count < (long)chunk ? count : (long)chunk);
            } else {
                bytesRead = -1L;
            }
            if (bytesRead > 0L) {
                this.metrics.incrementBytesTransferred(bytesRead);
            }
        }
        if (bytesRead == -1L) {
            this.completed = true;
            if (this.len < this.contentLength) {
                throw new ConnectionClosedException("Premature end of Content-Length delimited message body (expected: " + this.contentLength + "; received: " + this.len);
            }
        }
        this.len += bytesRead;
        if (this.len >= this.contentLength) {
            this.completed = true;
        }
        return bytesRead;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[content length: ");
        sb.append(this.contentLength);
        sb.append("; pos: ");
        sb.append(this.len);
        sb.append("; completed: ");
        sb.append(this.completed);
        sb.append("]");
        return sb.toString();
    }
}

