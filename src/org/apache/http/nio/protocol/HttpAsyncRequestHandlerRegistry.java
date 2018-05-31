/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.protocol;

import java.util.Map;
import org.apache.http.annotation.ThreadSafe;
import org.apache.http.nio.protocol.HttpAsyncRequestHandler;
import org.apache.http.nio.protocol.HttpAsyncRequestHandlerResolver;
import org.apache.http.protocol.UriPatternMatcher;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
@Deprecated
@ThreadSafe
public class HttpAsyncRequestHandlerRegistry
implements HttpAsyncRequestHandlerResolver {
    private final UriPatternMatcher<HttpAsyncRequestHandler<?>> matcher = new UriPatternMatcher();

    public void register(String pattern, HttpAsyncRequestHandler<?> handler) {
        this.matcher.register(pattern, handler);
    }

    public void unregister(String pattern) {
        this.matcher.unregister(pattern);
    }

    public void setHandlers(Map<String, HttpAsyncRequestHandler<?>> map) {
        this.matcher.setObjects(map);
    }

    public Map<String, HttpAsyncRequestHandler<?>> getHandlers() {
        return this.matcher.getObjects();
    }

    @Override
    public HttpAsyncRequestHandler<?> lookup(String requestURI) {
        return this.matcher.lookup(requestURI);
    }
}

