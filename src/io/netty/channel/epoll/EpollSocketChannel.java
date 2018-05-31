/*
 * Decompiled with CFR 0_129.
 */
package io.netty.channel.epoll;

import io.netty.channel.AbstractChannel;
import io.netty.channel.Channel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoop;
import io.netty.channel.epoll.AbstractEpollChannel;
import io.netty.channel.epoll.AbstractEpollStreamChannel;
import io.netty.channel.epoll.EpollChannelConfig;
import io.netty.channel.epoll.EpollSocketChannelConfig;
import io.netty.channel.epoll.EpollTcpInfo;
import io.netty.channel.epoll.Native;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.SocketChannelConfig;
import io.netty.channel.unix.FileDescriptor;
import io.netty.util.concurrent.GlobalEventExecutor;
import io.netty.util.internal.OneTimeTask;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.Executor;

public final class EpollSocketChannel
extends AbstractEpollStreamChannel
implements SocketChannel {
    private final EpollSocketChannelConfig config = new EpollSocketChannelConfig(this);
    private volatile InetSocketAddress local;
    private volatile InetSocketAddress remote;

    EpollSocketChannel(Channel parent, int fd, InetSocketAddress remote) {
        super(parent, fd);
        this.remote = remote;
        this.local = Native.localAddress(fd);
    }

    public EpollSocketChannel() {
        super(Native.socketStreamFd());
    }

    public EpollSocketChannel(FileDescriptor fd) {
        super(fd);
        this.remote = Native.remoteAddress(fd.intValue());
        this.local = Native.localAddress(fd.intValue());
    }

    public EpollTcpInfo tcpInfo() {
        return this.tcpInfo(new EpollTcpInfo());
    }

    public EpollTcpInfo tcpInfo(EpollTcpInfo info) {
        Native.tcpInfo(this.fd().intValue(), info);
        return info;
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
    protected SocketAddress localAddress0() {
        return this.local;
    }

    @Override
    protected SocketAddress remoteAddress0() {
        if (this.remote == null) {
            InetSocketAddress address = Native.remoteAddress(this.fd().intValue());
            if (address != null) {
                this.remote = address;
            }
            return address;
        }
        return this.remote;
    }

    @Override
    protected void doBind(SocketAddress local) throws Exception {
        InetSocketAddress localAddress = (InetSocketAddress)local;
        int fd = this.fd().intValue();
        Native.bind(fd, localAddress);
        this.local = Native.localAddress(fd);
    }

    @Override
    public EpollSocketChannelConfig config() {
        return this.config;
    }

    @Override
    public boolean isInputShutdown() {
        return this.isInputShutdown0();
    }

    @Override
    public boolean isOutputShutdown() {
        return this.isOutputShutdown0();
    }

    @Override
    public ChannelFuture shutdownOutput() {
        return this.shutdownOutput(this.newPromise());
    }

    @Override
    public ChannelFuture shutdownOutput(final ChannelPromise promise) {
        Executor closeExecutor = ((EpollSocketChannelUnsafe)this.unsafe()).closeExecutor();
        if (closeExecutor != null) {
            closeExecutor.execute(new OneTimeTask(){

                @Override
                public void run() {
                    EpollSocketChannel.this.shutdownOutput0(promise);
                }
            });
        } else {
            EventLoop loop = this.eventLoop();
            if (loop.inEventLoop()) {
                this.shutdownOutput0(promise);
            } else {
                loop.execute(new OneTimeTask(){

                    @Override
                    public void run() {
                        EpollSocketChannel.this.shutdownOutput0(promise);
                    }
                });
            }
        }
        return promise;
    }

    @Override
    public ServerSocketChannel parent() {
        return (ServerSocketChannel)super.parent();
    }

    @Override
    protected AbstractEpollChannel.AbstractEpollUnsafe newUnsafe() {
        return new EpollSocketChannelUnsafe();
    }

    @Override
    protected boolean doConnect(SocketAddress remoteAddress, SocketAddress localAddress) throws Exception {
        if (localAddress != null) {
            EpollSocketChannel.checkResolvable((InetSocketAddress)localAddress);
        }
        EpollSocketChannel.checkResolvable((InetSocketAddress)remoteAddress);
        int fd = this.fd().intValue();
        boolean connected = super.doConnect(remoteAddress, localAddress);
        if (connected) {
            this.remote = (InetSocketAddress)remoteAddress;
            return true;
        }
        this.local = Native.localAddress(fd);
        return connected;
    }

    private final class EpollSocketChannelUnsafe
    extends AbstractEpollStreamChannel.EpollStreamUnsafe {
        private EpollSocketChannelUnsafe() {
        }

        @Override
        protected Executor closeExecutor() {
            if (EpollSocketChannel.this.config().getSoLinger() > 0) {
                return GlobalEventExecutor.INSTANCE;
            }
            return null;
        }
    }

}

