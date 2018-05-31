/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.impl.nio.reactor;

import java.nio.channels.SocketChannel;
import org.apache.http.annotation.Immutable;
import org.apache.http.impl.nio.reactor.SessionRequestImpl;
import org.apache.http.util.Args;

@Immutable
public class ChannelEntry {
    private final SocketChannel channel;
    private final SessionRequestImpl sessionRequest;

    public ChannelEntry(SocketChannel channel, SessionRequestImpl sessionRequest) {
        Args.notNull(channel, "Socket channel");
        this.channel = channel;
        this.sessionRequest = sessionRequest;
    }

    public ChannelEntry(SocketChannel channel) {
        this(channel, null);
    }

    public SessionRequestImpl getSessionRequest() {
        return this.sessionRequest;
    }

    public Object getAttachment() {
        if (this.sessionRequest != null) {
            return this.sessionRequest.getAttachment();
        }
        return null;
    }

    public SocketChannel getChannel() {
        return this.channel;
    }
}

