/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.impl.nio;

import java.io.IOException;
import java.nio.channels.ByteChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpMessage;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseFactory;
import org.apache.http.StatusLine;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.config.MessageConstraints;
import org.apache.http.entity.ContentLengthStrategy;
import org.apache.http.impl.HttpConnectionMetricsImpl;
import org.apache.http.impl.io.HttpTransportMetricsImpl;
import org.apache.http.impl.nio.NHttpClientEventHandlerAdaptor;
import org.apache.http.impl.nio.NHttpConnectionBase;
import org.apache.http.impl.nio.codecs.DefaultHttpRequestWriter;
import org.apache.http.impl.nio.codecs.DefaultHttpRequestWriterFactory;
import org.apache.http.impl.nio.codecs.DefaultHttpResponseParser;
import org.apache.http.impl.nio.codecs.DefaultHttpResponseParserFactory;
import org.apache.http.impl.nio.reactor.SessionInputBufferImpl;
import org.apache.http.impl.nio.reactor.SessionOutputBufferImpl;
import org.apache.http.message.LineFormatter;
import org.apache.http.message.LineParser;
import org.apache.http.nio.ContentDecoder;
import org.apache.http.nio.ContentEncoder;
import org.apache.http.nio.NHttpClientConnection;
import org.apache.http.nio.NHttpClientEventHandler;
import org.apache.http.nio.NHttpClientHandler;
import org.apache.http.nio.NHttpClientIOTarget;
import org.apache.http.nio.NHttpMessageParser;
import org.apache.http.nio.NHttpMessageParserFactory;
import org.apache.http.nio.NHttpMessageWriter;
import org.apache.http.nio.NHttpMessageWriterFactory;
import org.apache.http.nio.reactor.IOSession;
import org.apache.http.nio.reactor.SessionBufferStatus;
import org.apache.http.nio.reactor.SessionInputBuffer;
import org.apache.http.nio.reactor.SessionOutputBuffer;
import org.apache.http.nio.util.ByteBufferAllocator;
import org.apache.http.params.HttpParamConfig;
import org.apache.http.params.HttpParams;
import org.apache.http.util.Args;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
@NotThreadSafe
public class DefaultNHttpClientConnection
extends NHttpConnectionBase
implements NHttpClientIOTarget {
    protected final NHttpMessageParser<HttpResponse> responseParser;
    protected final NHttpMessageWriter<HttpRequest> requestWriter;

    @Deprecated
    public DefaultNHttpClientConnection(IOSession session, HttpResponseFactory responseFactory, ByteBufferAllocator allocator, HttpParams params) {
        super(session, allocator, params);
        Args.notNull(responseFactory, "Response factory");
        this.responseParser = this.createResponseParser(this.inbuf, responseFactory, params);
        this.requestWriter = this.createRequestWriter(this.outbuf, params);
        this.hasBufferedInput = false;
        this.hasBufferedOutput = false;
        this.session.setBufferStatus(this);
    }

    public DefaultNHttpClientConnection(IOSession session, int buffersize, int fragmentSizeHint, ByteBufferAllocator allocator, CharsetDecoder chardecoder, CharsetEncoder charencoder, MessageConstraints constraints, ContentLengthStrategy incomingContentStrategy, ContentLengthStrategy outgoingContentStrategy, NHttpMessageWriterFactory<HttpRequest> requestWriterFactory, NHttpMessageParserFactory<HttpResponse> responseParserFactory) {
        super(session, buffersize, fragmentSizeHint, allocator, chardecoder, charencoder, incomingContentStrategy, outgoingContentStrategy);
        this.requestWriter = (requestWriterFactory != null ? requestWriterFactory : DefaultHttpRequestWriterFactory.INSTANCE).create(this.outbuf);
        this.responseParser = (responseParserFactory != null ? responseParserFactory : DefaultHttpResponseParserFactory.INSTANCE).create(this.inbuf, constraints);
    }

    public DefaultNHttpClientConnection(IOSession session, int buffersize, CharsetDecoder chardecoder, CharsetEncoder charencoder, MessageConstraints constraints) {
        this(session, buffersize, buffersize, null, chardecoder, charencoder, constraints, null, null, null, null);
    }

    public DefaultNHttpClientConnection(IOSession session, int buffersize) {
        this(session, buffersize, buffersize, null, null, null, null, null, null, null, null);
    }

    @Deprecated
    protected NHttpMessageParser<HttpResponse> createResponseParser(SessionInputBuffer buffer, HttpResponseFactory responseFactory, HttpParams params) {
        MessageConstraints constraints = HttpParamConfig.getMessageConstraints(params);
        return new DefaultHttpResponseParser(buffer, null, responseFactory, constraints);
    }

    @Deprecated
    protected NHttpMessageWriter<HttpRequest> createRequestWriter(SessionOutputBuffer buffer, HttpParams params) {
        return new DefaultHttpRequestWriter(buffer, null);
    }

    protected void onResponseReceived(HttpResponse response) {
    }

    protected void onRequestSubmitted(HttpRequest request) {
    }

    @Override
    public void resetInput() {
        this.response = null;
        this.contentDecoder = null;
        this.responseParser.reset();
    }

    @Override
    public void resetOutput() {
        this.request = null;
        this.contentEncoder = null;
        this.requestWriter.reset();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void consumeInput(NHttpClientEventHandler handler) {
        if (this.status != 0) {
            this.session.clearEvent(1);
            return;
        }
        try {
            if (this.response == null) {
                int bytesRead;
                do {
                    if ((bytesRead = this.responseParser.fillBuffer(this.session.channel())) > 0) {
                        this.inTransportMetrics.incrementBytesTransferred(bytesRead);
                    }
                    this.response = this.responseParser.parse();
                } while (bytesRead > 0 && this.response == null);
                if (this.response != null) {
                    if (this.response.getStatusLine().getStatusCode() >= 200) {
                        HttpEntity entity = this.prepareDecoder(this.response);
                        this.response.setEntity(entity);
                        this.connMetrics.incrementResponseCount();
                    }
                    this.onResponseReceived(this.response);
                    handler.responseReceived(this);
                    if (this.contentDecoder == null) {
                        this.resetInput();
                    }
                }
                if (bytesRead == -1) {
                    handler.endOfInput(this);
                }
            }
            if (this.contentDecoder != null && (this.session.getEventMask() & 1) > 0) {
                handler.inputReady(this, this.contentDecoder);
                if (this.contentDecoder.isCompleted()) {
                    this.resetInput();
                }
            }
        }
        catch (HttpException ex) {
            this.resetInput();
            handler.exception(this, ex);
        }
        catch (Exception ex) {
            handler.exception(this, ex);
        }
        finally {
            this.hasBufferedInput = this.inbuf.hasData();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void produceOutput(NHttpClientEventHandler handler) {
        try {
            int bytesWritten;
            if (this.status == 0) {
                if (this.contentEncoder == null) {
                    handler.requestReady(this);
                }
                if (this.contentEncoder != null) {
                    handler.outputReady(this, this.contentEncoder);
                    if (this.contentEncoder.isCompleted()) {
                        this.resetOutput();
                    }
                }
            }
            if (this.outbuf.hasData() && (bytesWritten = this.outbuf.flush(this.session.channel())) > 0) {
                this.outTransportMetrics.incrementBytesTransferred(bytesWritten);
            }
            if (!this.outbuf.hasData()) {
                if (this.status == 1) {
                    this.session.close();
                    this.status = 2;
                    this.resetOutput();
                }
                if (this.contentEncoder == null && this.status != 2) {
                    this.session.clearEvent(4);
                }
            }
        }
        catch (Exception ex) {
            handler.exception(this, ex);
        }
        finally {
            this.hasBufferedOutput = this.outbuf.hasData();
        }
    }

    @Override
    public void submitRequest(HttpRequest request) throws IOException, HttpException {
        Args.notNull(request, "HTTP request");
        this.assertNotClosed();
        if (this.request != null) {
            throw new HttpException("Request already submitted");
        }
        this.onRequestSubmitted(request);
        this.requestWriter.write(request);
        this.hasBufferedOutput = this.outbuf.hasData();
        if (request instanceof HttpEntityEnclosingRequest && ((HttpEntityEnclosingRequest)request).getEntity() != null) {
            this.prepareEncoder(request);
            this.request = request;
        }
        this.connMetrics.incrementRequestCount();
        this.session.setEvent(4);
    }

    @Override
    public boolean isRequestSubmitted() {
        return this.request != null;
    }

    @Override
    public void consumeInput(NHttpClientHandler handler) {
        this.consumeInput(new NHttpClientEventHandlerAdaptor(handler));
    }

    @Override
    public void produceOutput(NHttpClientHandler handler) {
        this.produceOutput(new NHttpClientEventHandlerAdaptor(handler));
    }
}

