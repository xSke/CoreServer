/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.impl.nio.conn;

import java.io.IOException;
import java.net.InetAddress;
import javax.net.ssl.SSLSession;
import org.apache.http.HttpConnectionMetrics;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.impl.conn.ConnectionShutdownException;
import org.apache.http.impl.nio.conn.CPoolEntry;
import org.apache.http.nio.NHttpClientConnection;
import org.apache.http.nio.conn.ManagedNHttpClientConnection;
import org.apache.http.nio.reactor.IOSession;
import org.apache.http.protocol.HttpContext;

@NotThreadSafe
class CPoolProxy
implements ManagedNHttpClientConnection {
    private volatile CPoolEntry poolEntry;

    CPoolProxy(CPoolEntry entry) {
        this.poolEntry = entry;
    }

    CPoolEntry getPoolEntry() {
        return this.poolEntry;
    }

    CPoolEntry detach() {
        CPoolEntry local = this.poolEntry;
        this.poolEntry = null;
        return local;
    }

    ManagedNHttpClientConnection getConnection() {
        CPoolEntry local = this.poolEntry;
        if (local == null) {
            return null;
        }
        return (ManagedNHttpClientConnection)local.getConnection();
    }

    ManagedNHttpClientConnection getValidConnection() {
        ManagedNHttpClientConnection conn = this.getConnection();
        if (conn == null) {
            throw new ConnectionShutdownException();
        }
        return conn;
    }

    public void close() throws IOException {
        CPoolEntry local = this.poolEntry;
        if (local != null) {
            local.closeConnection();
        }
    }

    public void shutdown() throws IOException {
        CPoolEntry local = this.poolEntry;
        if (local != null) {
            local.shutdownConnection();
        }
    }

    public HttpConnectionMetrics getMetrics() {
        return this.getValidConnection().getMetrics();
    }

    public void requestInput() {
        ManagedNHttpClientConnection conn = this.getConnection();
        if (conn != null) {
            conn.requestInput();
        }
    }

    public void suspendInput() {
        ManagedNHttpClientConnection conn = this.getConnection();
        if (conn != null) {
            conn.suspendInput();
        }
    }

    public void requestOutput() {
        ManagedNHttpClientConnection conn = this.getConnection();
        if (conn != null) {
            conn.requestOutput();
        }
    }

    public void suspendOutput() {
        ManagedNHttpClientConnection conn = this.getConnection();
        if (conn != null) {
            conn.suspendOutput();
        }
    }

    public InetAddress getLocalAddress() {
        return this.getValidConnection().getLocalAddress();
    }

    public int getLocalPort() {
        return this.getValidConnection().getLocalPort();
    }

    public InetAddress getRemoteAddress() {
        return this.getValidConnection().getRemoteAddress();
    }

    public int getRemotePort() {
        return this.getValidConnection().getRemotePort();
    }

    public boolean isOpen() {
        CPoolEntry local = this.poolEntry;
        if (local != null) {
            return !local.isClosed();
        }
        return false;
    }

    public boolean isStale() {
        ManagedNHttpClientConnection conn = this.getConnection();
        if (conn != null) {
            return conn.isStale() || !conn.isOpen();
        }
        return true;
    }

    public void setSocketTimeout(int i) {
        this.getValidConnection().setSocketTimeout(i);
    }

    public int getSocketTimeout() {
        return this.getValidConnection().getSocketTimeout();
    }

    public void submitRequest(HttpRequest request) throws IOException, HttpException {
        this.getValidConnection().submitRequest(request);
    }

    public boolean isRequestSubmitted() {
        return this.getValidConnection().isRequestSubmitted();
    }

    public void resetOutput() {
        this.getValidConnection().resetOutput();
    }

    public void resetInput() {
        this.getValidConnection().resetInput();
    }

    public int getStatus() {
        return this.getValidConnection().getStatus();
    }

    public HttpRequest getHttpRequest() {
        return this.getValidConnection().getHttpRequest();
    }

    public HttpResponse getHttpResponse() {
        return this.getValidConnection().getHttpResponse();
    }

    public HttpContext getContext() {
        return this.getValidConnection().getContext();
    }

    public static NHttpClientConnection newProxy(CPoolEntry poolEntry) {
        return new CPoolProxy(poolEntry);
    }

    private static CPoolProxy getProxy(NHttpClientConnection conn) {
        if (!CPoolProxy.class.isInstance(conn)) {
            throw new IllegalStateException("Unexpected connection proxy class: " + conn.getClass());
        }
        return CPoolProxy.class.cast(conn);
    }

    public static CPoolEntry getPoolEntry(NHttpClientConnection proxy) {
        CPoolEntry entry = CPoolProxy.getProxy(proxy).getPoolEntry();
        if (entry == null) {
            throw new ConnectionShutdownException();
        }
        return entry;
    }

    public static CPoolEntry detach(NHttpClientConnection proxy) {
        return CPoolProxy.getProxy(proxy).detach();
    }

    public String getId() {
        return this.getValidConnection().getId();
    }

    public void bind(IOSession iosession) {
        this.getValidConnection().bind(iosession);
    }

    public IOSession getIOSession() {
        return this.getValidConnection().getIOSession();
    }

    public SSLSession getSSLSession() {
        return this.getValidConnection().getSSLSession();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("CPoolProxy{");
        ManagedNHttpClientConnection conn = this.getConnection();
        if (conn != null) {
            sb.append(conn);
        } else {
            sb.append("detached");
        }
        sb.append('}');
        return sb.toString();
    }
}

