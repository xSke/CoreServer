/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.impl.nio.client;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.commons.logging.Log;
import org.apache.http.ConnectionClosedException;
import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.Header;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.ProtocolVersion;
import org.apache.http.RequestLine;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthProtocolState;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthenticationStrategy;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.NonRepeatableRequestException;
import org.apache.http.client.RedirectException;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.UserTokenHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.AbortableHttpRequest;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.ConnectionReleaseTrigger;
import org.apache.http.conn.routing.BasicRouteDirector;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.routing.HttpRouteDirector;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.conn.routing.RouteInfo;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.ClientParamsStack;
import org.apache.http.impl.client.EntityEnclosingRequestWrapper;
import org.apache.http.impl.client.HttpAuthenticator;
import org.apache.http.impl.client.RequestWrapper;
import org.apache.http.impl.client.RoutedRequest;
import org.apache.http.impl.nio.client.ParamConfig;
import org.apache.http.impl.nio.client.ResultCallback;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.nio.ContentDecoder;
import org.apache.http.nio.ContentEncoder;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.conn.ClientAsyncConnectionManager;
import org.apache.http.nio.conn.ManagedClientAsyncConnection;
import org.apache.http.nio.conn.scheme.AsyncScheme;
import org.apache.http.nio.conn.scheme.AsyncSchemeRegistry;
import org.apache.http.nio.protocol.HttpAsyncRequestExecutionHandler;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;
import org.apache.http.nio.protocol.HttpAsyncResponseConsumer;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
@Deprecated
class DefaultAsyncRequestDirector<T>
implements HttpAsyncRequestExecutionHandler<T> {
    private static final AtomicLong COUNTER = new AtomicLong(1L);
    private final Log log;
    private final HttpAsyncRequestProducer requestProducer;
    private final HttpAsyncResponseConsumer<T> responseConsumer;
    private final HttpContext localContext;
    private final ResultCallback<T> resultCallback;
    private final ClientAsyncConnectionManager connmgr;
    private final HttpProcessor httppocessor;
    private final HttpRoutePlanner routePlanner;
    private final HttpRouteDirector routeDirector;
    private final ConnectionReuseStrategy reuseStrategy;
    private final ConnectionKeepAliveStrategy keepaliveStrategy;
    private final RedirectStrategy redirectStrategy;
    private final AuthenticationStrategy targetAuthStrategy;
    private final AuthenticationStrategy proxyAuthStrategy;
    private final UserTokenHandler userTokenHandler;
    private final AuthState targetAuthState;
    private final AuthState proxyAuthState;
    private final HttpAuthenticator authenticator;
    private final HttpParams clientParams;
    private final long id;
    private volatile boolean closed;
    private volatile DefaultAsyncRequestDirector<T> connRequestCallback;
    private volatile ManagedClientAsyncConnection managedConn;
    private RoutedRequest mainRequest;
    private RoutedRequest followup;
    private HttpResponse finalResponse;
    private ClientParamsStack params;
    private RequestWrapper currentRequest;
    private HttpResponse currentResponse;
    private boolean routeEstablished;
    private int redirectCount;
    private ByteBuffer tmpbuf;
    private boolean requestContentProduced;
    private boolean requestSent;
    private int execCount;

    public DefaultAsyncRequestDirector(Log log, HttpAsyncRequestProducer requestProducer, HttpAsyncResponseConsumer<T> responseConsumer, HttpContext localContext, ResultCallback<T> callback, ClientAsyncConnectionManager connmgr, HttpProcessor httppocessor, HttpRoutePlanner routePlanner, ConnectionReuseStrategy reuseStrategy, ConnectionKeepAliveStrategy keepaliveStrategy, RedirectStrategy redirectStrategy, AuthenticationStrategy targetAuthStrategy, AuthenticationStrategy proxyAuthStrategy, UserTokenHandler userTokenHandler, HttpParams clientParams) {
        this.log = log;
        this.requestProducer = requestProducer;
        this.responseConsumer = responseConsumer;
        this.localContext = localContext;
        this.resultCallback = callback;
        this.connmgr = connmgr;
        this.httppocessor = httppocessor;
        this.routePlanner = routePlanner;
        this.reuseStrategy = reuseStrategy;
        this.keepaliveStrategy = keepaliveStrategy;
        this.redirectStrategy = redirectStrategy;
        this.routeDirector = new BasicRouteDirector();
        this.targetAuthStrategy = targetAuthStrategy;
        this.proxyAuthStrategy = proxyAuthStrategy;
        this.userTokenHandler = userTokenHandler;
        this.targetAuthState = new AuthState();
        this.proxyAuthState = new AuthState();
        this.authenticator = new HttpAuthenticator(log);
        this.clientParams = clientParams;
        this.id = COUNTER.getAndIncrement();
    }

    @Override
    public void close() {
        if (this.closed) {
            return;
        }
        this.closed = true;
        ManagedClientAsyncConnection localConn = this.managedConn;
        if (localConn != null) {
            if (this.log.isDebugEnabled()) {
                this.log.debug("[exchange: " + this.id + "] aborting connection " + localConn);
            }
            try {
                localConn.abortConnection();
            }
            catch (IOException ioex) {
                this.log.debug("I/O error releasing connection", ioex);
            }
        }
        try {
            this.requestProducer.close();
        }
        catch (IOException ex) {
            this.log.debug("I/O error closing request producer", ex);
        }
        try {
            this.responseConsumer.close();
        }
        catch (IOException ex) {
            this.log.debug("I/O error closing response consumer", ex);
        }
    }

    public synchronized void start() {
        try {
            if (this.log.isDebugEnabled()) {
                this.log.debug("[exchange: " + this.id + "] start execution");
            }
            this.localContext.setAttribute("http.auth.target-scope", this.targetAuthState);
            this.localContext.setAttribute("http.auth.proxy-scope", this.proxyAuthState);
            HttpHost target = this.requestProducer.getTarget();
            HttpRequest request = this.requestProducer.generateRequest();
            if (request instanceof AbortableHttpRequest) {
                ((AbortableHttpRequest)((Object)request)).setReleaseTrigger(new ConnectionReleaseTrigger(){

                    public void releaseConnection() throws IOException {
                    }

                    public void abortConnection() throws IOException {
                        DefaultAsyncRequestDirector.this.cancel();
                    }
                });
            }
            this.params = new ClientParamsStack(null, this.clientParams, request.getParams(), null);
            RequestWrapper wrapper = this.wrapRequest(request);
            wrapper.setParams(this.params);
            HttpRoute route = this.determineRoute(target, wrapper, this.localContext);
            this.mainRequest = new RoutedRequest(wrapper, route);
            RequestConfig config = ParamConfig.getRequestConfig(this.params);
            this.localContext.setAttribute("http.request-config", config);
            this.requestContentProduced = false;
            this.requestConnection();
        }
        catch (Exception ex) {
            this.failed(ex);
        }
    }

    @Override
    public HttpHost getTarget() {
        return this.requestProducer.getTarget();
    }

    @Override
    public synchronized HttpRequest generateRequest() throws IOException, HttpException {
        HttpHost target;
        HttpRoute route = this.mainRequest.getRoute();
        if (!this.routeEstablished) {
            int step;
            do {
                HttpRoute fact = this.managedConn.getRoute();
                step = this.routeDirector.nextStep(route, fact);
                switch (step) {
                    case 1: 
                    case 2: {
                        break;
                    }
                    case 3: {
                        if (this.log.isDebugEnabled()) {
                            this.log.debug("[exchange: " + this.id + "] Tunnel required");
                        }
                        HttpRequest connect = this.createConnectRequest(route);
                        this.currentRequest = this.wrapRequest(connect);
                        this.currentRequest.setParams(this.params);
                        break;
                    }
                    case 4: {
                        throw new HttpException("Proxy chains are not supported");
                    }
                    case 5: {
                        this.managedConn.layerProtocol(this.localContext, this.params);
                        break;
                    }
                    case -1: {
                        throw new HttpException("Unable to establish route: planned = " + route + "; current = " + fact);
                    }
                    case 0: {
                        this.routeEstablished = true;
                        break;
                    }
                    default: {
                        throw new IllegalStateException("Unknown step indicator " + step + " from RouteDirector.");
                    }
                }
            } while (step > 0 && this.currentRequest == null);
        }
        if ((target = (HttpHost)this.params.getParameter("http.virtual-host")) == null) {
            target = route.getTargetHost();
        }
        HttpHost proxy = route.getProxyHost();
        this.localContext.setAttribute("http.target_host", target);
        this.localContext.setAttribute("http.proxy_host", proxy);
        this.localContext.setAttribute("http.connection", this.managedConn);
        this.localContext.setAttribute("http.route", route);
        if (this.currentRequest == null) {
            this.currentRequest = this.mainRequest.getRequest();
            String userinfo = this.currentRequest.getURI().getUserInfo();
            if (userinfo != null) {
                this.targetAuthState.update(new BasicScheme(), new UsernamePasswordCredentials(userinfo));
            }
            this.rewriteRequestURI(this.currentRequest, route);
        }
        this.currentRequest.resetHeaders();
        this.currentRequest.incrementExecCount();
        if (this.currentRequest.getExecCount() > 1 && !this.requestProducer.isRepeatable() && this.requestContentProduced) {
            throw new NonRepeatableRequestException("Cannot retry request with a non-repeatable request entity.");
        }
        ++this.execCount;
        if (this.log.isDebugEnabled()) {
            this.log.debug("[exchange: " + this.id + "] Attempt " + this.execCount + " to execute request");
        }
        return this.currentRequest;
    }

    @Override
    public synchronized void produceContent(ContentEncoder encoder, IOControl ioctrl) throws IOException {
        if (this.log.isDebugEnabled()) {
            this.log.debug("[exchange: " + this.id + "] produce content");
        }
        this.requestContentProduced = true;
        this.requestProducer.produceContent(encoder, ioctrl);
        if (encoder.isCompleted()) {
            this.requestProducer.resetRequest();
        }
    }

    @Override
    public void requestCompleted(HttpContext context) {
        if (this.log.isDebugEnabled()) {
            this.log.debug("[exchange: " + this.id + "] Request completed");
        }
        this.requestSent = true;
        this.requestProducer.requestCompleted(context);
    }

    @Override
    public boolean isRepeatable() {
        return this.requestProducer.isRepeatable();
    }

    @Override
    public void resetRequest() throws IOException {
        this.requestSent = false;
        this.requestProducer.resetRequest();
    }

    @Override
    public synchronized void responseReceived(HttpResponse response) throws IOException, HttpException {
        if (this.log.isDebugEnabled()) {
            this.log.debug("[exchange: " + this.id + "] Response received " + response.getStatusLine());
        }
        this.currentResponse = response;
        this.currentResponse.setParams(this.params);
        int status = this.currentResponse.getStatusLine().getStatusCode();
        if (!this.routeEstablished) {
            String method = this.currentRequest.getMethod();
            if (method.equalsIgnoreCase("CONNECT") && status == 200) {
                this.managedConn.tunnelTarget(this.params);
            } else {
                this.followup = this.handleConnectResponse();
                if (this.followup == null) {
                    this.finalResponse = response;
                }
            }
        } else {
            this.followup = this.handleResponse();
            if (this.followup == null) {
                this.finalResponse = response;
            }
            Object userToken = this.localContext.getAttribute("http.user-token");
            if (this.managedConn != null) {
                if (userToken == null) {
                    userToken = this.userTokenHandler.getUserToken(this.localContext);
                    this.localContext.setAttribute("http.user-token", userToken);
                }
                if (userToken != null) {
                    this.managedConn.setState(userToken);
                }
            }
        }
        if (this.finalResponse != null) {
            this.responseConsumer.responseReceived(response);
        }
    }

    @Override
    public synchronized void consumeContent(ContentDecoder decoder, IOControl ioctrl) throws IOException {
        if (this.log.isDebugEnabled()) {
            this.log.debug("[exchange: " + this.id + "] Consume content");
        }
        if (this.finalResponse != null) {
            this.responseConsumer.consumeContent(decoder, ioctrl);
        } else {
            if (this.tmpbuf == null) {
                this.tmpbuf = ByteBuffer.allocate(2048);
            }
            this.tmpbuf.clear();
            decoder.read(this.tmpbuf);
        }
    }

    private void releaseConnection() {
        if (this.managedConn != null) {
            if (this.log.isDebugEnabled()) {
                this.log.debug("[exchange: " + this.id + "] releasing connection " + this.managedConn);
            }
            try {
                this.managedConn.getContext().removeAttribute("http.nio.exchange-handler");
                this.managedConn.releaseConnection();
            }
            catch (IOException ioex) {
                this.log.debug("I/O error releasing connection", ioex);
            }
            this.managedConn = null;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public synchronized void failed(Exception ex) {
        try {
            if (!this.requestSent) {
                this.requestProducer.failed(ex);
            }
            this.responseConsumer.failed(ex);
        }
        finally {
            try {
                this.resultCallback.failed(ex, this);
            }
            finally {
                this.close();
            }
        }
    }

    @Override
    public synchronized void responseCompleted(HttpContext context) {
        if (this.log.isDebugEnabled()) {
            this.log.debug("[exchange: " + this.id + "] Response fully read");
        }
        try {
            if (this.resultCallback.isDone()) {
                return;
            }
            if (this.managedConn.isOpen()) {
                long duration = this.keepaliveStrategy.getKeepAliveDuration(this.currentResponse, this.localContext);
                if (this.log.isDebugEnabled()) {
                    String s = duration > 0L ? "for " + duration + " " + (Object)((Object)TimeUnit.MILLISECONDS) : "indefinitely";
                    this.log.debug("[exchange: " + this.id + "] Connection can be kept alive " + s);
                }
                this.managedConn.setIdleDuration(duration, TimeUnit.MILLISECONDS);
            } else {
                if (this.log.isDebugEnabled()) {
                    this.log.debug("[exchange: " + this.id + "] Connection cannot be kept alive");
                }
                this.managedConn.unmarkReusable();
                if (this.proxyAuthState.getState() == AuthProtocolState.SUCCESS && this.proxyAuthState.getAuthScheme() != null && this.proxyAuthState.getAuthScheme().isConnectionBased()) {
                    if (this.log.isDebugEnabled()) {
                        this.log.debug("[exchange: " + this.id + "] Resetting proxy auth state");
                    }
                    this.proxyAuthState.reset();
                }
                if (this.targetAuthState.getState() == AuthProtocolState.SUCCESS && this.targetAuthState.getAuthScheme() != null && this.targetAuthState.getAuthScheme().isConnectionBased()) {
                    if (this.log.isDebugEnabled()) {
                        this.log.debug("[exchange: " + this.id + "] Resetting target auth state");
                    }
                    this.targetAuthState.reset();
                }
            }
            if (this.finalResponse != null) {
                this.responseConsumer.responseCompleted(this.localContext);
                if (this.log.isDebugEnabled()) {
                    this.log.debug("[exchange: " + this.id + "] Response processed");
                }
                this.releaseConnection();
                T result = this.responseConsumer.getResult();
                Exception ex = this.responseConsumer.getException();
                if (ex == null) {
                    this.resultCallback.completed(result, this);
                } else {
                    this.resultCallback.failed(ex, this);
                }
            } else {
                if (this.followup != null) {
                    HttpRoute newRoute;
                    HttpRoute actualRoute = this.mainRequest.getRoute();
                    if (!actualRoute.equals(newRoute = this.followup.getRoute())) {
                        this.releaseConnection();
                    }
                    this.mainRequest = this.followup;
                }
                if (this.managedConn != null && !this.managedConn.isOpen()) {
                    this.releaseConnection();
                }
                if (this.managedConn != null) {
                    this.managedConn.requestOutput();
                } else {
                    this.requestConnection();
                }
            }
            this.followup = null;
            this.currentRequest = null;
            this.currentResponse = null;
        }
        catch (RuntimeException runex) {
            this.failed(runex);
            throw runex;
        }
    }

    @Override
    public synchronized boolean cancel() {
        if (this.log.isDebugEnabled()) {
            this.log.debug("[exchange: " + this.id + "] Cancelled");
        }
        try {
            boolean cancelled = this.responseConsumer.cancel();
            T result = this.responseConsumer.getResult();
            Exception ex = this.responseConsumer.getException();
            if (ex != null) {
                this.resultCallback.failed(ex, this);
            } else if (result != null) {
                this.resultCallback.completed(result, this);
            } else {
                this.resultCallback.cancelled(this);
            }
            boolean bl = cancelled;
            return bl;
        }
        catch (RuntimeException runex) {
            this.resultCallback.failed(runex, this);
            throw runex;
        }
        finally {
            this.close();
        }
    }

    @Override
    public boolean isDone() {
        return this.resultCallback.isDone();
    }

    @Override
    public T getResult() {
        return this.responseConsumer.getResult();
    }

    @Override
    public Exception getException() {
        return this.responseConsumer.getException();
    }

    private synchronized void connectionRequestCompleted(ManagedClientAsyncConnection conn) {
        if (this.log.isDebugEnabled()) {
            this.log.debug("[exchange: " + this.id + "] Connection allocated: " + conn);
        }
        this.connRequestCallback = null;
        try {
            this.managedConn = conn;
            if (this.closed) {
                conn.releaseConnection();
                return;
            }
            HttpRoute route = this.mainRequest.getRoute();
            if (!conn.isOpen()) {
                conn.open(route, this.localContext, this.params);
            }
            conn.getContext().setAttribute("http.nio.exchange-handler", this);
            conn.requestOutput();
            this.routeEstablished = route.equals(conn.getRoute());
            if (!conn.isOpen()) {
                throw new ConnectionClosedException("Connection closed");
            }
        }
        catch (IOException ex) {
            this.failed(ex);
        }
        catch (RuntimeException runex) {
            this.failed(runex);
            throw runex;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private synchronized void connectionRequestFailed(Exception ex) {
        if (this.log.isDebugEnabled()) {
            this.log.debug("[exchange: " + this.id + "] connection request failed");
        }
        this.connRequestCallback = null;
        try {
            this.resultCallback.failed(ex, this);
        }
        finally {
            this.close();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private synchronized void connectionRequestCancelled() {
        if (this.log.isDebugEnabled()) {
            this.log.debug("[exchange: " + this.id + "] Connection request cancelled");
        }
        this.connRequestCallback = null;
        try {
            this.resultCallback.cancelled(this);
        }
        finally {
            this.close();
        }
    }

    private void requestConnection() {
        HttpRoute route = this.mainRequest.getRoute();
        if (this.log.isDebugEnabled()) {
            this.log.debug("[exchange: " + this.id + "] Request connection for " + route);
        }
        long connectTimeout = HttpConnectionParams.getConnectionTimeout(this.params);
        Object userToken = this.localContext.getAttribute("http.user-token");
        this.connRequestCallback = new InternalFutureCallback();
        this.connmgr.leaseConnection(route, userToken, connectTimeout, TimeUnit.MILLISECONDS, (FutureCallback<ManagedClientAsyncConnection>)((Object)this.connRequestCallback));
    }

    public synchronized void endOfStream() {
        if (this.managedConn != null) {
            if (this.log.isDebugEnabled()) {
                this.log.debug("[exchange: " + this.id + "] Unexpected end of data stream");
            }
            this.releaseConnection();
            if (this.connRequestCallback == null) {
                this.requestConnection();
            }
        }
    }

    protected HttpRoute determineRoute(HttpHost target, HttpRequest request, HttpContext context) throws HttpException {
        HttpHost t;
        HttpHost httpHost = t = target != null ? target : (HttpHost)request.getParams().getParameter("http.default-host");
        if (t == null) {
            throw new IllegalStateException("Target host could not be resolved");
        }
        return this.routePlanner.determineRoute(t, request, context);
    }

    private RequestWrapper wrapRequest(HttpRequest request) throws ProtocolException {
        if (request instanceof HttpEntityEnclosingRequest) {
            return new EntityEnclosingRequestWrapper((HttpEntityEnclosingRequest)request);
        }
        return new RequestWrapper(request);
    }

    protected void rewriteRequestURI(RequestWrapper request, HttpRoute route) throws ProtocolException {
        try {
            URI uri = request.getURI();
            if (route.getProxyHost() != null && !route.isTunnelled()) {
                if (!uri.isAbsolute()) {
                    HttpHost target = route.getTargetHost();
                    uri = URIUtils.rewriteURI(uri, target);
                    request.setURI(uri);
                }
            } else if (uri.isAbsolute()) {
                uri = URIUtils.rewriteURI(uri, null);
                request.setURI(uri);
            }
        }
        catch (URISyntaxException ex) {
            throw new ProtocolException("Invalid URI: " + request.getRequestLine().getUri(), ex);
        }
    }

    private AsyncSchemeRegistry getSchemeRegistry(HttpContext context) {
        AsyncSchemeRegistry reg = (AsyncSchemeRegistry)context.getAttribute("http.scheme-registry");
        if (reg == null) {
            reg = this.connmgr.getSchemeRegistry();
        }
        return reg;
    }

    private HttpRequest createConnectRequest(HttpRoute route) {
        HttpHost target = route.getTargetHost();
        String host = target.getHostName();
        int port = target.getPort();
        if (port < 0) {
            AsyncSchemeRegistry registry = this.getSchemeRegistry(this.localContext);
            AsyncScheme scheme = registry.getScheme(target.getSchemeName());
            port = scheme.getDefaultPort();
        }
        StringBuilder buffer = new StringBuilder(host.length() + 6);
        buffer.append(host);
        buffer.append(':');
        buffer.append(Integer.toString(port));
        ProtocolVersion ver = HttpProtocolParams.getVersion(this.params);
        BasicHttpRequest req = new BasicHttpRequest("CONNECT", buffer.toString(), ver);
        return req;
    }

    private RoutedRequest handleResponse() throws HttpException {
        CredentialsProvider credsProvider;
        RoutedRequest followup = null;
        if (HttpClientParams.isAuthenticating(this.params) && (credsProvider = (CredentialsProvider)this.localContext.getAttribute("http.auth.credentials-provider")) != null) {
            followup = this.handleTargetChallenge(credsProvider);
            if (followup != null) {
                return followup;
            }
            followup = this.handleProxyChallenge(credsProvider);
            if (followup != null) {
                return followup;
            }
        }
        if (HttpClientParams.isRedirecting(this.params) && (followup = this.handleRedirect()) != null) {
            return followup;
        }
        return null;
    }

    private RoutedRequest handleConnectResponse() throws HttpException {
        CredentialsProvider credsProvider;
        RoutedRequest followup = null;
        if (HttpClientParams.isAuthenticating(this.params) && (credsProvider = (CredentialsProvider)this.localContext.getAttribute("http.auth.credentials-provider")) != null && (followup = this.handleProxyChallenge(credsProvider)) != null) {
            return followup;
        }
        return null;
    }

    private RoutedRequest handleRedirect() throws HttpException {
        if (this.redirectStrategy.isRedirected(this.currentRequest, this.currentResponse, this.localContext)) {
            HttpRoute route = this.mainRequest.getRoute();
            RequestWrapper request = this.mainRequest.getRequest();
            int maxRedirects = this.params.getIntParameter("http.protocol.max-redirects", 100);
            if (this.redirectCount >= maxRedirects) {
                throw new RedirectException("Maximum redirects (" + maxRedirects + ") exceeded");
            }
            ++this.redirectCount;
            HttpUriRequest redirect = this.redirectStrategy.getRedirect(this.currentRequest, this.currentResponse, this.localContext);
            HttpRequest orig = request.getOriginal();
            redirect.setHeaders(orig.getAllHeaders());
            URI uri = redirect.getURI();
            if (uri.getHost() == null) {
                throw new ProtocolException("Redirect URI does not specify a valid host name: " + uri);
            }
            HttpHost newTarget = new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme());
            if (!route.getTargetHost().equals(newTarget)) {
                if (this.log.isDebugEnabled()) {
                    this.log.debug("[exchange: " + this.id + "] Resetting target auth state");
                }
                this.targetAuthState.reset();
                AuthScheme authScheme = this.proxyAuthState.getAuthScheme();
                if (authScheme != null && authScheme.isConnectionBased()) {
                    if (this.log.isDebugEnabled()) {
                        this.log.debug("[exchange: " + this.id + "] Resetting proxy auth state");
                    }
                    this.proxyAuthState.reset();
                }
            }
            RequestWrapper newRequest = this.wrapRequest(redirect);
            newRequest.setParams(this.params);
            HttpRoute newRoute = this.determineRoute(newTarget, newRequest, this.localContext);
            if (this.log.isDebugEnabled()) {
                this.log.debug("[exchange: " + this.id + "] Redirecting to '" + uri + "' via " + newRoute);
            }
            return new RoutedRequest(newRequest, newRoute);
        }
        return null;
    }

    private RoutedRequest handleTargetChallenge(CredentialsProvider credsProvider) throws HttpException {
        HttpRoute route = this.mainRequest.getRoute();
        HttpHost target = (HttpHost)this.localContext.getAttribute("http.target_host");
        if (target == null) {
            target = route.getTargetHost();
        }
        if (this.authenticator.isAuthenticationRequested(target, this.currentResponse, this.targetAuthStrategy, this.targetAuthState, this.localContext)) {
            if (this.authenticator.authenticate(target, this.currentResponse, this.targetAuthStrategy, this.targetAuthState, this.localContext)) {
                return this.mainRequest;
            }
            return null;
        }
        return null;
    }

    private RoutedRequest handleProxyChallenge(CredentialsProvider credsProvider) throws HttpException {
        HttpRoute route = this.mainRequest.getRoute();
        HttpHost proxy = route.getProxyHost();
        if (this.authenticator.isAuthenticationRequested(proxy, this.currentResponse, this.proxyAuthStrategy, this.proxyAuthState, this.localContext)) {
            if (this.authenticator.authenticate(proxy, this.currentResponse, this.proxyAuthStrategy, this.proxyAuthState, this.localContext)) {
                return this.mainRequest;
            }
            return null;
        }
        return null;
    }

    @Override
    public HttpContext getContext() {
        return this.localContext;
    }

    @Override
    public HttpProcessor getHttpProcessor() {
        return this.httppocessor;
    }

    @Override
    public ConnectionReuseStrategy getConnectionReuseStrategy() {
        return this.reuseStrategy;
    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    class InternalFutureCallback
    implements FutureCallback<ManagedClientAsyncConnection> {
        InternalFutureCallback() {
        }

        @Override
        public void completed(ManagedClientAsyncConnection session) {
            DefaultAsyncRequestDirector.this.connectionRequestCompleted(session);
        }

        @Override
        public void failed(Exception ex) {
            DefaultAsyncRequestDirector.this.connectionRequestFailed(ex);
        }

        @Override
        public void cancelled() {
            DefaultAsyncRequestDirector.this.connectionRequestCancelled();
        }
    }

}

