/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.conn;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.nio.conn.ManagedClientAsyncConnection;
import org.apache.http.nio.conn.scheme.AsyncSchemeRegistry;
import org.apache.http.nio.reactor.IOReactor;

@Deprecated
public interface ClientAsyncConnectionManager
extends IOReactor {
    public AsyncSchemeRegistry getSchemeRegistry();

    public Future<ManagedClientAsyncConnection> leaseConnection(HttpRoute var1, Object var2, long var3, TimeUnit var5, FutureCallback<ManagedClientAsyncConnection> var6);

    public void releaseConnection(ManagedClientAsyncConnection var1, long var2, TimeUnit var4);
}

