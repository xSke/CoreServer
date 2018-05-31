/*
 * Decompiled with CFR 0_129.
 */
package com.mashape.unirest.request;

import com.mashape.unirest.http.HttpMethod;
import com.mashape.unirest.request.HttpRequest;
import java.util.Map;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class GetRequest
extends HttpRequest {
    public GetRequest(HttpMethod method, String url) {
        super(method, url);
    }

    @Override
    public GetRequest routeParam(String name, String value) {
        super.routeParam(name, value);
        return this;
    }

    @Override
    public GetRequest header(String name, String value) {
        return (GetRequest)super.header(name, value);
    }

    @Override
    public GetRequest headers(Map<String, String> headers) {
        return (GetRequest)super.headers(headers);
    }

    @Override
    public GetRequest basicAuth(String username, String password) {
        super.basicAuth(username, password);
        return this;
    }
}

