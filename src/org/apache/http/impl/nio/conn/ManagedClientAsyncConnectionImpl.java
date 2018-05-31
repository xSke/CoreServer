/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.impl.nio.conn;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLSession;
import org.apache.http.HttpConnectionMetrics;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.routing.RouteTracker;
import org.apache.http.impl.conn.ConnectionShutdownException;
import org.apache.http.impl.nio.conn.HttpPoolEntry;
import org.apache.http.nio.conn.ClientAsyncConnection;
import org.apache.http.nio.conn.ClientAsyncConnectionFactory;
import org.apache.http.nio.conn.ClientAsyncConnectionManager;
import org.apache.http.nio.conn.ManagedClientAsyncConnection;
import org.apache.http.nio.conn.scheme.AsyncScheme;
import org.apache.http.nio.conn.scheme.AsyncSchemeRegistry;
import org.apache.http.nio.conn.scheme.LayeringStrategy;
import org.apache.http.nio.reactor.IOSession;
import org.apache.http.nio.reactor.ssl.SSLIOSession;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

@Deprecated
class ManagedClientAsyncConnectionImpl
implements ManagedClientAsyncConnection {
    private final ClientAsyncConnectionManager manager;
    private final ClientAsyncConnectionFactory connFactory;
    private volatile HttpPoolEntry poolEntry;
    private volatile boolean reusable;
    private volatile long duration;

    ManagedClientAsyncConnectionImpl(ClientAsyncConnectionManager manager, ClientAsyncConnectionFactory connFactory, HttpPoolEntry poolEntry) {
        this.manager = manager;
        this.connFactory = connFactory;
        this.poolEntry = poolEntry;
        this.reusable = true;
        this.duration = Long.MAX_VALUE;
    }

    HttpPoolEntry getPoolEntry() {
        return this.poolEntry;
    }

    HttpPoolEntry detach() {
        HttpPoolEntry local = this.poolEntry;
        this.poolEntry = null;
        return local;
    }

    public ClientAsyncConnectionManager getManager() {
        return this.manager;
    }

    private ClientAsyncConnection getConnection() {
        HttpPoolEntry local = this.poolEntry;
        if (local == null) {
            return null;
        }
        IOSession session = (IOSession)local.getConnection();
        return (ClientAsyncConnection)session.getAttribute("http.connection");
    }

    private ClientAsyncConnection ensureConnection() {
        HttpPoolEntry local = this.poolEntry;
        if (local == null) {
            throw new ConnectionShutdownException();
        }
        IOSession session = (IOSession)local.getConnection();
        return (ClientAsyncConnection)session.getAttribute("http.connection");
    }

    private HttpPoolEntry ensurePoolEntry() {
        HttpPoolEntry local = this.poolEntry;
        if (local == null) {
            throw new ConnectionShutdownException();
        }
        return local;
    }

    public void close() throws IOException {
        ClientAsyncConnection conn = this.getConnection();
        if (conn != null) {
            conn.close();
        }
    }

    public void shutdown() throws IOException {
        ClientAsyncConnection conn = this.getConnection();
        if (conn != null) {
            conn.shutdown();
        }
    }

    public boolean isOpen() {
        ClientAsyncConnection conn = this.getConnection();
        if (conn != null) {
            return conn.isOpen();
        }
        return false;
    }

    public boolean isStale() {
        return this.isOpen();
    }

    public void setSocketTimeout(int timeout) {
        ClientAsyncConnection conn = this.ensureConnection();
        conn.setSocketTimeout(timeout);
    }

    public int getSocketTimeout() {
        ClientAsyncConnection conn = this.ensureConnection();
        return conn.getSocketTimeout();
    }

    public HttpConnectionMetrics getMetrics() {
        ClientAsyncConnection conn = this.ensureConnection();
        return conn.getMetrics();
    }

    public InetAddress getLocalAddress() {
        ClientAsyncConnection conn = this.ensureConnection();
        return conn.getLocalAddress();
    }

    public int getLocalPort() {
        ClientAsyncConnection conn = this.ensureConnection();
        return conn.getLocalPort();
    }

    public InetAddress getRemoteAddress() {
        ClientAsyncConnection conn = this.ensureConnection();
        return conn.getRemoteAddress();
    }

    public int getRemotePort() {
        ClientAsyncConnection conn = this.ensureConnection();
        return conn.getRemotePort();
    }

    public int getStatus() {
        ClientAsyncConnection conn = this.ensureConnection();
        return conn.getStatus();
    }

    public HttpRequest getHttpRequest() {
        ClientAsyncConnection conn = this.ensureConnection();
        return conn.getHttpRequest();
    }

    public HttpResponse getHttpResponse() {
        ClientAsyncConnection conn = this.ensureConnection();
        return conn.getHttpResponse();
    }

    public HttpContext getContext() {
        ClientAsyncConnection conn = this.ensureConnection();
        return conn.getContext();
    }

    public void requestInput() {
        ClientAsyncConnection conn = this.ensureConnection();
        conn.requestInput();
    }

    public void suspendInput() {
        ClientAsyncConnection conn = this.ensureConnection();
        conn.suspendInput();
    }

    public void requestOutput() {
        ClientAsyncConnection conn = this.ensureConnection();
        conn.requestOutput();
    }

    public void suspendOutput() {
        ClientAsyncConnection conn = this.ensureConnection();
        conn.suspendOutput();
    }

    public void submitRequest(HttpRequest request) throws IOException, HttpException {
        ClientAsyncConnection conn = this.ensureConnection();
        conn.submitRequest(request);
    }

    public boolean isRequestSubmitted() {
        ClientAsyncConnection conn = this.ensureConnection();
        return conn.isRequestSubmitted();
    }

    public void resetOutput() {
        ClientAsyncConnection conn = this.ensureConnection();
        conn.resetOutput();
    }

    public void resetInput() {
        ClientAsyncConnection conn = this.ensureConnection();
        conn.resetInput();
    }

    public boolean isSecure() {
        ClientAsyncConnection conn = this.ensureConnection();
        return conn.getIOSession() instanceof SSLIOSession;
    }

    public HttpRoute getRoute() {
        HttpPoolEntry entry = this.ensurePoolEntry();
        return entry.getEffectiveRoute();
    }

    public SSLSession getSSLSession() {
        ClientAsyncConnection conn = this.ensureConnection();
        IOSession iosession = conn.getIOSession();
        if (iosession instanceof SSLIOSession) {
            return ((SSLIOSession)iosession).getSSLSession();
        }
        return null;
    }

    public Object getState() {
        HttpPoolEntry entry = this.ensurePoolEntry();
        return entry.getState();
    }

    public void setState(Object state) {
        HttpPoolEntry entry = this.ensurePoolEntry();
        entry.setState(state);
    }

    public void markReusable() {
        this.reusable = true;
    }

    public void unmarkReusable() {
        this.reusable = false;
    }

    public boolean isMarkedReusable() {
        return this.reusable;
    }

    public void setIdleDuration(long duration, TimeUnit unit) {
        this.duration = duration > 0L ? unit.toMillis(duration) : -1L;
    }

    private AsyncSchemeRegistry getSchemeRegistry(HttpContext context) {
        AsyncSchemeRegistry reg = (AsyncSchemeRegistry)context.getAttribute("http.scheme-registry");
        if (reg == null) {
            reg = this.manager.getSchemeRegistry();
        }
        return reg;
    }

    public synchronized void open(HttpRoute route, HttpContext context, HttpParams params) throws IOException {
        AsyncScheme scheme;
        LayeringStrategy layeringStrategy;
        HttpPoolEntry entry = this.ensurePoolEntry();
        RouteTracker tracker = entry.getTracker();
        if (tracker.isConnected()) {
            throw new IllegalStateException("Connection already open");
        }
        HttpHost target = route.getTargetHost();
        HttpHost proxy = route.getProxyHost();
        IOSession iosession = (IOSession)entry.getConnection();
        if (proxy == null && (layeringStrategy = (scheme = this.getSchemeRegistry(context).getScheme(target)).getLayeringStrategy()) != null) {
            iosession = layeringStrategy.layer(iosession);
        }
        ClientAsyncConnection conn = this.connFactory.create("http-outgoing-" + entry.getId(), iosession, params);
        iosession.setAttribute("http.connection", conn);
        if (proxy == null) {
            tracker.connectTarget(conn.getIOSession() instanceof SSLIOSession);
        } else {
            tracker.connectProxy(proxy, false);
        }
    }

    public synchronized void tunnelProxy(HttpHost next, HttpParams params) throws IOException {
        HttpPoolEntry entry = this.ensurePoolEntry();
        RouteTracker tracker = entry.getTracker();
        if (!tracker.isConnected()) {
            throw new IllegalStateException("Connection not open");
        }
        tracker.tunnelProxy(next, false);
    }

    public synchronized void tunnelTarget(HttpParams params) throws IOException {
        HttpPoolEntry entry = this.ensurePoolEntry();
        RouteTracker tracker = entry.getTracker();
        if (!tracker.isConnected()) {
            throw new IllegalStateException("Connection not open");
        }
        if (tracker.isTunnelled()) {
            throw new IllegalStateException("Connection is already tunnelled");
        }
        tracker.tunnelTarget(false);
    }

    public synchronized void layerProtocol(HttpContext context, HttpParams params) throws IOException {
        HttpPoolEntry entry = this.ensurePoolEntry();
        RouteTracker tracker = entry.getTracker();
        if (!tracker.isConnected()) {
            throw new IllegalStateException("Connection not open");
        }
        if (!tracker.isTunnelled()) {
            throw new IllegalStateException("Protocol layering without a tunnel not supported");
        }
        if (tracker.isLayered()) {
            throw new IllegalStateException("Multiple protocol layering not supported");
        }
        HttpHost target = tracker.getTargetHost();
        AsyncScheme scheme = this.getSchemeRegistry(context).getScheme(target);
        LayeringStrategy layeringStrategy = scheme.getLayeringStrategy();
        if (layeringStrategy == null) {
            throw new IllegalStateException(scheme.getName() + " scheme does not provider support for protocol layering");
        }
        IOSession iosession = (IOSession)entry.getConnection();
        ClientAsyncConnection conn = (ClientAsyncConnection)iosession.getAttribute("http.connection");
        conn.upgrade((SSLIOSession)layeringStrategy.layer(iosession));
        tracker.layerProtocol(layeringStrategy.isSecure());
    }

    public synchronized void releaseConnection() {
        if (this.poolEntry == null) {
            return;
        }
        this.manager.releaseConnection(this, this.duration, TimeUnit.MILLISECONDS);
        this.poolEntry = null;
    }

    public synchronized void abortConnection() {
        if (this.poolEntry == null) {
            return;
        }
        this.reusable = false;
        IOSession iosession = (IOSession)this.poolEntry.getConnection();
        ClientAsyncConnection conn = (ClientAsyncConnection)iosession.getAttribute("http.connection");
        try {
            conn.shutdown();
        }
        catch (IOException ignore) {
            // empty catch block
        }
        this.manager.releaseConnection(this, this.duration, TimeUnit.MILLISECONDS);
        this.poolEntry = null;
    }

    public synchronized String toString() {
        if (this.poolEntry != null) {
            return this.poolEntry.toString();
        }
        return "released";
    }
}

