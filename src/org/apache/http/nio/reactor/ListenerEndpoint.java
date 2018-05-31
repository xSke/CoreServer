/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.reactor;

import java.io.IOException;
import java.net.SocketAddress;

public interface ListenerEndpoint {
    public SocketAddress getAddress();

    public IOException getException();

    public void waitFor() throws InterruptedException;

    public boolean isClosed();

    public void close();
}

