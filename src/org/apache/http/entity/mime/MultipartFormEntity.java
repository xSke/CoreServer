/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.entity.mime;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.entity.mime.AbstractMultipartForm;
import org.apache.http.message.BasicHeader;

class MultipartFormEntity
implements HttpEntity {
    private final AbstractMultipartForm multipart;
    private final Header contentType;
    private final long contentLength;

    MultipartFormEntity(AbstractMultipartForm multipart, String contentType, long contentLength) {
        this.multipart = multipart;
        this.contentType = new BasicHeader("Content-Type", contentType);
        this.contentLength = contentLength;
    }

    AbstractMultipartForm getMultipart() {
        return this.multipart;
    }

    public boolean isRepeatable() {
        return this.contentLength != -1L;
    }

    public boolean isChunked() {
        return !this.isRepeatable();
    }

    public boolean isStreaming() {
        return !this.isRepeatable();
    }

    public long getContentLength() {
        return this.contentLength;
    }

    public Header getContentType() {
        return this.contentType;
    }

    public Header getContentEncoding() {
        return null;
    }

    public void consumeContent() throws IOException, UnsupportedOperationException {
        if (this.isStreaming()) {
            throw new UnsupportedOperationException("Streaming entity does not implement #consumeContent()");
        }
    }

    public InputStream getContent() throws IOException {
        throw new UnsupportedOperationException("Multipart form entity does not implement #getContent()");
    }

    public void writeTo(OutputStream outstream) throws IOException {
        this.multipart.writeTo(outstream);
    }
}

