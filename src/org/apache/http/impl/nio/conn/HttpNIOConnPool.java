/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.impl.nio.conn;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.commons.logging.Log;
import org.apache.http.HttpHost;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.nio.conn.HttpNIOConnPoolFactory;
import org.apache.http.impl.nio.conn.HttpPoolEntry;
import org.apache.http.nio.conn.scheme.AsyncScheme;
import org.apache.http.nio.conn.scheme.AsyncSchemeRegistry;
import org.apache.http.nio.pool.AbstractNIOConnPool;
import org.apache.http.nio.pool.NIOConnFactory;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.nio.reactor.IOSession;
import org.apache.http.pool.PoolEntry;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
@Deprecated
class HttpNIOConnPool
extends AbstractNIOConnPool<HttpRoute, IOSession, HttpPoolEntry> {
    private static final AtomicLong COUNTER = new AtomicLong(1L);
    private final Log log;
    private final AsyncSchemeRegistry schemeRegistry;
    private final long connTimeToLive;
    private final TimeUnit tunit;

    HttpNIOConnPool(Log log, ConnectingIOReactor ioreactor, AsyncSchemeRegistry schemeRegistry, long connTimeToLive, TimeUnit tunit) {
        super(ioreactor, new HttpNIOConnPoolFactory(), 2, 20);
        this.log = log;
        this.schemeRegistry = schemeRegistry;
        this.connTimeToLive = connTimeToLive;
        this.tunit = tunit;
    }

    @Override
    protected SocketAddress resolveLocalAddress(HttpRoute route) {
        return new InetSocketAddress(route.getLocalAddress(), 0);
    }

    @Override
    protected SocketAddress resolveRemoteAddress(HttpRoute route) {
        HttpHost firsthop = route.getProxyHost();
        if (firsthop == null) {
            firsthop = route.getTargetHost();
        }
        String hostname = firsthop.getHostName();
        int port = firsthop.getPort();
        if (port < 0) {
            AsyncScheme scheme = this.schemeRegistry.getScheme(firsthop);
            port = scheme.resolvePort(port);
        }
        return new InetSocketAddress(hostname, port);
    }

    @Override
    protected HttpPoolEntry createEntry(HttpRoute route, IOSession session) {
        String id = Long.toString(COUNTER.getAndIncrement());
        return new HttpPoolEntry(this.log, id, route, session, this.connTimeToLive, this.tunit);
    }
}

