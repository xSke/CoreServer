/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.entity;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.ContentEncoder;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.entity.HttpAsyncContentProducer;
import org.apache.http.nio.entity.ProducingNHttpEntity;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.Args;

@NotThreadSafe
public class NStringEntity
extends AbstractHttpEntity
implements HttpAsyncContentProducer,
ProducingNHttpEntity {
    private final byte[] b;
    private final ByteBuffer buf;
    @Deprecated
    protected final byte[] content;
    @Deprecated
    protected final ByteBuffer buffer;

    public NStringEntity(String s, ContentType contentType) {
        Charset charset;
        Args.notNull(s, "Source string");
        Charset charset2 = charset = contentType != null ? contentType.getCharset() : null;
        if (charset == null) {
            charset = HTTP.DEF_CONTENT_CHARSET;
        }
        try {
            this.b = s.getBytes(charset.name());
        }
        catch (UnsupportedEncodingException ex) {
            throw new UnsupportedCharsetException(charset.name());
        }
        this.buf = ByteBuffer.wrap(this.b);
        this.content = this.b;
        this.buffer = this.buf;
        if (contentType != null) {
            this.setContentType(contentType.toString());
        }
    }

    public NStringEntity(String s, String charset) throws UnsupportedEncodingException {
        this(s, ContentType.create(ContentType.TEXT_PLAIN.getMimeType(), charset));
    }

    public NStringEntity(String s, Charset charset) {
        this(s, ContentType.create(ContentType.TEXT_PLAIN.getMimeType(), charset));
    }

    public NStringEntity(String s) throws UnsupportedEncodingException {
        this(s, ContentType.DEFAULT_TEXT);
    }

    public boolean isRepeatable() {
        return true;
    }

    public long getContentLength() {
        return this.b.length;
    }

    public void close() {
        this.buf.rewind();
    }

    @Deprecated
    public void finish() {
        this.close();
    }

    public void produceContent(ContentEncoder encoder, IOControl ioctrl) throws IOException {
        encoder.write(this.buf);
        if (!this.buf.hasRemaining()) {
            encoder.complete();
        }
    }

    public boolean isStreaming() {
        return false;
    }

    public InputStream getContent() {
        return new ByteArrayInputStream(this.b);
    }

    public void writeTo(OutputStream outstream) throws IOException {
        Args.notNull(outstream, "Output stream");
        outstream.write(this.b);
        outstream.flush();
    }
}

