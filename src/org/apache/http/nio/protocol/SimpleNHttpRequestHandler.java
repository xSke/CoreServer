/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.protocol;

import java.io.IOException;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.annotation.Immutable;
import org.apache.http.nio.protocol.NHttpRequestHandler;
import org.apache.http.nio.protocol.NHttpResponseTrigger;
import org.apache.http.protocol.HttpContext;

@Deprecated
@Immutable
public abstract class SimpleNHttpRequestHandler
implements NHttpRequestHandler {
    public final void handle(HttpRequest request, HttpResponse response, NHttpResponseTrigger trigger, HttpContext context) throws HttpException, IOException {
        this.handle(request, response, context);
        trigger.submitResponse(response);
    }

    public abstract void handle(HttpRequest var1, HttpResponse var2, HttpContext var3) throws HttpException, IOException;
}

