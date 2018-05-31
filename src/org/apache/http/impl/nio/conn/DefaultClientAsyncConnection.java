/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.impl.nio.conn;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseFactory;
import org.apache.http.RequestLine;
import org.apache.http.StatusLine;
import org.apache.http.impl.nio.DefaultNHttpClientConnection;
import org.apache.http.impl.nio.conn.LoggingIOSession;
import org.apache.http.impl.nio.reactor.SessionInputBufferImpl;
import org.apache.http.nio.conn.ClientAsyncConnection;
import org.apache.http.nio.reactor.IOSession;
import org.apache.http.nio.util.ByteBufferAllocator;
import org.apache.http.params.HttpParams;

@Deprecated
public class DefaultClientAsyncConnection
extends DefaultNHttpClientConnection
implements ClientAsyncConnection {
    private final Log headerlog = LogFactory.getLog("org.apache.http.headers");
    private final Log wirelog = LogFactory.getLog("org.apache.http.wire");
    private final Log log;
    private final String id;
    private IOSession original;

    public DefaultClientAsyncConnection(String id, IOSession iosession, HttpResponseFactory responseFactory, ByteBufferAllocator allocator, HttpParams params) {
        super(iosession, responseFactory, allocator, params);
        this.id = id;
        this.original = iosession;
        this.log = LogFactory.getLog(iosession.getClass());
        if (this.log.isDebugEnabled() || this.wirelog.isDebugEnabled()) {
            this.bind(new LoggingIOSession(iosession, this.id, this.log, this.wirelog));
        }
    }

    public void upgrade(IOSession iosession) {
        this.original = iosession;
        if (this.log.isDebugEnabled() || this.wirelog.isDebugEnabled()) {
            this.log.debug(this.id + " Upgrade session " + iosession);
            this.bind(new LoggingIOSession(iosession, this.id, this.headerlog, this.wirelog));
        } else {
            this.bind(iosession);
        }
    }

    public IOSession getIOSession() {
        return this.original;
    }

    public String getId() {
        return this.id;
    }

    protected void onResponseReceived(HttpResponse response) {
        if (response != null && this.headerlog.isDebugEnabled()) {
            Header[] headers;
            this.headerlog.debug(this.id + " << " + response.getStatusLine().toString());
            for (Header header : headers = response.getAllHeaders()) {
                this.headerlog.debug(this.id + " << " + header.toString());
            }
        }
    }

    protected void onRequestSubmitted(HttpRequest request) {
        if (request != null && this.headerlog.isDebugEnabled()) {
            Header[] headers;
            this.headerlog.debug(this.id + " >> " + request.getRequestLine().toString());
            for (Header header : headers = request.getAllHeaders()) {
                this.headerlog.debug(this.id + " >> " + header.toString());
            }
        }
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(this.id);
        buf.append(" [");
        switch (this.status) {
            case 0: {
                buf.append("ACTIVE");
                if (!this.inbuf.hasData()) break;
                buf.append("(").append(this.inbuf.length()).append(")");
                break;
            }
            case 1: {
                buf.append("CLOSING");
                break;
            }
            case 2: {
                buf.append("CLOSED");
            }
        }
        buf.append("]");
        return buf.toString();
    }
}

