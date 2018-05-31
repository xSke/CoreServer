/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.protocol;

import java.io.IOException;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.nio.entity.ConsumingNHttpEntity;
import org.apache.http.nio.protocol.NHttpResponseTrigger;
import org.apache.http.protocol.HttpContext;

@Deprecated
public interface NHttpRequestHandler {
    public ConsumingNHttpEntity entityRequest(HttpEntityEnclosingRequest var1, HttpContext var2) throws HttpException, IOException;

    public void handle(HttpRequest var1, HttpResponse var2, NHttpResponseTrigger var3, HttpContext var4) throws HttpException, IOException;
}

