/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.conn.scheme;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.http.HttpHost;
import org.apache.http.nio.conn.scheme.AsyncScheme;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
@Deprecated
public final class AsyncSchemeRegistry {
    private final Map<String, AsyncScheme> registeredSchemes = new ConcurrentHashMap<String, AsyncScheme>();

    public final AsyncScheme getScheme(String name) {
        AsyncScheme found = this.get(name);
        if (found == null) {
            throw new IllegalStateException("Scheme '" + name + "' not registered.");
        }
        return found;
    }

    public final AsyncScheme getScheme(HttpHost host) {
        if (host == null) {
            throw new IllegalArgumentException("Host must not be null.");
        }
        return this.getScheme(host.getSchemeName());
    }

    public final AsyncScheme get(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name must not be null.");
        }
        AsyncScheme found = this.registeredSchemes.get(name);
        return found;
    }

    public final AsyncScheme register(AsyncScheme sch) {
        if (sch == null) {
            throw new IllegalArgumentException("Scheme must not be null.");
        }
        AsyncScheme old = this.registeredSchemes.put(sch.getName(), sch);
        return old;
    }

    public final AsyncScheme unregister(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name must not be null.");
        }
        AsyncScheme gone = this.registeredSchemes.remove(name);
        return gone;
    }

    public final List<String> getSchemeNames() {
        return new ArrayList<String>(this.registeredSchemes.keySet());
    }

    public void setItems(Map<String, AsyncScheme> map) {
        if (map == null) {
            return;
        }
        this.registeredSchemes.clear();
        this.registeredSchemes.putAll(map);
    }
}

