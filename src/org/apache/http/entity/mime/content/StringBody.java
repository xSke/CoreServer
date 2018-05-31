/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.entity.mime.content;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import org.apache.http.Consts;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.content.AbstractContentBody;
import org.apache.http.util.Args;

public class StringBody
extends AbstractContentBody {
    private final byte[] content;

    @Deprecated
    public static StringBody create(String text, String mimeType, Charset charset) throws IllegalArgumentException {
        try {
            return new StringBody(text, mimeType, charset);
        }
        catch (UnsupportedEncodingException ex) {
            throw new IllegalArgumentException("Charset " + charset + " is not supported", ex);
        }
    }

    @Deprecated
    public static StringBody create(String text, Charset charset) throws IllegalArgumentException {
        return StringBody.create(text, null, charset);
    }

    @Deprecated
    public static StringBody create(String text) throws IllegalArgumentException {
        return StringBody.create(text, null, null);
    }

    @Deprecated
    public StringBody(String text, String mimeType, Charset charset) throws UnsupportedEncodingException {
        this(text, ContentType.create(mimeType, charset));
    }

    @Deprecated
    public StringBody(String text, Charset charset) throws UnsupportedEncodingException {
        this(text, "text/plain", charset);
    }

    @Deprecated
    public StringBody(String text) throws UnsupportedEncodingException {
        this(text, "text/plain", Consts.ASCII);
    }

    public StringBody(String text, ContentType contentType) {
        super(contentType);
        Args.notNull(text, "Text");
        Charset charset = contentType.getCharset();
        String csname = charset != null ? charset.name() : Consts.ASCII.name();
        try {
            this.content = text.getBytes(csname);
        }
        catch (UnsupportedEncodingException ex) {
            throw new UnsupportedCharsetException(csname);
        }
    }

    public Reader getReader() {
        Charset charset = this.getContentType().getCharset();
        return new InputStreamReader((InputStream)new ByteArrayInputStream(this.content), charset != null ? charset : Consts.ASCII);
    }

    public void writeTo(OutputStream out) throws IOException {
        int l;
        Args.notNull(out, "Output stream");
        ByteArrayInputStream in = new ByteArrayInputStream(this.content);
        byte[] tmp = new byte[4096];
        while ((l = in.read(tmp)) != -1) {
            out.write(tmp, 0, l);
        }
        out.flush();
    }

    public String getTransferEncoding() {
        return "8bit";
    }

    public long getContentLength() {
        return this.content.length;
    }

    public String getFilename() {
        return null;
    }
}

