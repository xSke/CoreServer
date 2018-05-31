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
import org.apache.http.impl.nio.codecs.AbstractContentEncoder;
import org.apache.http.io.BufferInfo;
import org.apache.http.nio.reactor.SessionOutputBuffer;
import org.apache.http.util.CharArrayBuffer;

@NotThreadSafe
public class ChunkEncoder
extends AbstractContentEncoder {
    private final int fragHint;
    private final CharArrayBuffer lineBuffer;
    private final BufferInfo bufferinfo;

    public ChunkEncoder(WritableByteChannel channel, SessionOutputBuffer buffer, HttpTransportMetricsImpl metrics, int fragementSizeHint) {
        super(channel, buffer, metrics);
        this.fragHint = fragementSizeHint > 0 ? fragementSizeHint : 0;
        this.lineBuffer = new CharArrayBuffer(16);
        this.bufferinfo = buffer instanceof BufferInfo ? (BufferInfo)((Object)buffer) : null;
    }

    public ChunkEncoder(WritableByteChannel channel, SessionOutputBuffer buffer, HttpTransportMetricsImpl metrics) {
        this(channel, buffer, metrics, 0);
    }

    public int write(ByteBuffer src) throws IOException {
        if (src == null) {
            return 0;
        }
        this.assertNotCompleted();
        int total = 0;
        while (src.hasRemaining()) {
            int bytesWritten;
            int chunk = src.remaining();
            int avail = this.bufferinfo != null ? this.bufferinfo.available() : 4096;
            if ((avail -= 12) > 0) {
                if (avail < chunk) {
                    chunk = avail;
                    this.lineBuffer.clear();
                    this.lineBuffer.append(Integer.toHexString(chunk));
                    this.buffer.writeLine(this.lineBuffer);
                    int oldlimit = src.limit();
                    src.limit(src.position() + chunk);
                    this.buffer.write(src);
                    src.limit(oldlimit);
                } else {
                    this.lineBuffer.clear();
                    this.lineBuffer.append(Integer.toHexString(chunk));
                    this.buffer.writeLine(this.lineBuffer);
                    this.buffer.write(src);
                }
                this.lineBuffer.clear();
                this.buffer.writeLine(this.lineBuffer);
                total += chunk;
            }
            if (this.buffer.length() < this.fragHint && !src.hasRemaining() || (bytesWritten = this.flushToChannel()) != 0) continue;
            break;
        }
        return total;
    }

    public void complete() throws IOException {
        this.assertNotCompleted();
        this.lineBuffer.clear();
        this.lineBuffer.append("0");
        this.buffer.writeLine(this.lineBuffer);
        this.lineBuffer.clear();
        this.buffer.writeLine(this.lineBuffer);
        super.complete();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[chunk-coded; completed: ");
        sb.append(this.isCompleted());
        sb.append("]");
        return sb.toString();
    }
}

