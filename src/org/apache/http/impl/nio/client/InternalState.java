/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.impl.nio.client;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.routing.RouteTracker;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;
import org.apache.http.nio.protocol.HttpAsyncResponseConsumer;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
class InternalState {
    private static final AtomicLong COUNTER = new AtomicLong(1L);
    private final long id = COUNTER.getAndIncrement();
    private final HttpAsyncRequestProducer requestProducer;
    private final HttpAsyncResponseConsumer<?> responseConsumer;
    private final HttpClientContext localContext;
    private boolean routeEstablished;
    private RouteTracker routeTracker;
    private boolean reusable;
    private long validDuration;
    private HttpRoute route;
    private HttpRequestWrapper mainRequest;
    private HttpResponse finalResponse;
    private HttpRequestWrapper currentRequest;
    private HttpResponse currentResponse;
    private ByteBuffer tmpbuf;
    private boolean requestContentProduced;
    private int execCount;
    private int redirectCount;
    private HttpUriRequest redirect;

    public InternalState(HttpAsyncRequestProducer requestProducer, HttpAsyncResponseConsumer<?> responseConsumer, HttpClientContext localContext) {
        this.requestProducer = requestProducer;
        this.responseConsumer = responseConsumer;
        this.localContext = localContext;
    }

    public long getId() {
        return this.id;
    }

    public HttpAsyncRequestProducer getRequestProducer() {
        return this.requestProducer;
    }

    public HttpAsyncResponseConsumer<?> getResponseConsumer() {
        return this.responseConsumer;
    }

    public HttpClientContext getLocalContext() {
        return this.localContext;
    }

    public boolean isRouteEstablished() {
        return this.routeEstablished;
    }

    public void setRouteEstablished(boolean b) {
        this.routeEstablished = b;
    }

    public RouteTracker getRouteTracker() {
        return this.routeTracker;
    }

    public void setRouteTracker(RouteTracker routeTracker) {
        this.routeTracker = routeTracker;
    }

    public boolean isReusable() {
        return this.reusable;
    }

    public void setReusable() {
        this.reusable = true;
    }

    public void setNonReusable() {
        this.reusable = false;
    }

    public long getValidDuration() {
        return this.validDuration;
    }

    public void setValidDuration(long validDuration) {
        this.validDuration = validDuration;
    }

    public HttpRoute getRoute() {
        return this.route;
    }

    public void setRoute(HttpRoute route) {
        this.route = route;
    }

    public HttpRequestWrapper getMainRequest() {
        return this.mainRequest;
    }

    public void setMainRequest(HttpRequestWrapper mainRequest) {
        this.mainRequest = mainRequest;
    }

    public HttpResponse getFinalResponse() {
        return this.finalResponse;
    }

    public void setFinalResponse(HttpResponse finalResponse) {
        this.finalResponse = finalResponse;
    }

    public HttpRequestWrapper getCurrentRequest() {
        return this.currentRequest;
    }

    public void setCurrentRequest(HttpRequestWrapper currentRequest) {
        this.currentRequest = currentRequest;
    }

    public HttpResponse getCurrentResponse() {
        return this.currentResponse;
    }

    public void setCurrentResponse(HttpResponse currentResponse) {
        this.currentResponse = currentResponse;
    }

    public ByteBuffer getTmpbuf() {
        if (this.tmpbuf == null) {
            this.tmpbuf = ByteBuffer.allocate(4096);
        }
        return this.tmpbuf;
    }

    public boolean isRequestContentProduced() {
        return this.requestContentProduced;
    }

    public void setRequestContentProduced() {
        this.requestContentProduced = true;
    }

    public int getExecCount() {
        return this.execCount;
    }

    public void incrementExecCount() {
        ++this.execCount;
    }

    public int getRedirectCount() {
        return this.redirectCount;
    }

    public void incrementRedirectCount() {
        ++this.redirectCount;
    }

    public HttpUriRequest getRedirect() {
        return this.redirect;
    }

    public void setRedirect(HttpUriRequest redirect) {
        this.redirect = redirect;
    }

    public String toString() {
        return Long.toString(this.id);
    }
}

