/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.entity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import org.apache.http.HttpEntity;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.nio.ContentEncoder;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.entity.ProducingNHttpEntity;

@Deprecated
@NotThreadSafe
public class NHttpEntityWrapper
extends HttpEntityWrapper
implements ProducingNHttpEntity {
    private final ReadableByteChannel channel;
    private final ByteBuffer buffer;

    public NHttpEntityWrapper(HttpEntity httpEntity) throws IOException {
        super(httpEntity);
        this.channel = Channels.newChannel(httpEntity.getContent());
        this.buffer = ByteBuffer.allocate(4096);
    }

    public InputStream getContent() throws IOException, UnsupportedOperationException {
        throw new UnsupportedOperationException("Does not support blocking methods");
    }

    public boolean isStreaming() {
        return true;
    }

    public void writeTo(OutputStream out) throws IOException, UnsupportedOperationException {
        throw new UnsupportedOperationException("Does not support blocking methods");
    }

    public void produceContent(ContentEncoder encoder, IOControl ioctrl) throws IOException {
        int i = this.channel.read(this.buffer);
        this.buffer.flip();
        encoder.write(this.buffer);
        boolean buffering = this.buffer.hasRemaining();
        this.buffer.compact();
        if (i == -1 && !buffering) {
            encoder.complete();
            this.channel.close();
        }
    }

    public void finish() {
        try {
            this.channel.close();
        }
        catch (IOException ignore) {
            // empty catch block
        }
    }
}

