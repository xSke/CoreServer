/*
 * Decompiled with CFR 0_129.
 */
package io.netty.channel.udt;

import io.netty.channel.Channel;
import io.netty.channel.udt.UdtChannelConfig;
import java.net.InetSocketAddress;

public interface UdtChannel
extends Channel {
    @Override
    public UdtChannelConfig config();

    @Override
    public InetSocketAddress localAddress();

    @Override
    public InetSocketAddress remoteAddress();
}

