/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.client.methods;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URI;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.ContentEncoder;
import org.apache.http.nio.ContentEncoderChannel;
import org.apache.http.nio.FileContentEncoder;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Args;

abstract class BaseZeroCopyRequestProducer
implements HttpAsyncRequestProducer {
    private final URI requestURI;
    private final File file;
    private final RandomAccessFile accessfile;
    private final ContentType contentType;
    private FileChannel fileChannel;
    private long idx = -1L;

    protected BaseZeroCopyRequestProducer(URI requestURI, File file, ContentType contentType) throws FileNotFoundException {
        Args.notNull(requestURI, "Request URI");
        Args.notNull(file, "Source file");
        this.requestURI = requestURI;
        this.file = file;
        this.accessfile = new RandomAccessFile(file, "r");
        this.contentType = contentType;
    }

    private void closeChannel() throws IOException {
        if (this.fileChannel != null) {
            this.fileChannel.close();
            this.fileChannel = null;
        }
    }

    protected abstract HttpEntityEnclosingRequest createRequest(URI var1, HttpEntity var2);

    public HttpRequest generateRequest() throws IOException, HttpException {
        BasicHttpEntity entity = new BasicHttpEntity();
        entity.setChunked(false);
        entity.setContentLength(this.file.length());
        if (this.contentType != null) {
            entity.setContentType(this.contentType.toString());
        }
        return this.createRequest(this.requestURI, entity);
    }

    public synchronized HttpHost getTarget() {
        return URIUtils.extractHost(this.requestURI);
    }

    public synchronized void produceContent(ContentEncoder encoder, IOControl ioctrl) throws IOException {
        if (this.fileChannel == null) {
            this.fileChannel = this.accessfile.getChannel();
            this.idx = 0L;
        }
        long transferred = encoder instanceof FileContentEncoder ? ((FileContentEncoder)encoder).transfer(this.fileChannel, this.idx, Integer.MAX_VALUE) : this.fileChannel.transferTo(this.idx, Integer.MAX_VALUE, new ContentEncoderChannel(encoder));
        if (transferred > 0L) {
            this.idx += transferred;
        }
        if (this.idx >= this.fileChannel.size()) {
            encoder.complete();
            this.closeChannel();
        }
    }

    public void requestCompleted(HttpContext context) {
    }

    public void failed(Exception ex) {
    }

    public boolean isRepeatable() {
        return true;
    }

    public synchronized void resetRequest() throws IOException {
        this.closeChannel();
    }

    public synchronized void close() throws IOException {
        try {
            this.accessfile.close();
        }
        catch (IOException ignore) {
            // empty catch block
        }
    }
}

