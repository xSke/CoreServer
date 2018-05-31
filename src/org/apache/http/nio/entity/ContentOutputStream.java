/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.entity;

import java.io.IOException;
import java.io.OutputStream;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.nio.util.ContentOutputBuffer;
import org.apache.http.util.Args;

@NotThreadSafe
public class ContentOutputStream
extends OutputStream {
    private final ContentOutputBuffer buffer;

    public ContentOutputStream(ContentOutputBuffer buffer) {
        Args.notNull(buffer, "Output buffer");
        this.buffer = buffer;
    }

    public void close() throws IOException {
        this.buffer.writeCompleted();
    }

    public void flush() throws IOException {
    }

    public void write(byte[] b, int off, int len) throws IOException {
        this.buffer.write(b, off, len);
    }

    public void write(byte[] b) throws IOException {
        if (b == null) {
            return;
        }
        this.buffer.write(b, 0, b.length);
    }

    public void write(int b) throws IOException {
        this.buffer.write(b);
    }
}

