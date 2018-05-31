/*
 * Decompiled with CFR 0_129.
 */
package com.mashape.unirest.request;

import com.mashape.unirest.http.HttpMethod;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.request.HttpRequest;
import com.mashape.unirest.request.body.Body;
import com.mashape.unirest.request.body.MultipartBody;
import com.mashape.unirest.request.body.RawBody;
import com.mashape.unirest.request.body.RequestBodyEntity;
import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class HttpRequestWithBody
extends HttpRequest {
    public HttpRequestWithBody(HttpMethod method, String url) {
        super(method, url);
    }

    @Override
    public HttpRequestWithBody routeParam(String name, String value) {
        super.routeParam(name, value);
        return this;
    }

    @Override
    public HttpRequestWithBody header(String name, String value) {
        return (HttpRequestWithBody)super.header(name, value);
    }

    @Override
    public HttpRequestWithBody headers(Map<String, String> headers) {
        return (HttpRequestWithBody)super.headers(headers);
    }

    @Override
    public HttpRequestWithBody basicAuth(String username, String password) {
        super.basicAuth(username, password);
        return this;
    }

    @Override
    public HttpRequestWithBody queryString(Map<String, Object> parameters) {
        return (HttpRequestWithBody)super.queryString(parameters);
    }

    @Override
    public HttpRequestWithBody queryString(String name, Object value) {
        return (HttpRequestWithBody)super.queryString(name, value);
    }

    public MultipartBody field(String name, Collection<?> value) {
        MultipartBody body = new MultipartBody(this).field(name, value);
        this.body = body;
        return body;
    }

    public MultipartBody field(String name, Object value) {
        return this.field(name, value, null);
    }

    public MultipartBody field(String name, File file) {
        return this.field(name, file, null);
    }

    public MultipartBody field(String name, Object value, String contentType) {
        MultipartBody body = new MultipartBody(this).field(name, value == null ? "" : value.toString(), contentType);
        this.body = body;
        return body;
    }

    public MultipartBody field(String name, File file, String contentType) {
        MultipartBody body = new MultipartBody(this).field(name, file, contentType);
        this.body = body;
        return body;
    }

    public MultipartBody fields(Map<String, Object> parameters) {
        MultipartBody body = new MultipartBody(this);
        if (parameters != null) {
            for (Map.Entry<String, Object> param : parameters.entrySet()) {
                if (param.getValue() instanceof File) {
                    body.field(param.getKey(), (File)param.getValue());
                    continue;
                }
                body.field(param.getKey(), param.getValue() == null ? "" : param.getValue().toString());
            }
        }
        this.body = body;
        return body;
    }

    public RequestBodyEntity body(JsonNode body) {
        return this.body(body.toString());
    }

    public RequestBodyEntity body(String body) {
        RequestBodyEntity b = new RequestBodyEntity(this).body(body);
        this.body = b;
        return b;
    }

    public RawBody body(byte[] body) {
        RawBody b = new RawBody(this).body(body);
        this.body = b;
        return b;
    }
}

