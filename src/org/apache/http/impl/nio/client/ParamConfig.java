/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.impl.nio.client;

import java.net.InetAddress;
import java.util.Collection;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

@Deprecated
final class ParamConfig {
    private ParamConfig() {
    }

    public static RequestConfig getRequestConfig(HttpParams params) {
        return RequestConfig.custom().setAuthenticationEnabled(HttpClientParams.isAuthenticating(params)).setCircularRedirectsAllowed(params.getBooleanParameter("http.protocol.allow-circular-redirects", false)).setConnectionRequestTimeout((int)HttpClientParams.getConnectionManagerTimeout(params)).setConnectTimeout(HttpConnectionParams.getConnectionTimeout(params)).setCookieSpec(HttpClientParams.getCookiePolicy(params)).setProxy(ConnRouteParams.getDefaultProxy(params)).setExpectContinueEnabled(HttpProtocolParams.useExpectContinue(params)).setLocalAddress(ConnRouteParams.getLocalAddress(params)).setMaxRedirects(params.getIntParameter("http.protocol.max-redirects", 50)).setProxyPreferredAuthSchemes((Collection)params.getParameter("http.auth.proxy-scheme-pref")).setTargetPreferredAuthSchemes((Collection)params.getParameter("http.auth.target-scheme-pref")).setRedirectsEnabled(HttpClientParams.isRedirecting(params)).setRelativeRedirectsAllowed(!params.getBooleanParameter("http.protocol.reject-relative-redirect", false)).setSocketTimeout(HttpConnectionParams.getSoTimeout(params)).setStaleConnectionCheckEnabled(HttpConnectionParams.isStaleCheckingEnabled(params)).build();
    }
}

