/*
 * Decompiled with CFR 0_129.
 */
package com.mashape.unirest.request;

import com.mashape.unirest.http.HttpMethod;
import com.mashape.unirest.http.utils.Base64Coder;
import com.mashape.unirest.http.utils.URLParamEncoder;
import com.mashape.unirest.request.BaseRequest;
import com.mashape.unirest.request.body.Body;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class HttpRequest
extends BaseRequest {
    private HttpMethod httpMethod;
    protected String url;
    Map<String, List<String>> headers = new TreeMap<String, List<String>>(String.CASE_INSENSITIVE_ORDER);
    protected Body body;

    public HttpRequest(HttpMethod method, String url) {
        this.httpMethod = method;
        this.url = url;
        this.httpRequest = this;
    }

    public HttpRequest routeParam(String name, String value) {
        Matcher matcher = Pattern.compile("\\{" + name + "\\}").matcher(this.url);
        int count = 0;
        while (matcher.find()) {
            ++count;
        }
        if (count == 0) {
            throw new RuntimeException("Can't find route parameter name \"" + name + "\"");
        }
        this.url = this.url.replaceAll("\\{" + name + "\\}", URLParamEncoder.encode(value));
        return this;
    }

    public HttpRequest basicAuth(String username, String password) {
        this.header("Authorization", "Basic " + Base64Coder.encodeString(new StringBuilder().append(username).append(":").append(password).toString()));
        return this;
    }

    public HttpRequest header(String name, String value) {
        List<String> list = this.headers.get(name.trim());
        if (list == null) {
            list = new ArrayList<String>();
        }
        list.add(value);
        this.headers.put(name.trim(), list);
        return this;
    }

    public HttpRequest headers(Map<String, String> headers) {
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                this.header(entry.getKey(), entry.getValue());
            }
        }
        return this;
    }

    public HttpRequest queryString(String name, Collection<?> value) {
        for (Object cur : value) {
            this.queryString(name, cur);
        }
        return this;
    }

    public HttpRequest queryString(String name, Object value) {
        StringBuilder queryString = new StringBuilder();
        if (this.url.contains("?")) {
            queryString.append("&");
        } else {
            queryString.append("?");
        }
        try {
            queryString.append(name).append("=").append(URLEncoder.encode(value == null ? "" : value.toString(), "UTF-8"));
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        this.url = this.url + queryString.toString();
        return this;
    }

    public HttpRequest queryString(Map<String, Object> parameters) {
        if (parameters != null) {
            for (Map.Entry<String, Object> param : parameters.entrySet()) {
                if (param.getValue() instanceof String || param.getValue() instanceof Number || param.getValue() instanceof Boolean) {
                    this.queryString(param.getKey(), param.getValue());
                    continue;
                }
                throw new RuntimeException("Parameter \"" + param.getKey() + "\" can't be sent with a GET request because of type: " + param.getValue().getClass().getName());
            }
        }
        return this;
    }

    public HttpMethod getHttpMethod() {
        return this.httpMethod;
    }

    public String getUrl() {
        return this.url;
    }

    public Map<String, List<String>> getHeaders() {
        if (this.headers == null) {
            return new HashMap<String, List<String>>();
        }
        return this.headers;
    }

    public Body getBody() {
        return this.body;
    }
}

