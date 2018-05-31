/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.impl.nio.client;

import java.net.ProxySelector;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import javax.net.ssl.SSLContext;
import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.client.AuthenticationStrategy;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.UserTokenHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.RequestAddCookies;
import org.apache.http.client.protocol.RequestAuthCache;
import org.apache.http.client.protocol.RequestClientConnControl;
import org.apache.http.client.protocol.RequestDefaultHeaders;
import org.apache.http.client.protocol.RequestExpectContinue;
import org.apache.http.client.protocol.ResponseProcessCookies;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.Lookup;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.SchemePortResolver;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.NoConnectionReuseStrategy;
import org.apache.http.impl.auth.BasicSchemeFactory;
import org.apache.http.impl.auth.DigestSchemeFactory;
import org.apache.http.impl.auth.KerberosSchemeFactory;
import org.apache.http.impl.auth.NTLMSchemeFactory;
import org.apache.http.impl.auth.SPNegoSchemeFactory;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.NoopUserTokenHandler;
import org.apache.http.impl.client.ProxyAuthenticationStrategy;
import org.apache.http.impl.client.TargetAuthenticationStrategy;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.impl.conn.DefaultRoutePlanner;
import org.apache.http.impl.conn.DefaultSchemePortResolver;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;
import org.apache.http.impl.cookie.BestMatchSpecFactory;
import org.apache.http.impl.cookie.BrowserCompatSpecFactory;
import org.apache.http.impl.cookie.IgnoreSpecFactory;
import org.apache.http.impl.cookie.NetscapeDraftSpecFactory;
import org.apache.http.impl.cookie.RFC2109SpecFactory;
import org.apache.http.impl.cookie.RFC2965SpecFactory;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.DefaultAsyncUserTokenHandler;
import org.apache.http.impl.nio.client.IOReactorUtils;
import org.apache.http.impl.nio.client.InternalClientExec;
import org.apache.http.impl.nio.client.InternalHttpAsyncClient;
import org.apache.http.impl.nio.client.MainClientExec;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.conn.NHttpClientConnectionManager;
import org.apache.http.nio.conn.NoopIOSessionStrategy;
import org.apache.http.nio.conn.SchemeIOSessionStrategy;
import org.apache.http.nio.conn.ssl.SSLIOSessionStrategy;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpProcessorBuilder;
import org.apache.http.protocol.RequestContent;
import org.apache.http.protocol.RequestTargetHost;
import org.apache.http.protocol.RequestUserAgent;
import org.apache.http.util.TextUtils;
import org.apache.http.util.VersionInfo;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
@NotThreadSafe
public class HttpAsyncClientBuilder {
    static final String DEFAULT_USER_AGENT;
    private NHttpClientConnectionManager connManager;
    private SchemePortResolver schemePortResolver;
    private SchemeIOSessionStrategy sslStrategy;
    private X509HostnameVerifier hostnameVerifier;
    private SSLContext sslcontext;
    private ConnectionReuseStrategy reuseStrategy;
    private ConnectionKeepAliveStrategy keepAliveStrategy;
    private AuthenticationStrategy targetAuthStrategy;
    private AuthenticationStrategy proxyAuthStrategy;
    private UserTokenHandler userTokenHandler;
    private HttpProcessor httpprocessor;
    private LinkedList<HttpRequestInterceptor> requestFirst;
    private LinkedList<HttpRequestInterceptor> requestLast;
    private LinkedList<HttpResponseInterceptor> responseFirst;
    private LinkedList<HttpResponseInterceptor> responseLast;
    private HttpRoutePlanner routePlanner;
    private RedirectStrategy redirectStrategy;
    private Lookup<AuthSchemeProvider> authSchemeRegistry;
    private Lookup<CookieSpecProvider> cookieSpecRegistry;
    private CookieStore cookieStore;
    private CredentialsProvider credentialsProvider;
    private String userAgent;
    private HttpHost proxy;
    private Collection<? extends Header> defaultHeaders;
    private IOReactorConfig defaultIOReactorConfig;
    private ConnectionConfig defaultConnectionConfig;
    private RequestConfig defaultRequestConfig;
    private ThreadFactory threadFactory;
    private boolean systemProperties;
    private boolean cookieManagementDisabled;
    private boolean authCachingDisabled;
    private boolean connectionStateDisabled;
    private int maxConnTotal = 0;
    private int maxConnPerRoute = 0;

    public static HttpAsyncClientBuilder create() {
        return new HttpAsyncClientBuilder();
    }

    protected HttpAsyncClientBuilder() {
    }

    public final HttpAsyncClientBuilder setConnectionManager(NHttpClientConnectionManager connManager) {
        this.connManager = connManager;
        return this;
    }

    public final HttpAsyncClientBuilder setSchemePortResolver(SchemePortResolver schemePortResolver) {
        this.schemePortResolver = schemePortResolver;
        return this;
    }

    public final HttpAsyncClientBuilder setMaxConnTotal(int maxConnTotal) {
        this.maxConnTotal = maxConnTotal;
        return this;
    }

    public final HttpAsyncClientBuilder setMaxConnPerRoute(int maxConnPerRoute) {
        this.maxConnPerRoute = maxConnPerRoute;
        return this;
    }

    public final HttpAsyncClientBuilder setConnectionReuseStrategy(ConnectionReuseStrategy reuseStrategy) {
        this.reuseStrategy = reuseStrategy;
        return this;
    }

    public final HttpAsyncClientBuilder setKeepAliveStrategy(ConnectionKeepAliveStrategy keepAliveStrategy) {
        this.keepAliveStrategy = keepAliveStrategy;
        return this;
    }

    public final HttpAsyncClientBuilder setUserTokenHandler(UserTokenHandler userTokenHandler) {
        this.userTokenHandler = userTokenHandler;
        return this;
    }

    public final HttpAsyncClientBuilder setTargetAuthenticationStrategy(AuthenticationStrategy targetAuthStrategy) {
        this.targetAuthStrategy = targetAuthStrategy;
        return this;
    }

    public final HttpAsyncClientBuilder setProxyAuthenticationStrategy(AuthenticationStrategy proxyAuthStrategy) {
        this.proxyAuthStrategy = proxyAuthStrategy;
        return this;
    }

    public final HttpAsyncClientBuilder setHttpProcessor(HttpProcessor httpprocessor) {
        this.httpprocessor = httpprocessor;
        return this;
    }

    public final HttpAsyncClientBuilder addInterceptorFirst(HttpResponseInterceptor itcp) {
        if (itcp == null) {
            return this;
        }
        if (this.responseFirst == null) {
            this.responseFirst = new LinkedList();
        }
        this.responseFirst.addFirst(itcp);
        return this;
    }

    public final HttpAsyncClientBuilder addInterceptorLast(HttpResponseInterceptor itcp) {
        if (itcp == null) {
            return this;
        }
        if (this.responseLast == null) {
            this.responseLast = new LinkedList();
        }
        this.responseLast.addLast(itcp);
        return this;
    }

    public final HttpAsyncClientBuilder addInterceptorFirst(HttpRequestInterceptor itcp) {
        if (itcp == null) {
            return this;
        }
        if (this.requestFirst == null) {
            this.requestFirst = new LinkedList();
        }
        this.requestFirst.addFirst(itcp);
        return this;
    }

    public final HttpAsyncClientBuilder addInterceptorLast(HttpRequestInterceptor itcp) {
        if (itcp == null) {
            return this;
        }
        if (this.requestLast == null) {
            this.requestLast = new LinkedList();
        }
        this.requestLast.addLast(itcp);
        return this;
    }

    public final HttpAsyncClientBuilder setRoutePlanner(HttpRoutePlanner routePlanner) {
        this.routePlanner = routePlanner;
        return this;
    }

    public final HttpAsyncClientBuilder setRedirectStrategy(RedirectStrategy redirectStrategy) {
        this.redirectStrategy = redirectStrategy;
        return this;
    }

    public final HttpAsyncClientBuilder setDefaultCookieStore(CookieStore cookieStore) {
        this.cookieStore = cookieStore;
        return this;
    }

    public final HttpAsyncClientBuilder setDefaultCredentialsProvider(CredentialsProvider credentialsProvider) {
        this.credentialsProvider = credentialsProvider;
        return this;
    }

    public final HttpAsyncClientBuilder setDefaultAuthSchemeRegistry(Lookup<AuthSchemeProvider> authSchemeRegistry) {
        this.authSchemeRegistry = authSchemeRegistry;
        return this;
    }

    public final HttpAsyncClientBuilder setDefaultCookieSpecRegistry(Lookup<CookieSpecProvider> cookieSpecRegistry) {
        this.cookieSpecRegistry = cookieSpecRegistry;
        return this;
    }

    public final HttpAsyncClientBuilder setUserAgent(String userAgent) {
        this.userAgent = userAgent;
        return this;
    }

    public final HttpAsyncClientBuilder setProxy(HttpHost proxy) {
        this.proxy = proxy;
        return this;
    }

    public final HttpAsyncClientBuilder setSSLStrategy(SchemeIOSessionStrategy strategy) {
        this.sslStrategy = strategy;
        return this;
    }

    public final HttpAsyncClientBuilder setSSLContext(SSLContext sslcontext) {
        this.sslcontext = sslcontext;
        return this;
    }

    public final HttpAsyncClientBuilder setHostnameVerifier(X509HostnameVerifier hostnameVerifier) {
        this.hostnameVerifier = hostnameVerifier;
        return this;
    }

    public final HttpAsyncClientBuilder setDefaultHeaders(Collection<? extends Header> defaultHeaders) {
        this.defaultHeaders = defaultHeaders;
        return this;
    }

    public final HttpAsyncClientBuilder setDefaultIOReactorConfig(IOReactorConfig config) {
        this.defaultIOReactorConfig = config;
        return this;
    }

    public final HttpAsyncClientBuilder setDefaultConnectionConfig(ConnectionConfig config) {
        this.defaultConnectionConfig = config;
        return this;
    }

    public final HttpAsyncClientBuilder setDefaultRequestConfig(RequestConfig config) {
        this.defaultRequestConfig = config;
        return this;
    }

    public final HttpAsyncClientBuilder setThreadFactory(ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
        return this;
    }

    public final HttpAsyncClientBuilder disableConnectionState() {
        this.connectionStateDisabled = true;
        return this;
    }

    public final HttpAsyncClientBuilder disableCookieManagement() {
        this.cookieManagementDisabled = true;
        return this;
    }

    public final HttpAsyncClientBuilder disableAuthCaching() {
        this.authCachingDisabled = true;
        return this;
    }

    public final HttpAsyncClientBuilder useSystemProperties() {
        this.systemProperties = true;
        return this;
    }

    private static String[] split(String s) {
        if (TextUtils.isBlank(s)) {
            return null;
        }
        return s.split(" *, *");
    }

    public CloseableHttpAsyncClient build() {
        UserTokenHandler userTokenHandler;
        ConnectionKeepAliveStrategy keepAliveStrategy;
        CookieStore defaultCookieStore22;
        void defaultCookieStore22;
        Lookup<CookieSpecProvider> cookieSpecRegistry;
        RedirectStrategy redirectStrategy;
        AuthenticationStrategy targetAuthStrategy;
        SchemePortResolver schemePortResolver;
        HttpRoutePlanner routePlanner;
        Lookup<AuthSchemeProvider> authSchemeRegistry;
        HttpProcessor httpprocessor;
        ConnectionReuseStrategy reuseStrategy;
        ThreadFactory threadFactory;
        RequestConfig defaultRequestConfig;
        CredentialsProvider defaultCredentialsProvider;
        AuthenticationStrategy proxyAuthStrategy;
        NHttpClientConnectionManager connManager = this.connManager;
        if (connManager == null) {
            SchemeIOSessionStrategy sslStrategy = this.sslStrategy;
            if (sslStrategy == null) {
                SSLContext sslcontext = this.sslcontext;
                if (sslcontext == null) {
                    sslcontext = this.systemProperties ? SSLContexts.createDefault() : SSLContexts.createSystemDefault();
                }
                String[] supportedProtocols = this.systemProperties ? HttpAsyncClientBuilder.split(System.getProperty("https.protocols")) : null;
                String[] supportedCipherSuites = this.systemProperties ? HttpAsyncClientBuilder.split(System.getProperty("https.cipherSuites")) : null;
                X509HostnameVerifier hostnameVerifier = this.hostnameVerifier;
                if (hostnameVerifier == null) {
                    hostnameVerifier = SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER;
                }
                sslStrategy = new SSLIOSessionStrategy(sslcontext, supportedProtocols, supportedCipherSuites, hostnameVerifier);
            }
            ConnectingIOReactor ioreactor = IOReactorUtils.create(this.defaultIOReactorConfig != null ? this.defaultIOReactorConfig : IOReactorConfig.DEFAULT);
            PoolingNHttpClientConnectionManager poolingmgr = new PoolingNHttpClientConnectionManager(ioreactor, RegistryBuilder.create().register("http", NoopIOSessionStrategy.INSTANCE).register("https", (NoopIOSessionStrategy)sslStrategy).build());
            if (this.defaultConnectionConfig != null) {
                poolingmgr.setDefaultConnectionConfig(this.defaultConnectionConfig);
            }
            if (this.systemProperties) {
                String s = System.getProperty("http.keepAlive", "true");
                if ("true".equalsIgnoreCase(s)) {
                    s = System.getProperty("http.maxConnections", "5");
                    int max = Integer.parseInt(s);
                    poolingmgr.setDefaultMaxPerRoute(max);
                    poolingmgr.setMaxTotal(2 * max);
                }
            } else {
                if (this.maxConnTotal > 0) {
                    poolingmgr.setMaxTotal(this.maxConnTotal);
                }
                if (this.maxConnPerRoute > 0) {
                    poolingmgr.setDefaultMaxPerRoute(this.maxConnPerRoute);
                }
            }
            connManager = poolingmgr;
        }
        if ((reuseStrategy = this.reuseStrategy) == null) {
            String s;
            reuseStrategy = this.systemProperties ? ("true".equalsIgnoreCase(s = System.getProperty("http.keepAlive", "true")) ? DefaultConnectionReuseStrategy.INSTANCE : NoConnectionReuseStrategy.INSTANCE) : DefaultConnectionReuseStrategy.INSTANCE;
        }
        if ((keepAliveStrategy = this.keepAliveStrategy) == null) {
            keepAliveStrategy = DefaultConnectionKeepAliveStrategy.INSTANCE;
        }
        if ((targetAuthStrategy = this.targetAuthStrategy) == null) {
            targetAuthStrategy = TargetAuthenticationStrategy.INSTANCE;
        }
        if ((proxyAuthStrategy = this.proxyAuthStrategy) == null) {
            proxyAuthStrategy = ProxyAuthenticationStrategy.INSTANCE;
        }
        if ((userTokenHandler = this.userTokenHandler) == null) {
            userTokenHandler = !this.connectionStateDisabled ? DefaultAsyncUserTokenHandler.INSTANCE : NoopUserTokenHandler.INSTANCE;
        }
        if ((schemePortResolver = this.schemePortResolver) == null) {
            schemePortResolver = DefaultSchemePortResolver.INSTANCE;
        }
        if ((httpprocessor = this.httpprocessor) == null) {
            String userAgent = this.userAgent;
            if (userAgent == null) {
                if (this.systemProperties) {
                    userAgent = System.getProperty("http.agent");
                }
                if (userAgent == null) {
                    userAgent = DEFAULT_USER_AGENT;
                }
            }
            HttpProcessorBuilder b = HttpProcessorBuilder.create();
            if (this.requestFirst != null) {
                for (HttpRequestInterceptor i : this.requestFirst) {
                    b.addFirst(i);
                }
            }
            if (this.responseFirst != null) {
                for (HttpResponseInterceptor i : this.responseFirst) {
                    b.addFirst(i);
                }
            }
            b.addAll(new RequestDefaultHeaders(this.defaultHeaders), new RequestContent(), new RequestTargetHost(), new RequestClientConnControl(), new RequestUserAgent(userAgent), new RequestExpectContinue());
            if (!this.cookieManagementDisabled) {
                b.add(new RequestAddCookies());
            }
            if (!this.authCachingDisabled) {
                b.add(new RequestAuthCache());
            }
            if (!this.cookieManagementDisabled) {
                b.add(new ResponseProcessCookies());
            }
            if (this.requestLast != null) {
                for (HttpRequestInterceptor i : this.requestLast) {
                    b.addLast(i);
                }
            }
            if (this.responseLast != null) {
                for (HttpResponseInterceptor i : this.responseLast) {
                    b.addLast(i);
                }
            }
            httpprocessor = b.build();
        }
        if ((routePlanner = this.routePlanner) == null) {
            routePlanner = this.proxy != null ? new DefaultProxyRoutePlanner(this.proxy, schemePortResolver) : (this.systemProperties ? new SystemDefaultRoutePlanner(schemePortResolver, ProxySelector.getDefault()) : new DefaultRoutePlanner(schemePortResolver));
        }
        if ((authSchemeRegistry = this.authSchemeRegistry) == null) {
            authSchemeRegistry = RegistryBuilder.create().register("Basic", new BasicSchemeFactory()).register("Digest", (BasicSchemeFactory)((Object)new DigestSchemeFactory())).register("NTLM", (DigestSchemeFactory)((Object)new NTLMSchemeFactory())).register("negotiate", (NTLMSchemeFactory)((Object)new SPNegoSchemeFactory())).register("Kerberos", (SPNegoSchemeFactory)((Object)new KerberosSchemeFactory())).build();
        }
        if ((cookieSpecRegistry = this.cookieSpecRegistry) == null) {
            cookieSpecRegistry = RegistryBuilder.create().register("best-match", new BestMatchSpecFactory()).register("standard", (BestMatchSpecFactory)((Object)new RFC2965SpecFactory())).register("compatibility", (RFC2965SpecFactory)((Object)new BrowserCompatSpecFactory())).register("netscape", (BrowserCompatSpecFactory)((Object)new NetscapeDraftSpecFactory())).register("ignoreCookies", (NetscapeDraftSpecFactory)((Object)new IgnoreSpecFactory())).register("rfc2109", (IgnoreSpecFactory)((Object)new RFC2109SpecFactory())).register("rfc2965", (RFC2109SpecFactory)((Object)new RFC2965SpecFactory())).build();
        }
        if ((defaultCookieStore22 = this.cookieStore) == null) {
            BasicCookieStore defaultCookieStore22 = new BasicCookieStore();
        }
        if ((defaultCredentialsProvider = this.credentialsProvider) == null) {
            defaultCredentialsProvider = new BasicCredentialsProvider();
        }
        if ((redirectStrategy = this.redirectStrategy) == null) {
            redirectStrategy = DefaultRedirectStrategy.INSTANCE;
        }
        if ((defaultRequestConfig = this.defaultRequestConfig) == null) {
            defaultRequestConfig = RequestConfig.DEFAULT;
        }
        if ((threadFactory = this.threadFactory) == null) {
            threadFactory = Executors.defaultThreadFactory();
        }
        MainClientExec exec = new MainClientExec(connManager, httpprocessor, routePlanner, reuseStrategy, keepAliveStrategy, redirectStrategy, targetAuthStrategy, proxyAuthStrategy, userTokenHandler);
        return new InternalHttpAsyncClient(connManager, exec, cookieSpecRegistry, authSchemeRegistry, (CookieStore)defaultCookieStore22, defaultCredentialsProvider, defaultRequestConfig, threadFactory);
    }

    static {
        VersionInfo vi = VersionInfo.loadVersionInfo("org.apache.http.nio.client", HttpAsyncClientBuilder.class.getClassLoader());
        String release = vi != null ? vi.getRelease() : "UNAVAILABLE";
        DEFAULT_USER_AGENT = "Apache-HttpAsyncClient/" + release + " (java 1.5)";
    }
}

