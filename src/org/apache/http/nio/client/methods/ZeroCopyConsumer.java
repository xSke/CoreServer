/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.client.methods;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.nio.ContentDecoder;
import org.apache.http.nio.ContentDecoderChannel;
import org.apache.http.nio.FileContentDecoder;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.protocol.AbstractAsyncResponseConsumer;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Asserts;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public abstract class ZeroCopyConsumer<T>
extends AbstractAsyncResponseConsumer<T> {
    private final File file;
    private final RandomAccessFile accessfile;
    private HttpResponse response;
    private ContentType contentType;
    private FileChannel fileChannel;
    private long idx = -1L;

    public ZeroCopyConsumer(File file) throws FileNotFoundException {
        if (file == null) {
            throw new IllegalArgumentException("File may nor be null");
        }
        this.file = file;
        this.accessfile = new RandomAccessFile(this.file, "rw");
    }

    @Override
    protected void onResponseReceived(HttpResponse response) {
        this.response = response;
    }

    @Override
    protected void onEntityEnclosed(HttpEntity entity, ContentType contentType) throws IOException {
        this.contentType = contentType;
        this.fileChannel = this.accessfile.getChannel();
        this.idx = 0L;
    }

    @Override
    protected void onContentReceived(ContentDecoder decoder, IOControl ioctrl) throws IOException {
        Asserts.notNull(this.fileChannel, "File channel");
        long transferred = decoder instanceof FileContentDecoder ? ((FileContentDecoder)decoder).transfer(this.fileChannel, this.idx, Integer.MAX_VALUE) : this.fileChannel.transferFrom(new ContentDecoderChannel(decoder), this.idx, Integer.MAX_VALUE);
        if (transferred > 0L) {
            this.idx += transferred;
        }
        if (decoder.isCompleted()) {
            this.fileChannel.close();
        }
    }

    protected abstract T process(HttpResponse var1, File var2, ContentType var3) throws Exception;

    @Override
    protected T buildResult(HttpContext context) throws Exception {
        this.response.setEntity(new FileEntity(this.file, this.contentType));
        return this.process(this.response, this.file, this.contentType);
    }

    @Override
    protected void releaseResources() {
        try {
            this.accessfile.close();
        }
        catch (IOException ignore) {
            // empty catch block
        }
    }
}

