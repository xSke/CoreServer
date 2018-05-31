/*
 * Decompiled with CFR 0_129.
 */
package io.netty.channel.epoll;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.AbstractChannel;
import io.netty.channel.Channel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelMetadata;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoop;
import io.netty.channel.epoll.EpollChannelConfig;
import io.netty.channel.epoll.EpollEventLoop;
import io.netty.channel.epoll.Native;
import io.netty.channel.unix.FileDescriptor;
import io.netty.channel.unix.UnixChannel;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.OneTimeTask;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.UnresolvedAddressException;

abstract class AbstractEpollChannel
extends AbstractChannel
implements UnixChannel {
    private static final ChannelMetadata DATA = new ChannelMetadata(false);
    private final int readFlag;
    private final FileDescriptor fileDescriptor;
    protected int flags = Native.EPOLLET;
    protected volatile boolean active;

    AbstractEpollChannel(int fd, int flag) {
        this(null, fd, flag, false);
    }

    AbstractEpollChannel(Channel parent, int fd, int flag, boolean active) {
        this(parent, new FileDescriptor(fd), flag, active);
    }

    AbstractEpollChannel(Channel parent, FileDescriptor fd, int flag, boolean active) {
        super(parent);
        if (fd == null) {
            throw new NullPointerException("fd");
        }
        this.readFlag = flag;
        this.flags |= flag;
        this.active = active;
        this.fileDescriptor = fd;
    }

    void setFlag(int flag) throws IOException {
        if (!this.isFlagSet(flag)) {
            this.flags |= flag;
            this.modifyEvents();
        }
    }

    void clearFlag(int flag) throws IOException {
        if (this.isFlagSet(flag)) {
            this.flags &= ~ flag;
            this.modifyEvents();
        }
    }

    boolean isFlagSet(int flag) {
        return (this.flags & flag) != 0;
    }

    @Override
    public final FileDescriptor fd() {
        return this.fileDescriptor;
    }

    @Override
    public abstract EpollChannelConfig config();

    @Override
    public boolean isActive() {
        return this.active;
    }

    @Override
    public ChannelMetadata metadata() {
        return DATA;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void doClose() throws Exception {
        this.active = false;
        try {
            this.doDeregister();
        }
        finally {
            FileDescriptor fd = this.fileDescriptor;
            fd.close();
        }
    }

    @Override
    protected void doDisconnect() throws Exception {
        this.doClose();
    }

    @Override
    protected boolean isCompatible(EventLoop loop) {
        return loop instanceof EpollEventLoop;
    }

    @Override
    public boolean isOpen() {
        return this.fileDescriptor.isOpen();
    }

    @Override
    protected void doDeregister() throws Exception {
        ((EpollEventLoop)this.eventLoop()).remove(this);
    }

    @Override
    protected void doBeginRead() throws Exception {
        ((AbstractEpollUnsafe)this.unsafe()).readPending = true;
        this.setFlag(this.readFlag);
    }

    final void clearEpollIn() {
        if (this.isRegistered()) {
            EventLoop loop = this.eventLoop();
            final AbstractEpollUnsafe unsafe = (AbstractEpollUnsafe)this.unsafe();
            if (loop.inEventLoop()) {
                unsafe.clearEpollIn0();
            } else {
                loop.execute(new OneTimeTask(){

                    @Override
                    public void run() {
                        if (!AbstractEpollChannel.this.config().isAutoRead() && !unsafe.readPending) {
                            unsafe.clearEpollIn0();
                        }
                    }
                });
            }
        } else {
            this.flags &= ~ this.readFlag;
        }
    }

    private void modifyEvents() throws IOException {
        if (this.isOpen() && this.isRegistered()) {
            ((EpollEventLoop)this.eventLoop()).modify(this);
        }
    }

    @Override
    protected void doRegister() throws Exception {
        EpollEventLoop loop = (EpollEventLoop)this.eventLoop();
        loop.add(this);
    }

    @Override
    protected abstract AbstractEpollUnsafe newUnsafe();

    protected final ByteBuf newDirectBuffer(ByteBuf buf) {
        return this.newDirectBuffer(buf, buf);
    }

    protected final ByteBuf newDirectBuffer(Object holder, ByteBuf buf) {
        int readableBytes = buf.readableBytes();
        if (readableBytes == 0) {
            ReferenceCountUtil.safeRelease(holder);
            return Unpooled.EMPTY_BUFFER;
        }
        ByteBufAllocator alloc = this.alloc();
        if (alloc.isDirectBufferPooled()) {
            return AbstractEpollChannel.newDirectBuffer0(holder, buf, alloc, readableBytes);
        }
        ByteBuf directBuf = ByteBufUtil.threadLocalDirectBuffer();
        if (directBuf == null) {
            return AbstractEpollChannel.newDirectBuffer0(holder, buf, alloc, readableBytes);
        }
        directBuf.writeBytes(buf, buf.readerIndex(), readableBytes);
        ReferenceCountUtil.safeRelease(holder);
        return directBuf;
    }

    private static ByteBuf newDirectBuffer0(Object holder, ByteBuf buf, ByteBufAllocator alloc, int capacity) {
        ByteBuf directBuf = alloc.directBuffer(capacity);
        directBuf.writeBytes(buf, buf.readerIndex(), capacity);
        ReferenceCountUtil.safeRelease(holder);
        return directBuf;
    }

    protected static void checkResolvable(InetSocketAddress addr) {
        if (addr.isUnresolved()) {
            throw new UnresolvedAddressException();
        }
    }

    protected final int doReadBytes(ByteBuf byteBuf) throws Exception {
        int localReadAmount;
        int writerIndex = byteBuf.writerIndex();
        if (byteBuf.hasMemoryAddress()) {
            localReadAmount = Native.readAddress(this.fileDescriptor.intValue(), byteBuf.memoryAddress(), writerIndex, byteBuf.capacity());
        } else {
            ByteBuffer buf = byteBuf.internalNioBuffer(writerIndex, byteBuf.writableBytes());
            localReadAmount = Native.read(this.fileDescriptor.intValue(), buf, buf.position(), buf.limit());
        }
        if (localReadAmount > 0) {
            byteBuf.writerIndex(writerIndex + localReadAmount);
        }
        return localReadAmount;
    }

    protected final int doWriteBytes(ByteBuf buf, int writeSpinCount) throws Exception {
        int writtenBytes;
        int readableBytes;
        readableBytes = buf.readableBytes();
        writtenBytes = 0;
        if (buf.hasMemoryAddress()) {
            int localFlushedAmount;
            long memoryAddress = buf.memoryAddress();
            int readerIndex = buf.readerIndex();
            int writerIndex = buf.writerIndex();
            for (int i = writeSpinCount - 1; i >= 0 && (localFlushedAmount = Native.writeAddress(this.fileDescriptor.intValue(), memoryAddress, readerIndex, writerIndex)) > 0; --i) {
                if ((writtenBytes += localFlushedAmount) == readableBytes) {
                    return writtenBytes;
                }
                readerIndex += localFlushedAmount;
            }
        } else {
            ByteBuffer nioBuf = buf.nioBufferCount() == 1 ? buf.internalNioBuffer(buf.readerIndex(), buf.readableBytes()) : buf.nioBuffer();
            for (int i = writeSpinCount - 1; i >= 0; --i) {
                int pos = nioBuf.position();
                int limit = nioBuf.limit();
                int localFlushedAmount = Native.write(this.fileDescriptor.intValue(), nioBuf, pos, limit);
                if (localFlushedAmount > 0) {
                    nioBuf.position(pos + localFlushedAmount);
                    if ((writtenBytes += localFlushedAmount) != readableBytes) continue;
                    return writtenBytes;
                }
                break;
            }
        }
        if (writtenBytes < readableBytes) {
            this.setFlag(Native.EPOLLOUT);
        }
        return writtenBytes;
    }

    protected abstract class AbstractEpollUnsafe
    extends AbstractChannel.AbstractUnsafe {
        protected boolean readPending;

        protected AbstractEpollUnsafe() {
        }

        abstract void epollInReady();

        void epollRdHupReady() {
        }

        @Override
        protected void flush0() {
            if (AbstractEpollChannel.this.isFlagSet(Native.EPOLLOUT)) {
                return;
            }
            super.flush0();
        }

        void epollOutReady() {
            super.flush0();
        }

        protected final void clearEpollIn0() {
            assert (AbstractEpollChannel.this.eventLoop().inEventLoop());
            try {
                AbstractEpollChannel.this.clearFlag(AbstractEpollChannel.this.readFlag);
            }
            catch (IOException e) {
                AbstractEpollChannel.this.pipeline().fireExceptionCaught(e);
                AbstractEpollChannel.this.unsafe().close(AbstractEpollChannel.this.unsafe().voidPromise());
            }
        }
    }

}

