/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.reactor;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.Set;
import org.apache.http.nio.reactor.IOReactor;
import org.apache.http.nio.reactor.ListenerEndpoint;

public interface ListeningIOReactor
extends IOReactor {
    public ListenerEndpoint listen(SocketAddress var1);

    public void pause() throws IOException;

    public void resume() throws IOException;

    public Set<ListenerEndpoint> getEndpoints();
}

