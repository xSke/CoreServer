/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.conn;

import org.apache.http.config.ConnectionConfig;
import org.apache.http.nio.NHttpConnection;
import org.apache.http.nio.reactor.IOSession;

public interface NHttpConnectionFactory<T extends NHttpConnection> {
    public T create(IOSession var1, ConnectionConfig var2);
}

