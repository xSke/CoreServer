/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.pool;

import java.io.IOException;
import java.net.SocketAddress;

public interface SocketAddressResolver<T> {
    public SocketAddress resolveLocalAddress(T var1) throws IOException;

    public SocketAddress resolveRemoteAddress(T var1) throws IOException;
}

