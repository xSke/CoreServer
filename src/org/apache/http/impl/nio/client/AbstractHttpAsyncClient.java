/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.impl.nio.client;

import java.io.IOException;
import java.net.URI;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.auth.AuthSchemeFactory;
import org.apache.http.auth.AuthSchemeRegistry;
import org.apache.http.client.AuthenticationStrategy;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.UserTokenHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.concurrent.BasicFuture;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.cookie.CookieSpecFactory;
import org.apache.http.cookie.CookieSpecRegistry;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.auth.BasicSchemeFactory;
import org.apache.http.impl.auth.DigestSchemeFactory;
import org.apache.http.impl.auth.KerberosSchemeFactory;
import org.apache.http.impl.auth.NTLMSchemeFactory;
import org.apache.http.impl.auth.SPNegoSchemeFactory;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.DefaultUserTokenHandler;
import org.apache.http.impl.client.ProxyAuthenticationStrategy;
import org.apache.http.impl.client.TargetAuthenticationStrategy;
import org.apache.http.impl.cookie.BestMatchSpecFactory;
import org.apache.http.impl.cookie.BrowserCompatSpecFactory;
import org.apache.http.impl.cookie.IgnoreSpecFactory;
import org.apache.http.impl.cookie.NetscapeDraftSpecFactory;
import org.apache.http.impl.cookie.RFC2109SpecFactory;
import org.apache.http.impl.cookie.RFC2965SpecFactory;
import org.apache.http.impl.nio.DefaultHttpClientIODispatch;
import org.apache.http.impl.nio.client.DefaultAsyncRequestDirector;
import org.apache.http.impl.nio.client.DefaultResultCallback;
import org.apache.http.impl.nio.client.InternalIOReactorExceptionHandler;
import org.apache.http.impl.nio.client.LoggingAsyncRequestExecutor;
import org.apache.http.impl.nio.client.ResultCallback;
import org.apache.http.impl.nio.conn.DefaultHttpAsyncRoutePlanner;
import org.apache.http.impl.nio.conn.PoolingClientAsyncConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.NHttpClientEventHandler;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.http.nio.client.methods.HttpAsyncMethods;
import org.apache.http.nio.conn.ClientAsyncConnectionManager;
import org.apache.http.nio.conn.scheme.AsyncSchemeRegistry;
import org.apache.http.nio.protocol.HttpAsyncRequestExecutionHandler;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;
import org.apache.http.nio.protocol.HttpAsyncResponseConsumer;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.nio.reactor.IOEventDispatch;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.nio.reactor.IOReactorExceptionHandler;
import org.apache.http.nio.reactor.IOReactorStatus;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.BasicHttpProcessor;
import org.apache.http.protocol.DefaultedHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.ImmutableHttpProcessor;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
@Deprecated
public abstract class AbstractHttpAsyncClient
implements HttpAsyncClient {
    private final Log log = LogFactory.getLog(this.getClass());
    private final ClientAsyncConnectionManager connmgr;
    private final Queue<HttpAsyncRequestExecutionHandler<?>> queue;
    private Thread reactorThread;
    private BasicHttpProcessor mutableProcessor;
    private ImmutableHttpProcessor protocolProcessor;
    private ConnectionReuseStrategy reuseStrategy;
    private ConnectionKeepAliveStrategy keepAliveStrategy;
    private RedirectStrategy redirectStrategy;
    private CookieSpecRegistry supportedCookieSpecs;
    private CookieStore cookieStore;
    private AuthSchemeRegistry supportedAuthSchemes;
    private AuthenticationStrategy targetAuthStrategy;
    private AuthenticationStrategy proxyAuthStrategy;
    private CredentialsProvider credsProvider;
    private HttpRoutePlanner routePlanner;
    private UserTokenHandler userTokenHandler;
    private HttpParams params;
    private volatile boolean terminated;

    protected AbstractHttpAsyncClient(ClientAsyncConnectionManager connmgr) {
        this.connmgr = connmgr;
        this.queue = new ConcurrentLinkedQueue();
    }

    protected AbstractHttpAsyncClient(IOReactorConfig config) throws IOReactorException {
        DefaultConnectingIOReactor defaultioreactor = new DefaultConnectingIOReactor(config);
        defaultioreactor.setExceptionHandler(new InternalIOReactorExceptionHandler(this.log));
        this.connmgr = new PoolingClientAsyncConnectionManager(defaultioreactor);
        this.queue = new ConcurrentLinkedQueue();
    }

    protected abstract HttpParams createHttpParams();

    protected abstract BasicHttpProcessor createHttpProcessor();

    protected HttpContext createHttpContext() {
        BasicHttpContext context = new BasicHttpContext();
        context.setAttribute("http.scheme-registry", this.getConnectionManager().getSchemeRegistry());
        context.setAttribute("http.authscheme-registry", this.getAuthSchemes());
        context.setAttribute("http.cookiespec-registry", this.getCookieSpecs());
        context.setAttribute("http.cookie-store", this.getCookieStore());
        context.setAttribute("http.auth.credentials-provider", this.getCredentialsProvider());
        return context;
    }

    protected ConnectionReuseStrategy createConnectionReuseStrategy() {
        return new DefaultConnectionReuseStrategy();
    }

    protected ConnectionKeepAliveStrategy createConnectionKeepAliveStrategy() {
        return new DefaultConnectionKeepAliveStrategy();
    }

    protected AuthSchemeRegistry createAuthSchemeRegistry() {
        AuthSchemeRegistry registry = new AuthSchemeRegistry();
        registry.register("Basic", new BasicSchemeFactory());
        registry.register("Digest", new DigestSchemeFactory());
        registry.register("NTLM", new NTLMSchemeFactory());
        registry.register("negotiate", new SPNegoSchemeFactory());
        registry.register("Kerberos", new KerberosSchemeFactory());
        return registry;
    }

    protected CookieSpecRegistry createCookieSpecRegistry() {
        CookieSpecRegistry registry = new CookieSpecRegistry();
        registry.register("best-match", new BestMatchSpecFactory());
        registry.register("compatibility", new BrowserCompatSpecFactory());
        registry.register("netscape", new NetscapeDraftSpecFactory());
        registry.register("rfc2109", new RFC2109SpecFactory());
        registry.register("rfc2965", new RFC2965SpecFactory());
        registry.register("ignoreCookies", new IgnoreSpecFactory());
        return registry;
    }

    protected AuthenticationStrategy createTargetAuthenticationStrategy() {
        return new TargetAuthenticationStrategy();
    }

    protected AuthenticationStrategy createProxyAuthenticationStrategy() {
        return new ProxyAuthenticationStrategy();
    }

    protected CookieStore createCookieStore() {
        return new BasicCookieStore();
    }

    protected CredentialsProvider createCredentialsProvider() {
        return new BasicCredentialsProvider();
    }

    protected HttpRoutePlanner createHttpRoutePlanner() {
        return new DefaultHttpAsyncRoutePlanner(this.getConnectionManager().getSchemeRegistry());
    }

    protected UserTokenHandler createUserTokenHandler() {
        return new DefaultUserTokenHandler();
    }

    public final synchronized HttpParams getParams() {
        if (this.params == null) {
            this.params = this.createHttpParams();
        }
        return this.params;
    }

    public synchronized void setParams(HttpParams params) {
        this.params = params;
    }

    public synchronized ClientAsyncConnectionManager getConnectionManager() {
        return this.connmgr;
    }

    public final synchronized ConnectionReuseStrategy getConnectionReuseStrategy() {
        if (this.reuseStrategy == null) {
            this.reuseStrategy = this.createConnectionReuseStrategy();
        }
        return this.reuseStrategy;
    }

    public synchronized void setReuseStrategy(ConnectionReuseStrategy reuseStrategy) {
        this.reuseStrategy = reuseStrategy;
    }

    public final synchronized ConnectionKeepAliveStrategy getConnectionKeepAliveStrategy() {
        if (this.keepAliveStrategy == null) {
            this.keepAliveStrategy = this.createConnectionKeepAliveStrategy();
        }
        return this.keepAliveStrategy;
    }

    public synchronized void setKeepAliveStrategy(ConnectionKeepAliveStrategy keepAliveStrategy) {
        this.keepAliveStrategy = keepAliveStrategy;
    }

    public final synchronized RedirectStrategy getRedirectStrategy() {
        if (this.redirectStrategy == null) {
            this.redirectStrategy = new DefaultRedirectStrategy();
        }
        return this.redirectStrategy;
    }

    public synchronized void setRedirectStrategy(RedirectStrategy redirectStrategy) {
        this.redirectStrategy = redirectStrategy;
    }

    public final synchronized AuthSchemeRegistry getAuthSchemes() {
        if (this.supportedAuthSchemes == null) {
            this.supportedAuthSchemes = this.createAuthSchemeRegistry();
        }
        return this.supportedAuthSchemes;
    }

    public synchronized void setAuthSchemes(AuthSchemeRegistry authSchemeRegistry) {
        this.supportedAuthSchemes = authSchemeRegistry;
    }

    public final synchronized CookieSpecRegistry getCookieSpecs() {
        if (this.supportedCookieSpecs == null) {
            this.supportedCookieSpecs = this.createCookieSpecRegistry();
        }
        return this.supportedCookieSpecs;
    }

    public synchronized void setCookieSpecs(CookieSpecRegistry cookieSpecRegistry) {
        this.supportedCookieSpecs = cookieSpecRegistry;
    }

    public final synchronized AuthenticationStrategy getTargetAuthenticationStrategy() {
        if (this.targetAuthStrategy == null) {
            this.targetAuthStrategy = this.createTargetAuthenticationStrategy();
        }
        return this.targetAuthStrategy;
    }

    public synchronized void setTargetAuthenticationStrategy(AuthenticationStrategy targetAuthStrategy) {
        this.targetAuthStrategy = targetAuthStrategy;
    }

    public final synchronized AuthenticationStrategy getProxyAuthenticationStrategy() {
        if (this.proxyAuthStrategy == null) {
            this.proxyAuthStrategy = this.createProxyAuthenticationStrategy();
        }
        return this.proxyAuthStrategy;
    }

    public synchronized void setProxyAuthenticationStrategy(AuthenticationStrategy proxyAuthStrategy) {
        this.proxyAuthStrategy = proxyAuthStrategy;
    }

    public final synchronized CookieStore getCookieStore() {
        if (this.cookieStore == null) {
            this.cookieStore = this.createCookieStore();
        }
        return this.cookieStore;
    }

    public synchronized void setCookieStore(CookieStore cookieStore) {
        this.cookieStore = cookieStore;
    }

    public final synchronized CredentialsProvider getCredentialsProvider() {
        if (this.credsProvider == null) {
            this.credsProvider = this.createCredentialsProvider();
        }
        return this.credsProvider;
    }

    public synchronized void setCredentialsProvider(CredentialsProvider credsProvider) {
        this.credsProvider = credsProvider;
    }

    public final synchronized HttpRoutePlanner getRoutePlanner() {
        if (this.routePlanner == null) {
            this.routePlanner = this.createHttpRoutePlanner();
        }
        return this.routePlanner;
    }

    public synchronized void setRoutePlanner(HttpRoutePlanner routePlanner) {
        this.routePlanner = routePlanner;
    }

    public final synchronized UserTokenHandler getUserTokenHandler() {
        if (this.userTokenHandler == null) {
            this.userTokenHandler = this.createUserTokenHandler();
        }
        return this.userTokenHandler;
    }

    public synchronized void setUserTokenHandler(UserTokenHandler userTokenHandler) {
        this.userTokenHandler = userTokenHandler;
    }

    protected final synchronized BasicHttpProcessor getHttpProcessor() {
        if (this.mutableProcessor == null) {
            this.mutableProcessor = this.createHttpProcessor();
        }
        return this.mutableProcessor;
    }

    private final synchronized HttpProcessor getProtocolProcessor() {
        if (this.protocolProcessor == null) {
            BasicHttpProcessor proc = this.getHttpProcessor();
            int reqc = proc.getRequestInterceptorCount();
            HttpRequestInterceptor[] reqinterceptors = new HttpRequestInterceptor[reqc];
            for (int i = 0; i < reqc; ++i) {
                reqinterceptors[i] = proc.getRequestInterceptor(i);
            }
            int resc = proc.getResponseInterceptorCount();
            HttpResponseInterceptor[] resinterceptors = new HttpResponseInterceptor[resc];
            for (int i = 0; i < resc; ++i) {
                resinterceptors[i] = proc.getResponseInterceptor(i);
            }
            this.protocolProcessor = new ImmutableHttpProcessor(reqinterceptors, resinterceptors);
        }
        return this.protocolProcessor;
    }

    public synchronized int getResponseInterceptorCount() {
        return this.getHttpProcessor().getResponseInterceptorCount();
    }

    public synchronized HttpResponseInterceptor getResponseInterceptor(int index) {
        return this.getHttpProcessor().getResponseInterceptor(index);
    }

    public synchronized HttpRequestInterceptor getRequestInterceptor(int index) {
        return this.getHttpProcessor().getRequestInterceptor(index);
    }

    public synchronized int getRequestInterceptorCount() {
        return this.getHttpProcessor().getRequestInterceptorCount();
    }

    public synchronized void addResponseInterceptor(HttpResponseInterceptor itcp) {
        this.getHttpProcessor().addInterceptor(itcp);
        this.protocolProcessor = null;
    }

    public synchronized void addResponseInterceptor(HttpResponseInterceptor itcp, int index) {
        this.getHttpProcessor().addInterceptor(itcp, index);
        this.protocolProcessor = null;
    }

    public synchronized void clearResponseInterceptors() {
        this.getHttpProcessor().clearResponseInterceptors();
        this.protocolProcessor = null;
    }

    public synchronized void removeResponseInterceptorByClass(Class<? extends HttpResponseInterceptor> clazz) {
        this.getHttpProcessor().removeResponseInterceptorByClass(clazz);
        this.protocolProcessor = null;
    }

    public synchronized void addRequestInterceptor(HttpRequestInterceptor itcp) {
        this.getHttpProcessor().addInterceptor(itcp);
        this.protocolProcessor = null;
    }

    public synchronized void addRequestInterceptor(HttpRequestInterceptor itcp, int index) {
        this.getHttpProcessor().addInterceptor(itcp, index);
        this.protocolProcessor = null;
    }

    public synchronized void clearRequestInterceptors() {
        this.getHttpProcessor().clearRequestInterceptors();
        this.protocolProcessor = null;
    }

    public synchronized void removeRequestInterceptorByClass(Class<? extends HttpRequestInterceptor> clazz) {
        this.getHttpProcessor().removeRequestInterceptorByClass(clazz);
        this.protocolProcessor = null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void doExecute() {
        LoggingAsyncRequestExecutor handler = new LoggingAsyncRequestExecutor();
        try {
            DefaultHttpClientIODispatch ioEventDispatch = new DefaultHttpClientIODispatch((NHttpClientEventHandler)handler, this.getParams());
            this.connmgr.execute(ioEventDispatch);
        }
        catch (Exception ex) {
            this.log.error("I/O reactor terminated abnormally", ex);
        }
        finally {
            this.terminated = true;
            while (!this.queue.isEmpty()) {
                HttpAsyncRequestExecutionHandler<?> exchangeHandler = this.queue.remove();
                exchangeHandler.cancel();
            }
        }
    }

    public IOReactorStatus getStatus() {
        return this.connmgr.getStatus();
    }

    public synchronized void start() {
        this.reactorThread = new Thread(){

            public void run() {
                AbstractHttpAsyncClient.this.doExecute();
            }
        };
        this.reactorThread.start();
    }

    public void shutdown() throws InterruptedException {
        try {
            this.connmgr.shutdown(5000L);
        }
        catch (IOException ex) {
            this.log.error("I/O error shutting down", ex);
        }
        if (this.reactorThread != null) {
            this.reactorThread.join();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public <T> Future<T> execute(HttpAsyncRequestProducer requestProducer, HttpAsyncResponseConsumer<T> responseConsumer, HttpContext context, FutureCallback<T> callback) {
        DefaultAsyncRequestDirector<T> httpexchange;
        if (this.terminated) {
            throw new IllegalStateException("Client has been shut down");
        }
        BasicFuture<T> future = new BasicFuture<T>(callback);
        DefaultResultCallback<T> resultCallback = new DefaultResultCallback<T>(future, this.queue);
        AbstractHttpAsyncClient abstractHttpAsyncClient = this;
        synchronized (abstractHttpAsyncClient) {
            HttpContext defaultContext = this.createHttpContext();
            HttpContext execContext = context == null ? defaultContext : new DefaultedHttpContext(context, defaultContext);
            httpexchange = new DefaultAsyncRequestDirector<T>(this.log, requestProducer, responseConsumer, execContext, resultCallback, this.connmgr, this.getProtocolProcessor(), this.getRoutePlanner(), this.getConnectionReuseStrategy(), this.getConnectionKeepAliveStrategy(), this.getRedirectStrategy(), this.getTargetAuthenticationStrategy(), this.getProxyAuthenticationStrategy(), this.getUserTokenHandler(), this.getParams());
        }
        this.queue.add(httpexchange);
        httpexchange.start();
        return future;
    }

    @Override
    public <T> Future<T> execute(HttpAsyncRequestProducer requestProducer, HttpAsyncResponseConsumer<T> responseConsumer, FutureCallback<T> callback) {
        return this.execute(requestProducer, responseConsumer, (HttpContext)new BasicHttpContext(), callback);
    }

    @Override
    public Future<HttpResponse> execute(HttpHost target, HttpRequest request, HttpContext context, FutureCallback<HttpResponse> callback) {
        return this.execute(HttpAsyncMethods.create(target, request), HttpAsyncMethods.createConsumer(), context, callback);
    }

    @Override
    public Future<HttpResponse> execute(HttpHost target, HttpRequest request, FutureCallback<HttpResponse> callback) {
        return this.execute(target, request, (HttpContext)new BasicHttpContext(), callback);
    }

    @Override
    public Future<HttpResponse> execute(HttpUriRequest request, FutureCallback<HttpResponse> callback) {
        return this.execute(request, new BasicHttpContext(), callback);
    }

    @Override
    public Future<HttpResponse> execute(HttpUriRequest request, HttpContext context, FutureCallback<HttpResponse> callback) {
        HttpHost target;
        try {
            target = this.determineTarget(request);
        }
        catch (ClientProtocolException ex) {
            BasicFuture<HttpResponse> future = new BasicFuture<HttpResponse>(callback);
            future.failed(ex);
            return future;
        }
        return this.execute(target, request, context, callback);
    }

    private HttpHost determineTarget(HttpUriRequest request) throws ClientProtocolException {
        HttpHost target = null;
        URI requestURI = request.getURI();
        if (requestURI.isAbsolute() && (target = URIUtils.extractHost(requestURI)) == null) {
            throw new ClientProtocolException("URI does not specify a valid host name: " + requestURI);
        }
        return target;
    }

}

