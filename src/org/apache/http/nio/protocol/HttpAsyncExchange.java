/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.protocol;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.concurrent.Cancellable;
import org.apache.http.nio.protocol.HttpAsyncResponseProducer;

public interface HttpAsyncExchange {
    public HttpRequest getRequest();

    public HttpResponse getResponse();

    public void submitResponse();

    public void submitResponse(HttpAsyncResponseProducer var1);

    public boolean isCompleted();

    public void setCallback(Cancellable var1);

    public void setTimeout(int var1);

    public int getTimeout();
}

