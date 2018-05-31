/*
 * Decompiled with CFR 0_129.
 */
package io.netty.channel.epoll;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;
import io.netty.channel.AbstractChannel;
import io.netty.channel.AddressedEnvelope;
import io.netty.channel.Channel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelMetadata;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelOutboundBuffer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.channel.DefaultAddressedEnvelope;
import io.netty.channel.EventLoop;
import io.netty.channel.RecvByteBufAllocator;
import io.netty.channel.epoll.AbstractEpollChannel;
import io.netty.channel.epoll.EpollChannelConfig;
import io.netty.channel.epoll.EpollDatagramChannelConfig;
import io.netty.channel.epoll.IovArray;
import io.netty.channel.epoll.IovArrayThreadLocal;
import io.netty.channel.epoll.Native;
import io.netty.channel.epoll.NativeDatagramPacketArray;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramChannelConfig;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.unix.FileDescriptor;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.StringUtil;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.NotYetConnectedException;
import java.util.ArrayList;
import java.util.List;

public final class EpollDatagramChannel
extends AbstractEpollChannel
implements DatagramChannel {
    private static final ChannelMetadata METADATA = new ChannelMetadata(true);
    private static final String EXPECTED_TYPES = " (expected: " + StringUtil.simpleClassName(DatagramPacket.class) + ", " + StringUtil.simpleClassName(AddressedEnvelope.class) + '<' + StringUtil.simpleClassName(ByteBuf.class) + ", " + StringUtil.simpleClassName(InetSocketAddress.class) + ">, " + StringUtil.simpleClassName(ByteBuf.class) + ')';
    private volatile InetSocketAddress local;
    private volatile InetSocketAddress remote;
    private volatile boolean connected;
    private final EpollDatagramChannelConfig config = new EpollDatagramChannelConfig(this);

    public EpollDatagramChannel() {
        super(Native.socketDgramFd(), Native.EPOLLIN);
    }

    public EpollDatagramChannel(FileDescriptor fd) {
        super(null, fd, Native.EPOLLIN, true);
        this.local = Native.localAddress(fd.intValue());
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
    public ChannelMetadata metadata() {
        return METADATA;
    }

    @Override
    public boolean isActive() {
        return this.fd().isOpen() && (this.config.getOption(ChannelOption.DATAGRAM_CHANNEL_ACTIVE_ON_REGISTRATION) != false && this.isRegistered() || this.active);
    }

    @Override
    public boolean isConnected() {
        return this.connected;
    }

    @Override
    public ChannelFuture joinGroup(InetAddress multicastAddress) {
        return this.joinGroup(multicastAddress, this.newPromise());
    }

    @Override
    public ChannelFuture joinGroup(InetAddress multicastAddress, ChannelPromise promise) {
        try {
            return this.joinGroup(multicastAddress, NetworkInterface.getByInetAddress(this.localAddress().getAddress()), null, promise);
        }
        catch (SocketException e) {
            promise.setFailure(e);
            return promise;
        }
    }

    @Override
    public ChannelFuture joinGroup(InetSocketAddress multicastAddress, NetworkInterface networkInterface) {
        return this.joinGroup(multicastAddress, networkInterface, this.newPromise());
    }

    @Override
    public ChannelFuture joinGroup(InetSocketAddress multicastAddress, NetworkInterface networkInterface, ChannelPromise promise) {
        return this.joinGroup(multicastAddress.getAddress(), networkInterface, null, promise);
    }

    @Override
    public ChannelFuture joinGroup(InetAddress multicastAddress, NetworkInterface networkInterface, InetAddress source) {
        return this.joinGroup(multicastAddress, networkInterface, source, this.newPromise());
    }

    @Override
    public ChannelFuture joinGroup(InetAddress multicastAddress, NetworkInterface networkInterface, InetAddress source, ChannelPromise promise) {
        if (multicastAddress == null) {
            throw new NullPointerException("multicastAddress");
        }
        if (networkInterface == null) {
            throw new NullPointerException("networkInterface");
        }
        promise.setFailure(new UnsupportedOperationException("Multicast not supported"));
        return promise;
    }

    @Override
    public ChannelFuture leaveGroup(InetAddress multicastAddress) {
        return this.leaveGroup(multicastAddress, this.newPromise());
    }

    @Override
    public ChannelFuture leaveGroup(InetAddress multicastAddress, ChannelPromise promise) {
        try {
            return this.leaveGroup(multicastAddress, NetworkInterface.getByInetAddress(this.localAddress().getAddress()), null, promise);
        }
        catch (SocketException e) {
            promise.setFailure(e);
            return promise;
        }
    }

    @Override
    public ChannelFuture leaveGroup(InetSocketAddress multicastAddress, NetworkInterface networkInterface) {
        return this.leaveGroup(multicastAddress, networkInterface, this.newPromise());
    }

    @Override
    public ChannelFuture leaveGroup(InetSocketAddress multicastAddress, NetworkInterface networkInterface, ChannelPromise promise) {
        return this.leaveGroup(multicastAddress.getAddress(), networkInterface, null, promise);
    }

    @Override
    public ChannelFuture leaveGroup(InetAddress multicastAddress, NetworkInterface networkInterface, InetAddress source) {
        return this.leaveGroup(multicastAddress, networkInterface, source, this.newPromise());
    }

    @Override
    public ChannelFuture leaveGroup(InetAddress multicastAddress, NetworkInterface networkInterface, InetAddress source, ChannelPromise promise) {
        if (multicastAddress == null) {
            throw new NullPointerException("multicastAddress");
        }
        if (networkInterface == null) {
            throw new NullPointerException("networkInterface");
        }
        promise.setFailure(new UnsupportedOperationException("Multicast not supported"));
        return promise;
    }

    @Override
    public ChannelFuture block(InetAddress multicastAddress, NetworkInterface networkInterface, InetAddress sourceToBlock) {
        return this.block(multicastAddress, networkInterface, sourceToBlock, this.newPromise());
    }

    @Override
    public ChannelFuture block(InetAddress multicastAddress, NetworkInterface networkInterface, InetAddress sourceToBlock, ChannelPromise promise) {
        if (multicastAddress == null) {
            throw new NullPointerException("multicastAddress");
        }
        if (sourceToBlock == null) {
            throw new NullPointerException("sourceToBlock");
        }
        if (networkInterface == null) {
            throw new NullPointerException("networkInterface");
        }
        promise.setFailure(new UnsupportedOperationException("Multicast not supported"));
        return promise;
    }

    @Override
    public ChannelFuture block(InetAddress multicastAddress, InetAddress sourceToBlock) {
        return this.block(multicastAddress, sourceToBlock, this.newPromise());
    }

    @Override
    public ChannelFuture block(InetAddress multicastAddress, InetAddress sourceToBlock, ChannelPromise promise) {
        try {
            return this.block(multicastAddress, NetworkInterface.getByInetAddress(this.localAddress().getAddress()), sourceToBlock, promise);
        }
        catch (Throwable e) {
            promise.setFailure(e);
            return promise;
        }
    }

    @Override
    protected AbstractEpollChannel.AbstractEpollUnsafe newUnsafe() {
        return new EpollDatagramChannelUnsafe();
    }

    @Override
    protected InetSocketAddress localAddress0() {
        return this.local;
    }

    @Override
    protected InetSocketAddress remoteAddress0() {
        return this.remote;
    }

    @Override
    protected void doBind(SocketAddress localAddress) throws Exception {
        InetSocketAddress addr = (InetSocketAddress)localAddress;
        EpollDatagramChannel.checkResolvable(addr);
        int fd = this.fd().intValue();
        Native.bind(fd, addr);
        this.local = Native.localAddress(fd);
        this.active = true;
    }

    @Override
    protected void doWrite(ChannelOutboundBuffer in) throws Exception {
        block2 : do {
            Object msg;
            if ((msg = in.current()) == null) {
                this.clearFlag(Native.EPOLLOUT);
                break;
            }
            try {
                NativeDatagramPacketArray array;
                int cnt;
                if (Native.IS_SUPPORTING_SENDMMSG && in.size() > 1 && (cnt = (array = NativeDatagramPacketArray.getInstance(in)).count()) >= 1) {
                    int offset = 0;
                    NativeDatagramPacketArray.NativeDatagramPacket[] packets = array.packets();
                    do {
                        if (cnt <= 0) continue block2;
                        int send = Native.sendmmsg(this.fd().intValue(), packets, offset, cnt);
                        if (send == 0) {
                            this.setFlag(Native.EPOLLOUT);
                            return;
                        }
                        for (int i = 0; i < send; ++i) {
                            in.remove();
                        }
                        cnt -= send;
                        offset += send;
                    } while (true);
                }
                boolean done = false;
                for (int i = this.config().getWriteSpinCount() - 1; i >= 0; --i) {
                    if (!this.doWriteMessage(msg)) continue;
                    done = true;
                    break;
                }
                if (done) {
                    in.remove();
                    continue;
                }
                this.setFlag(Native.EPOLLOUT);
            }
            catch (IOException e) {
                in.remove(e);
                continue;
            }
            break;
        } while (true);
    }

    private boolean doWriteMessage(Object msg) throws Exception {
        ByteBuf data;
        int writtenBytes;
        InetSocketAddress remoteAddress;
        if (msg instanceof AddressedEnvelope) {
            AddressedEnvelope envelope = (AddressedEnvelope)msg;
            data = (ByteBuf)envelope.content();
            remoteAddress = (InetSocketAddress)envelope.recipient();
        } else {
            data = (ByteBuf)msg;
            remoteAddress = null;
        }
        int dataLen = data.readableBytes();
        if (dataLen == 0) {
            return true;
        }
        if (remoteAddress == null && (remoteAddress = this.remote) == null) {
            throw new NotYetConnectedException();
        }
        if (data.hasMemoryAddress()) {
            long memoryAddress = data.memoryAddress();
            writtenBytes = Native.sendToAddress(this.fd().intValue(), memoryAddress, data.readerIndex(), data.writerIndex(), remoteAddress.getAddress(), remoteAddress.getPort());
        } else if (data instanceof CompositeByteBuf) {
            IovArray array = IovArrayThreadLocal.get((CompositeByteBuf)data);
            int cnt = array.count();
            assert (cnt != 0);
            writtenBytes = Native.sendToAddresses(this.fd().intValue(), array.memoryAddress(0), cnt, remoteAddress.getAddress(), remoteAddress.getPort());
        } else {
            ByteBuffer nioData = data.internalNioBuffer(data.readerIndex(), data.readableBytes());
            writtenBytes = Native.sendTo(this.fd().intValue(), nioData, nioData.position(), nioData.limit(), remoteAddress.getAddress(), remoteAddress.getPort());
        }
        return writtenBytes > 0;
    }

    @Override
    protected Object filterOutboundMessage(Object msg) {
        AddressedEnvelope e;
        if (msg instanceof DatagramPacket) {
            CompositeByteBuf comp;
            DatagramPacket packet = (DatagramPacket)msg;
            ByteBuf content = (ByteBuf)packet.content();
            if (content.hasMemoryAddress()) {
                return msg;
            }
            if (content.isDirect() && content instanceof CompositeByteBuf && (comp = (CompositeByteBuf)content).isDirect() && comp.nioBufferCount() <= Native.IOV_MAX) {
                return msg;
            }
            return new DatagramPacket(this.newDirectBuffer(packet, content), (InetSocketAddress)packet.recipient());
        }
        if (msg instanceof ByteBuf) {
            ByteBuf buf = (ByteBuf)msg;
            if (!(buf.hasMemoryAddress() || !PlatformDependent.hasUnsafe() && buf.isDirect())) {
                if (buf instanceof CompositeByteBuf) {
                    CompositeByteBuf comp = (CompositeByteBuf)buf;
                    if (!comp.isDirect() || comp.nioBufferCount() > Native.IOV_MAX) {
                        buf = this.newDirectBuffer(buf);
                        assert (buf.hasMemoryAddress());
                    }
                } else {
                    buf = this.newDirectBuffer(buf);
                    assert (buf.hasMemoryAddress());
                }
            }
            return buf;
        }
        if (msg instanceof AddressedEnvelope && (e = (AddressedEnvelope)msg).content() instanceof ByteBuf && (e.recipient() == null || e.recipient() instanceof InetSocketAddress)) {
            CompositeByteBuf comp;
            ByteBuf content = (ByteBuf)e.content();
            if (content.hasMemoryAddress()) {
                return e;
            }
            if (content instanceof CompositeByteBuf && (comp = (CompositeByteBuf)content).isDirect() && comp.nioBufferCount() <= Native.IOV_MAX) {
                return e;
            }
            return new DefaultAddressedEnvelope<ByteBuf, InetSocketAddress>(this.newDirectBuffer(e, content), (InetSocketAddress)e.recipient());
        }
        throw new UnsupportedOperationException("unsupported message type: " + StringUtil.simpleClassName(msg) + EXPECTED_TYPES);
    }

    @Override
    public EpollDatagramChannelConfig config() {
        return this.config;
    }

    @Override
    protected void doDisconnect() throws Exception {
        this.connected = false;
    }

    static final class DatagramSocketAddress
    extends InetSocketAddress {
        private static final long serialVersionUID = 1348596211215015739L;
        final int receivedAmount;

        DatagramSocketAddress(String addr, int port, int receivedAmount) {
            super(addr, port);
            this.receivedAmount = receivedAmount;
        }
    }

    final class EpollDatagramChannelUnsafe
    extends AbstractEpollChannel.AbstractEpollUnsafe {
        private RecvByteBufAllocator.Handle allocHandle;
        private final List<Object> readBuf = new ArrayList<Object>();

        EpollDatagramChannelUnsafe() {
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void connect(SocketAddress remote, SocketAddress local, ChannelPromise channelPromise) {
            boolean success = false;
            try {
                try {
                    boolean wasActive = EpollDatagramChannel.this.isActive();
                    InetSocketAddress remoteAddress = (InetSocketAddress)remote;
                    if (local != null) {
                        InetSocketAddress localAddress = (InetSocketAddress)local;
                        EpollDatagramChannel.this.doBind(localAddress);
                    }
                    AbstractEpollChannel.checkResolvable(remoteAddress);
                    EpollDatagramChannel.this.remote = remoteAddress;
                    EpollDatagramChannel.this.local = Native.localAddress(EpollDatagramChannel.this.fd().intValue());
                    success = true;
                    if (!wasActive && EpollDatagramChannel.this.isActive()) {
                        EpollDatagramChannel.this.pipeline().fireChannelActive();
                    }
                }
                finally {
                    if (!success) {
                        EpollDatagramChannel.this.doClose();
                    } else {
                        channelPromise.setSuccess();
                        EpollDatagramChannel.this.connected = true;
                    }
                }
            }
            catch (Throwable cause) {
                channelPromise.setFailure(cause);
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        void epollInReady() {
            assert (EpollDatagramChannel.this.eventLoop().inEventLoop());
            EpollDatagramChannelConfig config = EpollDatagramChannel.this.config();
            boolean edgeTriggered = EpollDatagramChannel.this.isFlagSet(Native.EPOLLET);
            if (!(this.readPending || edgeTriggered || config.isAutoRead())) {
                this.clearEpollIn0();
                return;
            }
            RecvByteBufAllocator.Handle allocHandle = this.allocHandle;
            if (allocHandle == null) {
                this.allocHandle = allocHandle = config.getRecvByteBufAllocator().newHandle();
            }
            ChannelPipeline pipeline = EpollDatagramChannel.this.pipeline();
            Throwable exception = null;
            try {
                int maxMessagesPerRead = edgeTriggered ? Integer.MAX_VALUE : config.getMaxMessagesPerRead();
                int messages = 0;
                do {
                    ByteBuf data = null;
                    try {
                        DatagramSocketAddress remoteAddress;
                        data = allocHandle.allocate(config.getAllocator());
                        int writerIndex = data.writerIndex();
                        if (data.hasMemoryAddress()) {
                            remoteAddress = Native.recvFromAddress(EpollDatagramChannel.this.fd().intValue(), data.memoryAddress(), writerIndex, data.capacity());
                        } else {
                            ByteBuffer nioData = data.internalNioBuffer(writerIndex, data.writableBytes());
                            remoteAddress = Native.recvFrom(EpollDatagramChannel.this.fd().intValue(), nioData, nioData.position(), nioData.limit());
                        }
                        if (remoteAddress == null) break;
                        int readBytes = remoteAddress.receivedAmount;
                        data.writerIndex(data.writerIndex() + readBytes);
                        allocHandle.record(readBytes);
                        this.readPending = false;
                        this.readBuf.add(new DatagramPacket(data, (InetSocketAddress)this.localAddress(), remoteAddress));
                        data = null;
                    }
                    catch (Throwable t) {
                        exception = t;
                    }
                    finally {
                        if (data != null) {
                            data.release();
                        }
                        if (edgeTriggered || config.isAutoRead()) {
                            // empty if block
                        }
                    }
                } while (++messages < maxMessagesPerRead);
                int size = this.readBuf.size();
                for (int i = 0; i < size; ++i) {
                    pipeline.fireChannelRead(this.readBuf.get(i));
                }
                this.readBuf.clear();
                pipeline.fireChannelReadComplete();
                if (exception != null) {
                    pipeline.fireExceptionCaught(exception);
                }
            }
            finally {
                if (!this.readPending && !config.isAutoRead()) {
                    EpollDatagramChannel.this.clearEpollIn();
                }
            }
        }
    }

}

