/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.conn;

import javax.net.ssl.SSLSession;
import org.apache.http.HttpInetConnection;
import org.apache.http.nio.NHttpClientConnection;
import org.apache.http.nio.reactor.IOSession;

public interface ManagedNHttpClientConnection
extends NHttpClientConnection,
HttpInetConnection {
    public String getId();

    public void bind(IOSession var1);

    public IOSession getIOSession();

    public SSLSession getSSLSession();
}

