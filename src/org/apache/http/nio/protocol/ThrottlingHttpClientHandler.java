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
import org.apache.http.StatusLine;
import org.apache.http.annotation.ThreadSafe;
import org.apache.http.nio.ContentDecoder;
import org.apache.http.nio.ContentEncoder;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.NHttpClientConnection;
import org.apache.http.nio.NHttpClientHandler;
import org.apache.http.nio.NHttpConnection;
import org.apache.http.nio.entity.ContentBufferEntity;
import org.apache.http.nio.entity.ContentOutputStream;
import org.apache.http.nio.protocol.EventListener;
import org.apache.http.nio.protocol.HttpRequestExecutionHandler;
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
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.util.Args;

@Deprecated
@ThreadSafe
public class ThrottlingHttpClientHandler
extends NHttpHandlerBase
implements NHttpClientHandler {
    protected HttpRequestExecutionHandler execHandler;
    protected final Executor executor;
    private final int bufsize;

    public ThrottlingHttpClientHandler(HttpProcessor httpProcessor, HttpRequestExecutionHandler execHandler, ConnectionReuseStrategy connStrategy, ByteBufferAllocator allocator, Executor executor, HttpParams params) {
        super(httpProcessor, connStrategy, allocator, params);
        Args.notNull(execHandler, "HTTP request execution handler");
        Args.notNull(executor, "Executor");
        this.execHandler = execHandler;
        this.executor = executor;
        this.bufsize = this.params.getIntParameter("http.nio.content-buffer-size", 20480);
    }

    public ThrottlingHttpClientHandler(HttpProcessor httpProcessor, HttpRequestExecutionHandler execHandler, ConnectionReuseStrategy connStrategy, Executor executor, HttpParams params) {
        this(httpProcessor, execHandler, connStrategy, DirectByteBufferAllocator.INSTANCE, executor, params);
    }

    public void connected(NHttpClientConnection conn, Object attachment) {
        HttpContext context = conn.getContext();
        this.initialize(conn, attachment);
        ClientConnState connState = new ClientConnState(this.bufsize, conn, this.allocator);
        context.setAttribute("http.nio.conn-state", connState);
        if (this.eventListener != null) {
            this.eventListener.connectionOpen(conn);
        }
        this.requestReady(conn);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void closed(NHttpClientConnection conn) {
        HttpContext context = conn.getContext();
        ClientConnState connState = (ClientConnState)context.getAttribute("http.nio.conn-state");
        if (connState != null) {
            ClientConnState clientConnState = connState;
            synchronized (clientConnState) {
                connState.close();
                connState.notifyAll();
            }
        }
        this.execHandler.finalizeContext(context);
        if (this.eventListener != null) {
            this.eventListener.connectionClosed(conn);
        }
    }

    public void exception(NHttpClientConnection conn, HttpException ex) {
        this.closeConnection(conn, ex);
        if (this.eventListener != null) {
            this.eventListener.fatalProtocolException(ex, conn);
        }
    }

    public void exception(NHttpClientConnection conn, IOException ex) {
        this.shutdownConnection(conn, ex);
        if (this.eventListener != null) {
            this.eventListener.fatalIOException(ex, conn);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void requestReady(NHttpClientConnection conn) {
        block12 : {
            HttpContext context = conn.getContext();
            ClientConnState connState = (ClientConnState)context.getAttribute("http.nio.conn-state");
            try {
                ClientConnState clientConnState = connState;
                synchronized (clientConnState) {
                    if (connState.getOutputState() != 0) {
                        return;
                    }
                    HttpRequest request = this.execHandler.submitRequest(context);
                    if (request == null) {
                        return;
                    }
                    request.setParams(new DefaultedHttpParams(request.getParams(), this.params));
                    context.setAttribute("http.request", request);
                    this.httpProcessor.process(request, context);
                    connState.setRequest(request);
                    conn.submitRequest(request);
                    connState.setOutputState(1);
                    conn.requestInput();
                    if (request instanceof HttpEntityEnclosingRequest) {
                        if (((HttpEntityEnclosingRequest)request).expectContinue()) {
                            int timeout = conn.getSocketTimeout();
                            connState.setTimeout(timeout);
                            timeout = this.params.getIntParameter("http.protocol.wait-for-continue", 3000);
                            conn.setSocketTimeout(timeout);
                            connState.setOutputState(2);
                        } else {
                            this.sendRequestBody((HttpEntityEnclosingRequest)request, connState, conn);
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
    public void outputReady(NHttpClientConnection conn, ContentEncoder encoder) {
        block8 : {
            HttpContext context = conn.getContext();
            ClientConnState connState = (ClientConnState)context.getAttribute("http.nio.conn-state");
            try {
                ClientConnState clientConnState = connState;
                synchronized (clientConnState) {
                    if (connState.getOutputState() == 2) {
                        conn.suspendOutput();
                        return;
                    }
                    ContentOutputBuffer buffer = connState.getOutbuffer();
                    buffer.produceContent(encoder);
                    if (encoder.isCompleted()) {
                        connState.setInputState(8);
                    } else {
                        connState.setInputState(4);
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

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void responseReceived(NHttpClientConnection conn) {
        block13 : {
            HttpContext context = conn.getContext();
            ClientConnState connState = (ClientConnState)context.getAttribute("http.nio.conn-state");
            try {
                ClientConnState clientConnState = connState;
                synchronized (clientConnState) {
                    HttpResponse response = conn.getHttpResponse();
                    response.setParams(new DefaultedHttpParams(response.getParams(), this.params));
                    HttpRequest request = connState.getRequest();
                    int statusCode = response.getStatusLine().getStatusCode();
                    if (statusCode < 200) {
                        if (statusCode == 100 && connState.getOutputState() == 2) {
                            connState.setOutputState(1);
                            this.continueRequest(conn, connState);
                        }
                        return;
                    }
                    connState.setResponse(response);
                    connState.setInputState(16);
                    if (connState.getOutputState() == 2) {
                        int timeout = connState.getTimeout();
                        conn.setSocketTimeout(timeout);
                        conn.resetOutput();
                    }
                    if (!this.canResponseHaveBody(request, response)) {
                        conn.resetInput();
                        response.setEntity(null);
                        connState.setInputState(64);
                        if (!this.connStrategy.keepAlive(response, context)) {
                            conn.close();
                        }
                    }
                    if (response.getEntity() != null) {
                        response.setEntity(new ContentBufferEntity(response.getEntity(), connState.getInbuffer()));
                    }
                    context.setAttribute("http.response", response);
                    this.httpProcessor.process(response, context);
                    this.handleResponse(response, connState, conn);
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
                if (this.eventListener == null) break block13;
                this.eventListener.fatalProtocolException(ex, conn);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void inputReady(NHttpClientConnection conn, ContentDecoder decoder) {
        block8 : {
            HttpContext context = conn.getContext();
            ClientConnState connState = (ClientConnState)context.getAttribute("http.nio.conn-state");
            try {
                ClientConnState clientConnState = connState;
                synchronized (clientConnState) {
                    HttpResponse response = connState.getResponse();
                    ContentInputBuffer buffer = connState.getInbuffer();
                    buffer.consumeContent(decoder);
                    if (decoder.isCompleted()) {
                        connState.setInputState(64);
                        if (!this.connStrategy.keepAlive(response, context)) {
                            conn.close();
                        }
                    } else {
                        connState.setInputState(32);
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

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void timeout(NHttpClientConnection conn) {
        block6 : {
            HttpContext context = conn.getContext();
            ClientConnState connState = (ClientConnState)context.getAttribute("http.nio.conn-state");
            try {
                ClientConnState clientConnState = connState;
                synchronized (clientConnState) {
                    if (connState.getOutputState() == 2) {
                        connState.setOutputState(1);
                        this.continueRequest(conn, connState);
                        connState.notifyAll();
                        return;
                    }
                }
            }
            catch (IOException ex) {
                this.shutdownConnection(conn, ex);
                if (this.eventListener == null) break block6;
                this.eventListener.fatalIOException(ex, conn);
            }
        }
        this.handleTimeout(conn);
    }

    private void initialize(NHttpClientConnection conn, Object attachment) {
        HttpContext context = conn.getContext();
        context.setAttribute("http.connection", conn);
        this.execHandler.initalizeContext(context, attachment);
    }

    private void continueRequest(NHttpClientConnection conn, ClientConnState connState) throws IOException {
        HttpRequest request = connState.getRequest();
        int timeout = connState.getTimeout();
        conn.setSocketTimeout(timeout);
        this.sendRequestBody((HttpEntityEnclosingRequest)request, connState, conn);
    }

    private void sendRequestBody(final HttpEntityEnclosingRequest request, final ClientConnState connState, final NHttpClientConnection conn) throws IOException {
        HttpEntity entity = request.getEntity();
        if (entity != null) {
            this.executor.execute(new Runnable(){

                /*
                 * WARNING - Removed try catching itself - possible behaviour change.
                 */
                public void run() {
                    block13 : {
                        try {
                            ClientConnState clientConnState = connState;
                            synchronized (clientConnState) {
                                try {
                                    do {
                                        int currentState = connState.getOutputState();
                                        if (connState.isWorkerRunning()) {
                                            if (currentState == -1) {
                                                return;
                                            }
                                            connState.wait();
                                            continue;
                                        }
                                        break;
                                    } while (true);
                                }
                                catch (InterruptedException ex) {
                                    connState.shutdown();
                                    return;
                                }
                                connState.setWorkerRunning(true);
                            }
                            ContentOutputStream outstream = new ContentOutputStream(connState.getOutbuffer());
                            request.getEntity().writeTo(outstream);
                            outstream.flush();
                            outstream.close();
                            ClientConnState ex = connState;
                            synchronized (ex) {
                                connState.setWorkerRunning(false);
                                connState.notifyAll();
                            }
                        }
                        catch (IOException ex) {
                            ThrottlingHttpClientHandler.this.shutdownConnection(conn, ex);
                            if (ThrottlingHttpClientHandler.this.eventListener == null) break block13;
                            ThrottlingHttpClientHandler.this.eventListener.fatalIOException(ex, conn);
                        }
                    }
                }
            });
        }
    }

    private void handleResponse(final HttpResponse response, final ClientConnState connState, final NHttpClientConnection conn) {
        final HttpContext context = conn.getContext();
        this.executor.execute(new Runnable(){

            /*
             * WARNING - Removed try catching itself - possible behaviour change.
             */
            public void run() {
                block18 : {
                    try {
                        int currentState;
                        ClientConnState clientConnState = connState;
                        synchronized (clientConnState) {
                            try {
                                do {
                                    currentState = connState.getOutputState();
                                    if (connState.isWorkerRunning()) {
                                        if (currentState == -1) {
                                            return;
                                        }
                                        connState.wait();
                                        continue;
                                    }
                                    break;
                                } while (true);
                            }
                            catch (InterruptedException ex) {
                                connState.shutdown();
                                return;
                            }
                            connState.setWorkerRunning(true);
                        }
                        ThrottlingHttpClientHandler.this.execHandler.handleResponse(response, context);
                        clientConnState = connState;
                        synchronized (clientConnState) {
                            try {
                                while ((currentState = connState.getInputState()) != 64) {
                                    if (currentState == -1) {
                                        return;
                                    }
                                    connState.wait();
                                }
                            }
                            catch (InterruptedException ex) {
                                connState.shutdown();
                            }
                            connState.resetInput();
                            connState.resetOutput();
                            if (conn.isOpen()) {
                                conn.requestOutput();
                            }
                            connState.setWorkerRunning(false);
                            connState.notifyAll();
                        }
                    }
                    catch (IOException ex) {
                        ThrottlingHttpClientHandler.this.shutdownConnection(conn, ex);
                        if (ThrottlingHttpClientHandler.this.eventListener == null) break block18;
                        ThrottlingHttpClientHandler.this.eventListener.fatalIOException(ex, conn);
                    }
                }
            }
        });
    }

    static class ClientConnState {
        public static final int SHUTDOWN = -1;
        public static final int READY = 0;
        public static final int REQUEST_SENT = 1;
        public static final int EXPECT_CONTINUE = 2;
        public static final int REQUEST_BODY_STREAM = 4;
        public static final int REQUEST_BODY_DONE = 8;
        public static final int RESPONSE_RECEIVED = 16;
        public static final int RESPONSE_BODY_STREAM = 32;
        public static final int RESPONSE_BODY_DONE = 64;
        public static final int RESPONSE_DONE = 64;
        private final SharedInputBuffer inbuffer;
        private final SharedOutputBuffer outbuffer;
        private volatile int inputState;
        private volatile int outputState;
        private volatile HttpRequest request;
        private volatile HttpResponse response;
        private volatile int timeout;
        private volatile boolean workerRunning;

        public ClientConnState(int bufsize, IOControl ioControl, ByteBufferAllocator allocator) {
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

        public int getTimeout() {
            return this.timeout;
        }

        public void setTimeout(int timeout) {
            this.timeout = timeout;
        }

        public boolean isWorkerRunning() {
            return this.workerRunning;
        }

        public void setWorkerRunning(boolean b) {
            this.workerRunning = b;
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
        }
    }

}

