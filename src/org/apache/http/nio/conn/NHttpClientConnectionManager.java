/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.conn;

import java.io.IOException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.nio.NHttpClientConnection;
import org.apache.http.nio.reactor.IOEventDispatch;
import org.apache.http.protocol.HttpContext;

public interface NHttpClientConnectionManager {
    public Future<NHttpClientConnection> requestConnection(HttpRoute var1, Object var2, long var3, long var5, TimeUnit var7, FutureCallback<NHttpClientConnection> var8);

    public void releaseConnection(NHttpClientConnection var1, Object var2, long var3, TimeUnit var5);

    public void startRoute(NHttpClientConnection var1, HttpRoute var2, HttpContext var3) throws IOException;

    public void upgrade(NHttpClientConnection var1, HttpRoute var2, HttpContext var3) throws IOException;

    public void routeComplete(NHttpClientConnection var1, HttpRoute var2, HttpContext var3);

    public boolean isRouteComplete(NHttpClientConnection var1);

    public void closeIdleConnections(long var1, TimeUnit var3);

    public void closeExpiredConnections();

    public void execute(IOEventDispatch var1) throws IOException;

    public void shutdown() throws IOException;
}

