/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.conn;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.apache.http.HttpHost;
import org.apache.http.conn.ConnectionReleaseTrigger;
import org.apache.http.conn.HttpRoutedConnection;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.nio.NHttpClientConnection;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

@Deprecated
public interface ManagedClientAsyncConnection
extends HttpRoutedConnection,
NHttpClientConnection,
ConnectionReleaseTrigger {
    public Object getState();

    public void setState(Object var1);

    public void markReusable();

    public void unmarkReusable();

    public boolean isMarkedReusable();

    public void open(HttpRoute var1, HttpContext var2, HttpParams var3) throws IOException;

    public void tunnelTarget(HttpParams var1) throws IOException;

    public void tunnelProxy(HttpHost var1, HttpParams var2) throws IOException;

    public void layerProtocol(HttpContext var1, HttpParams var2) throws IOException;

    public void setIdleDuration(long var1, TimeUnit var3);
}

