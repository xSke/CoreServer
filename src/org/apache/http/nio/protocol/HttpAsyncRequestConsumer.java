/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.protocol;

import java.io.Closeable;
import java.io.IOException;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.nio.ContentDecoder;
import org.apache.http.nio.IOControl;
import org.apache.http.protocol.HttpContext;

public interface HttpAsyncRequestConsumer<T>
extends Closeable {
    public void requestReceived(HttpRequest var1) throws HttpException, IOException;

    public void consumeContent(ContentDecoder var1, IOControl var2) throws IOException;

    public void requestCompleted(HttpContext var1);

    public void failed(Exception var1);

    public Exception getException();

    public T getResult();

    public boolean isDone();
}

