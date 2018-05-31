/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.client.methods;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.ContentDecoder;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.conn.ManagedNHttpClientConnection;
import org.apache.http.nio.protocol.AbstractAsyncResponseConsumer;
import org.apache.http.nio.reactor.IOSession;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.Asserts;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public abstract class AsyncCharConsumer<T>
extends AbstractAsyncResponseConsumer<T> {
    private final ByteBuffer bbuf;
    private final CharBuffer cbuf;
    private CharsetDecoder chardecoder;
    private ContentType contentType;

    public AsyncCharConsumer(int bufSize) {
        this.bbuf = ByteBuffer.allocate(bufSize);
        this.cbuf = CharBuffer.allocate(bufSize);
    }

    public AsyncCharConsumer() {
        this(8192);
    }

    protected abstract void onCharReceived(CharBuffer var1, IOControl var2) throws IOException;

    @Override
    protected final void onEntityEnclosed(HttpEntity entity, ContentType contentType) throws IOException {
        this.contentType = contentType != null ? contentType : ContentType.DEFAULT_TEXT;
        Charset charset = this.contentType.getCharset();
        if (charset == null) {
            charset = HTTP.DEF_CONTENT_CHARSET;
        }
        this.chardecoder = charset.newDecoder();
    }

    @Override
    protected final void onContentReceived(ContentDecoder decoder, IOControl ioctrl) throws IOException {
        ManagedNHttpClientConnection conn;
        int bytesRead;
        Asserts.notNull(this.bbuf, "Byte buffer");
        IOSession iosession = ioctrl instanceof ManagedNHttpClientConnection ? ((conn = (ManagedNHttpClientConnection)ioctrl) != null ? conn.getIOSession() : null) : null;
        while (!this.isDone() && (bytesRead = decoder.read(this.bbuf)) > 0) {
            this.bbuf.flip();
            boolean completed = decoder.isCompleted();
            CoderResult result = this.chardecoder.decode(this.bbuf, this.cbuf, completed);
            this.handleDecodingResult(result, ioctrl);
            this.bbuf.compact();
            if (completed) {
                result = this.chardecoder.flush(this.cbuf);
                this.handleDecodingResult(result, ioctrl);
                break;
            }
            if (iosession == null || !iosession.isClosed() && (iosession.getEventMask() & 1) != 0) continue;
            break;
        }
    }

    private void handleDecodingResult(CoderResult result, IOControl ioctrl) throws IOException {
        if (result.isError()) {
            result.throwException();
        }
        this.cbuf.flip();
        if (this.cbuf.hasRemaining()) {
            this.onCharReceived(this.cbuf, ioctrl);
        }
        this.cbuf.clear();
    }

    @Override
    protected void releaseResources() {
    }
}

