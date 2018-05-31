/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.conn.scheme;

import org.apache.http.nio.reactor.IOSession;

@Deprecated
public interface LayeringStrategy {
    public boolean isSecure();

    public IOSession layer(IOSession var1);
}

