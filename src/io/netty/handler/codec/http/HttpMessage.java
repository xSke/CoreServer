/*
 * Decompiled with CFR 0_129.
 */
package io.netty.handler.codec.http;

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpVersion;

public interface HttpMessage
extends HttpObject {
    public HttpVersion getProtocolVersion();

    public HttpMessage setProtocolVersion(HttpVersion var1);

    public HttpHeaders headers();
}

