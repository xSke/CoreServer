/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.client.methods;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.ContentDecoder;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.conn.ManagedNHttpClientConnection;
import org.apache.http.nio.protocol.AbstractAsyncResponseConsumer;
import org.apache.http.nio.reactor.IOSession;
import org.apache.http.util.Asserts;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public abstract class AsyncByteConsumer<T>
extends AbstractAsyncResponseConsumer<T> {
    private final ByteBuffer bbuf;

    public AsyncByteConsumer(int bufSize) {
        this.bbuf = ByteBuffer.allocate(bufSize);
    }

    public AsyncByteConsumer() {
        this(8192);
    }

    protected abstract void onByteReceived(ByteBuffer var1, IOControl var2) throws IOException;

    @Override
    protected final void onEntityEnclosed(HttpEntity entity, ContentType contentType) throws IOException {
    }

    @Override
    protected final void onContentReceived(ContentDecoder decoder, IOControl ioctrl) throws IOException {
        ManagedNHttpClientConnection conn;
        int bytesRead;
        Asserts.notNull(this.bbuf, "Byte buffer");
        IOSession iosession = ioctrl instanceof ManagedNHttpClientConnection ? ((conn = (ManagedNHttpClientConnection)ioctrl) != null ? conn.getIOSession() : null) : null;
        while (!this.isDone() && (bytesRead = decoder.read(this.bbuf)) > 0) {
            this.bbuf.flip();
            this.onByteReceived(this.bbuf, ioctrl);
            this.bbuf.clear();
            if (!decoder.isCompleted() && (iosession == null || !iosession.isClosed() && (iosession.getEventMask() & 1) != 0)) continue;
            break;
        }
    }

    @Override
    protected void releaseResources() {
    }
}

