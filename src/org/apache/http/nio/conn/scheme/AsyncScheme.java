/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.conn.scheme;

import java.util.Locale;
import org.apache.http.nio.conn.scheme.LayeringStrategy;
import org.apache.http.util.Args;
import org.apache.http.util.LangUtils;

@Deprecated
public final class AsyncScheme {
    private final String name;
    private final LayeringStrategy strategy;
    private final int defaultPort;
    private String stringRep;

    public AsyncScheme(String name, int port, LayeringStrategy strategy) {
        Args.notNull(name, "Scheme name");
        if (port <= 0 || port > 65535) {
            throw new IllegalArgumentException("Port is invalid: " + port);
        }
        this.name = name.toLowerCase(Locale.ENGLISH);
        this.strategy = strategy;
        this.defaultPort = port;
    }

    public final int getDefaultPort() {
        return this.defaultPort;
    }

    public final LayeringStrategy getLayeringStrategy() {
        return this.strategy;
    }

    public final String getName() {
        return this.name;
    }

    public final int resolvePort(int port) {
        return port <= 0 ? this.defaultPort : port;
    }

    public final String toString() {
        if (this.stringRep == null) {
            StringBuilder buffer = new StringBuilder();
            buffer.append(this.name);
            buffer.append(':');
            buffer.append(Integer.toString(this.defaultPort));
            this.stringRep = buffer.toString();
        }
        return this.stringRep;
    }

    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof AsyncScheme) {
            AsyncScheme that = (AsyncScheme)obj;
            return this.name.equals(that.name) && this.defaultPort == that.defaultPort && this.strategy.equals(that.strategy);
        }
        return false;
    }

    public int hashCode() {
        int hash = 17;
        hash = LangUtils.hashCode(hash, this.defaultPort);
        hash = LangUtils.hashCode(hash, this.name);
        hash = LangUtils.hashCode(hash, this.strategy);
        return hash;
    }
}

