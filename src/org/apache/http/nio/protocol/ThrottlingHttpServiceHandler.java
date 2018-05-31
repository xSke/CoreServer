/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.protocol;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.Executor;
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
import org.apache.http.annotation.ThreadSafe;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.nio.ContentDecoder;
import org.apache.http.nio.ContentEncoder;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.NHttpConnection;
import org.apache.http.nio.NHttpServerConnection;
import org.apache.http.nio.NHttpServiceHandler;
import org.apache.http.nio.entity.ContentBufferEntity;
import org.apache.http.nio.entity.ContentOutputStream;
import org.apache.http.nio.protocol.EventListener;
import org.apache.http.nio.protocol.NHttpHandlerBase;
import org.apache.http.nio.util.ByteBufferAllocator;
import org.apache.http.nio.util.ContentInputBuffer;
import org.apache.http.nio.util.ContentOutputBuffer;
import org.apache.http.nio.util.DirectByteBufferAllocator;
import org.apache.http.nio.util.SharedInputBuffer;
import org.apache.http.nio.util.SharedOutputBuffer;
import org.apache.http.params.DefaultedHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpExpectationVerifier;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.protocol.HttpRequestHandlerResolver;
import org.apache.http.util.Args;
import org.apache.http.util.EncodingUtils;
import org.apache.http.util.EntityUtils;

@Deprecated
@ThreadSafe
public class ThrottlingHttpServiceHandler
extends NHttpHandlerBase
implements NHttpServiceHandler {
    protected final HttpResponseFactory responseFactory;
    protected final Executor executor;
    protected HttpRequestHandlerResolver handlerResolver;
    protected HttpExpectationVerifier expectationVerifier;
    private final int bufsize;

    public ThrottlingHttpServiceHandler(HttpProcessor httpProcessor, HttpResponseFactory responseFactory, ConnectionReuseStrategy connStrategy, ByteBufferAllocator allocator, Executor executor, HttpParams params) {
        super(httpProcessor, connStrategy, allocator, params);
        Args.notNull(responseFactory, "Response factory");
        Args.notNull(executor, "Executor");
        this.responseFactory = responseFactory;
        this.executor = executor;
        this.bufsize = this.params.getIntParameter("http.nio.content-buffer-size", 20480);
    }

    public ThrottlingHttpServiceHandler(HttpProcessor httpProcessor, HttpResponseFactory responseFactory, ConnectionReuseStrategy connStrategy, Executor executor, HttpParams params) {
        this(httpProcessor, responseFactory, connStrategy, DirectByteBufferAllocator.INSTANCE, executor, params);
    }

    public void setHandlerResolver(HttpRequestHandlerResolver handlerResolver) {
        this.handlerResolver = handlerResolver;
    }

    public void setExpectationVerifier(HttpExpectationVerifier expectationVerifier) {
        this.expectationVerifier = expectationVerifier;
    }

    public void connected(NHttpServerConnection conn) {
        HttpContext context = conn.getContext();
        ServerConnState connState = new ServerConnState(this.bufsize, conn, this.allocator);
        context.setAttribute("http.nio.conn-state", connState);
        if (this.eventListener != null) {
            this.eventListener.connectionOpen(conn);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void closed(NHttpServerConnection conn) {
        HttpContext context = conn.getContext();
        ServerConnState connState = (ServerConnState)context.getAttribute("http.nio.conn-state");
        if (connState != null) {
            ServerConnState serverConnState = connState;
            synchronized (serverConnState) {
                connState.close();
                connState.notifyAll();
            }
        }
        if (this.eventListener != null) {
            this.eventListener.connectionClosed(conn);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void exception(NHttpServerConnection conn, HttpException httpex) {
        block9 : {
            if (conn.isResponseSubmitted()) {
                if (this.eventListener != null) {
                    this.eventListener.fatalProtocolException(httpex, conn);
                }
                return;
            }
            HttpContext context = conn.getContext();
            ServerConnState connState = (ServerConnState)context.getAttribute("http.nio.conn-state");
            try {
                HttpResponse response = this.responseFactory.newHttpResponse(HttpVersion.HTTP_1_0, 500, context);
                response.setParams(new DefaultedHttpParams(response.getParams(), this.params));
                this.handleException(httpex, response);
                response.setEntity(null);
                this.httpProcessor.process(response, context);
                ServerConnState serverConnState = connState;
                synchronized (serverConnState) {
                    connState.setResponse(response);
                    conn.requestOutput();
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
                if (this.eventListener == null) break block9;
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

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void requestReceived(final NHttpServerConnection conn) {
        ServerConnState connState;
        HttpContext context = conn.getContext();
        final HttpRequest request = conn.getHttpRequest();
        ServerConnState serverConnState = connState = (ServerConnState)context.getAttribute("http.nio.conn-state");
        synchronized (serverConnState) {
            HttpEntity entity;
            boolean contentExpected = false;
            if (request instanceof HttpEntityEnclosingRequest && (entity = ((HttpEntityEnclosingRequest)request).getEntity()) != null) {
                contentExpected = true;
            }
            if (!contentExpected) {
                conn.suspendInput();
            }
            this.executor.execute(new Runnable(){

                public void run() {
                    block4 : {
                        try {
                            ThrottlingHttpServiceHandler.this.handleRequest(request, connState, conn);
                        }
                        catch (IOException ex) {
                            ThrottlingHttpServiceHandler.this.shutdownConnection(conn, ex);
                            if (ThrottlingHttpServiceHandler.this.eventListener != null) {
                                ThrottlingHttpServiceHandler.this.eventListener.fatalIOException(ex, conn);
                            }
                        }
                        catch (HttpException ex) {
                            ThrottlingHttpServiceHandler.this.shutdownConnection(conn, ex);
                            if (ThrottlingHttpServiceHandler.this.eventListener == null) break block4;
                            ThrottlingHttpServiceHandler.this.eventListener.fatalProtocolException(ex, conn);
                        }
                    }
                }
            });
            connState.notifyAll();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void inputReady(NHttpServerConnection conn, ContentDecoder decoder) {
        block7 : {
            HttpContext context = conn.getContext();
            ServerConnState connState = (ServerConnState)context.getAttribute("http.nio.conn-state");
            try {
                ServerConnState serverConnState = connState;
                synchronized (serverConnState) {
                    ContentInputBuffer buffer = connState.getInbuffer();
                    buffer.consumeContent(decoder);
                    if (decoder.isCompleted()) {
                        connState.setInputState(4);
                    } else {
                        connState.setInputState(2);
                    }
                    connState.notifyAll();
                }
            }
            catch (IOException ex) {
                this.shutdownConnection(conn, ex);
                if (this.eventListener == null) break block7;
                this.eventListener.fatalIOException(ex, conn);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void responseReady(NHttpServerConnection conn) {
        block12 : {
            HttpContext context = conn.getContext();
            ServerConnState connState = (ServerConnState)context.getAttribute("http.nio.conn-state");
            try {
                ServerConnState serverConnState = connState;
                synchronized (serverConnState) {
                    if (connState.isExpectationFailed()) {
                        conn.resetInput();
                        connState.setExpectationFailed(false);
                    }
                    HttpResponse response = connState.getResponse();
                    if (connState.getOutputState() == 0 && response != null && !conn.isResponseSubmitted()) {
                        conn.submitResponse(response);
                        int statusCode = response.getStatusLine().getStatusCode();
                        HttpEntity entity = response.getEntity();
                        if (statusCode >= 200 && entity == null) {
                            connState.setOutputState(32);
                            if (!this.connStrategy.keepAlive(response, context)) {
                                conn.close();
                            }
                        } else {
                            connState.setOutputState(8);
                        }
                    }
                    connState.notifyAll();
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
                if (this.eventListener == null) break block12;
                this.eventListener.fatalProtocolException(ex, conn);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void outputReady(NHttpServerConnection conn, ContentEncoder encoder) {
        block8 : {
            HttpContext context = conn.getContext();
            ServerConnState connState = (ServerConnState)context.getAttribute("http.nio.conn-state");
            try {
                ServerConnState serverConnState = connState;
                synchronized (serverConnState) {
                    HttpResponse response = connState.getResponse();
                    ContentOutputBuffer buffer = connState.getOutbuffer();
                    buffer.produceContent(encoder);
                    if (encoder.isCompleted()) {
                        connState.setOutputState(32);
                        if (!this.connStrategy.keepAlive(response, context)) {
                            conn.close();
                        }
                    } else {
                        connState.setOutputState(16);
                    }
                    connState.notifyAll();
                }
            }
            catch (IOException ex) {
                this.shutdownConnection(conn, ex);
                if (this.eventListener == null) break block8;
                this.eventListener.fatalIOException(ex, conn);
            }
        }
    }

    private void handleException(HttpException ex, HttpResponse response) {
        if (ex instanceof MethodNotSupportedException) {
            response.setStatusCode(501);
        } else if (ex instanceof UnsupportedHttpVersionException) {
            response.setStatusCode(505);
        } else if (ex instanceof ProtocolException) {
            response.setStatusCode(400);
        } else {
            response.setStatusCode(500);
        }
        byte[] msg = EncodingUtils.getAsciiBytes(ex.getMessage());
        ByteArrayEntity entity = new ByteArrayEntity(msg);
        entity.setContentType("text/plain; charset=US-ASCII");
        response.setEntity(entity);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void handleRequest(HttpRequest request, ServerConnState connState, NHttpServerConnection conn) throws HttpException, IOException {
        Object buffer;
        HttpEntityEnclosingRequest eeRequest;
        HttpContext context = conn.getContext();
        ServerConnState serverConnState = connState;
        synchronized (serverConnState) {
            try {
                int currentState;
                while ((currentState = connState.getOutputState()) != 0) {
                    if (currentState == -1) {
                        return;
                    }
                    connState.wait();
                }
            }
            catch (InterruptedException ex) {
                connState.shutdown();
                return;
            }
            connState.setInputState(1);
            connState.setRequest(request);
        }
        request.setParams(new DefaultedHttpParams(request.getParams(), this.params));
        context.setAttribute("http.connection", conn);
        context.setAttribute("http.request", request);
        ProtocolVersion ver = request.getRequestLine().getProtocolVersion();
        if (!ver.lessEquals(HttpVersion.HTTP_1_1)) {
            ver = HttpVersion.HTTP_1_1;
        }
        HttpResponse response = null;
        if (request instanceof HttpEntityEnclosingRequest) {
            eeRequest = (HttpEntityEnclosingRequest)request;
            if (eeRequest.expectContinue()) {
                response = this.responseFactory.newHttpResponse(ver, 100, context);
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
                ServerConnState ex = connState;
                synchronized (ex) {
                    if (response.getStatusLine().getStatusCode() < 200) {
                        connState.setResponse(response);
                        conn.requestOutput();
                        try {
                            int currentState;
                            while ((currentState = connState.getOutputState()) != 8) {
                                if (currentState == -1) {
                                    return;
                                }
                                connState.wait();
                            }
                        }
                        catch (InterruptedException ex2) {
                            connState.shutdown();
                            return;
                        }
                        connState.resetOutput();
                        response = null;
                    } else {
                        eeRequest.setEntity(null);
                        conn.suspendInput();
                        connState.setExpectationFailed(true);
                    }
                }
            }
            if (eeRequest.getEntity() != null) {
                eeRequest.setEntity(new ContentBufferEntity(eeRequest.getEntity(), connState.getInbuffer()));
            }
        }
        if (response == null) {
            response = this.responseFactory.newHttpResponse(ver, 200, context);
            response.setParams(new DefaultedHttpParams(response.getParams(), this.params));
            context.setAttribute("http.response", response);
            try {
                this.httpProcessor.process(request, context);
                HttpRequestHandler handler = null;
                if (this.handlerResolver != null) {
                    String requestURI = request.getRequestLine().getUri();
                    handler = this.handlerResolver.lookup(requestURI);
                }
                if (handler != null) {
                    handler.handle(request, response, context);
                } else {
                    response.setStatusCode(501);
                }
            }
            catch (HttpException ex) {
                response = this.responseFactory.newHttpResponse(HttpVersion.HTTP_1_0, 500, context);
                response.setParams(new DefaultedHttpParams(response.getParams(), this.params));
                this.handleException(ex, response);
            }
        }
        if (request instanceof HttpEntityEnclosingRequest) {
            eeRequest = (HttpEntityEnclosingRequest)request;
            HttpEntity entity = eeRequest.getEntity();
            EntityUtils.consume(entity);
        }
        connState.resetInput();
        this.httpProcessor.process(response, context);
        if (!this.canResponseHaveBody(request, response)) {
            response.setEntity(null);
        }
        connState.setResponse(response);
        conn.requestOutput();
        if (response.getEntity() != null) {
            buffer = connState.getOutbuffer();
            ContentOutputStream outstream = new ContentOutputStream((ContentOutputBuffer)buffer);
            HttpEntity entity = response.getEntity();
            entity.writeTo(outstream);
            outstream.flush();
            outstream.close();
        }
        buffer = connState;
        synchronized (buffer) {
            try {
                int currentState;
                while ((currentState = connState.getOutputState()) != 32) {
                    if (currentState == -1) {
                        return;
                    }
                    connState.wait();
                }
            }
            catch (InterruptedException ex) {
                connState.shutdown();
                return;
            }
            connState.resetOutput();
            conn.requestInput();
            connState.notifyAll();
        }
    }

    protected void shutdownConnection(NHttpConnection conn, Throwable cause) {
        HttpContext context = conn.getContext();
        ServerConnState connState = (ServerConnState)context.getAttribute("http.nio.conn-state");
        super.shutdownConnection(conn, cause);
        if (connState != null) {
            connState.shutdown();
        }
    }

    static class ServerConnState {
        public static final int SHUTDOWN = -1;
        public static final int READY = 0;
        public static final int REQUEST_RECEIVED = 1;
        public static final int REQUEST_BODY_STREAM = 2;
        public static final int REQUEST_BODY_DONE = 4;
        public static final int RESPONSE_SENT = 8;
        public static final int RESPONSE_BODY_STREAM = 16;
        public static final int RESPONSE_BODY_DONE = 32;
        public static final int RESPONSE_DONE = 32;
        private final SharedInputBuffer inbuffer;
        private final SharedOutputBuffer outbuffer;
        private volatile int inputState;
        private volatile int outputState;
        private volatile HttpRequest request;
        private volatile HttpResponse response;
        private volatile boolean expectationFailure;

        public ServerConnState(int bufsize, IOControl ioControl, ByteBufferAllocator allocator) {
            this.inbuffer = new SharedInputBuffer(bufsize, ioControl, allocator);
            this.outbuffer = new SharedOutputBuffer(bufsize, ioControl, allocator);
            this.inputState = 0;
            this.outputState = 0;
        }

        public ContentInputBuffer getInbuffer() {
            return this.inbuffer;
        }

        public ContentOutputBuffer getOutbuffer() {
            return this.outbuffer;
        }

        public int getInputState() {
            return this.inputState;
        }

        public void setInputState(int inputState) {
            this.inputState = inputState;
        }

        public int getOutputState() {
            return this.outputState;
        }

        public void setOutputState(int outputState) {
            this.outputState = outputState;
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

        public boolean isExpectationFailed() {
            return this.expectationFailure;
        }

        public void setExpectationFailed(boolean b) {
            this.expectationFailure = b;
        }

        public void close() {
            this.inbuffer.close();
            this.outbuffer.close();
            this.inputState = -1;
            this.outputState = -1;
        }

        public void shutdown() {
            this.inbuffer.shutdown();
            this.outbuffer.shutdown();
            this.inputState = -1;
            this.outputState = -1;
        }

        public void resetInput() {
            this.inbuffer.reset();
            this.request = null;
            this.inputState = 0;
        }

        public void resetOutput() {
            this.outbuffer.reset();
            this.response = null;
            this.outputState = 0;
            this.expectationFailure = false;
        }
    }

}

