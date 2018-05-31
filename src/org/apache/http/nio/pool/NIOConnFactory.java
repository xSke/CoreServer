/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.pool;

import java.io.IOException;
import org.apache.http.nio.reactor.IOSession;

public interface NIOConnFactory<T, C> {
    public C create(T var1, IOSession var2) throws IOException;
}

