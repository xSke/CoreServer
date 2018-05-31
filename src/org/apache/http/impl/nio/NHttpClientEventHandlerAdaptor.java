/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.impl.nio;

import java.io.IOException;
import org.apache.http.HttpException;
import org.apache.http.nio.ContentDecoder;
import org.apache.http.nio.ContentEncoder;
import org.apache.http.nio.NHttpClientConnection;
import org.apache.http.nio.NHttpClientEventHandler;
import org.apache.http.nio.NHttpClientHandler;

@Deprecated
class NHttpClientEventHandlerAdaptor
implements NHttpClientEventHandler {
    private final NHttpClientHandler handler;

    public NHttpClientEventHandlerAdaptor(NHttpClientHandler handler) {
        this.handler = handler;
    }

    public void connected(NHttpClientConnection conn, Object attachment) {
        this.handler.connected(conn, attachment);
    }

    public void requestReady(NHttpClientConnection conn) throws IOException, HttpException {
        this.handler.requestReady(conn);
    }

    public void responseReceived(NHttpClientConnection conn) throws IOException, HttpException {
        this.handler.responseReceived(conn);
    }

    public void inputReady(NHttpClientConnection conn, ContentDecoder decoder) throws IOException, HttpException {
        this.handler.inputReady(conn, decoder);
    }

    public void outputReady(NHttpClientConnection conn, ContentEncoder encoder) throws IOException, HttpException {
        this.handler.outputReady(conn, encoder);
    }

    public void exception(NHttpClientConnection conn, Exception ex) {
        if (ex instanceof HttpException) {
            this.handler.exception(conn, (HttpException)ex);
        } else if (ex instanceof IOException) {
            this.handler.exception(conn, (IOException)ex);
        } else {
            if (ex instanceof RuntimeException) {
                throw (RuntimeException)ex;
            }
            throw new Error("Unexpected exception: ", ex);
        }
    }

    public void endOfInput(NHttpClientConnection conn) throws IOException {
        conn.close();
    }

    public void timeout(NHttpClientConnection conn) {
        this.handler.timeout(conn);
    }

    public void closed(NHttpClientConnection conn) {
        this.handler.closed(conn);
    }
}

