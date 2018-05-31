/*
 * Decompiled with CFR 0_129.
 */
package com.mashape.unirest.http;

import com.mashape.unirest.http.HttpDeleteWithBody;
import com.mashape.unirest.http.HttpMethod;
import com.mashape.unirest.http.HttpPatchWithBody;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.async.Callback;
import com.mashape.unirest.http.async.utils.AsyncIdleConnectionMonitorThread;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.http.options.Option;
import com.mashape.unirest.http.options.Options;
import com.mashape.unirest.http.utils.ClientFactory;
import com.mashape.unirest.request.HttpRequest;
import com.mashape.unirest.request.body.Body;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.nio.entity.NByteArrayEntity;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class HttpClientHelper {
    private static final String CONTENT_TYPE = "content-type";
    private static final String ACCEPT_ENCODING_HEADER = "accept-encoding";
    private static final String USER_AGENT_HEADER = "user-agent";
    private static final String USER_AGENT = "unirest-java/1.3.11";

    private static <T> FutureCallback<org.apache.http.HttpResponse> prepareCallback(final Class<T> responseClass, final Callback<T> callback) {
        if (callback == null) {
            return null;
        }
        return new FutureCallback<org.apache.http.HttpResponse>(){

            @Override
            public void cancelled() {
                callback.cancelled();
            }

            @Override
            public void completed(org.apache.http.HttpResponse arg0) {
                callback.completed(new HttpResponse(arg0, responseClass));
            }

            @Override
            public void failed(Exception arg0) {
                callback.failed(new UnirestException(arg0));
            }
        };
    }

    public static <T> Future<HttpResponse<T>> requestAsync(HttpRequest request, final Class<T> responseClass, Callback<T> callback) {
        HttpRequestBase requestObj = HttpClientHelper.prepareRequest(request, true);
        CloseableHttpAsyncClient asyncHttpClient = ClientFactory.getAsyncHttpClient();
        if (!asyncHttpClient.isRunning()) {
            asyncHttpClient.start();
            AsyncIdleConnectionMonitorThread asyncIdleConnectionMonitorThread = (AsyncIdleConnectionMonitorThread)Options.getOption(Option.ASYNC_MONITOR);
            asyncIdleConnectionMonitorThread.start();
        }
        final Future<org.apache.http.HttpResponse> future = asyncHttpClient.execute(requestObj, HttpClientHelper.prepareCallback(responseClass, callback));
        return new Future<HttpResponse<T>>(){

            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return future.cancel(mayInterruptIfRunning);
            }

            @Override
            public boolean isCancelled() {
                return future.isCancelled();
            }

            @Override
            public boolean isDone() {
                return future.isDone();
            }

            @Override
            public HttpResponse<T> get() throws InterruptedException, ExecutionException {
                org.apache.http.HttpResponse httpResponse = (org.apache.http.HttpResponse)future.get();
                return new HttpResponse(httpResponse, responseClass);
            }

            @Override
            public HttpResponse<T> get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                org.apache.http.HttpResponse httpResponse = (org.apache.http.HttpResponse)future.get(timeout, unit);
                return new HttpResponse(httpResponse, responseClass);
            }
        };
    }

    public static <T> HttpResponse<T> request(HttpRequest request, Class<T> responseClass) throws UnirestException {
        HttpRequestBase requestObj = HttpClientHelper.prepareRequest(request, false);
        HttpClient client = ClientFactory.getHttpClient();
        try {
            org.apache.http.HttpResponse response = client.execute(requestObj);
            HttpResponse<T> httpResponse = new HttpResponse<T>(response, responseClass);
            requestObj.releaseConnection();
            HttpResponse<T> httpResponse2 = httpResponse;
            return httpResponse2;
        }
        catch (Exception e) {
            throw new UnirestException(e);
        }
        finally {
            requestObj.releaseConnection();
        }
    }

    private static HttpRequestBase prepareRequest(HttpRequest request, boolean async) {
        Object defaultHeaders = Options.getOption(Option.DEFAULT_HEADERS);
        if (defaultHeaders != null) {
            Set entrySet = ((Map)defaultHeaders).entrySet();
            for (Map.Entry entry : entrySet) {
                request.header((String)entry.getKey(), (String)entry.getValue());
            }
        }
        if (!request.getHeaders().containsKey(USER_AGENT_HEADER)) {
            request.header(USER_AGENT_HEADER, USER_AGENT);
        }
        if (!request.getHeaders().containsKey(ACCEPT_ENCODING_HEADER)) {
            request.header(ACCEPT_ENCODING_HEADER, "gzip");
        }
        HttpRequestBase reqObj = null;
        String urlToRequest = null;
        try {
            URL url = new URL(request.getUrl());
            URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), URLDecoder.decode(url.getPath(), "UTF-8"), "", url.getRef());
            urlToRequest = uri.toURL().toString();
            if (url.getQuery() != null && !url.getQuery().trim().equals("")) {
                if (!urlToRequest.substring(urlToRequest.length() - 1).equals("?")) {
                    urlToRequest = urlToRequest + "?";
                }
                urlToRequest = urlToRequest + url.getQuery();
            } else if (urlToRequest.substring(urlToRequest.length() - 1).equals("?")) {
                urlToRequest = urlToRequest.substring(0, urlToRequest.length() - 1);
            }
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        switch (request.getHttpMethod()) {
            case GET: {
                reqObj = new HttpGet(urlToRequest);
                break;
            }
            case POST: {
                reqObj = new HttpPost(urlToRequest);
                break;
            }
            case PUT: {
                reqObj = new HttpPut(urlToRequest);
                break;
            }
            case DELETE: {
                reqObj = new HttpDeleteWithBody(urlToRequest);
                break;
            }
            case PATCH: {
                reqObj = new HttpPatchWithBody(urlToRequest);
                break;
            }
            case OPTIONS: {
                reqObj = new HttpOptions(urlToRequest);
                break;
            }
            case HEAD: {
                reqObj = new HttpHead(urlToRequest);
            }
        }
        Set<Map.Entry<String, List<String>>> entrySet = request.getHeaders().entrySet();
        for (Map.Entry entry : entrySet) {
            List values = (List)entry.getValue();
            if (values == null) continue;
            for (String value : values) {
                reqObj.addHeader((String)entry.getKey(), value);
            }
        }
        if (request.getHttpMethod() != HttpMethod.GET && request.getHttpMethod() != HttpMethod.HEAD && request.getBody() != null) {
            HttpEntity entity = request.getBody().getEntity();
            if (async) {
                if (reqObj.getHeaders(CONTENT_TYPE) == null || reqObj.getHeaders(CONTENT_TYPE).length == 0) {
                    reqObj.setHeader(entity.getContentType());
                }
                try {
                    ByteArrayOutputStream output = new ByteArrayOutputStream();
                    entity.writeTo(output);
                    NByteArrayEntity en = new NByteArrayEntity(output.toByteArray());
                    ((HttpEntityEnclosingRequestBase)reqObj).setEntity(en);
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                ((HttpEntityEnclosingRequestBase)reqObj).setEntity(entity);
            }
        }
        return reqObj;
    }

}

