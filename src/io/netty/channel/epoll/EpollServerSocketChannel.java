/*
 * Decompiled with CFR 0_129.
 */
package io.netty.channel.epoll;

import io.netty.channel.Channel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.EventLoop;
import io.netty.channel.epoll.AbstractEpollServerChannel;
import io.netty.channel.epoll.EpollChannelConfig;
import io.netty.channel.epoll.EpollEventLoop;
import io.netty.channel.epoll.EpollServerSocketChannelConfig;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.epoll.Native;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.ServerSocketChannelConfig;
import io.netty.channel.unix.FileDescriptor;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public final class EpollServerSocketChannel
extends AbstractEpollServerChannel
implements ServerSocketChannel {
    private final EpollServerSocketChannelConfig config = new EpollServerSocketChannelConfig(this);
    private volatile InetSocketAddress local;

    public EpollServerSocketChannel() {
        super(Native.socketStreamFd());
    }

    public EpollServerSocketChannel(FileDescriptor fd) {
        super(fd);
        this.local = Native.localAddress(fd.intValue());
    }

    @Override
    protected boolean isCompatible(EventLoop loop) {
        return loop instanceof EpollEventLoop;
    }

    @Override
    protected void doBind(SocketAddress localAddress) throws Exception {
        InetSocketAddress addr = (InetSocketAddress)localAddress;
        EpollServerSocketChannel.checkResolvable(addr);
        int fd = this.fd().intValue();
        Native.bind(fd, addr);
        this.local = Native.localAddress(fd);
        Native.listen(fd, this.config.getBacklog());
        this.active = true;
    }

    @Override
    public InetSocketAddress remoteAddress() {
        return (InetSocketAddress)super.remoteAddress();
    }

    @Override
    public InetSocketAddress localAddress() {
        return (InetSocketAddress)super.localAddress();
    }

    @Override
    public EpollServerSocketChannelConfig config() {
        return this.config;
    }

    @Override
    protected InetSocketAddress localAddress0() {
        return this.local;
    }

    @Override
    protected Channel newChildChannel(int fd, byte[] address, int offset, int len) throws Exception {
        return new EpollSocketChannel(this, fd, Native.address(address, offset, len));
    }
}

