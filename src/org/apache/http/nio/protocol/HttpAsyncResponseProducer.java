/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.protocol;

import java.io.Closeable;
import java.io.IOException;
import org.apache.http.HttpResponse;
import org.apache.http.nio.ContentEncoder;
import org.apache.http.nio.IOControl;
import org.apache.http.protocol.HttpContext;

public interface HttpAsyncResponseProducer
extends Closeable {
    public HttpResponse generateResponse();

    public void produceContent(ContentEncoder var1, IOControl var2) throws IOException;

    public void responseCompleted(HttpContext var1);

    public void failed(Exception var1);
}

