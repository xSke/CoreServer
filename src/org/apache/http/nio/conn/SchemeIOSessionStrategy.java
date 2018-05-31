/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.conn;

import java.io.IOException;
import org.apache.http.HttpHost;
import org.apache.http.nio.reactor.IOSession;

public interface SchemeIOSessionStrategy {
    public boolean isLayeringRequired();

    public IOSession upgrade(HttpHost var1, IOSession var2) throws IOException;
}

