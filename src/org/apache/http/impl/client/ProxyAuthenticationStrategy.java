/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.impl.client;

import java.util.Collection;
import java.util.Map;
import java.util.Queue;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.annotation.Immutable;
import org.apache.http.auth.AuthOption;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.MalformedChallengeException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.AuthenticationStrategyImpl;
import org.apache.http.protocol.HttpContext;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
@Immutable
public class ProxyAuthenticationStrategy
extends AuthenticationStrategyImpl {
    public static final ProxyAuthenticationStrategy INSTANCE = new ProxyAuthenticationStrategy();

    public ProxyAuthenticationStrategy() {
        super(407, "Proxy-Authenticate");
    }

    @Override
    Collection<String> getPreferredAuthSchemes(RequestConfig config) {
        return config.getProxyPreferredAuthSchemes();
    }
}

