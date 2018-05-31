/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.protocol;

import java.io.IOException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.nio.entity.ConsumingNHttpEntity;
import org.apache.http.protocol.HttpContext;

@Deprecated
public interface NHttpRequestExecutionHandler {
    public void initalizeContext(HttpContext var1, Object var2);

    public HttpRequest submitRequest(HttpContext var1);

    public ConsumingNHttpEntity responseEntity(HttpResponse var1, HttpContext var2) throws IOException;

    public void handleResponse(HttpResponse var1, HttpContext var2) throws IOException;

    public void finalizeContext(HttpContext var1);
}

