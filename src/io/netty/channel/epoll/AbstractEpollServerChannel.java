/*
 * Decompiled with CFR 0_129.
 */
package io.netty.channel.epoll;

import io.netty.channel.AbstractChannel;
import io.netty.channel.Channel;
import io.netty.channel.ChannelMetadata;
import io.netty.channel.ChannelOutboundBuffer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoop;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.AbstractEpollChannel;
import io.netty.channel.epoll.EpollChannelConfig;
import io.netty.channel.epoll.EpollEventLoop;
import io.netty.channel.epoll.Native;
import io.netty.channel.unix.FileDescriptor;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public abstract class AbstractEpollServerChannel
extends AbstractEpollChannel
implements ServerChannel {
    protected AbstractEpollServerChannel(int fd) {
        super(fd, Native.EPOLLIN);
    }

    protected AbstractEpollServerChannel(FileDescriptor fd) {
        super(null, fd, Native.EPOLLIN, Native.getSoError(fd.intValue()) == 0);
    }

    @Override
    protected boolean isCompatible(EventLoop loop) {
        return loop instanceof EpollEventLoop;
    }

    @Override
    protected InetSocketAddress remoteAddress0() {
        return null;
    }

    @Override
    protected AbstractEpollChannel.AbstractEpollUnsafe newUnsafe() {
        return new EpollServerSocketUnsafe();
    }

    @Override
    protected void doWrite(ChannelOutboundBuffer in) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    protected Object filterOutboundMessage(Object msg) throws Exception {
        throw new UnsupportedOperationException();
    }

    abstract Channel newChildChannel(int var1, byte[] var2, int var3, int var4) throws Exception;

    final class EpollServerSocketUnsafe
    extends AbstractEpollChannel.AbstractEpollUnsafe {
        private final byte[] acceptedAddress = new byte[26];

        EpollServerSocketUnsafe() {
        }

        @Override
        public void connect(SocketAddress socketAddress, SocketAddress socketAddress2, ChannelPromise channelPromise) {
            channelPromise.setFailure(new UnsupportedOperationException());
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        void epollInReady() {
            assert (AbstractEpollServerChannel.this.eventLoop().inEventLoop());
            boolean edgeTriggered = AbstractEpollServerChannel.this.isFlagSet(Native.EPOLLET);
            EpollChannelConfig config = AbstractEpollServerChannel.this.config();
            if (!(this.readPending || edgeTriggered || config.isAutoRead())) {
                this.clearEpollIn0();
                return;
            }
            ChannelPipeline pipeline = AbstractEpollServerChannel.this.pipeline();
            Throwable exception = null;
            try {
                try {
                    int socketFd;
                    int maxMessagesPerRead = edgeTriggered ? Integer.MAX_VALUE : config.getMaxMessagesPerRead();
                    int messages = 0;
                    while ((socketFd = Native.accept(AbstractEpollServerChannel.this.fd().intValue(), this.acceptedAddress)) != -1) {
                        this.readPending = false;
                        try {
                            byte len = this.acceptedAddress[0];
                            pipeline.fireChannelRead(AbstractEpollServerChannel.this.newChildChannel(socketFd, this.acceptedAddress, 1, len));
                        }
                        catch (Throwable t) {
                            pipeline.fireChannelReadComplete();
                            pipeline.fireExceptionCaught(t);
                        }
                        finally {
                            if (edgeTriggered || config.isAutoRead()) {
                            }
                            break;
                        }
                        if (++messages < maxMessagesPerRead) continue;
                        break;
                    }
                }
                catch (Throwable t) {
                    exception = t;
                }
                pipeline.fireChannelReadComplete();
                if (exception != null) {
                    pipeline.fireExceptionCaught(exception);
                }
            }
            finally {
                if (!this.readPending && !config.isAutoRead()) {
                    this.clearEpollIn0();
                }
            }
        }
    }

}

