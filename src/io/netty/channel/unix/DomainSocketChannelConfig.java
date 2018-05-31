/*
 * Decompiled with CFR 0_129.
 */
package io.netty.channel.unix;

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelConfig;
import io.netty.channel.MessageSizeEstimator;
import io.netty.channel.RecvByteBufAllocator;
import io.netty.channel.unix.DomainSocketReadMode;

public interface DomainSocketChannelConfig
extends ChannelConfig {
    @Override
    public DomainSocketChannelConfig setMaxMessagesPerRead(int var1);

    @Override
    public DomainSocketChannelConfig setConnectTimeoutMillis(int var1);

    @Override
    public DomainSocketChannelConfig setWriteSpinCount(int var1);

    @Override
    public DomainSocketChannelConfig setAllocator(ByteBufAllocator var1);

    @Override
    public DomainSocketChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator var1);

    @Override
    public DomainSocketChannelConfig setAutoRead(boolean var1);

    @Override
    public DomainSocketChannelConfig setAutoClose(boolean var1);

    @Override
    public DomainSocketChannelConfig setWriteBufferHighWaterMark(int var1);

    @Override
    public DomainSocketChannelConfig setWriteBufferLowWaterMark(int var1);

    @Override
    public DomainSocketChannelConfig setMessageSizeEstimator(MessageSizeEstimator var1);

    public DomainSocketChannelConfig setReadMode(DomainSocketReadMode var1);

    public DomainSocketReadMode getReadMode();
}

