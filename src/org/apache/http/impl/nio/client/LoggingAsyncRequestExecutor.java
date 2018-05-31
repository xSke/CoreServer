/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.impl.nio.client;

import java.io.IOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpException;
import org.apache.http.nio.ContentDecoder;
import org.apache.http.nio.ContentEncoder;
import org.apache.http.nio.NHttpClientConnection;
import org.apache.http.nio.protocol.HttpAsyncRequestExecutor;

class LoggingAsyncRequestExecutor
extends HttpAsyncRequestExecutor {
    private final Log log = LogFactory.getLog(HttpAsyncRequestExecutor.class);

    protected void log(Exception ex) {
        this.log.debug(ex.getMessage(), ex);
    }

    public void connected(NHttpClientConnection conn, Object attachment) throws IOException, HttpException {
        if (this.log.isDebugEnabled()) {
            this.log.debug(conn + ": Connected");
        }
        super.connected(conn, attachment);
    }

    public void closed(NHttpClientConnection conn) {
        if (this.log.isDebugEnabled()) {
            this.log.debug(conn + ": Disconnected");
        }
        super.closed(conn);
    }

    public void requestReady(NHttpClientConnection conn) throws IOException, HttpException {
        if (this.log.isDebugEnabled()) {
            this.log.debug(conn + " Request ready");
        }
        super.requestReady(conn);
    }

    public void inputReady(NHttpClientConnection conn, ContentDecoder decoder) throws IOException, HttpException {
        if (this.log.isDebugEnabled()) {
            this.log.debug(conn + " Input ready");
        }
        super.inputReady(conn, decoder);
        if (this.log.isDebugEnabled()) {
            this.log.debug(conn + " " + decoder);
        }
    }

    public void outputReady(NHttpClientConnection conn, ContentEncoder encoder) throws IOException, HttpException {
        if (this.log.isDebugEnabled()) {
            this.log.debug(conn + " Output ready");
        }
        super.outputReady(conn, encoder);
        if (this.log.isDebugEnabled()) {
            this.log.debug(conn + " " + encoder);
        }
    }

    public void responseReceived(NHttpClientConnection conn) throws HttpException, IOException {
        if (this.log.isDebugEnabled()) {
            this.log.debug(conn + " Response received");
        }
        super.responseReceived(conn);
    }

    public void timeout(NHttpClientConnection conn) throws IOException {
        if (this.log.isDebugEnabled()) {
            this.log.debug(conn + " Timeout");
        }
        super.timeout(conn);
    }

    public void endOfInput(NHttpClientConnection conn) throws IOException {
        if (this.log.isDebugEnabled()) {
            this.log.debug(conn + " End of input");
        }
        super.endOfInput(conn);
    }
}

