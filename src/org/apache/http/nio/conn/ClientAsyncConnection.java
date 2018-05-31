/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.conn;

import org.apache.http.HttpInetConnection;
import org.apache.http.nio.NHttpClientConnection;
import org.apache.http.nio.reactor.IOSession;

@Deprecated
public interface ClientAsyncConnection
extends NHttpClientConnection,
HttpInetConnection {
    public void upgrade(IOSession var1);

    public IOSession getIOSession();
}

