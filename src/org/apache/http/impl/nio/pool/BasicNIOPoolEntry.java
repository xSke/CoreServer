/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.impl.nio.pool;

import java.io.IOException;
import org.apache.http.HttpHost;
import org.apache.http.annotation.ThreadSafe;
import org.apache.http.nio.NHttpClientConnection;
import org.apache.http.pool.PoolEntry;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
@ThreadSafe
public class BasicNIOPoolEntry
extends PoolEntry<HttpHost, NHttpClientConnection> {
    private volatile int socketTimeout;

    public BasicNIOPoolEntry(String id, HttpHost route, NHttpClientConnection conn) {
        super(id, route, conn);
    }

    @Override
    public void close() {
        try {
            ((NHttpClientConnection)this.getConnection()).close();
        }
        catch (IOException ignore) {
            // empty catch block
        }
    }

    @Override
    public boolean isClosed() {
        return !((NHttpClientConnection)this.getConnection()).isOpen();
    }

    int getSocketTimeout() {
        return this.socketTimeout;
    }

    void setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
    }
}

