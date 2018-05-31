/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio;

import org.apache.http.nio.NHttpConnection;
import org.apache.http.nio.reactor.IOSession;

public interface NHttpConnectionFactory<T extends NHttpConnection> {
    public T createConnection(IOSession var1);
}

