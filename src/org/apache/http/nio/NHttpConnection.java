/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio;

import org.apache.http.HttpConnection;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.nio.IOControl;
import org.apache.http.protocol.HttpContext;

public interface NHttpConnection
extends HttpConnection,
IOControl {
    public static final int ACTIVE = 0;
    public static final int CLOSING = 1;
    public static final int CLOSED = 2;

    public int getStatus();

    public HttpRequest getHttpRequest();

    public HttpResponse getHttpResponse();

    public HttpContext getContext();
}

