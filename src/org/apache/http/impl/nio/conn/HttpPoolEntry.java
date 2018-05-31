/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.impl.nio.conn;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.apache.commons.logging.Log;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.routing.RouteTracker;
import org.apache.http.nio.conn.ClientAsyncConnection;
import org.apache.http.nio.reactor.IOSession;
import org.apache.http.pool.PoolEntry;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
@Deprecated
class HttpPoolEntry
extends PoolEntry<HttpRoute, IOSession> {
    private final Log log;
    private final RouteTracker tracker;

    HttpPoolEntry(Log log, String id, HttpRoute route, IOSession session, long timeToLive, TimeUnit tunit) {
        super(id, route, session, timeToLive, tunit);
        this.log = log;
        this.tracker = new RouteTracker(route);
    }

    @Override
    public boolean isExpired(long now) {
        boolean expired = super.isExpired(now);
        if (expired && this.log.isDebugEnabled()) {
            this.log.debug("Connection " + this + " expired @ " + new Date(this.getExpiry()));
        }
        return expired;
    }

    public ClientAsyncConnection getOperatedClientConnection() {
        IOSession session = (IOSession)this.getConnection();
        return (ClientAsyncConnection)session.getAttribute("http.connection");
    }

    @Override
    public void close() {
        block2 : {
            try {
                this.getOperatedClientConnection().shutdown();
            }
            catch (IOException ex) {
                if (!this.log.isDebugEnabled()) break block2;
                this.log.debug("I/O error shutting down connection", ex);
            }
        }
    }

    @Override
    public boolean isClosed() {
        IOSession session = (IOSession)this.getConnection();
        return session.isClosed();
    }

    HttpRoute getPlannedRoute() {
        return (HttpRoute)super.getRoute();
    }

    RouteTracker getTracker() {
        return this.tracker;
    }

    HttpRoute getEffectiveRoute() {
        return this.tracker.toRoute();
    }
}

