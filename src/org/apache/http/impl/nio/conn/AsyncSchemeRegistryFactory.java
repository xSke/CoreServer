/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.impl.nio.conn;

import org.apache.http.annotation.ThreadSafe;
import org.apache.http.nio.conn.scheme.AsyncScheme;
import org.apache.http.nio.conn.scheme.AsyncSchemeRegistry;
import org.apache.http.nio.conn.scheme.LayeringStrategy;
import org.apache.http.nio.conn.ssl.SSLLayeringStrategy;

@Deprecated
@ThreadSafe
public final class AsyncSchemeRegistryFactory {
    public static AsyncSchemeRegistry createDefault() {
        AsyncSchemeRegistry registry = new AsyncSchemeRegistry();
        registry.register(new AsyncScheme("http", 80, null));
        registry.register(new AsyncScheme("https", 443, SSLLayeringStrategy.getDefaultStrategy()));
        return registry;
    }
}

