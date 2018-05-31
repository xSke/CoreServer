/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.impl.nio.client;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.Configurable;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.nio.client.InternalClientExec;
import org.apache.http.impl.nio.client.InternalConnManager;
import org.apache.http.impl.nio.client.InternalState;
import org.apache.http.nio.ContentDecoder;
import org.apache.http.nio.ContentEncoder;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.NHttpClientConnection;
import org.apache.http.nio.conn.NHttpClientConnectionManager;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;
import org.apache.http.nio.protocol.HttpAsyncResponseConsumer;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;

class MinimalClientExec
implements InternalClientExec {
    private final Log log = LogFactory.getLog(this.getClass());
    private final NHttpClientConnectionManager connmgr;
    private final HttpProcessor httpProcessor;
    private final ConnectionReuseStrategy connReuseStrategy;
    private final ConnectionKeepAliveStrategy keepaliveStrategy;

    public MinimalClientExec(NHttpClientConnectionManager connmgr, HttpProcessor httpProcessor, ConnectionReuseStrategy connReuseStrategy, ConnectionKeepAliveStrategy keepaliveStrategy) {
        this.connmgr = connmgr;
        this.httpProcessor = httpProcessor;
        this.connReuseStrategy = connReuseStrategy;
        this.keepaliveStrategy = keepaliveStrategy;
    }

    public void prepare(InternalState state, HttpHost target, HttpRequest original) throws HttpException, IOException {
        RequestConfig config;
        if (this.log.isDebugEnabled()) {
            this.log.debug("[exchange: " + state.getId() + "] start execution");
        }
        HttpClientContext localContext = state.getLocalContext();
        if (original instanceof Configurable && (config = ((Configurable)((Object)original)).getConfig()) != null) {
            localContext.setRequestConfig(config);
        }
        HttpRequestWrapper request = HttpRequestWrapper.wrap(original);
        HttpRoute route = new HttpRoute(target);
        state.setRoute(route);
        state.setMainRequest(request);
        state.setCurrentRequest(request);
        localContext.setAttribute("http.request", request);
        localContext.setAttribute("http.target_host", target);
        localContext.setAttribute("http.route", route);
        this.httpProcessor.process(request, localContext);
    }

    public HttpRequest generateRequest(InternalState state, InternalConnManager connManager) throws IOException, HttpException {
        HttpClientContext localContext = state.getLocalContext();
        HttpRoute route = state.getRoute();
        NHttpClientConnection localConn = connManager.getConnection();
        if (!this.connmgr.isRouteComplete(localConn)) {
            this.connmgr.startRoute(localConn, route, localContext);
            this.connmgr.routeComplete(localConn, route, localContext);
        }
        localContext.setAttribute("http.connection", localConn);
        RequestConfig config = localContext.getRequestConfig();
        if (config.getSocketTimeout() > 0) {
            localConn.setSocketTimeout(config.getSocketTimeout());
        }
        return state.getCurrentRequest();
    }

    public void produceContent(InternalState state, ContentEncoder encoder, IOControl ioctrl) throws IOException {
        if (this.log.isDebugEnabled()) {
            this.log.debug("[exchange: " + state.getId() + "] produce content");
        }
        HttpAsyncRequestProducer requestProducer = state.getRequestProducer();
        state.setRequestContentProduced();
        requestProducer.produceContent(encoder, ioctrl);
        if (encoder.isCompleted()) {
            requestProducer.resetRequest();
        }
    }

    public void requestCompleted(InternalState state) {
        if (this.log.isDebugEnabled()) {
            this.log.debug("[exchange: " + state.getId() + "] Request completed");
        }
        HttpClientContext localContext = state.getLocalContext();
        HttpAsyncRequestProducer requestProducer = state.getRequestProducer();
        requestProducer.requestCompleted(localContext);
    }

    public void responseReceived(InternalState state, HttpResponse response) throws IOException, HttpException {
        if (this.log.isDebugEnabled()) {
            this.log.debug("[exchange: " + state.getId() + "] Response received " + response.getStatusLine());
        }
        HttpClientContext localContext = state.getLocalContext();
        localContext.setAttribute("http.response", response);
        this.httpProcessor.process(response, localContext);
        state.setFinalResponse(response);
        HttpAsyncResponseConsumer responseConsumer = state.getResponseConsumer();
        responseConsumer.responseReceived(response);
    }

    public void consumeContent(InternalState state, ContentDecoder decoder, IOControl ioctrl) throws IOException {
        if (this.log.isDebugEnabled()) {
            this.log.debug("[exchange: " + state.getId() + "] Consume content");
        }
        HttpAsyncResponseConsumer responseConsumer = state.getResponseConsumer();
        responseConsumer.consumeContent(decoder, ioctrl);
    }

    public void responseCompleted(InternalState state, InternalConnManager connManager) throws IOException, HttpException {
        HttpClientContext localContext = state.getLocalContext();
        HttpResponse response = state.getFinalResponse();
        if (this.connReuseStrategy.keepAlive(response, localContext)) {
            long validDuration = this.keepaliveStrategy.getKeepAliveDuration(response, localContext);
            if (this.log.isDebugEnabled()) {
                String s = validDuration > 0L ? "for " + validDuration + " " + (Object)((Object)TimeUnit.MILLISECONDS) : "indefinitely";
                this.log.debug("[exchange: " + state.getId() + "] Connection can be kept alive " + s);
            }
            state.setValidDuration(validDuration);
            state.setReusable();
        } else {
            if (this.log.isDebugEnabled()) {
                this.log.debug("[exchange: " + state.getId() + "] Connection cannot be kept alive");
            }
            state.setNonReusable();
        }
        HttpAsyncResponseConsumer responseConsumer = state.getResponseConsumer();
        responseConsumer.responseCompleted(localContext);
        if (this.log.isDebugEnabled()) {
            this.log.debug("[exchange: " + state.getId() + "] Response processed");
        }
        connManager.releaseConnection();
    }
}

