/*
 * Decompiled with CFR 0_129.
 */
package io.netty.handler.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.CodecUtil;
import io.netty.handler.codec.DecoderException;
import io.netty.util.internal.RecyclableArrayList;
import io.netty.util.internal.StringUtil;
import java.util.List;

public abstract class ByteToMessageDecoder
extends ChannelInboundHandlerAdapter {
    public static final Cumulator MERGE_CUMULATOR = new Cumulator(){

        @Override
        public ByteBuf cumulate(ByteBufAllocator alloc, ByteBuf cumulation, ByteBuf in) {
            ByteBuf buffer = cumulation.writerIndex() > cumulation.maxCapacity() - in.readableBytes() || cumulation.refCnt() > 1 ? ByteToMessageDecoder.expandCumulation(alloc, cumulation, in.readableBytes()) : cumulation;
            buffer.writeBytes(in);
            in.release();
            return buffer;
        }
    };
    public static final Cumulator COMPOSITE_CUMULATOR = new Cumulator(){

        @Override
        public ByteBuf cumulate(ByteBufAllocator alloc, ByteBuf cumulation, ByteBuf in) {
            ByteBuf buffer;
            if (cumulation.refCnt() > 1) {
                buffer = ByteToMessageDecoder.expandCumulation(alloc, cumulation, in.readableBytes());
                buffer.writeBytes(in);
                in.release();
            } else {
                CompositeByteBuf composite;
                if (cumulation instanceof CompositeByteBuf) {
                    composite = (CompositeByteBuf)cumulation;
                } else {
                    int readable = cumulation.readableBytes();
                    composite = alloc.compositeBuffer();
                    composite.addComponent(cumulation).writerIndex(readable);
                }
                composite.addComponent(in).writerIndex(composite.writerIndex() + in.readableBytes());
                buffer = composite;
            }
            return buffer;
        }
    };
    ByteBuf cumulation;
    private Cumulator cumulator = MERGE_CUMULATOR;
    private boolean singleDecode;
    private boolean decodeWasNull;
    private boolean first;

    protected ByteToMessageDecoder() {
        CodecUtil.ensureNotSharable(this);
    }

    public void setSingleDecode(boolean singleDecode) {
        this.singleDecode = singleDecode;
    }

    public boolean isSingleDecode() {
        return this.singleDecode;
    }

    public void setCumulator(Cumulator cumulator) {
        if (cumulator == null) {
            throw new NullPointerException("cumulator");
        }
        this.cumulator = cumulator;
    }

    protected int actualReadableBytes() {
        return this.internalBuffer().readableBytes();
    }

    protected ByteBuf internalBuffer() {
        if (this.cumulation != null) {
            return this.cumulation;
        }
        return Unpooled.EMPTY_BUFFER;
    }

    @Override
    public final void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        ByteBuf buf = this.internalBuffer();
        int readable = buf.readableBytes();
        if (readable > 0) {
            ByteBuf bytes = buf.readBytes(readable);
            buf.release();
            ctx.fireChannelRead(bytes);
        } else {
            buf.release();
        }
        this.cumulation = null;
        ctx.fireChannelReadComplete();
        this.handlerRemoved0(ctx);
    }

    protected void handlerRemoved0(ChannelHandlerContext ctx) throws Exception {
    }

    /*
     * Loose catch block
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     * Lifted jumps to return sites
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        int size;
        if (!(msg instanceof ByteBuf)) {
            ctx.fireChannelRead(msg);
            return;
        }
        RecyclableArrayList out = RecyclableArrayList.newInstance();
        try {
            ByteBuf data = (ByteBuf)msg;
            this.first = this.cumulation == null;
            this.cumulation = this.first ? data : this.cumulator.cumulate(ctx.alloc(), this.cumulation, data);
            this.callDecode(ctx, this.cumulation, out);
        }
        catch (DecoderException e) {
            try {
                throw e;
                catch (Throwable t) {
                    throw new DecoderException(t);
                }
            }
            catch (Throwable throwable) {
                int size2;
                if (this.cumulation != null && !this.cumulation.isReadable()) {
                    this.cumulation.release();
                    this.cumulation = null;
                }
                this.decodeWasNull = (size2 = out.size()) == 0;
                int i = 0;
                do {
                    if (i >= size2) {
                        out.recycle();
                        throw throwable;
                    }
                    ctx.fireChannelRead(out.get(i));
                    ++i;
                } while (true);
            }
        }
        if (this.cumulation != null && !this.cumulation.isReadable()) {
            this.cumulation.release();
            this.cumulation = null;
        }
        this.decodeWasNull = (size = out.size()) == 0;
        int i = 0;
        do {
            if (i >= size) {
                out.recycle();
                return;
            }
            ctx.fireChannelRead(out.get(i));
            ++i;
        } while (true);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        this.discardSomeReadBytes();
        if (this.decodeWasNull) {
            this.decodeWasNull = false;
            if (!ctx.channel().config().isAutoRead()) {
                ctx.read();
            }
        }
        ctx.fireChannelReadComplete();
    }

    protected final void discardSomeReadBytes() {
        if (this.cumulation != null && !this.first && this.cumulation.refCnt() == 1) {
            this.cumulation.discardSomeReadBytes();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        RecyclableArrayList out = RecyclableArrayList.newInstance();
        try {
            if (this.cumulation != null) {
                this.callDecode(ctx, this.cumulation, out);
                this.decodeLast(ctx, this.cumulation, out);
            } else {
                this.decodeLast(ctx, Unpooled.EMPTY_BUFFER, out);
            }
        }
        catch (DecoderException e) {
            throw e;
        }
        catch (Exception e) {
            throw new DecoderException(e);
        }
        finally {
            try {
                if (this.cumulation != null) {
                    this.cumulation.release();
                    this.cumulation = null;
                }
                int size = out.size();
                for (int i = 0; i < size; ++i) {
                    ctx.fireChannelRead(out.get(i));
                }
                if (size > 0) {
                    ctx.fireChannelReadComplete();
                }
                ctx.fireChannelInactive();
            }
            finally {
                out.recycle();
            }
        }
    }

    protected void callDecode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        try {
            while (in.isReadable()) {
                int outSize = out.size();
                int oldInputLength = in.readableBytes();
                this.decode(ctx, in, out);
                if (!ctx.isRemoved()) {
                    if (outSize == out.size()) {
                        if (oldInputLength != in.readableBytes()) continue;
                    } else {
                        if (oldInputLength == in.readableBytes()) {
                            throw new DecoderException(StringUtil.simpleClassName(this.getClass()) + ".decode() did not read anything but decoded a message.");
                        }
                        if (!this.isSingleDecode()) continue;
                    }
                }
                break;
            }
        }
        catch (DecoderException e) {
            throw e;
        }
        catch (Throwable cause) {
            throw new DecoderException(cause);
        }
    }

    protected abstract void decode(ChannelHandlerContext var1, ByteBuf var2, List<Object> var3) throws Exception;

    protected void decodeLast(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        this.decode(ctx, in, out);
    }

    static ByteBuf expandCumulation(ByteBufAllocator alloc, ByteBuf cumulation, int readable) {
        ByteBuf oldCumulation = cumulation;
        cumulation = alloc.buffer(oldCumulation.readableBytes() + readable);
        cumulation.writeBytes(oldCumulation);
        oldCumulation.release();
        return cumulation;
    }

    public static interface Cumulator {
        public ByteBuf cumulate(ByteBufAllocator var1, ByteBuf var2, ByteBuf var3);
    }

}

