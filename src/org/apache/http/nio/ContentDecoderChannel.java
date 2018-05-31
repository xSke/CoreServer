/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import org.apache.http.nio.ContentDecoder;

public class ContentDecoderChannel
implements ReadableByteChannel {
    private final ContentDecoder decoder;

    public ContentDecoderChannel(ContentDecoder decoder) {
        this.decoder = decoder;
    }

    public int read(ByteBuffer dst) throws IOException {
        return this.decoder.read(dst);
    }

    public void close() {
    }

    public boolean isOpen() {
        return true;
    }
}

