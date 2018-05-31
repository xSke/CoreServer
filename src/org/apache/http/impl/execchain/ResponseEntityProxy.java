/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.impl.execchain;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.conn.EofSensorInputStream;
import org.apache.http.conn.EofSensorWatcher;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.impl.execchain.ConnectionHolder;

@NotThreadSafe
class ResponseEntityProxy
extends HttpEntityWrapper
implements EofSensorWatcher {
    private final ConnectionHolder connHolder;

    public static void enchance(HttpResponse response, ConnectionHolder connHolder) {
        HttpEntity entity = response.getEntity();
        if (entity != null && entity.isStreaming() && connHolder != null) {
            response.setEntity(new ResponseEntityProxy(entity, connHolder));
        }
    }

    ResponseEntityProxy(HttpEntity entity, ConnectionHolder connHolder) {
        super(entity);
        this.connHolder = connHolder;
    }

    private void cleanup() {
        if (this.connHolder != null) {
            this.connHolder.abortConnection();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void releaseConnection() throws IOException {
        if (this.connHolder != null) {
            try {
                if (this.connHolder.isReusable()) {
                    this.connHolder.releaseConnection();
                }
            }
            finally {
                this.cleanup();
            }
        }
    }

    public boolean isRepeatable() {
        return false;
    }

    public InputStream getContent() throws IOException {
        return new EofSensorInputStream(this.wrappedEntity.getContent(), this);
    }

    @Deprecated
    public void consumeContent() throws IOException {
        this.releaseConnection();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void writeTo(OutputStream outstream) throws IOException {
        try {
            this.wrappedEntity.writeTo(outstream);
            this.releaseConnection();
        }
        finally {
            this.cleanup();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean eofDetected(InputStream wrapped) throws IOException {
        try {
            wrapped.close();
            this.releaseConnection();
        }
        finally {
            this.cleanup();
        }
        return false;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean streamClosed(InputStream wrapped) throws IOException {
        try {
            boolean open = this.connHolder != null && !this.connHolder.isReleased();
            try {
                wrapped.close();
                this.releaseConnection();
            }
            catch (SocketException ex) {
                if (open) {
                    throw ex;
                }
            }
        }
        finally {
            this.cleanup();
        }
        return false;
    }

    public boolean streamAbort(InputStream wrapped) throws IOException {
        this.cleanup();
        return false;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("ResponseEntityProxy{");
        sb.append(this.wrappedEntity);
        sb.append('}');
        return sb.toString();
    }
}

