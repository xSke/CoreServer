/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.protocol;

import java.io.IOException;
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
import org.apache.http.nio.ContentDecoder;
import org.apache.http.nio.ContentEncoder;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.NHttpConnection;
import org.apache.http.nio.NHttpServerConnection;
import org.apache.http.nio.NHttpServiceHandler;
import org.apache.http.nio.entity.ConsumingNHttpEntity;
import org.apache.http.nio.entity.NByteArrayEntity;
import org.apache.http.nio.entity.NHttpEntityWrapper;
import org.apache.http.nio.entity.ProducingNHttpEntity;
import org.apache.http.nio.protocol.EventListener;
import org.apache.http.nio.protocol.NHttpHandlerBase;
import org.apache.http.nio.protocol.NHttpRequestHandler;
import org.apache.http.nio.protocol.NHttpRequestHandlerResolver;
import org.apache.http.nio.protocol.NHttpResponseTrigger;
import org.apache.http.nio.protocol.NullNHttpEntity;
import org.apache.http.nio.util.ByteBufferAllocator;
import org.apache.http.nio.util.HeapByteBufferAllocator;
import org.apache.http.params.DefaultedHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpExpectationVerifier;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.util.Args;
import org.apache.http.util.Asserts;
import org.apache.http.util.EncodingUtils;

@Deprecated
@Immutable
public class AsyncNHttpServiceHandler
extends NHttpHandlerBase
implements NHttpServiceHandler {
    protected final HttpResponseFactory responseFactory;
    protected NHttpRequestHandlerResolver handlerResolver;
    protected HttpExpectationVerifier expectationVerifier;

    public AsyncNHttpServiceHandler(HttpProcessor httpProcessor, HttpResponseFactory responseFactory, ConnectionReuseStrategy connStrategy, ByteBufferAllocator allocator, HttpParams params) {
        super(httpProcessor, connStrategy, allocator, params);
        Args.notNull(responseFactory, "Response factory");
        this.responseFactory = responseFactory;
    }

    public AsyncNHttpServiceHandler(HttpProcessor httpProcessor, HttpResponseFactory responseFactory, ConnectionReuseStrategy connStrategy, HttpParams params) {
        this(httpProcessor, responseFactory, connStrategy, HeapByteBufferAllocator.INSTANCE, params);
    }

    public void setExpectationVerifier(HttpExpectationVerifier expectationVerifier) {
        this.expectationVerifier = expectationVerifier;
    }

    public void setHandlerResolver(NHttpRequestHandlerResolver handlerResolver) {
        this.handlerResolver = handlerResolver;
    }

    public void connected(NHttpServerConnection conn) {
        HttpContext context = conn.getContext();
        ServerConnState connState = new ServerConnState();
        context.setAttribute("http.nio.conn-state", connState);
        context.setAttribute("http.connection", conn);
        if (this.eventListener != null) {
            this.eventListener.connectionOpen(conn);
        }
    }

    public void requestReceived(NHttpServerConnection conn) {
        block14 : {
            HttpContext context = conn.getContext();
            ServerConnState connState = (ServerConnState)context.getAttribute("http.nio.conn-state");
            HttpRequest request = conn.getHttpRequest();
            request.setParams(new DefaultedHttpParams(request.getParams(), this.params));
            connState.setRequest(request);
            NHttpRequestHandler requestHandler = this.getRequestHandler(request);
            connState.setRequestHandler(requestHandler);
            ProtocolVersion ver = request.getRequestLine().getProtocolVersion();
            if (!ver.lessEquals(HttpVersion.HTTP_1_1)) {
                ver = HttpVersion.HTTP_1_1;
            }
            try {
                if (request instanceof HttpEntityEnclosingRequest) {
                    HttpEntityEnclosingRequest entityRequest = (HttpEntityEnclosingRequest)request;
                    if (entityRequest.expectContinue()) {
                        HttpResponse response = this.responseFactory.newHttpResponse(ver, 100, context);
                        response.setParams(new DefaultedHttpParams(response.getParams(), this.params));
                        if (this.expectationVerifier != null) {
                            try {
                                this.expectationVerifier.verify(request, response, context);
                            }
                            catch (HttpException ex) {
                                response = this.responseFactory.newHttpResponse(HttpVersion.HTTP_1_0, 500, context);
                                response.setParams(new DefaultedHttpParams(response.getParams(), this.params));
                                this.handleException(ex, response);
                            }
                        }
                        if (response.getStatusLine().getStatusCode() < 200) {
                            conn.submitResponse(response);
                        } else {
                            conn.resetInput();
                            this.sendResponse(conn, request, response);
                        }
                    }
                    ConsumingNHttpEntity consumingEntity = null;
                    if (requestHandler != null) {
                        consumingEntity = requestHandler.entityRequest(entityRequest, context);
                    }
                    if (consumingEntity == null) {
                        consumingEntity = new NullNHttpEntity(entityRequest.getEntity());
                    }
                    entityRequest.setEntity(consumingEntity);
                    connState.setConsumingEntity(consumingEntity);
                    break block14;
                }
                conn.suspendInput();
                this.processRequest(conn, request);
            }
            catch (IOException ex) {
                this.shutdownConnection(conn, ex);
                if (this.eventListener != null) {
                    this.eventListener.fatalIOException(ex, conn);
                }
            }
            catch (HttpException ex) {
                this.closeConnection(conn, ex);
                if (this.eventListener == null) break block14;
                this.eventListener.fatalProtocolException(ex, conn);
            }
        }
    }

    public void closed(NHttpServerConnection conn) {
        block3 : {
            HttpContext context = conn.getContext();
            ServerConnState connState = (ServerConnState)context.getAttribute("http.nio.conn-state");
            try {
                connState.reset();
            }
            catch (IOException ex) {
                if (this.eventListener == null) break block3;
                this.eventListener.fatalIOException(ex, conn);
            }
        }
        if (this.eventListener != null) {
            this.eventListener.connectionClosed(conn);
        }
    }

    public void exception(NHttpServerConnection conn, HttpException httpex) {
        block6 : {
            if (conn.isResponseSubmitted()) {
                this.closeConnection(conn, httpex);
                if (this.eventListener != null) {
                    this.eventListener.fatalProtocolException(httpex, conn);
                }
                return;
            }
            HttpContext context = conn.getContext();
            try {
                HttpResponse response = this.responseFactory.newHttpResponse(HttpVersion.HTTP_1_0, 500, context);
                response.setParams(new DefaultedHttpParams(response.getParams(), this.params));
                this.handleException(httpex, response);
                response.setEntity(null);
                this.sendResponse(conn, null, response);
            }
            catch (IOException ex) {
                this.shutdownConnection(conn, ex);
                if (this.eventListener != null) {
                    this.eventListener.fatalIOException(ex, conn);
                }
            }
            catch (HttpException ex) {
                this.closeConnection(conn, ex);
                if (this.eventListener == null) break block6;
                this.eventListener.fatalProtocolException(ex, conn);
            }
        }
    }

    public void exception(NHttpServerConnection conn, IOException ex) {
        this.shutdownConnection(conn, ex);
        if (this.eventListener != null) {
            this.eventListener.fatalIOException(ex, conn);
        }
    }

    public void timeout(NHttpServerConnection conn) {
        this.handleTimeout(conn);
    }

    public void inputReady(NHttpServerConnection conn, ContentDecoder decoder) {
        block5 : {
            HttpContext context = conn.getContext();
            ServerConnState connState = (ServerConnState)context.getAttribute("http.nio.conn-state");
            HttpRequest request = connState.getRequest();
            ConsumingNHttpEntity consumingEntity = connState.getConsumingEntity();
            try {
                consumingEntity.consumeContent(decoder, conn);
                if (decoder.isCompleted()) {
                    conn.suspendInput();
                    this.processRequest(conn, request);
                }
            }
            catch (IOException ex) {
                this.shutdownConnection(conn, ex);
                if (this.eventListener != null) {
                    this.eventListener.fatalIOException(ex, conn);
                }
            }
            catch (HttpException ex) {
                this.closeConnection(conn, ex);
                if (this.eventListener == null) break block5;
                this.eventListener.fatalProtocolException(ex, conn);
            }
        }
    }

    public void responseReady(NHttpServerConnection conn) {
        block8 : {
            HttpContext context = conn.getContext();
            ServerConnState connState = (ServerConnState)context.getAttribute("http.nio.conn-state");
            if (connState.isHandled()) {
                return;
            }
            HttpRequest request = connState.getRequest();
            try {
                HttpResponse response;
                IOException ioex = connState.getIOException();
                if (ioex != null) {
                    throw ioex;
                }
                HttpException httpex = connState.getHttpException();
                if (httpex != null) {
                    response = this.responseFactory.newHttpResponse(HttpVersion.HTTP_1_0, 500, context);
                    response.setParams(new DefaultedHttpParams(response.getParams(), this.params));
                    this.handleException(httpex, response);
                    connState.setResponse(response);
                }
                if ((response = connState.getResponse()) != null) {
                    connState.setHandled(true);
                    this.sendResponse(conn, request, response);
                }
            }
            catch (IOException ex) {
                this.shutdownConnection(conn, ex);
                if (this.eventListener != null) {
                    this.eventListener.fatalIOException(ex, conn);
                }
            }
            catch (HttpException ex) {
                this.closeConnection(conn, ex);
                if (this.eventListener == null) break block8;
                this.eventListener.fatalProtocolException(ex, conn);
            }
        }
    }

    public void outputReady(NHttpServerConnection conn, ContentEncoder encoder) {
        block5 : {
            HttpContext context = conn.getContext();
            ServerConnState connState = (ServerConnState)context.getAttribute("http.nio.conn-state");
            HttpResponse response = conn.getHttpResponse();
            try {
                ProducingNHttpEntity entity = connState.getProducingEntity();
                entity.produceContent(encoder, conn);
                if (encoder.isCompleted()) {
                    connState.finishOutput();
                    if (!this.connStrategy.keepAlive(response, context)) {
                        conn.close();
                    } else {
                        connState.reset();
                        conn.requestInput();
                    }
                    this.responseComplete(response, context);
                }
            }
            catch (IOException ex) {
                this.shutdownConnection(conn, ex);
                if (this.eventListener == null) break block5;
                this.eventListener.fatalIOException(ex, conn);
            }
        }
    }

    private void handleException(HttpException ex, HttpResponse response) {
        int code = 500;
        if (ex instanceof MethodNotSupportedException) {
            code = 501;
        } else if (ex instanceof UnsupportedHttpVersionException) {
            code = 505;
        } else if (ex instanceof ProtocolException) {
            code = 400;
        }
        response.setStatusCode(code);
        byte[] msg = EncodingUtils.getAsciiBytes(ex.getMessage());
        NByteArrayEntity entity = new NByteArrayEntity(msg);
        entity.setContentType("text/plain; charset=US-ASCII");
        response.setEntity(entity);
    }

    private void processRequest(NHttpServerConnection conn, HttpRequest request) throws IOException, HttpException {
        HttpContext context = conn.getContext();
        ServerConnState connState = (ServerConnState)context.getAttribute("http.nio.conn-state");
        ProtocolVersion ver = request.getRequestLine().getProtocolVersion();
        if (!ver.lessEquals(HttpVersion.HTTP_1_1)) {
            ver = HttpVersion.HTTP_1_1;
        }
        ResponseTriggerImpl trigger = new ResponseTriggerImpl(connState, conn);
        try {
            this.httpProcessor.process(request, context);
            NHttpRequestHandler handler = connState.getRequestHandler();
            if (handler != null) {
                HttpResponse response = this.responseFactory.newHttpResponse(ver, 200, context);
                response.setParams(new DefaultedHttpParams(response.getParams(), this.params));
                handler.handle(request, response, trigger, context);
            } else {
                HttpResponse response = this.responseFactory.newHttpResponse(ver, 501, context);
                response.setParams(new DefaultedHttpParams(response.getParams(), this.params));
                trigger.submitResponse(response);
            }
        }
        catch (HttpException ex) {
            trigger.handleException(ex);
        }
    }

    private void sendResponse(NHttpServerConnection conn, HttpRequest request, HttpResponse response) throws IOException, HttpException {
        HttpEntity entity;
        HttpContext context = conn.getContext();
        ServerConnState connState = (ServerConnState)context.getAttribute("http.nio.conn-state");
        connState.finishInput();
        context.setAttribute("http.request", request);
        this.httpProcessor.process(response, context);
        context.setAttribute("http.request", null);
        if (response.getEntity() != null && !this.canResponseHaveBody(request, response)) {
            response.setEntity(null);
        }
        if ((entity = response.getEntity()) != null) {
            if (entity instanceof ProducingNHttpEntity) {
                connState.setProducingEntity((ProducingNHttpEntity)entity);
            } else {
                connState.setProducingEntity(new NHttpEntityWrapper(entity));
            }
        }
        conn.submitResponse(response);
        if (entity == null) {
            if (!this.connStrategy.keepAlive(response, context)) {
                conn.close();
            } else {
                connState.reset();
                conn.requestInput();
            }
            this.responseComplete(response, context);
        }
    }

    protected void responseComplete(HttpResponse response, HttpContext context) {
    }

    private NHttpRequestHandler getRequestHandler(HttpRequest request) {
        NHttpRequestHandler handler = null;
        if (this.handlerResolver != null) {
            String requestURI = request.getRequestLine().getUri();
            handler = this.handlerResolver.lookup(requestURI);
        }
        return handler;
    }

    private static class ResponseTriggerImpl
    implements NHttpResponseTrigger {
        private final ServerConnState connState;
        private final IOControl iocontrol;
        private volatile boolean triggered;

        public ResponseTriggerImpl(ServerConnState connState, IOControl iocontrol) {
            this.connState = connState;
            this.iocontrol = iocontrol;
        }

        public void submitResponse(HttpResponse response) {
            Args.notNull(response, "Response");
            Asserts.check(!this.triggered, "Response already triggered");
            this.triggered = true;
            this.connState.setResponse(response);
            this.iocontrol.requestOutput();
        }

        public void handleException(HttpException ex) {
            Asserts.check(!this.triggered, "Response already triggered");
            this.triggered = true;
            this.connState.setHttpException(ex);
            this.iocontrol.requestOutput();
        }

        public void handleException(IOException ex) {
            Asserts.check(!this.triggered, "Response already triggered");
            this.triggered = true;
            this.connState.setIOException(ex);
            this.iocontrol.requestOutput();
        }
    }

    protected static class ServerConnState {
        private volatile NHttpRequestHandler requestHandler;
        private volatile HttpRequest request;
        private volatile ConsumingNHttpEntity consumingEntity;
        private volatile HttpResponse response;
        private volatile ProducingNHttpEntity producingEntity;
        private volatile IOException ioex;
        private volatile HttpException httpex;
        private volatile boolean handled;

        protected ServerConnState() {
        }

        public void finishInput() throws IOException {
            if (this.consumingEntity != null) {
                this.consumingEntity.finish();
                this.consumingEntity = null;
            }
        }

        public void finishOutput() throws IOException {
            if (this.producingEntity != null) {
                this.producingEntity.finish();
                this.producingEntity = null;
            }
        }

        public void reset() throws IOException {
            this.finishInput();
            this.request = null;
            this.finishOutput();
            this.handled = false;
            this.response = null;
            this.ioex = null;
            this.httpex = null;
            this.requestHandler = null;
        }

        public NHttpRequestHandler getRequestHandler() {
            return this.requestHandler;
        }

        public void setRequestHandler(NHttpRequestHandler requestHandler) {
            this.requestHandler = requestHandler;
        }

        public HttpRequest getRequest() {
            return this.request;
        }

        public void setRequest(HttpRequest request) {
            this.request = request;
        }

        public ConsumingNHttpEntity getConsumingEntity() {
            return this.consumingEntity;
        }

        public void setConsumingEntity(ConsumingNHttpEntity consumingEntity) {
            this.consumingEntity = consumingEntity;
        }

        public HttpResponse getResponse() {
            return this.response;
        }

        public void setResponse(HttpResponse response) {
            this.response = response;
        }

        public ProducingNHttpEntity getProducingEntity() {
            return this.producingEntity;
        }

        public void setProducingEntity(ProducingNHttpEntity producingEntity) {
            this.producingEntity = producingEntity;
        }

        public IOException getIOException() {
            return this.ioex;
        }

        public IOException getIOExepction() {
            return this.ioex;
        }

        public void setIOException(IOException ex) {
            this.ioex = ex;
        }

        public void setIOExepction(IOException ex) {
            this.ioex = ex;
        }

        public HttpException getHttpException() {
            return this.httpex;
        }

        public HttpException getHttpExepction() {
            return this.httpex;
        }

        public void setHttpException(HttpException ex) {
            this.httpex = ex;
        }

        public void setHttpExepction(HttpException ex) {
            this.httpex = ex;
        }

        public boolean isHandled() {
            return this.handled;
        }

        public void setHandled(boolean handled) {
            this.handled = handled;
        }
    }

}

