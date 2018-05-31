/*
 * Decompiled with CFR 0_129.
 */
package com.mashape.unirest.http;

import java.net.URI;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

class HttpPatchWithBody
extends HttpEntityEnclosingRequestBase {
    public static final String METHOD_NAME = "PATCH";

    public String getMethod() {
        return METHOD_NAME;
    }

    public HttpPatchWithBody(String uri) {
        this.setURI(URI.create(uri));
    }

    public HttpPatchWithBody(URI uri) {
        this.setURI(uri);
    }

    public HttpPatchWithBody() {
    }
}

