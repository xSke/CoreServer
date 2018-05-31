/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.impl.nio.conn;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHost;
import org.apache.http.annotation.ThreadSafe;
import org.apache.http.concurrent.BasicFuture;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.Lookup;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.DnsResolver;
import org.apache.http.conn.SchemePortResolver;
import org.apache.http.conn.UnsupportedSchemeException;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.conn.DefaultSchemePortResolver;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;
import org.apache.http.impl.nio.conn.CPool;
import org.apache.http.impl.nio.conn.CPoolEntry;
import org.apache.http.impl.nio.conn.CPoolProxy;
import org.apache.http.impl.nio.conn.ManagedNHttpClientConnectionFactory;
import org.apache.http.nio.NHttpClientConnection;
import org.apache.http.nio.conn.ManagedNHttpClientConnection;
import org.apache.http.nio.conn.NHttpClientConnectionManager;
import org.apache.http.nio.conn.NHttpConnectionFactory;
import org.apache.http.nio.conn.NoopIOSessionStrategy;
import org.apache.http.nio.conn.SchemeIOSessionStrategy;
import org.apache.http.nio.conn.ssl.SSLIOSessionStrategy;
import org.apache.http.nio.pool.NIOConnFactory;
import org.apache.http.nio.pool.SocketAddressResolver;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.nio.reactor.IOEventDispatch;
import org.apache.http.nio.reactor.IOSession;
import org.apache.http.pool.ConnPoolControl;
import org.apache.http.pool.PoolStats;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Args;
import org.apache.http.util.Asserts;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
@ThreadSafe
public class PoolingNHttpClientConnectionManager
implements NHttpClientConnectionManager,
ConnPoolControl<HttpRoute> {
    private final Log log = LogFactory.getLog(this.getClass());
    static final String IOSESSION_FACTORY_REGISTRY = "http.iosession-factory-registry";
    private final ConnectingIOReactor ioreactor;
    private final ConfigData configData;
    private final CPool pool;
    private final Registry<SchemeIOSessionStrategy> iosessionFactoryRegistry;

    private static Registry<SchemeIOSessionStrategy> getDefaultRegistry() {
        return RegistryBuilder.create().register("http", NoopIOSessionStrategy.INSTANCE).register("https", (NoopIOSessionStrategy)((Object)SSLIOSessionStrategy.getDefaultStrategy())).build();
    }

    public PoolingNHttpClientConnectionManager(ConnectingIOReactor ioreactor) {
        this(ioreactor, PoolingNHttpClientConnectionManager.getDefaultRegistry());
    }

    public PoolingNHttpClientConnectionManager(ConnectingIOReactor ioreactor, Registry<SchemeIOSessionStrategy> iosessionFactoryRegistry) {
        this(ioreactor, null, iosessionFactoryRegistry, null);
    }

    public PoolingNHttpClientConnectionManager(ConnectingIOReactor ioreactor, NHttpConnectionFactory<ManagedNHttpClientConnection> connFactory, DnsResolver dnsResolver) {
        this(ioreactor, connFactory, PoolingNHttpClientConnectionManager.getDefaultRegistry(), dnsResolver);
    }

    public PoolingNHttpClientConnectionManager(ConnectingIOReactor ioreactor, NHttpConnectionFactory<ManagedNHttpClientConnection> connFactory) {
        this(ioreactor, connFactory, PoolingNHttpClientConnectionManager.getDefaultRegistry(), null);
    }

    public PoolingNHttpClientConnectionManager(ConnectingIOReactor ioreactor, NHttpConnectionFactory<ManagedNHttpClientConnection> connFactory, Registry<SchemeIOSessionStrategy> iosessionFactoryRegistry) {
        this(ioreactor, connFactory, iosessionFactoryRegistry, null);
    }

    public PoolingNHttpClientConnectionManager(ConnectingIOReactor ioreactor, NHttpConnectionFactory<ManagedNHttpClientConnection> connFactory, Registry<SchemeIOSessionStrategy> iosessionFactoryRegistry, DnsResolver dnsResolver) {
        this(ioreactor, connFactory, iosessionFactoryRegistry, null, dnsResolver, -1L, TimeUnit.MILLISECONDS);
    }

    public PoolingNHttpClientConnectionManager(ConnectingIOReactor ioreactor, NHttpConnectionFactory<ManagedNHttpClientConnection> connFactory, Registry<SchemeIOSessionStrategy> iosessionFactoryRegistry, SchemePortResolver schemePortResolver, DnsResolver dnsResolver, long timeToLive, TimeUnit tunit) {
        Args.notNull(ioreactor, "I/O reactor");
        Args.notNull(iosessionFactoryRegistry, "I/O session factory registry");
        this.ioreactor = ioreactor;
        this.configData = new ConfigData();
        this.pool = new CPool(ioreactor, new InternalConnectionFactory(this.configData, connFactory), new InternalAddressResolver(schemePortResolver, dnsResolver), 2, 20, timeToLive, tunit != null ? tunit : TimeUnit.MILLISECONDS);
        this.iosessionFactoryRegistry = iosessionFactoryRegistry;
    }

    PoolingNHttpClientConnectionManager(ConnectingIOReactor ioreactor, CPool pool, Registry<SchemeIOSessionStrategy> iosessionFactoryRegistry) {
        this.ioreactor = ioreactor;
        this.configData = new ConfigData();
        this.pool = pool;
        this.iosessionFactoryRegistry = iosessionFactoryRegistry;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void finalize() throws Throwable {
        try {
            this.shutdown();
        }
        finally {
            super.finalize();
        }
    }

    @Override
    public void execute(IOEventDispatch eventDispatch) throws IOException {
        this.ioreactor.execute(eventDispatch);
    }

    public void shutdown(long waitMs) throws IOException {
        this.log.debug("Connection manager is shutting down");
        this.pool.shutdown(waitMs);
        this.log.debug("Connection manager shut down");
    }

    @Override
    public void shutdown() throws IOException {
        this.log.debug("Connection manager is shutting down");
        this.pool.shutdown(2000L);
        this.log.debug("Connection manager shut down");
    }

    private String format(HttpRoute route, Object state) {
        StringBuilder buf = new StringBuilder();
        buf.append("[route: ").append(route).append("]");
        if (state != null) {
            buf.append("[state: ").append(state).append("]");
        }
        return buf.toString();
    }

    private String formatStats(HttpRoute route) {
        StringBuilder buf = new StringBuilder();
        PoolStats totals = this.pool.getTotalStats();
        PoolStats stats = this.pool.getStats(route);
        buf.append("[total kept alive: ").append(totals.getAvailable()).append("; ");
        buf.append("route allocated: ").append(stats.getLeased() + stats.getAvailable());
        buf.append(" of ").append(stats.getMax()).append("; ");
        buf.append("total allocated: ").append(totals.getLeased() + totals.getAvailable());
        buf.append(" of ").append(totals.getMax()).append("]");
        return buf.toString();
    }

    private String format(CPoolEntry entry) {
        StringBuilder buf = new StringBuilder();
        buf.append("[id: ").append(entry.getId()).append("]");
        buf.append("[route: ").append(entry.getRoute()).append("]");
        Object state = entry.getState();
        if (state != null) {
            buf.append("[state: ").append(state).append("]");
        }
        return buf.toString();
    }

    @Override
    public Future<NHttpClientConnection> requestConnection(HttpRoute route, Object state, long connectTimeout, long leaseTimeout, TimeUnit tunit, FutureCallback<NHttpClientConnection> callback) {
        Args.notNull(route, "HTTP route");
        if (this.log.isDebugEnabled()) {
            this.log.debug("Connection request: " + this.format(route, state) + this.formatStats(route));
        }
        BasicFuture<NHttpClientConnection> future = new BasicFuture<NHttpClientConnection>(callback);
        HttpHost host = route.getProxyHost() != null ? route.getProxyHost() : route.getTargetHost();
        SchemeIOSessionStrategy sf = this.iosessionFactoryRegistry.lookup(host.getSchemeName());
        if (sf == null) {
            future.failed(new UnsupportedSchemeException(host.getSchemeName() + " protocol is not supported"));
            return future;
        }
        this.pool.lease(route, state, connectTimeout, leaseTimeout, tunit != null ? tunit : TimeUnit.MILLISECONDS, new InternalPoolEntryCallback(future));
        return future;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void releaseConnection(NHttpClientConnection managedConn, Object state, long keepalive, TimeUnit tunit) {
        Args.notNull(managedConn, "Managed connection");
        NHttpClientConnection nHttpClientConnection = managedConn;
        synchronized (nHttpClientConnection) {
            NHttpClientConnection conn;
            CPoolEntry entry;
            block9 : {
                entry = CPoolProxy.detach(managedConn);
                if (entry == null) {
                    return;
                }
                if (this.log.isDebugEnabled()) {
                    this.log.debug("Releasing connection: " + this.format(entry) + this.formatStats((HttpRoute)entry.getRoute()));
                }
                conn = (NHttpClientConnection)entry.getConnection();
                try {
                    if (!conn.isOpen()) break block9;
                    entry.setState(state);
                    entry.updateExpiry(keepalive, tunit != null ? tunit : TimeUnit.MILLISECONDS);
                    if (!this.log.isDebugEnabled()) break block9;
                    String s = keepalive > 0L ? "for " + (double)keepalive / 1000.0 + " seconds" : "indefinitely";
                    this.log.debug("Connection " + this.format(entry) + " can be kept alive " + s);
                }
                catch (Throwable throwable) {
                    this.pool.release(entry, conn.isOpen() && entry.isRouteComplete());
                    if (this.log.isDebugEnabled()) {
                        this.log.debug("Connection released: " + this.format(entry) + this.formatStats((HttpRoute)entry.getRoute()));
                    }
                    throw throwable;
                }
            }
            this.pool.release(entry, conn.isOpen() && entry.isRouteComplete());
            if (this.log.isDebugEnabled()) {
                this.log.debug("Connection released: " + this.format(entry) + this.formatStats((HttpRoute)entry.getRoute()));
            }
        }
    }

    private Lookup<SchemeIOSessionStrategy> getIOSessionFactoryRegistry(HttpContext context) {
        Registry<SchemeIOSessionStrategy> reg = (Registry<SchemeIOSessionStrategy>)context.getAttribute(IOSESSION_FACTORY_REGISTRY);
        if (reg == null) {
            reg = this.iosessionFactoryRegistry;
        }
        return reg;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void startRoute(NHttpClientConnection managedConn, HttpRoute route, HttpContext context) throws IOException {
        Args.notNull(managedConn, "Managed connection");
        Args.notNull(route, "HTTP route");
        HttpHost host = route.getProxyHost() != null ? route.getProxyHost() : route.getTargetHost();
        Lookup<SchemeIOSessionStrategy> reg = this.getIOSessionFactoryRegistry(context);
        SchemeIOSessionStrategy sf = reg.lookup(host.getSchemeName());
        if (sf == null) {
            throw new UnsupportedSchemeException(host.getSchemeName() + " protocol is not supported");
        }
        if (sf.isLayeringRequired()) {
            NHttpClientConnection nHttpClientConnection = managedConn;
            synchronized (nHttpClientConnection) {
                CPoolEntry entry = CPoolProxy.getPoolEntry(managedConn);
                ManagedNHttpClientConnection conn = (ManagedNHttpClientConnection)entry.getConnection();
                IOSession currentSession = sf.upgrade(host, conn.getIOSession());
                conn.bind(currentSession);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void upgrade(NHttpClientConnection managedConn, HttpRoute route, HttpContext context) throws IOException {
        Args.notNull(managedConn, "Managed connection");
        Args.notNull(route, "HTTP route");
        HttpHost host = route.getTargetHost();
        Lookup<SchemeIOSessionStrategy> reg = this.getIOSessionFactoryRegistry(context);
        SchemeIOSessionStrategy sf = reg.lookup(host.getSchemeName());
        if (sf == null) {
            throw new UnsupportedSchemeException(host.getSchemeName() + " protocol is not supported");
        }
        if (!sf.isLayeringRequired()) {
            throw new UnsupportedSchemeException(host.getSchemeName() + " protocol does not support connection upgrade");
        }
        NHttpClientConnection nHttpClientConnection = managedConn;
        synchronized (nHttpClientConnection) {
            CPoolEntry entry = CPoolProxy.getPoolEntry(managedConn);
            ManagedNHttpClientConnection conn = (ManagedNHttpClientConnection)entry.getConnection();
            IOSession currentSession = sf.upgrade(host, conn.getIOSession());
            conn.bind(currentSession);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void routeComplete(NHttpClientConnection managedConn, HttpRoute route, HttpContext context) {
        Args.notNull(managedConn, "Managed connection");
        Args.notNull(route, "HTTP route");
        NHttpClientConnection nHttpClientConnection = managedConn;
        synchronized (nHttpClientConnection) {
            CPoolEntry entry = CPoolProxy.getPoolEntry(managedConn);
            entry.markRouteComplete();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean isRouteComplete(NHttpClientConnection managedConn) {
        Args.notNull(managedConn, "Managed connection");
        NHttpClientConnection nHttpClientConnection = managedConn;
        synchronized (nHttpClientConnection) {
            CPoolEntry entry = CPoolProxy.getPoolEntry(managedConn);
            return entry.isRouteComplete();
        }
    }

    @Override
    public void closeIdleConnections(long idleTimeout, TimeUnit tunit) {
        if (this.log.isDebugEnabled()) {
            this.log.debug("Closing connections idle longer than " + idleTimeout + " " + (Object)((Object)tunit));
        }
        this.pool.closeIdle(idleTimeout, tunit);
    }

    @Override
    public void closeExpiredConnections() {
        this.log.debug("Closing expired connections");
        this.pool.closeExpired();
    }

    @Override
    public int getMaxTotal() {
        return this.pool.getMaxTotal();
    }

    @Override
    public void setMaxTotal(int max) {
        this.pool.setMaxTotal(max);
    }

    @Override
    public int getDefaultMaxPerRoute() {
        return this.pool.getDefaultMaxPerRoute();
    }

    @Override
    public void setDefaultMaxPerRoute(int max) {
        this.pool.setDefaultMaxPerRoute(max);
    }

    @Override
    public int getMaxPerRoute(HttpRoute route) {
        return this.pool.getMaxPerRoute(route);
    }

    @Override
    public void setMaxPerRoute(HttpRoute route, int max) {
        this.pool.setMaxPerRoute(route, max);
    }

    @Override
    public PoolStats getTotalStats() {
        return this.pool.getTotalStats();
    }

    @Override
    public PoolStats getStats(HttpRoute route) {
        return this.pool.getStats(route);
    }

    public ConnectionConfig getDefaultConnectionConfig() {
        return this.configData.getDefaultConnectionConfig();
    }

    public void setDefaultConnectionConfig(ConnectionConfig defaultConnectionConfig) {
        this.configData.setDefaultConnectionConfig(defaultConnectionConfig);
    }

    public ConnectionConfig getConnectionConfig(HttpHost host) {
        return this.configData.getConnectionConfig(host);
    }

    public void setConnectionConfig(HttpHost host, ConnectionConfig connectionConfig) {
        this.configData.setConnectionConfig(host, connectionConfig);
    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    static class InternalAddressResolver
    implements SocketAddressResolver<HttpRoute> {
        private final SchemePortResolver schemePortResolver;
        private final DnsResolver dnsResolver;

        public InternalAddressResolver(SchemePortResolver schemePortResolver, DnsResolver dnsResolver) {
            this.schemePortResolver = schemePortResolver != null ? schemePortResolver : DefaultSchemePortResolver.INSTANCE;
            this.dnsResolver = dnsResolver != null ? dnsResolver : SystemDefaultDnsResolver.INSTANCE;
        }

        @Override
        public SocketAddress resolveLocalAddress(HttpRoute route) throws IOException {
            return route.getLocalAddress() != null ? new InetSocketAddress(route.getLocalAddress(), 0) : null;
        }

        @Override
        public SocketAddress resolveRemoteAddress(HttpRoute route) throws IOException {
            HttpHost host = route.getProxyHost() != null ? route.getProxyHost() : route.getTargetHost();
            int port = this.schemePortResolver.resolve(host);
            InetAddress[] addresses = this.dnsResolver.resolve(host.getHostName());
            return new InetSocketAddress(addresses[0], port);
        }
    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    static class InternalConnectionFactory
    implements NIOConnFactory<HttpRoute, ManagedNHttpClientConnection> {
        private final ConfigData configData;
        private final NHttpConnectionFactory<ManagedNHttpClientConnection> connFactory;

        InternalConnectionFactory(ConfigData configData, NHttpConnectionFactory<ManagedNHttpClientConnection> connFactory) {
            this.configData = configData != null ? configData : new ConfigData();
            this.connFactory = connFactory != null ? connFactory : ManagedNHttpClientConnectionFactory.INSTANCE;
        }

        @Override
        public ManagedNHttpClientConnection create(HttpRoute route, IOSession iosession) throws IOException {
            ConnectionConfig config = null;
            if (route.getProxyHost() != null) {
                config = this.configData.getConnectionConfig(route.getProxyHost());
            }
            if (config == null) {
                config = this.configData.getConnectionConfig(route.getTargetHost());
            }
            if (config == null) {
                config = this.configData.getDefaultConnectionConfig();
            }
            if (config == null) {
                config = ConnectionConfig.DEFAULT;
            }
            ManagedNHttpClientConnection conn = this.connFactory.create(iosession, config);
            iosession.setAttribute("http.connection", conn);
            return conn;
        }
    }

    static class ConfigData {
        private final Map<HttpHost, ConnectionConfig> connectionConfigMap = new ConcurrentHashMap<HttpHost, ConnectionConfig>();
        private volatile ConnectionConfig defaultConnectionConfig;

        ConfigData() {
        }

        public ConnectionConfig getDefaultConnectionConfig() {
            return this.defaultConnectionConfig;
        }

        public void setDefaultConnectionConfig(ConnectionConfig defaultConnectionConfig) {
            this.defaultConnectionConfig = defaultConnectionConfig;
        }

        public ConnectionConfig getConnectionConfig(HttpHost host) {
            return this.connectionConfigMap.get(host);
        }

        public void setConnectionConfig(HttpHost host, ConnectionConfig connectionConfig) {
            this.connectionConfigMap.put(host, connectionConfig);
        }
    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    class InternalPoolEntryCallback
    implements FutureCallback<CPoolEntry> {
        private final BasicFuture<NHttpClientConnection> future;

        public InternalPoolEntryCallback(BasicFuture<NHttpClientConnection> future) {
            this.future = future;
        }

        @Override
        public void completed(CPoolEntry entry) {
            NHttpClientConnection managedConn;
            Asserts.check(entry.getConnection() != null, "Pool entry with no connection");
            if (PoolingNHttpClientConnectionManager.this.log.isDebugEnabled()) {
                PoolingNHttpClientConnectionManager.this.log.debug("Connection leased: " + PoolingNHttpClientConnectionManager.this.format(entry) + PoolingNHttpClientConnectionManager.this.formatStats((HttpRoute)entry.getRoute()));
            }
            if (!this.future.completed(managedConn = CPoolProxy.newProxy(entry))) {
                PoolingNHttpClientConnectionManager.this.pool.release(entry, true);
            }
        }

        @Override
        public void failed(Exception ex) {
            if (PoolingNHttpClientConnectionManager.this.log.isDebugEnabled()) {
                PoolingNHttpClientConnectionManager.this.log.debug("Connection request failed", ex);
            }
            this.future.failed(ex);
        }

        @Override
        public void cancelled() {
            PoolingNHttpClientConnectionManager.this.log.debug("Connection request cancelled");
            this.future.cancel(true);
        }
    }

}

