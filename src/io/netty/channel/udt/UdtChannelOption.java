/*
 * Decompiled with CFR 0_129.
 */
package io.netty.channel.udt;

import io.netty.channel.ChannelOption;

public final class UdtChannelOption<T>
extends ChannelOption<T> {
    public static final UdtChannelOption<Integer> PROTOCOL_RECEIVE_BUFFER_SIZE = new UdtChannelOption<T>("PROTOCOL_RECEIVE_BUFFER_SIZE");
    public static final UdtChannelOption<Integer> PROTOCOL_SEND_BUFFER_SIZE = new UdtChannelOption<T>("PROTOCOL_SEND_BUFFER_SIZE");
    public static final UdtChannelOption<Integer> SYSTEM_RECEIVE_BUFFER_SIZE = new UdtChannelOption<T>("SYSTEM_RECEIVE_BUFFER_SIZE");
    public static final UdtChannelOption<Integer> SYSTEM_SEND_BUFFER_SIZE = new UdtChannelOption<T>("SYSTEM_SEND_BUFFER_SIZE");

    private UdtChannelOption(String name) {
        super(name);
    }
}

