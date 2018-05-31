/*
 * Decompiled with CFR 0_129.
 */
package io.netty.channel.sctp;

import com.sun.nio.sctp.SctpStandardSocketOptions;
import io.netty.channel.ChannelOption;
import java.net.SocketAddress;

public class SctpChannelOption<T>
extends ChannelOption<T> {
    public static final SctpChannelOption<Boolean> SCTP_DISABLE_FRAGMENTS = new SctpChannelOption<T>("SCTP_DISABLE_FRAGMENTS");
    public static final SctpChannelOption<Boolean> SCTP_EXPLICIT_COMPLETE = new SctpChannelOption<T>("SCTP_EXPLICIT_COMPLETE");
    public static final SctpChannelOption<Integer> SCTP_FRAGMENT_INTERLEAVE = new SctpChannelOption<T>("SCTP_FRAGMENT_INTERLEAVE");
    public static final SctpChannelOption<SctpStandardSocketOptions.InitMaxStreams> SCTP_INIT_MAXSTREAMS = new SctpChannelOption<T>("SCTP_INIT_MAXSTREAMS");
    public static final SctpChannelOption<Boolean> SCTP_NODELAY = new SctpChannelOption<T>("SCTP_NODELAY");
    public static final SctpChannelOption<SocketAddress> SCTP_PRIMARY_ADDR = new SctpChannelOption<T>("SCTP_PRIMARY_ADDR");
    public static final SctpChannelOption<SocketAddress> SCTP_SET_PEER_PRIMARY_ADDR = new SctpChannelOption<T>("SCTP_SET_PEER_PRIMARY_ADDR");

    @Deprecated
    protected SctpChannelOption(String name) {
        super(name);
    }
}

