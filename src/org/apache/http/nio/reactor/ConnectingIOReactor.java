/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.nio.reactor;

import java.net.SocketAddress;
import org.apache.http.nio.reactor.IOReactor;
import org.apache.http.nio.reactor.SessionRequest;
import org.apache.http.nio.reactor.SessionRequestCallback;

public interface ConnectingIOReactor
extends IOReactor {
    public SessionRequest connect(SocketAddress var1, SocketAddress var2, Object var3, SessionRequestCallback var4);
}

