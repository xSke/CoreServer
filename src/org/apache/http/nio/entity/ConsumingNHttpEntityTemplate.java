/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.entity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.http.HttpEntity;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.nio.ContentDecoder;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.entity.ConsumingNHttpEntity;
import org.apache.http.nio.entity.ContentListener;

@Deprecated
public class ConsumingNHttpEntityTemplate
extends HttpEntityWrapper
implements ConsumingNHttpEntity {
    private final ContentListener contentListener;

    public ConsumingNHttpEntityTemplate(HttpEntity httpEntity, ContentListener contentListener) {
        super(httpEntity);
        this.contentListener = contentListener;
    }

    public ContentListener getContentListener() {
        return this.contentListener;
    }

    public InputStream getContent() throws IOException, UnsupportedOperationException {
        throw new UnsupportedOperationException("Does not support blocking methods");
    }

    public boolean isStreaming() {
        return true;
    }

    public void writeTo(OutputStream out) throws IOException, UnsupportedOperationException {
        throw new UnsupportedOperationException("Does not support blocking methods");
    }

    public void consumeContent(ContentDecoder decoder, IOControl ioctrl) throws IOException {
        this.contentListener.contentAvailable(decoder, ioctrl);
    }

    public void finish() {
        this.contentListener.finished();
    }
}

