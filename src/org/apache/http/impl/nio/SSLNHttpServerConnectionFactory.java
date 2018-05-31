/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.impl.nio;

import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import javax.net.ssl.SSLContext;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestFactory;
import org.apache.http.HttpResponse;
import org.apache.http.annotation.Immutable;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.MessageConstraints;
import org.apache.http.entity.ContentLengthStrategy;
import org.apache.http.impl.ConnSupport;
import org.apache.http.impl.DefaultHttpRequestFactory;
import org.apache.http.impl.nio.DefaultNHttpServerConnection;
import org.apache.http.impl.nio.SSLContextUtils;
import org.apache.http.impl.nio.codecs.DefaultHttpRequestParserFactory;
import org.apache.http.message.LineParser;
import org.apache.http.nio.NHttpConnection;
import org.apache.http.nio.NHttpConnectionFactory;
import org.apache.http.nio.NHttpMessageParserFactory;
import org.apache.http.nio.NHttpMessageWriterFactory;
import org.apache.http.nio.reactor.IOSession;
import org.apache.http.nio.reactor.ssl.SSLIOSession;
import org.apache.http.nio.reactor.ssl.SSLMode;
import org.apache.http.nio.reactor.ssl.SSLSetupHandler;
import org.apache.http.nio.util.ByteBufferAllocator;
import org.apache.http.nio.util.HeapByteBufferAllocator;
import org.apache.http.params.HttpParamConfig;
import org.apache.http.params.HttpParams;
import org.apache.http.util.Args;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
@Immutable
public class SSLNHttpServerConnectionFactory
implements NHttpConnectionFactory<DefaultNHttpServerConnection> {
    private final SSLContext sslcontext;
    private final SSLSetupHandler sslHandler;
    private final ContentLengthStrategy incomingContentStrategy;
    private final ContentLengthStrategy outgoingContentStrategy;
    private final NHttpMessageParserFactory<HttpRequest> requestParserFactory;
    private final NHttpMessageWriterFactory<HttpResponse> responseWriterFactory;
    private final ByteBufferAllocator allocator;
    private final ConnectionConfig cconfig;

    @Deprecated
    public SSLNHttpServerConnectionFactory(SSLContext sslcontext, SSLSetupHandler sslHandler, HttpRequestFactory requestFactory, ByteBufferAllocator allocator, HttpParams params) {
        Args.notNull(requestFactory, "HTTP request factory");
        Args.notNull(allocator, "Byte buffer allocator");
        Args.notNull(params, "HTTP parameters");
        this.sslcontext = sslcontext;
        this.sslHandler = sslHandler;
        this.incomingContentStrategy = null;
        this.outgoingContentStrategy = null;
        this.requestParserFactory = new DefaultHttpRequestParserFactory(null, requestFactory);
        this.responseWriterFactory = null;
        this.allocator = allocator;
        this.cconfig = HttpParamConfig.getConnectionConfig(params);
    }

    @Deprecated
    public SSLNHttpServerConnectionFactory(SSLContext sslcontext, SSLSetupHandler sslHandler, HttpParams params) {
        this(sslcontext, sslHandler, DefaultHttpRequestFactory.INSTANCE, HeapByteBufferAllocator.INSTANCE, params);
    }

    @Deprecated
    public SSLNHttpServerConnectionFactory(HttpParams params) {
        this(null, null, params);
    }

    public SSLNHttpServerConnectionFactory(SSLContext sslcontext, SSLSetupHandler sslHandler, ContentLengthStrategy incomingContentStrategy, ContentLengthStrategy outgoingContentStrategy, NHttpMessageParserFactory<HttpRequest> requestParserFactory, NHttpMessageWriterFactory<HttpResponse> responseWriterFactory, ByteBufferAllocator allocator, ConnectionConfig cconfig) {
        this.sslcontext = sslcontext;
        this.sslHandler = sslHandler;
        this.incomingContentStrategy = incomingContentStrategy;
        this.outgoingContentStrategy = outgoingContentStrategy;
        this.requestParserFactory = requestParserFactory;
        this.responseWriterFactory = responseWriterFactory;
        this.allocator = allocator;
        this.cconfig = cconfig != null ? cconfig : ConnectionConfig.DEFAULT;
    }

    public SSLNHttpServerConnectionFactory(SSLContext sslcontext, SSLSetupHandler sslHandler, NHttpMessageParserFactory<HttpRequest> requestParserFactory, NHttpMessageWriterFactory<HttpResponse> responseWriterFactory, ByteBufferAllocator allocator, ConnectionConfig cconfig) {
        this(sslcontext, sslHandler, null, null, requestParserFactory, responseWriterFactory, allocator, cconfig);
    }

    public SSLNHttpServerConnectionFactory(SSLContext sslcontext, SSLSetupHandler sslHandler, NHttpMessageParserFactory<HttpRequest> requestParserFactory, NHttpMessageWriterFactory<HttpResponse> responseWriterFactory, ConnectionConfig cconfig) {
        this(sslcontext, sslHandler, null, null, requestParserFactory, responseWriterFactory, null, cconfig);
    }

    public SSLNHttpServerConnectionFactory(SSLContext sslcontext, SSLSetupHandler sslHandler, ConnectionConfig config) {
        this(sslcontext, sslHandler, null, null, null, null, null, config);
    }

    public SSLNHttpServerConnectionFactory(ConnectionConfig config) {
        this(null, null, null, null, null, null, null, config);
    }

    public SSLNHttpServerConnectionFactory() {
        this(null, null, null, null, null, null, null, null);
    }

    @Deprecated
    protected DefaultNHttpServerConnection createConnection(IOSession session, HttpRequestFactory requestFactory, ByteBufferAllocator allocator, HttpParams params) {
        return new DefaultNHttpServerConnection(session, requestFactory, allocator, params);
    }

    protected SSLIOSession createSSLIOSession(IOSession iosession, SSLContext sslcontext, SSLSetupHandler sslHandler) {
        SSLIOSession ssliosession = new SSLIOSession(iosession, SSLMode.SERVER, sslcontext != null ? sslcontext : SSLContextUtils.getDefault(), sslHandler);
        return ssliosession;
    }

    @Override
    public DefaultNHttpServerConnection createConnection(IOSession iosession) {
        SSLIOSession ssliosession = this.createSSLIOSession(iosession, this.sslcontext, this.sslHandler);
        iosession.setAttribute("http.session.ssl", ssliosession);
        return new DefaultNHttpServerConnection(ssliosession, this.cconfig.getBufferSize(), this.cconfig.getFragmentSizeHint(), this.allocator, ConnSupport.createDecoder(this.cconfig), ConnSupport.createEncoder(this.cconfig), this.cconfig.getMessageConstraints(), this.incomingContentStrategy, this.outgoingContentStrategy, this.requestParserFactory, this.responseWriterFactory);
    }
}

