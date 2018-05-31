/*
 * Decompiled with CFR 0_129.
 */
package com.mashape.unirest.http.async;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.exceptions.UnirestException;

public interface Callback<T> {
    public void completed(HttpResponse<T> var1);

    public void failed(UnirestException var1);

    public void cancelled();
}

