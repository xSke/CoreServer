/*
 * Decompiled with CFR 0_129.
 */
package com.mashape.unirest.request.body;

import com.mashape.unirest.http.utils.MapUtil;
import com.mashape.unirest.request.BaseRequest;
import com.mashape.unirest.request.HttpRequest;
import com.mashape.unirest.request.body.Body;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class MultipartBody
extends BaseRequest
implements Body {
    private List<String> keyOrder = new ArrayList<String>();
    private Map<String, List<Object>> parameters = new HashMap<String, List<Object>>();
    private Map<String, ContentType> contentTypes = new HashMap<String, ContentType>();
    private boolean hasFile;
    private HttpRequest httpRequestObj;
    private HttpMultipartMode mode;

    public MultipartBody(HttpRequest httpRequest) {
        super(httpRequest);
        this.httpRequestObj = httpRequest;
    }

    public MultipartBody field(String name, String value) {
        return this.field(name, value, false, null);
    }

    public MultipartBody field(String name, String value, String contentType) {
        return this.field(name, value, false, contentType);
    }

    public MultipartBody field(String name, Collection<?> collection) {
        for (Object current : collection) {
            boolean isFile = current instanceof File;
            this.field(name, current, isFile, null);
        }
        return this;
    }

    public MultipartBody field(String name, Object value) {
        return this.field(name, value, false, null);
    }

    public MultipartBody field(String name, Object value, boolean file) {
        return this.field(name, value, file, null);
    }

    public MultipartBody field(String name, Object value, boolean file, String contentType) {
        this.keyOrder.add(name);
        List<Object> list = this.parameters.get(name);
        if (list == null) {
            list = new LinkedList<Object>();
        }
        list.add(value);
        this.parameters.put(name, list);
        ContentType type = null;
        type = contentType != null && !contentType.isEmpty() ? ContentType.parse(contentType) : (file ? ContentType.APPLICATION_OCTET_STREAM : ContentType.APPLICATION_FORM_URLENCODED.withCharset("UTF-8"));
        this.contentTypes.put(name, type);
        if (!this.hasFile && file) {
            this.hasFile = true;
        }
        return this;
    }

    public MultipartBody field(String name, File file) {
        return this.field(name, file, true, null);
    }

    public MultipartBody field(String name, File file, String contentType) {
        return this.field(name, file, true, contentType);
    }

    public MultipartBody basicAuth(String username, String password) {
        this.httpRequestObj.basicAuth(username, password);
        return this;
    }

    public MultipartBody mode(String mode) {
        this.mode = HttpMultipartMode.valueOf(mode);
        return this;
    }

    @Override
    public HttpEntity getEntity() {
        if (this.hasFile) {
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            if (this.mode != null) {
                builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            }
            for (String key : this.keyOrder) {
                List<Object> value = this.parameters.get(key);
                ContentType contentType = this.contentTypes.get(key);
                for (Object cur : value) {
                    if (cur instanceof File) {
                        File file = (File)cur;
                        builder.addPart(key, new FileBody(file, contentType, file.getName()));
                        continue;
                    }
                    builder.addPart(key, new StringBody(cur.toString(), contentType));
                }
            }
            return builder.build();
        }
        try {
            return new UrlEncodedFormEntity(MapUtil.getList(this.parameters), "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}

