/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.entity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.http.HttpEntity;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.nio.ContentDecoder;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.entity.ConsumingNHttpEntity;
import org.apache.http.nio.entity.ContentInputStream;
import org.apache.http.nio.util.ByteBufferAllocator;
import org.apache.http.nio.util.ContentInputBuffer;
import org.apache.http.nio.util.SimpleInputBuffer;
import org.apache.http.util.Args;
import org.apache.http.util.Asserts;

@Deprecated
@NotThreadSafe
public class BufferingNHttpEntity
extends HttpEntityWrapper
implements ConsumingNHttpEntity {
    private static final int BUFFER_SIZE = 2048;
    private final SimpleInputBuffer buffer;
    private boolean finished;
    private boolean consumed;

    public BufferingNHttpEntity(HttpEntity httpEntity, ByteBufferAllocator allocator) {
        super(httpEntity);
        this.buffer = new SimpleInputBuffer(2048, allocator);
    }

    public void consumeContent(ContentDecoder decoder, IOControl ioctrl) throws IOException {
        this.buffer.consumeContent(decoder);
        if (decoder.isCompleted()) {
            this.finished = true;
        }
    }

    public void finish() {
        this.finished = true;
    }

    public InputStream getContent() throws IOException {
        Asserts.check(this.finished, "Entity content has not been fully received");
        Asserts.check(!this.consumed, "Entity content has been consumed");
        this.consumed = true;
        return new ContentInputStream(this.buffer);
    }

    public boolean isRepeatable() {
        return false;
    }

    public boolean isStreaming() {
        return true;
    }

    public void writeTo(OutputStream outstream) throws IOException {
        int l;
        Args.notNull(outstream, "Output stream");
        InputStream instream = this.getContent();
        byte[] buff = new byte[2048];
        while ((l = instream.read(buff)) != -1) {
            outstream.write(buff, 0, l);
        }
    }
}

