/*
 * Decompiled with CFR 0_129.
 */
package com.mashape.unirest.http;

import java.net.URI;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

class HttpDeleteWithBody
extends HttpEntityEnclosingRequestBase {
    public static final String METHOD_NAME = "DELETE";

    public String getMethod() {
        return METHOD_NAME;
    }

    public HttpDeleteWithBody(String uri) {
        this.setURI(URI.create(uri));
    }

    public HttpDeleteWithBody(URI uri) {
        this.setURI(uri);
    }

    public HttpDeleteWithBody() {
    }
}

