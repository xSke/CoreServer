/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.impl.nio.client;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.protocol.RequestClientConnControl;
import org.apache.http.concurrent.BasicFuture;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClientBase;
import org.apache.http.impl.nio.client.DefaultClientExchangeHandlerImpl;
import org.apache.http.impl.nio.client.InternalClientExec;
import org.apache.http.impl.nio.client.MinimalClientExec;
import org.apache.http.nio.conn.NHttpClientConnectionManager;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;
import org.apache.http.nio.protocol.HttpAsyncResponseConsumer;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.ImmutableHttpProcessor;
import org.apache.http.protocol.RequestContent;
import org.apache.http.protocol.RequestTargetHost;
import org.apache.http.protocol.RequestUserAgent;
import org.apache.http.util.Asserts;
import org.apache.http.util.VersionInfo;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
class MinimalHttpAsyncClient
extends CloseableHttpAsyncClientBase {
    private final Log log = LogFactory.getLog(this.getClass());
    private final NHttpClientConnectionManager connmgr;
    private final InternalClientExec execChain;

    public MinimalHttpAsyncClient(NHttpClientConnectionManager connmgr, ThreadFactory threadFactory) {
        super(connmgr, threadFactory);
        this.connmgr = connmgr;
        ImmutableHttpProcessor httpProcessor = new ImmutableHttpProcessor(new RequestContent(), new RequestTargetHost(), new RequestClientConnControl(), new RequestUserAgent(VersionInfo.getUserAgent("Apache-HttpAsyncClient", "org.apache.http.nio.client", this.getClass())));
        this.execChain = new MinimalClientExec(connmgr, httpProcessor, DefaultConnectionReuseStrategy.INSTANCE, DefaultConnectionKeepAliveStrategy.INSTANCE);
    }

    public MinimalHttpAsyncClient(NHttpClientConnectionManager connmgr) {
        this(connmgr, Executors.defaultThreadFactory());
    }

    @Override
    public <T> Future<T> execute(HttpAsyncRequestProducer requestProducer, HttpAsyncResponseConsumer<T> responseConsumer, HttpContext context, FutureCallback<T> callback) {
        CloseableHttpAsyncClientBase.Status status = this.getStatus();
        Asserts.check(status == CloseableHttpAsyncClientBase.Status.ACTIVE, "Request cannot be executed; I/O reactor status: %s", new Object[]{status});
        BasicFuture<T> future = new BasicFuture<T>(callback);
        HttpClientContext localcontext = HttpClientContext.adapt(context != null ? context : new BasicHttpContext());
        DefaultClientExchangeHandlerImpl<T> handler = new DefaultClientExchangeHandlerImpl<T>(this.log, requestProducer, responseConsumer, localcontext, future, this.connmgr, this.execChain);
        try {
            handler.start();
        }
        catch (Exception ex) {
            handler.failed(ex);
        }
        return future;
    }
}

