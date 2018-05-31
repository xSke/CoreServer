/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.protocol;

import java.io.Closeable;
import java.io.IOException;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.concurrent.Cancellable;
import org.apache.http.nio.ContentDecoder;
import org.apache.http.nio.ContentEncoder;
import org.apache.http.nio.IOControl;

public interface HttpAsyncClientExchangeHandler
extends Closeable,
Cancellable {
    public HttpRequest generateRequest() throws IOException, HttpException;

    public void produceContent(ContentEncoder var1, IOControl var2) throws IOException;

    public void requestCompleted();

    public void responseReceived(HttpResponse var1) throws IOException, HttpException;

    public void consumeContent(ContentDecoder var1, IOControl var2) throws IOException;

    public void responseCompleted() throws IOException, HttpException;

    public void inputTerminated();

    public void failed(Exception var1);

    public boolean isDone();
}

