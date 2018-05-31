/*
 * Decompiled with CFR 0_129.
 */
package io.netty.channel.epoll;

import io.netty.channel.ChannelOption;
import io.netty.channel.epoll.EpollMode;
import io.netty.channel.unix.DomainSocketReadMode;

public final class EpollChannelOption<T>
extends ChannelOption<T> {
    public static final ChannelOption<Boolean> TCP_CORK = EpollChannelOption.valueOf("TCP_CORK");
    public static final ChannelOption<Boolean> SO_REUSEPORT = EpollChannelOption.valueOf("SO_REUSEPORT");
    public static final ChannelOption<Long> TCP_NOTSENT_LOWAT = EpollChannelOption.valueOf("TCP_NOTSENT_LOWAT");
    public static final ChannelOption<Integer> TCP_KEEPIDLE = EpollChannelOption.valueOf("TCP_KEEPIDLE");
    public static final ChannelOption<Integer> TCP_KEEPINTVL = EpollChannelOption.valueOf("TCP_KEEPINTVL");
    public static final ChannelOption<Integer> TCP_KEEPCNT = EpollChannelOption.valueOf("TCP_KEEPCNT");
    public static final ChannelOption<DomainSocketReadMode> DOMAIN_SOCKET_READ_MODE = EpollChannelOption.valueOf("DOMAIN_SOCKET_READ_MODE");
    public static final ChannelOption<EpollMode> EPOLL_MODE = EpollChannelOption.valueOf("EPOLL_MODE");

    private EpollChannelOption(String name) {
        super(name);
    }
}

