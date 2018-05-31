/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.protocol;

import java.io.Closeable;
import java.io.IOException;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.nio.ContentEncoder;
import org.apache.http.nio.IOControl;
import org.apache.http.protocol.HttpContext;

public interface HttpAsyncRequestProducer
extends Closeable {
    public HttpHost getTarget();

    public HttpRequest generateRequest() throws IOException, HttpException;

    public void produceContent(ContentEncoder var1, IOControl var2) throws IOException;

    public void requestCompleted(HttpContext var1);

    public void failed(Exception var1);

    public boolean isRepeatable();

    public void resetRequest() throws IOException;
}

