/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.protocol;

import java.io.IOException;
import java.net.SocketTimeoutException;
import org.apache.http.ConnectionClosedException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.RequestLine;
import org.apache.http.StatusLine;
import org.apache.http.annotation.Immutable;
import org.apache.http.nio.ContentDecoder;
import org.apache.http.nio.ContentEncoder;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.NHttpClientConnection;
import org.apache.http.nio.NHttpClientEventHandler;
import org.apache.http.nio.NHttpConnection;
import org.apache.http.nio.protocol.HttpAsyncClientExchangeHandler;
import org.apache.http.nio.protocol.MessageState;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Args;
import org.apache.http.util.Asserts;

@Immutable
public class HttpAsyncRequestExecutor
implements NHttpClientEventHandler {
    public static final int DEFAULT_WAIT_FOR_CONTINUE = 3000;
    public static final String HTTP_HANDLER = "http.nio.exchange-handler";
    private final int waitForContinue;
    static final String HTTP_EXCHANGE_STATE = "http.nio.http-exchange-state";

    public HttpAsyncRequestExecutor(int waitForContinue) {
        this.waitForContinue = Args.positive(waitForContinue, "Wait for continue time");
    }

    public HttpAsyncRequestExecutor() {
        this(3000);
    }

    public void connected(NHttpClientConnection conn, Object attachment) throws IOException, HttpException {
        State state = new State();
        HttpContext context = conn.getContext();
        context.setAttribute(HTTP_EXCHANGE_STATE, state);
        this.requestReady(conn);
    }

    public void closed(NHttpClientConnection conn) {
        State state = this.getState(conn);
        HttpAsyncClientExchangeHandler handler = this.getHandler(conn);
        if (state == null || handler != null && handler.isDone()) {
            this.closeHandler(handler);
        }
        if (state != null) {
            state.reset();
        }
    }

    public void exception(NHttpClientConnection conn, Exception cause) {
        this.shutdownConnection(conn);
        HttpAsyncClientExchangeHandler handler = this.getHandler(conn);
        if (handler != null) {
            handler.failed(cause);
        } else {
            this.log(cause);
        }
    }

    public void requestReady(NHttpClientConnection conn) throws IOException, HttpException {
        State state = this.ensureNotNull(this.getState(conn));
        if (state.getRequestState() != MessageState.READY) {
            return;
        }
        HttpAsyncClientExchangeHandler handler = this.getHandler(conn);
        if (handler != null && handler.isDone()) {
            this.closeHandler(handler);
            state.reset();
            handler = null;
        }
        if (handler == null) {
            return;
        }
        HttpRequest request = handler.generateRequest();
        if (request == null) {
            return;
        }
        state.setRequest(request);
        conn.submitRequest(request);
        if (request instanceof HttpEntityEnclosingRequest) {
            if (((HttpEntityEnclosingRequest)request).expectContinue()) {
                int timeout = conn.getSocketTimeout();
                state.setTimeout(timeout);
                conn.setSocketTimeout(this.waitForContinue);
                state.setRequestState(MessageState.ACK_EXPECTED);
            } else {
                state.setRequestState(MessageState.BODY_STREAM);
            }
        } else {
            handler.requestCompleted();
            state.setRequestState(MessageState.COMPLETED);
        }
    }

    public void outputReady(NHttpClientConnection conn, ContentEncoder encoder) throws IOException, HttpException {
        State state = this.ensureNotNull(this.getState(conn));
        HttpAsyncClientExchangeHandler handler = this.ensureNotNull(this.getHandler(conn));
        if (state.getRequestState() == MessageState.ACK_EXPECTED) {
            conn.suspendOutput();
            return;
        }
        handler.produceContent(encoder, conn);
        state.setRequestState(MessageState.BODY_STREAM);
        if (encoder.isCompleted()) {
            handler.requestCompleted();
            state.setRequestState(MessageState.COMPLETED);
        }
    }

    public void responseReceived(NHttpClientConnection conn) throws HttpException, IOException {
        State state = this.ensureNotNull(this.getState(conn));
        HttpRequest request = state.getRequest();
        if (request == null) {
            throw new HttpException("Out of sequence response");
        }
        HttpAsyncClientExchangeHandler handler = this.ensureNotNull(this.getHandler(conn));
        HttpResponse response = conn.getHttpResponse();
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode < 200) {
            if (statusCode != 100) {
                throw new ProtocolException("Unexpected response: " + response.getStatusLine());
            }
            if (state.getRequestState() == MessageState.ACK_EXPECTED) {
                int timeout = state.getTimeout();
                conn.setSocketTimeout(timeout);
                conn.requestOutput();
                state.setRequestState(MessageState.ACK);
            }
            return;
        }
        state.setResponse(response);
        if (state.getRequestState() == MessageState.ACK_EXPECTED) {
            int timeout = state.getTimeout();
            conn.setSocketTimeout(timeout);
            conn.resetOutput();
            state.setRequestState(MessageState.COMPLETED);
        } else if (state.getRequestState() == MessageState.BODY_STREAM) {
            conn.resetOutput();
            conn.suspendOutput();
            state.setRequestState(MessageState.COMPLETED);
            state.invalidate();
        }
        handler.responseReceived(response);
        state.setResponseState(MessageState.BODY_STREAM);
        if (!this.canResponseHaveBody(request, response)) {
            response.setEntity(null);
            conn.resetInput();
            this.processResponse(conn, state, handler);
        }
    }

    public void inputReady(NHttpClientConnection conn, ContentDecoder decoder) throws IOException, HttpException {
        State state = this.ensureNotNull(this.getState(conn));
        HttpAsyncClientExchangeHandler handler = this.ensureNotNull(this.getHandler(conn));
        handler.consumeContent(decoder, conn);
        state.setResponseState(MessageState.BODY_STREAM);
        if (decoder.isCompleted()) {
            this.processResponse(conn, state, handler);
        }
    }

    public void endOfInput(NHttpClientConnection conn) throws IOException {
        State state = this.getState(conn);
        if (state != null) {
            HttpAsyncClientExchangeHandler handler;
            if (state.getRequestState().compareTo(MessageState.READY) != 0) {
                state.invalidate();
            }
            if ((handler = this.getHandler(conn)) != null) {
                if (state.isValid()) {
                    handler.inputTerminated();
                } else {
                    handler.failed(new ConnectionClosedException("Connection closed"));
                }
            }
        }
        if (conn.getSocketTimeout() <= 0) {
            conn.setSocketTimeout(1000);
        }
        conn.close();
    }

    public void timeout(NHttpClientConnection conn) throws IOException {
        State state = this.getState(conn);
        if (state != null) {
            if (state.getRequestState() == MessageState.ACK_EXPECTED) {
                int timeout = state.getTimeout();
                conn.setSocketTimeout(timeout);
                conn.requestOutput();
                state.setRequestState(MessageState.BODY_STREAM);
                return;
            }
            state.invalidate();
            HttpAsyncClientExchangeHandler handler = this.getHandler(conn);
            if (handler != null) {
                handler.failed(new SocketTimeoutException());
                handler.close();
            }
        }
        if (conn.getStatus() == 0) {
            conn.close();
            if (conn.getStatus() == 1) {
                conn.setSocketTimeout(250);
            }
        } else {
            conn.shutdown();
        }
    }

    protected void log(Exception ex) {
    }

    private State getState(NHttpConnection conn) {
        return (State)conn.getContext().getAttribute(HTTP_EXCHANGE_STATE);
    }

    private State ensureNotNull(State state) {
        Asserts.notNull(state, "HTTP exchange state");
        return state;
    }

    private HttpAsyncClientExchangeHandler getHandler(NHttpConnection conn) {
        return (HttpAsyncClientExchangeHandler)conn.getContext().getAttribute(HTTP_HANDLER);
    }

    private HttpAsyncClientExchangeHandler ensureNotNull(HttpAsyncClientExchangeHandler handler) {
        Asserts.notNull(handler, "HTTP exchange handler");
        return handler;
    }

    private void shutdownConnection(NHttpConnection conn) {
        try {
            conn.shutdown();
        }
        catch (IOException ex) {
            this.log(ex);
        }
    }

    private void closeHandler(HttpAsyncClientExchangeHandler handler) {
        if (handler != null) {
            try {
                handler.close();
            }
            catch (IOException ioex) {
                this.log(ioex);
            }
        }
    }

    private void processResponse(NHttpClientConnection conn, State state, HttpAsyncClientExchangeHandler handler) throws IOException, HttpException {
        if (!state.isValid()) {
            conn.close();
        }
        handler.responseCompleted();
        state.reset();
        if (!handler.isDone() && conn.isOpen()) {
            conn.requestOutput();
        }
    }

    private boolean canResponseHaveBody(HttpRequest request, HttpResponse response) {
        String method = request.getRequestLine().getMethod();
        int status = response.getStatusLine().getStatusCode();
        if (method.equalsIgnoreCase("HEAD")) {
            return false;
        }
        if (method.equalsIgnoreCase("CONNECT") && status < 300) {
            return false;
        }
        return status >= 200 && status != 204 && status != 304 && status != 205;
    }

    static class State {
        private volatile MessageState requestState = MessageState.READY;
        private volatile MessageState responseState = MessageState.READY;
        private volatile HttpRequest request;
        private volatile HttpResponse response;
        private volatile boolean valid = true;
        private volatile int timeout;

        State() {
        }

        public MessageState getRequestState() {
            return this.requestState;
        }

        public void setRequestState(MessageState state) {
            this.requestState = state;
        }

        public MessageState getResponseState() {
            return this.responseState;
        }

        public void setResponseState(MessageState state) {
            this.responseState = state;
        }

        public HttpRequest getRequest() {
            return this.request;
        }

        public void setRequest(HttpRequest request) {
            this.request = request;
        }

        public HttpResponse getResponse() {
            return this.response;
        }

        public void setResponse(HttpResponse response) {
            this.response = response;
        }

        public int getTimeout() {
            return this.timeout;
        }

        public void setTimeout(int timeout) {
            this.timeout = timeout;
        }

        public void reset() {
            this.responseState = MessageState.READY;
            this.requestState = MessageState.READY;
            this.response = null;
            this.request = null;
            this.timeout = 0;
        }

        public boolean isValid() {
            return this.valid;
        }

        public void invalidate() {
            this.valid = false;
        }

        public String toString() {
            StringBuilder buf = new StringBuilder();
            buf.append("request state: ");
            buf.append((Object)this.requestState);
            buf.append("; request: ");
            if (this.request != null) {
                buf.append(this.request.getRequestLine());
            }
            buf.append("; response state: ");
            buf.append((Object)this.responseState);
            buf.append("; response: ");
            if (this.response != null) {
                buf.append(this.response.getStatusLine());
            }
            buf.append("; valid: ");
            buf.append(this.valid);
            buf.append(";");
            return buf.toString();
        }
    }

}

