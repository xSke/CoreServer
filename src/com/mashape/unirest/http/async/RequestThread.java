/*
 * Decompiled with CFR 0_129.
 */
package com.mashape.unirest.http.async;

import com.mashape.unirest.http.HttpClientHelper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.async.Callback;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequest;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class RequestThread<T>
extends Thread {
    private HttpRequest httpRequest;
    private Class<T> responseClass;
    private Callback<T> callback;

    public RequestThread(HttpRequest httpRequest, Class<T> responseClass, Callback<T> callback) {
        this.httpRequest = httpRequest;
        this.responseClass = responseClass;
        this.callback = callback;
    }

    @Override
    public void run() {
        try {
            HttpResponse<T> response = HttpClientHelper.request(this.httpRequest, this.responseClass);
            if (this.callback != null) {
                this.callback.completed(response);
            }
        }
        catch (UnirestException e) {
            this.callback.failed(e);
        }
    }
}

