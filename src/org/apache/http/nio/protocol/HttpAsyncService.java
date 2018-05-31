/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.protocol;

import java.io.IOException;
import java.net.SocketTimeoutException;
import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseFactory;
import org.apache.http.HttpVersion;
import org.apache.http.MethodNotSupportedException;
import org.apache.http.ProtocolException;
import org.apache.http.ProtocolVersion;
import org.apache.http.RequestLine;
import org.apache.http.StatusLine;
import org.apache.http.UnsupportedHttpVersionException;
import org.apache.http.annotation.Immutable;
import org.apache.http.concurrent.Cancellable;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.nio.ContentDecoder;
import org.apache.http.nio.ContentEncoder;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.NHttpConnection;
import org.apache.http.nio.NHttpServerConnection;
import org.apache.http.nio.NHttpServerEventHandler;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.nio.protocol.BasicAsyncResponseProducer;
import org.apache.http.nio.protocol.ErrorResponseProducer;
import org.apache.http.nio.protocol.HttpAsyncExchange;
import org.apache.http.nio.protocol.HttpAsyncExpectationVerifier;
import org.apache.http.nio.protocol.HttpAsyncRequestConsumer;
import org.apache.http.nio.protocol.HttpAsyncRequestHandler;
import org.apache.http.nio.protocol.HttpAsyncRequestHandlerMapper;
import org.apache.http.nio.protocol.HttpAsyncRequestHandlerResolver;
import org.apache.http.nio.protocol.HttpAsyncResponseProducer;
import org.apache.http.nio.protocol.MessageState;
import org.apache.http.nio.protocol.NullRequestHandler;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.util.Args;
import org.apache.http.util.Asserts;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
@Immutable
public class HttpAsyncService
implements NHttpServerEventHandler {
    static final String HTTP_EXCHANGE_STATE = "http.nio.http-exchange-state";
    private final HttpProcessor httpProcessor;
    private final ConnectionReuseStrategy connStrategy;
    private final HttpResponseFactory responseFactory;
    private final HttpAsyncRequestHandlerMapper handlerMapper;
    private final HttpAsyncExpectationVerifier expectationVerifier;

    @Deprecated
    public HttpAsyncService(HttpProcessor httpProcessor, ConnectionReuseStrategy connStrategy, HttpResponseFactory responseFactory, HttpAsyncRequestHandlerResolver handlerResolver, HttpAsyncExpectationVerifier expectationVerifier, HttpParams params) {
        this(httpProcessor, connStrategy, responseFactory, new HttpAsyncRequestHandlerResolverAdapter(handlerResolver), expectationVerifier);
    }

    @Deprecated
    public HttpAsyncService(HttpProcessor httpProcessor, ConnectionReuseStrategy connStrategy, HttpAsyncRequestHandlerResolver handlerResolver, HttpParams params) {
        this(httpProcessor, connStrategy, DefaultHttpResponseFactory.INSTANCE, new HttpAsyncRequestHandlerResolverAdapter(handlerResolver), null);
    }

    public HttpAsyncService(HttpProcessor httpProcessor, ConnectionReuseStrategy connStrategy, HttpResponseFactory responseFactory, HttpAsyncRequestHandlerMapper handlerMapper, HttpAsyncExpectationVerifier expectationVerifier) {
        this.httpProcessor = Args.notNull(httpProcessor, "HTTP processor");
        this.connStrategy = connStrategy != null ? connStrategy : DefaultConnectionReuseStrategy.INSTANCE;
        this.responseFactory = responseFactory != null ? responseFactory : DefaultHttpResponseFactory.INSTANCE;
        this.handlerMapper = handlerMapper;
        this.expectationVerifier = expectationVerifier;
    }

    public HttpAsyncService(HttpProcessor httpProcessor, HttpAsyncRequestHandlerMapper handlerMapper) {
        this(httpProcessor, null, null, handlerMapper, null);
    }

    @Override
    public void connected(NHttpServerConnection conn) {
        State state = new State();
        conn.getContext().setAttribute(HTTP_EXCHANGE_STATE, state);
    }

    @Override
    public void closed(NHttpServerConnection conn) {
        State state = this.getState(conn);
        if (state != null) {
            state.setTerminated();
            this.closeHandlers(state);
            Cancellable cancellable = state.getCancellable();
            if (cancellable != null) {
                cancellable.cancel();
            }
            state.reset();
        }
    }

    @Override
    public void exception(NHttpServerConnection conn, Exception cause) {
        State state = this.getState(conn);
        if (state == null) {
            this.shutdownConnection(conn);
            this.log(cause);
            return;
        }
        state.setTerminated();
        this.closeHandlers(state, cause);
        Cancellable cancellable = state.getCancellable();
        if (cancellable != null) {
            cancellable.cancel();
        }
        if (conn.isResponseSubmitted() || state.getResponseState().compareTo(MessageState.INIT) > 0) {
            this.closeConnection(conn);
        } else {
            HttpContext context = state.getContext();
            HttpAsyncResponseProducer responseProducer = this.handleException(cause, context);
            state.setResponseProducer(responseProducer);
            try {
                HttpResponse response = responseProducer.generateResponse();
                state.setResponse(response);
                this.commitFinalResponse(conn, state);
            }
            catch (Exception ex) {
                this.shutdownConnection(conn);
                this.closeHandlers(state);
                if (ex instanceof RuntimeException) {
                    throw (RuntimeException)ex;
                }
                this.log(ex);
            }
        }
    }

    @Override
    public void requestReceived(NHttpServerConnection conn) throws IOException, HttpException {
        State state = this.ensureNotNull(this.getState(conn));
        if (state.getResponseState() != MessageState.READY) {
            throw new ProtocolException("Out of sequence request message detected (pipelining is not supported)");
        }
        HttpRequest request = conn.getHttpRequest();
        HttpContext context = state.getContext();
        context.setAttribute("http.request", request);
        context.setAttribute("http.connection", conn);
        this.httpProcessor.process(request, context);
        state.setRequest(request);
        HttpAsyncRequestHandler<Object> requestHandler = this.getRequestHandler(request);
        state.setRequestHandler(requestHandler);
        HttpAsyncRequestConsumer<Object> consumer = requestHandler.processRequest(request, context);
        state.setRequestConsumer(consumer);
        consumer.requestReceived(request);
        if (request instanceof HttpEntityEnclosingRequest) {
            if (((HttpEntityEnclosingRequest)request).expectContinue()) {
                state.setRequestState(MessageState.ACK_EXPECTED);
                HttpResponse ack = this.responseFactory.newHttpResponse(HttpVersion.HTTP_1_1, 100, context);
                if (this.expectationVerifier != null) {
                    conn.suspendInput();
                    Exchange httpex = new Exchange(request, ack, state, conn);
                    this.expectationVerifier.verify(httpex, context);
                } else {
                    conn.submitResponse(ack);
                    state.setRequestState(MessageState.BODY_STREAM);
                }
            } else {
                state.setRequestState(MessageState.BODY_STREAM);
            }
        } else {
            this.processRequest(conn, state);
        }
    }

    @Override
    public void inputReady(NHttpServerConnection conn, ContentDecoder decoder) throws IOException, HttpException {
        State state = this.ensureNotNull(this.getState(conn));
        HttpAsyncRequestConsumer<Object> consumer = this.ensureNotNull(state.getRequestConsumer());
        consumer.consumeContent(decoder, conn);
        state.setRequestState(MessageState.BODY_STREAM);
        if (decoder.isCompleted()) {
            this.processRequest(conn, state);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public void responseReady(NHttpServerConnection conn) throws IOException, HttpException {
        State state = this.ensureNotNull(this.getState(conn));
        if (state.getResponse() != null) {
            return;
        }
        HttpAsyncResponseProducer responseProducer = state.getResponseProducer();
        if (responseProducer == null) {
            return;
        }
        HttpContext context = state.getContext();
        HttpResponse response = responseProducer.generateResponse();
        int status = response.getStatusLine().getStatusCode();
        if (state.getRequestState() == MessageState.ACK_EXPECTED) {
            if (status == 100) {
                try {
                    response.setEntity(null);
                    conn.requestInput();
                    state.setRequestState(MessageState.BODY_STREAM);
                    conn.submitResponse(response);
                    responseProducer.responseCompleted(context);
                    return;
                }
                finally {
                    state.setResponseProducer(null);
                    responseProducer.close();
                }
            } else {
                if (status < 400) throw new HttpException("Invalid response: " + response.getStatusLine());
                conn.resetInput();
                state.setRequestState(MessageState.COMPLETED);
                state.setResponse(response);
                this.commitFinalResponse(conn, state);
            }
            return;
        } else {
            if (status < 200) throw new HttpException("Invalid response: " + response.getStatusLine());
            state.setResponse(response);
            this.commitFinalResponse(conn, state);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void outputReady(NHttpServerConnection conn, ContentEncoder encoder) throws IOException {
        State state = this.ensureNotNull(this.getState(conn));
        HttpAsyncResponseProducer responseProducer = state.getResponseProducer();
        HttpContext context = state.getContext();
        HttpResponse response = state.getResponse();
        responseProducer.produceContent(encoder, conn);
        state.setResponseState(MessageState.BODY_STREAM);
        if (encoder.isCompleted()) {
            try {
                responseProducer.responseCompleted(context);
                state.reset();
            }
            finally {
                responseProducer.close();
            }
            if (!this.connStrategy.keepAlive(response, context)) {
                conn.close();
            } else {
                conn.requestInput();
            }
        }
    }

    @Override
    public void endOfInput(NHttpServerConnection conn) throws IOException {
        if (conn.getSocketTimeout() <= 0) {
            conn.setSocketTimeout(1000);
        }
        conn.close();
    }

    @Override
    public void timeout(NHttpServerConnection conn) throws IOException {
        State state = this.getState(conn);
        if (state != null) {
            this.closeHandlers(state, new SocketTimeoutException());
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

    private State getState(NHttpConnection conn) {
        return (State)conn.getContext().getAttribute(HTTP_EXCHANGE_STATE);
    }

    private State ensureNotNull(State state) {
        Asserts.notNull(state, "HTTP exchange state");
        return state;
    }

    private HttpAsyncRequestConsumer<Object> ensureNotNull(HttpAsyncRequestConsumer<Object> requestConsumer) {
        Asserts.notNull(requestConsumer, "Request consumer");
        return requestConsumer;
    }

    protected void log(Exception ex) {
    }

    private void closeConnection(NHttpConnection conn) {
        try {
            conn.close();
        }
        catch (IOException ex) {
            this.log(ex);
        }
    }

    private void shutdownConnection(NHttpConnection conn) {
        try {
            conn.shutdown();
        }
        catch (IOException ex) {
            this.log(ex);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void closeHandlers(State state, Exception ex) {
        HttpAsyncResponseProducer producer;
        HttpAsyncRequestConsumer<Object> consumer = state.getRequestConsumer();
        if (consumer != null) {
            try {
                consumer.failed(ex);
            }
            finally {
                try {
                    consumer.close();
                }
                catch (IOException ioex) {
                    this.log(ioex);
                }
            }
        }
        if ((producer = state.getResponseProducer()) != null) {
            try {
                producer.failed(ex);
            }
            finally {
                try {
                    producer.close();
                }
                catch (IOException ioex) {
                    this.log(ioex);
                }
            }
        }
    }

    private void closeHandlers(State state) {
        HttpAsyncResponseProducer producer;
        HttpAsyncRequestConsumer<Object> consumer = state.getRequestConsumer();
        if (consumer != null) {
            try {
                consumer.close();
            }
            catch (IOException ioex) {
                this.log(ioex);
            }
        }
        if ((producer = state.getResponseProducer()) != null) {
            try {
                producer.close();
            }
            catch (IOException ioex) {
                this.log(ioex);
            }
        }
    }

    protected HttpAsyncResponseProducer handleException(Exception ex, HttpContext context) {
        int code = ex instanceof MethodNotSupportedException ? 501 : (ex instanceof UnsupportedHttpVersionException ? 505 : (ex instanceof ProtocolException ? 400 : 500));
        String message = ex.getMessage();
        if (message == null) {
            message = ex.toString();
        }
        HttpResponse response = this.responseFactory.newHttpResponse(HttpVersion.HTTP_1_1, code, context);
        return new ErrorResponseProducer(response, new NStringEntity(message, ContentType.DEFAULT_TEXT), false);
    }

    private boolean canResponseHaveBody(HttpRequest request, HttpResponse response) {
        if (request != null && "HEAD".equalsIgnoreCase(request.getRequestLine().getMethod())) {
            return false;
        }
        int status = response.getStatusLine().getStatusCode();
        return status >= 200 && status != 204 && status != 304 && status != 205;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void processRequest(NHttpServerConnection conn, State state) throws IOException, HttpException {
        HttpContext context;
        Object result;
        HttpAsyncRequestConsumer<Object> consumer;
        context = state.getContext();
        state.setRequestState(MessageState.COMPLETED);
        state.setResponseState(MessageState.INIT);
        consumer = state.getRequestConsumer();
        try {
            consumer.requestCompleted(context);
            result = consumer.getResult();
        }
        finally {
            consumer.close();
        }
        if (result != null) {
            HttpResponse response = this.responseFactory.newHttpResponse(HttpVersion.HTTP_1_1, 200, context);
            HttpRequest request = state.getRequest();
            Exchange httpexchange = new Exchange(request, response, state, conn);
            HttpAsyncRequestHandler<Object> handler = state.getRequestHandler();
            handler.handle(result, httpexchange, context);
        } else {
            Exception exception = consumer.getException();
            HttpAsyncResponseProducer responseProducer = this.handleException(exception != null ? exception : new HttpException("Internal failure processing request"), context);
            state.setResponseProducer(responseProducer);
            conn.requestOutput();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void commitFinalResponse(NHttpServerConnection conn, State state) throws IOException, HttpException {
        HttpContext context = state.getContext();
        HttpRequest request = state.getRequest();
        HttpResponse response = state.getResponse();
        context.setAttribute("http.response", response);
        this.httpProcessor.process(response, context);
        HttpEntity entity = response.getEntity();
        if (entity != null && !this.canResponseHaveBody(request, response)) {
            response.setEntity(null);
            entity = null;
        }
        conn.submitResponse(response);
        if (entity == null) {
            HttpAsyncResponseProducer responseProducer = state.getResponseProducer();
            try {
                responseProducer.responseCompleted(context);
                state.reset();
            }
            finally {
                responseProducer.close();
            }
            if (!this.connStrategy.keepAlive(response, context)) {
                conn.close();
            } else {
                conn.requestInput();
            }
        } else {
            state.setResponseState(MessageState.BODY_STREAM);
        }
    }

    private HttpAsyncRequestHandler<Object> getRequestHandler(HttpRequest request) {
        NullRequestHandler handler = null;
        if (this.handlerMapper != null) {
            handler = this.handlerMapper.lookup(request);
        }
        if (handler == null) {
            handler = new NullRequestHandler();
        }
        return handler;
    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    @Deprecated
    private static class HttpAsyncRequestHandlerResolverAdapter
    implements HttpAsyncRequestHandlerMapper {
        private final HttpAsyncRequestHandlerResolver resolver;

        public HttpAsyncRequestHandlerResolverAdapter(HttpAsyncRequestHandlerResolver resolver) {
            this.resolver = resolver;
        }

        @Override
        public HttpAsyncRequestHandler<?> lookup(HttpRequest request) {
            return this.resolver.lookup(request.getRequestLine().getUri());
        }
    }

    static class Exchange
    implements HttpAsyncExchange {
        private final HttpRequest request;
        private final HttpResponse response;
        private final State state;
        private final NHttpServerConnection conn;
        private volatile boolean completed;

        public Exchange(HttpRequest request, HttpResponse response, State state, NHttpServerConnection conn) {
            this.request = request;
            this.response = response;
            this.state = state;
            this.conn = conn;
        }

        public HttpRequest getRequest() {
            return this.request;
        }

        public HttpResponse getResponse() {
            return this.response;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public void setCallback(Cancellable cancellable) {
            Exchange exchange = this;
            synchronized (exchange) {
                Asserts.check(!this.completed, "Response already submitted");
                if (this.state.isTerminated() && cancellable != null) {
                    cancellable.cancel();
                } else {
                    this.state.setCancellable(cancellable);
                    this.conn.requestInput();
                }
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public void submitResponse(HttpAsyncResponseProducer responseProducer) {
            Args.notNull(responseProducer, "Response producer");
            Exchange exchange = this;
            synchronized (exchange) {
                Asserts.check(!this.completed, "Response already submitted");
                this.completed = true;
                if (!this.state.isTerminated()) {
                    this.state.setResponseProducer(responseProducer);
                    this.state.setCancellable(null);
                    this.conn.requestOutput();
                } else {
                    try {
                        responseProducer.close();
                    }
                    catch (IOException ex) {
                        // empty catch block
                    }
                }
            }
        }

        public void submitResponse() {
            this.submitResponse(new BasicAsyncResponseProducer(this.response));
        }

        public boolean isCompleted() {
            return this.completed;
        }

        public void setTimeout(int timeout) {
            this.conn.setSocketTimeout(timeout);
        }

        public int getTimeout() {
            return this.conn.getSocketTimeout();
        }
    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    static class State {
        private volatile BasicHttpContext context = new BasicHttpContext();
        private volatile boolean terminated;
        private volatile HttpAsyncRequestHandler<Object> requestHandler;
        private volatile MessageState requestState = MessageState.READY;
        private volatile MessageState responseState = MessageState.READY;
        private volatile HttpAsyncRequestConsumer<Object> requestConsumer;
        private volatile HttpAsyncResponseProducer responseProducer;
        private volatile HttpRequest request;
        private volatile HttpResponse response;
        private volatile Cancellable cancellable;

        State() {
        }

        public HttpContext getContext() {
            return this.context;
        }

        public boolean isTerminated() {
            return this.terminated;
        }

        public void setTerminated() {
            this.terminated = true;
        }

        public HttpAsyncRequestHandler<Object> getRequestHandler() {
            return this.requestHandler;
        }

        public void setRequestHandler(HttpAsyncRequestHandler<Object> requestHandler) {
            this.requestHandler = requestHandler;
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

        public HttpAsyncRequestConsumer<Object> getRequestConsumer() {
            return this.requestConsumer;
        }

        public void setRequestConsumer(HttpAsyncRequestConsumer<Object> requestConsumer) {
            this.requestConsumer = requestConsumer;
        }

        public HttpAsyncResponseProducer getResponseProducer() {
            return this.responseProducer;
        }

        public void setResponseProducer(HttpAsyncResponseProducer responseProducer) {
            this.responseProducer = responseProducer;
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

        public Cancellable getCancellable() {
            return this.cancellable;
        }

        public void setCancellable(Cancellable cancellable) {
            this.cancellable = cancellable;
        }

        public void reset() {
            this.context = new BasicHttpContext();
            this.responseState = MessageState.READY;
            this.requestState = MessageState.READY;
            this.requestHandler = null;
            this.requestConsumer = null;
            this.responseProducer = null;
            this.request = null;
            this.response = null;
            this.cancellable = null;
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
            buf.append(";");
            return buf.toString();
        }
    }

}

