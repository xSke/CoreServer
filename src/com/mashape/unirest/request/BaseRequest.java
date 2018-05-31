/*
 * Decompiled with CFR 0_129.
 */
package com.mashape.unirest.request;

import com.mashape.unirest.http.HttpClientHelper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.async.Callback;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequest;
import java.io.InputStream;
import java.util.concurrent.Future;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public abstract class BaseRequest {
    protected static final String UTF_8 = "UTF-8";
    protected HttpRequest httpRequest;

    protected BaseRequest(HttpRequest httpRequest) {
        this.httpRequest = httpRequest;
    }

    public HttpRequest getHttpRequest() {
        return this.httpRequest;
    }

    protected BaseRequest() {
    }

    public HttpResponse<String> asString() throws UnirestException {
        return HttpClientHelper.request(this.httpRequest, String.class);
    }

    public Future<HttpResponse<String>> asStringAsync() {
        return HttpClientHelper.requestAsync(this.httpRequest, String.class, null);
    }

    public Future<HttpResponse<String>> asStringAsync(Callback<String> callback) {
        return HttpClientHelper.requestAsync(this.httpRequest, String.class, callback);
    }

    public HttpResponse<JsonNode> asJson() throws UnirestException {
        return HttpClientHelper.request(this.httpRequest, JsonNode.class);
    }

    public Future<HttpResponse<JsonNode>> asJsonAsync() {
        return HttpClientHelper.requestAsync(this.httpRequest, JsonNode.class, null);
    }

    public Future<HttpResponse<JsonNode>> asJsonAsync(Callback<JsonNode> callback) {
        return HttpClientHelper.requestAsync(this.httpRequest, JsonNode.class, callback);
    }

    public HttpResponse<InputStream> asBinary() throws UnirestException {
        return HttpClientHelper.request(this.httpRequest, InputStream.class);
    }

    public Future<HttpResponse<InputStream>> asBinaryAsync() {
        return HttpClientHelper.requestAsync(this.httpRequest, InputStream.class, null);
    }

    public Future<HttpResponse<InputStream>> asBinaryAsync(Callback<InputStream> callback) {
        return HttpClientHelper.requestAsync(this.httpRequest, InputStream.class, callback);
    }
}

