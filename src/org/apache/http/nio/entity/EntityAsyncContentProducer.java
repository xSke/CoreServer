/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.entity;

import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import org.apache.http.HttpEntity;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.nio.ContentEncoder;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.entity.HttpAsyncContentProducer;
import org.apache.http.util.Args;

@NotThreadSafe
public class EntityAsyncContentProducer
implements HttpAsyncContentProducer {
    private final HttpEntity entity;
    private final ByteBuffer buffer;
    private ReadableByteChannel channel;

    public EntityAsyncContentProducer(HttpEntity entity) {
        Args.notNull(entity, "HTTP entity");
        this.entity = entity;
        this.buffer = ByteBuffer.allocate(4096);
    }

    public void produceContent(ContentEncoder encoder, IOControl ioctrl) throws IOException {
        if (this.channel == null) {
            this.channel = Channels.newChannel(this.entity.getContent());
        }
        int i = this.channel.read(this.buffer);
        this.buffer.flip();
        encoder.write(this.buffer);
        boolean buffering = this.buffer.hasRemaining();
        this.buffer.compact();
        if (i == -1 && !buffering) {
            encoder.complete();
            this.close();
        }
    }

    public boolean isRepeatable() {
        return this.entity.isRepeatable();
    }

    public void close() throws IOException {
        ReadableByteChannel local = this.channel;
        this.channel = null;
        if (local != null) {
            local.close();
        }
        if (this.entity.isStreaming()) {
            InputStream instream = this.entity.getContent();
            instream.close();
        }
    }
}

