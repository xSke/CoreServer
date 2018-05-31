/*
 * Decompiled with CFR 0_129.
 */
package com.mashape.unirest.http;

import com.mashape.unirest.http.Headers;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.utils.ResponseUtils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.util.EntityUtils;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class HttpResponse<T> {
    private int statusCode;
    private String statusText;
    private Headers headers;
    private InputStream rawBody;
    private T body;

    public HttpResponse(org.apache.http.HttpResponse response, Class<T> responseClass) {
        HttpEntity responseEntity;
        block14 : {
            Header[] allHeaders;
            this.headers = new Headers();
            responseEntity = response.getEntity();
            for (Header header : allHeaders = response.getAllHeaders()) {
                String headerName = header.getName().toLowerCase();
                ArrayList<String> list = (ArrayList<String>)this.headers.get(headerName);
                if (list == null) {
                    list = new ArrayList<String>();
                }
                list.add(header.getValue());
                this.headers.put(headerName, list);
            }
            StatusLine statusLine = response.getStatusLine();
            this.statusCode = statusLine.getStatusCode();
            this.statusText = statusLine.getReasonPhrase();
            if (responseEntity != null) {
                String responseCharset;
                String charset = "UTF-8";
                Header contentType = responseEntity.getContentType();
                if (contentType != null && (responseCharset = ResponseUtils.getCharsetFromContentType(contentType.getValue())) != null && !responseCharset.trim().equals("")) {
                    charset = responseCharset;
                }
                try {
                    byte[] rawBody;
                    try {
                        InputStream responseInputStream = responseEntity.getContent();
                        if (ResponseUtils.isGzipped(responseEntity.getContentEncoding())) {
                            responseInputStream = new GZIPInputStream(responseEntity.getContent());
                        }
                        rawBody = ResponseUtils.getBytes(responseInputStream);
                    }
                    catch (IOException e2) {
                        throw new RuntimeException(e2);
                    }
                    ByteArrayInputStream inputStream = new ByteArrayInputStream(rawBody);
                    this.rawBody = inputStream;
                    if (JsonNode.class.equals(responseClass)) {
                        String jsonString = new String(rawBody, charset).trim();
                        this.body = new JsonNode(jsonString);
                        break block14;
                    }
                    if (String.class.equals(responseClass)) {
                        this.body = new String(rawBody, charset);
                        break block14;
                    }
                    if (InputStream.class.equals(responseClass)) {
                        this.body = this.rawBody;
                        break block14;
                    }
                    throw new Exception("Unknown result type. Only String, JsonNode and InputStream are supported.");
                }
                catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        try {
            EntityUtils.consume(responseEntity);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int getStatus() {
        return this.statusCode;
    }

    public String getStatusText() {
        return this.statusText;
    }

    public Headers getHeaders() {
        return this.headers;
    }

    public InputStream getRawBody() {
        return this.rawBody;
    }

    public T getBody() {
        return this.body;
    }
}

