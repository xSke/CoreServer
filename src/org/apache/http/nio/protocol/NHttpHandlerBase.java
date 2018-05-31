/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.protocol;

import java.io.IOException;
import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.RequestLine;
import org.apache.http.StatusLine;
import org.apache.http.annotation.Immutable;
import org.apache.http.nio.NHttpConnection;
import org.apache.http.nio.protocol.EventListener;
import org.apache.http.nio.util.ByteBufferAllocator;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.util.Args;

@Deprecated
@Immutable
public abstract class NHttpHandlerBase {
    protected static final String CONN_STATE = "http.nio.conn-state";
    protected final HttpProcessor httpProcessor;
    protected final ConnectionReuseStrategy connStrategy;
    protected final ByteBufferAllocator allocator;
    protected final HttpParams params;
    protected EventListener eventListener;

    public NHttpHandlerBase(HttpProcessor httpProcessor, ConnectionReuseStrategy connStrategy, ByteBufferAllocator allocator, HttpParams params) {
        Args.notNull(httpProcessor, "HTTP processor");
        Args.notNull(connStrategy, "Connection reuse strategy");
        Args.notNull(allocator, "ByteBuffer allocator");
        Args.notNull(params, "HTTP parameters");
        this.httpProcessor = httpProcessor;
        this.connStrategy = connStrategy;
        this.allocator = allocator;
        this.params = params;
    }

    public HttpParams getParams() {
        return this.params;
    }

    public void setEventListener(EventListener eventListener) {
        this.eventListener = eventListener;
    }

    protected void closeConnection(NHttpConnection conn, Throwable cause) {
        try {
            conn.close();
        }
        catch (IOException ex) {
            try {
                conn.shutdown();
            }
            catch (IOException ignore) {
                // empty catch block
            }
        }
    }

    protected void shutdownConnection(NHttpConnection conn, Throwable cause) {
        try {
            conn.shutdown();
        }
        catch (IOException ignore) {
            // empty catch block
        }
    }

    protected void handleTimeout(NHttpConnection conn) {
        try {
            if (conn.getStatus() == 0) {
                conn.close();
                if (conn.getStatus() == 1) {
                    conn.setSocketTimeout(250);
                }
                if (this.eventListener != null) {
                    this.eventListener.connectionTimeout(conn);
                }
            } else {
                conn.shutdown();
            }
        }
        catch (IOException ignore) {
            // empty catch block
        }
    }

    protected boolean canResponseHaveBody(HttpRequest request, HttpResponse response) {
        if (request != null && "HEAD".equalsIgnoreCase(request.getRequestLine().getMethod())) {
            return false;
        }
        int status = response.getStatusLine().getStatusCode();
        return status >= 200 && status != 204 && status != 304 && status != 205;
    }
}

