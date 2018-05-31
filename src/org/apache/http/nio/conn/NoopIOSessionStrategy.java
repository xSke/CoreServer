/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.conn;

import org.apache.http.HttpHost;
import org.apache.http.nio.conn.SchemeIOSessionStrategy;
import org.apache.http.nio.reactor.IOSession;

public class NoopIOSessionStrategy
implements SchemeIOSessionStrategy {
    public static final NoopIOSessionStrategy INSTANCE = new NoopIOSessionStrategy();

    public IOSession upgrade(HttpHost host, IOSession iosession) {
        return iosession;
    }

    public boolean isLayeringRequired() {
        return false;
    }
}

