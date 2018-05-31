/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.protocol;

import org.apache.http.HttpRequest;
import org.apache.http.RequestLine;
import org.apache.http.annotation.ThreadSafe;
import org.apache.http.nio.protocol.HttpAsyncRequestHandler;
import org.apache.http.nio.protocol.HttpAsyncRequestHandlerMapper;
import org.apache.http.protocol.UriPatternMatcher;
import org.apache.http.util.Args;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
@ThreadSafe
public class UriHttpAsyncRequestHandlerMapper
implements HttpAsyncRequestHandlerMapper {
    private final UriPatternMatcher<HttpAsyncRequestHandler<?>> matcher;

    protected UriHttpAsyncRequestHandlerMapper(UriPatternMatcher<HttpAsyncRequestHandler<?>> matcher) {
        this.matcher = Args.notNull(matcher, "Pattern matcher");
    }

    public UriHttpAsyncRequestHandlerMapper() {
        this(new UriPatternMatcher());
    }

    public void register(String pattern, HttpAsyncRequestHandler<?> handler) {
        this.matcher.register(pattern, handler);
    }

    public void unregister(String pattern) {
        this.matcher.unregister(pattern);
    }

    protected String getRequestPath(HttpRequest request) {
        String uriPath = request.getRequestLine().getUri();
        int index = uriPath.indexOf("?");
        if (index != -1) {
            uriPath = uriPath.substring(0, index);
        } else {
            index = uriPath.indexOf("#");
            if (index != -1) {
                uriPath = uriPath.substring(0, index);
            }
        }
        return uriPath;
    }

    @Override
    public HttpAsyncRequestHandler<?> lookup(HttpRequest request) {
        return this.matcher.lookup(this.getRequestPath(request));
    }
}

