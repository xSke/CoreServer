/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.reactor;

import java.io.IOException;
import java.net.SocketAddress;
import org.apache.http.nio.reactor.IOSession;

public interface SessionRequest {
    public SocketAddress getRemoteAddress();

    public SocketAddress getLocalAddress();

    public Object getAttachment();

    public boolean isCompleted();

    public IOSession getSession();

    public IOException getException();

    public void waitFor() throws InterruptedException;

    public void setConnectTimeout(int var1);

    public int getConnectTimeout();

    public void cancel();
}

