/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.protocol;

import org.apache.http.HttpRequest;
import org.apache.http.protocol.HttpRequestHandler;

public interface HttpRequestHandlerMapper {
    public HttpRequestHandler lookup(HttpRequest var1);
}

