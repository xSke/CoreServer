/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import org.apache.http.nio.ContentEncoder;

public class ContentEncoderChannel
implements WritableByteChannel {
    private final ContentEncoder contentEncoder;

    public ContentEncoderChannel(ContentEncoder contentEncoder) {
        this.contentEncoder = contentEncoder;
    }

    public int write(ByteBuffer src) throws IOException {
        return this.contentEncoder.write(src);
    }

    public void close() {
    }

    public boolean isOpen() {
        return true;
    }
}

