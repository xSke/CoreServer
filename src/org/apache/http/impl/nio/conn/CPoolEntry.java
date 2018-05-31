/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.impl.nio.conn;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.apache.commons.logging.Log;
import org.apache.http.annotation.ThreadSafe;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.nio.conn.ManagedNHttpClientConnection;
import org.apache.http.pool.PoolEntry;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
@ThreadSafe
class CPoolEntry
extends PoolEntry<HttpRoute, ManagedNHttpClientConnection> {
    private final Log log;
    private volatile boolean routeComplete;

    public CPoolEntry(Log log, String id, HttpRoute route, ManagedNHttpClientConnection conn, long timeToLive, TimeUnit tunit) {
        super(id, route, conn, timeToLive, tunit);
        this.log = log;
    }

    public boolean isRouteComplete() {
        return this.routeComplete;
    }

    public void markRouteComplete() {
        this.routeComplete = true;
    }

    public void closeConnection() throws IOException {
        ManagedNHttpClientConnection conn = (ManagedNHttpClientConnection)this.getConnection();
        conn.close();
    }

    public void shutdownConnection() throws IOException {
        ManagedNHttpClientConnection conn = (ManagedNHttpClientConnection)this.getConnection();
        conn.shutdown();
    }

    @Override
    public boolean isExpired(long now) {
        boolean expired = super.isExpired(now);
        if (expired && this.log.isDebugEnabled()) {
            this.log.debug("Connection " + this + " expired @ " + new Date(this.getExpiry()));
        }
        return expired;
    }

    @Override
    public boolean isClosed() {
        ManagedNHttpClientConnection conn = (ManagedNHttpClientConnection)this.getConnection();
        return !conn.isOpen();
    }

    @Override
    public void close() {
        try {
            this.closeConnection();
        }
        catch (IOException ex) {
            this.log.debug("I/O error closing connection", ex);
        }
    }
}

