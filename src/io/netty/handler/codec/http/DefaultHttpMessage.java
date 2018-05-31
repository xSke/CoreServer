/*
 * Decompiled with CFR 0_129.
 */
package io.netty.handler.codec.http;

import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.DefaultHttpObject;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpVersion;

public abstract class DefaultHttpMessage
extends DefaultHttpObject
implements HttpMessage {
    private HttpVersion version;
    private final HttpHeaders headers;

    protected DefaultHttpMessage(HttpVersion version) {
        this(version, true);
    }

    protected DefaultHttpMessage(HttpVersion version, boolean validate) {
        if (version == null) {
            throw new NullPointerException("version");
        }
        this.version = version;
        this.headers = new DefaultHttpHeaders(validate);
    }

    @Override
    public HttpHeaders headers() {
        return this.headers;
    }

    @Override
    public HttpVersion getProtocolVersion() {
        return this.version;
    }

    @Override
    public HttpMessage setProtocolVersion(HttpVersion version) {
        if (version == null) {
            throw new NullPointerException("version");
        }
        this.version = version;
        return this;
    }
}

