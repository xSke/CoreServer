/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.impl.nio.client;

import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.nio.reactor.IOReactorException;

final class IOReactorUtils {
    private IOReactorUtils() {
    }

    public static ConnectingIOReactor create(IOReactorConfig config) {
        try {
            return new DefaultConnectingIOReactor(config);
        }
        catch (IOReactorException ex) {
            throw new IllegalStateException(ex);
        }
    }
}

