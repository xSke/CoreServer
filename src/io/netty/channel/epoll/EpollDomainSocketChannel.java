/*
 * Decompiled with CFR 0_129.
 */
package io.netty.channel.epoll;

import io.netty.channel.AbstractChannel;
import io.netty.channel.Channel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelOutboundBuffer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoop;
import io.netty.channel.epoll.AbstractEpollChannel;
import io.netty.channel.epoll.AbstractEpollStreamChannel;
import io.netty.channel.epoll.EpollChannelConfig;
import io.netty.channel.epoll.EpollDomainSocketChannelConfig;
import io.netty.channel.epoll.Native;
import io.netty.channel.unix.DomainSocketAddress;
import io.netty.channel.unix.DomainSocketChannel;
import io.netty.channel.unix.DomainSocketChannelConfig;
import io.netty.channel.unix.DomainSocketReadMode;
import io.netty.channel.unix.FileDescriptor;
import java.net.SocketAddress;

public final class EpollDomainSocketChannel
extends AbstractEpollStreamChannel
implements DomainSocketChannel {
    private final EpollDomainSocketChannelConfig config = new EpollDomainSocketChannelConfig(this);
    private volatile DomainSocketAddress local;
    private volatile DomainSocketAddress remote;

    public EpollDomainSocketChannel() {
        super(Native.socketDomainFd());
    }

    public EpollDomainSocketChannel(Channel parent, FileDescriptor fd) {
        super(parent, fd.intValue());
    }

    public EpollDomainSocketChannel(FileDescriptor fd) {
        super(fd);
    }

    EpollDomainSocketChannel(Channel parent, int fd) {
        super(parent, fd);
    }

    @Override
    protected AbstractEpollChannel.AbstractEpollUnsafe newUnsafe() {
        return new EpollDomainUnsafe();
    }

    @Override
    protected DomainSocketAddress localAddress0() {
        return this.local;
    }

    @Override
    protected DomainSocketAddress remoteAddress0() {
        return this.remote;
    }

    @Override
    protected void doBind(SocketAddress localAddress) throws Exception {
        Native.bind(this.fd().intValue(), localAddress);
        this.local = (DomainSocketAddress)localAddress;
    }

    @Override
    public EpollDomainSocketChannelConfig config() {
        return this.config;
    }

    @Override
    protected boolean doConnect(SocketAddress remoteAddress, SocketAddress localAddress) throws Exception {
        if (super.doConnect(remoteAddress, localAddress)) {
            this.local = (DomainSocketAddress)localAddress;
            this.remote = (DomainSocketAddress)remoteAddress;
            return true;
        }
        return false;
    }

    @Override
    public DomainSocketAddress remoteAddress() {
        return (DomainSocketAddress)super.remoteAddress();
    }

    @Override
    public DomainSocketAddress localAddress() {
        return (DomainSocketAddress)super.localAddress();
    }

    @Override
    protected boolean doWriteSingle(ChannelOutboundBuffer in, int writeSpinCount) throws Exception {
        Object msg = in.current();
        if (msg instanceof FileDescriptor && Native.sendFd(this.fd().intValue(), ((FileDescriptor)msg).intValue()) > 0) {
            in.remove();
            return true;
        }
        return super.doWriteSingle(in, writeSpinCount);
    }

    @Override
    protected Object filterOutboundMessage(Object msg) {
        if (msg instanceof FileDescriptor) {
            return msg;
        }
        return super.filterOutboundMessage(msg);
    }

    private final class EpollDomainUnsafe
    extends AbstractEpollStreamChannel.EpollStreamUnsafe {
        private EpollDomainUnsafe() {
        }

        @Override
        void epollInReady() {
            switch (EpollDomainSocketChannel.this.config().getReadMode()) {
                case BYTES: {
                    super.epollInReady();
                    break;
                }
                case FILE_DESCRIPTORS: {
                    this.epollInReadFd();
                    break;
                }
                default: {
                    throw new Error();
                }
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        private void epollInReadFd() {
            boolean edgeTriggered = EpollDomainSocketChannel.this.isFlagSet(Native.EPOLLET);
            EpollDomainSocketChannelConfig config = EpollDomainSocketChannel.this.config();
            if (!(this.readPending || edgeTriggered || config.isAutoRead())) {
                this.clearEpollIn0();
                return;
            }
            ChannelPipeline pipeline = EpollDomainSocketChannel.this.pipeline();
            try {
                int socketFd;
                int maxMessagesPerRead = edgeTriggered ? Integer.MAX_VALUE : config.getMaxMessagesPerRead();
                int messages = 0;
                while ((socketFd = Native.recvFd(EpollDomainSocketChannel.this.fd().intValue())) != 0) {
                    if (socketFd == -1) {
                        this.close(this.voidPromise());
                        return;
                    }
                    this.readPending = false;
                    try {
                        pipeline.fireChannelRead(new FileDescriptor(socketFd));
                    }
                    catch (Throwable t) {
                        pipeline.fireChannelReadComplete();
                        pipeline.fireExceptionCaught(t);
                    }
                    finally {
                        if (!edgeTriggered && !config.isAutoRead()) break;
                    }
                    if (++messages < maxMessagesPerRead) continue;
                }
                pipeline.fireChannelReadComplete();
            }
            catch (Throwable t) {
                pipeline.fireChannelReadComplete();
                pipeline.fireExceptionCaught(t);
                EpollDomainSocketChannel.this.eventLoop().execute(new Runnable(){

                    @Override
                    public void run() {
                        EpollDomainUnsafe.this.epollInReady();
                    }
                });
            }
            finally {
                if (!this.readPending && !config.isAutoRead()) {
                    this.clearEpollIn0();
                }
            }
        }

    }

}

