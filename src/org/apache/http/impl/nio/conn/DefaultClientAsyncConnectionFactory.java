/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.impl.nio.conn;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseFactory;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.MessageConstraints;
import org.apache.http.entity.ContentLengthStrategy;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.nio.codecs.DefaultHttpResponseParserFactory;
import org.apache.http.impl.nio.conn.DefaultClientAsyncConnection;
import org.apache.http.impl.nio.conn.ManagedNHttpClientConnectionImpl;
import org.apache.http.message.BasicLineParser;
import org.apache.http.message.LineParser;
import org.apache.http.nio.NHttpConnection;
import org.apache.http.nio.NHttpMessageParserFactory;
import org.apache.http.nio.NHttpMessageWriterFactory;
import org.apache.http.nio.conn.ClientAsyncConnection;
import org.apache.http.nio.conn.ClientAsyncConnectionFactory;
import org.apache.http.nio.conn.ManagedNHttpClientConnection;
import org.apache.http.nio.conn.NHttpConnectionFactory;
import org.apache.http.nio.reactor.IOSession;
import org.apache.http.nio.util.ByteBufferAllocator;
import org.apache.http.nio.util.HeapByteBufferAllocator;
import org.apache.http.params.HttpParams;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
@Deprecated
public class DefaultClientAsyncConnectionFactory
implements ClientAsyncConnectionFactory,
NHttpConnectionFactory<ManagedNHttpClientConnection> {
    private final Log headerlog = LogFactory.getLog("org.apache.http.headers");
    private final Log wirelog = LogFactory.getLog("org.apache.http.wire");
    private final Log log = LogFactory.getLog(ManagedNHttpClientConnectionImpl.class);
    public static final DefaultClientAsyncConnectionFactory INSTANCE = new DefaultClientAsyncConnectionFactory(null, null);
    private static AtomicLong COUNTER = new AtomicLong();
    private final HttpResponseFactory responseFactory = this.createHttpResponseFactory();
    private final NHttpMessageParserFactory<HttpResponse> responseParserFactory;
    private final ByteBufferAllocator allocator;

    public DefaultClientAsyncConnectionFactory(NHttpMessageParserFactory<HttpResponse> responseParserFactory, ByteBufferAllocator allocator) {
        this.responseParserFactory = responseParserFactory != null ? responseParserFactory : DefaultHttpResponseParserFactory.INSTANCE;
        this.allocator = allocator != null ? allocator : HeapByteBufferAllocator.INSTANCE;
    }

    public DefaultClientAsyncConnectionFactory() {
        this.responseParserFactory = new DefaultHttpResponseParserFactory(BasicLineParser.INSTANCE, this.responseFactory);
        this.allocator = this.createByteBufferAllocator();
    }

    @Deprecated
    @Override
    public ClientAsyncConnection create(String id, IOSession iosession, HttpParams params) {
        return new DefaultClientAsyncConnection(id, iosession, this.responseFactory, this.allocator, params);
    }

    @Deprecated
    protected ByteBufferAllocator createByteBufferAllocator() {
        return HeapByteBufferAllocator.INSTANCE;
    }

    @Deprecated
    protected HttpResponseFactory createHttpResponseFactory() {
        return DefaultHttpResponseFactory.INSTANCE;
    }

    @Override
    public ManagedNHttpClientConnection create(IOSession iosession, ConnectionConfig config) {
        CodingErrorAction unmappableInputAction;
        String id = "http-outgoing-" + Long.toString(COUNTER.getAndIncrement());
        CharsetDecoder chardecoder = null;
        CharsetEncoder charencoder = null;
        Charset charset = config.getCharset();
        CodingErrorAction malformedInputAction = config.getMalformedInputAction() != null ? config.getMalformedInputAction() : CodingErrorAction.REPORT;
        CodingErrorAction codingErrorAction = unmappableInputAction = config.getUnmappableInputAction() != null ? config.getUnmappableInputAction() : CodingErrorAction.REPORT;
        if (charset != null) {
            chardecoder = charset.newDecoder();
            chardecoder.onMalformedInput(malformedInputAction);
            chardecoder.onUnmappableCharacter(unmappableInputAction);
            charencoder = charset.newEncoder();
            charencoder.onMalformedInput(malformedInputAction);
            charencoder.onUnmappableCharacter(unmappableInputAction);
        }
        ManagedNHttpClientConnectionImpl conn = new ManagedNHttpClientConnectionImpl(id, this.log, this.headerlog, this.wirelog, iosession, config.getBufferSize(), config.getFragmentSizeHint(), this.allocator, chardecoder, charencoder, config.getMessageConstraints(), null, null, null, this.responseParserFactory);
        iosession.setAttribute("http.connection", conn);
        return conn;
    }
}

