/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.protocol;

import java.io.IOException;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.nio.protocol.HttpAsyncExchange;
import org.apache.http.nio.protocol.HttpAsyncRequestConsumer;
import org.apache.http.protocol.HttpContext;

public interface HttpAsyncRequestHandler<T> {
    public HttpAsyncRequestConsumer<T> processRequest(HttpRequest var1, HttpContext var2) throws HttpException, IOException;

    public void handle(T var1, HttpAsyncExchange var2, HttpContext var3) throws HttpException, IOException;
}

