/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.impl.nio.conn;

import java.net.InetAddress;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.nio.conn.scheme.AsyncScheme;
import org.apache.http.nio.conn.scheme.AsyncSchemeRegistry;
import org.apache.http.nio.conn.scheme.LayeringStrategy;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

@Deprecated
public class DefaultHttpAsyncRoutePlanner
implements HttpRoutePlanner {
    private final AsyncSchemeRegistry schemeRegistry;

    public DefaultHttpAsyncRoutePlanner(AsyncSchemeRegistry schemeRegistry) {
        this.schemeRegistry = schemeRegistry;
    }

    private AsyncSchemeRegistry getSchemeRegistry(HttpContext context) {
        AsyncSchemeRegistry reg = (AsyncSchemeRegistry)context.getAttribute("http.scheme-registry");
        if (reg == null) {
            reg = this.schemeRegistry;
        }
        return reg;
    }

    public HttpRoute determineRoute(HttpHost target, HttpRequest request, HttpContext context) throws HttpException {
        AsyncScheme scheme;
        if (request == null) {
            throw new IllegalStateException("Request may not be null");
        }
        HttpRoute route = ConnRouteParams.getForcedRoute(request.getParams());
        if (route != null) {
            return route;
        }
        if (target == null) {
            throw new IllegalStateException("Target host may be null");
        }
        InetAddress local = ConnRouteParams.getLocalAddress(request.getParams());
        HttpHost proxy = ConnRouteParams.getDefaultProxy(request.getParams());
        try {
            AsyncSchemeRegistry registry = this.getSchemeRegistry(context);
            scheme = registry.getScheme(target);
        }
        catch (IllegalStateException ex) {
            throw new HttpException(ex.getMessage());
        }
        LayeringStrategy layeringStrategy = scheme.getLayeringStrategy();
        boolean secure = layeringStrategy != null && layeringStrategy.isSecure();
        route = proxy == null ? new HttpRoute(target, local, secure) : new HttpRoute(target, local, proxy, secure);
        return route;
    }
}

