/*
 * Decompiled with CFR 0_129.
 */
package io.netty.handler.ssl;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelPromise;
import io.netty.channel.ChannelPromiseNotifier;
import io.netty.channel.PendingWriteQueue;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.UnsupportedMessageTypeException;
import io.netty.handler.ssl.ApplicationProtocolAccessor;
import io.netty.handler.ssl.NotSslRecordException;
import io.netty.handler.ssl.OpenSslEngine;
import io.netty.handler.ssl.SslHandshakeCompletionEvent;
import io.netty.util.ReferenceCounted;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.ImmediateExecutor;
import io.netty.util.concurrent.Promise;
import io.netty.util.internal.EmptyArrays;
import io.netty.util.internal.OneTimeTask;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;

public class SslHandler
extends ByteToMessageDecoder
implements ChannelOutboundHandler {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(SslHandler.class);
    private static final Pattern IGNORABLE_CLASS_IN_STACK = Pattern.compile("^.*(?:Socket|Datagram|Sctp|Udt)Channel.*$");
    private static final Pattern IGNORABLE_ERROR_MESSAGE = Pattern.compile("^.*(?:connection.*(?:reset|closed|abort|broken)|broken.*pipe).*$", 2);
    private static final SSLException SSLENGINE_CLOSED = new SSLException("SSLEngine closed already");
    private static final SSLException HANDSHAKE_TIMED_OUT = new SSLException("handshake timed out");
    private static final ClosedChannelException CHANNEL_CLOSED = new ClosedChannelException();
    private volatile ChannelHandlerContext ctx;
    private final SSLEngine engine;
    private final int maxPacketBufferSize;
    private final Executor delegatedTaskExecutor;
    private final ByteBuffer[] singleBuffer = new ByteBuffer[1];
    private final boolean wantsDirectBuffer;
    private final boolean wantsLargeOutboundNetworkBuffer;
    private boolean wantsInboundHeapBuffer;
    private final boolean startTls;
    private boolean sentFirstMessage;
    private boolean flushedBeforeHandshake;
    private boolean readDuringHandshake;
    private PendingWriteQueue pendingUnencryptedWrites;
    private Promise<Channel> handshakePromise = new LazyChannelPromise();
    private final LazyChannelPromise sslCloseFuture = new LazyChannelPromise();
    private boolean needsFlush;
    private int packetLength;
    private boolean firedChannelRead;
    private volatile long handshakeTimeoutMillis = 10000L;
    private volatile long closeNotifyTimeoutMillis = 3000L;

    public SslHandler(SSLEngine engine) {
        this(engine, false);
    }

    public SslHandler(SSLEngine engine, boolean startTls) {
        this(engine, startTls, ImmediateExecutor.INSTANCE);
    }

    @Deprecated
    public SslHandler(SSLEngine engine, Executor delegatedTaskExecutor) {
        this(engine, false, delegatedTaskExecutor);
    }

    @Deprecated
    public SslHandler(SSLEngine engine, boolean startTls, Executor delegatedTaskExecutor) {
        boolean opensslEngine;
        if (engine == null) {
            throw new NullPointerException("engine");
        }
        if (delegatedTaskExecutor == null) {
            throw new NullPointerException("delegatedTaskExecutor");
        }
        this.engine = engine;
        this.delegatedTaskExecutor = delegatedTaskExecutor;
        this.startTls = startTls;
        this.maxPacketBufferSize = engine.getSession().getPacketBufferSize();
        this.wantsDirectBuffer = opensslEngine = engine instanceof OpenSslEngine;
        this.wantsLargeOutboundNetworkBuffer = !opensslEngine;
        this.setCumulator(opensslEngine ? COMPOSITE_CUMULATOR : MERGE_CUMULATOR);
    }

    public long getHandshakeTimeoutMillis() {
        return this.handshakeTimeoutMillis;
    }

    public void setHandshakeTimeout(long handshakeTimeout, TimeUnit unit) {
        if (unit == null) {
            throw new NullPointerException("unit");
        }
        this.setHandshakeTimeoutMillis(unit.toMillis(handshakeTimeout));
    }

    public void setHandshakeTimeoutMillis(long handshakeTimeoutMillis) {
        if (handshakeTimeoutMillis < 0L) {
            throw new IllegalArgumentException("handshakeTimeoutMillis: " + handshakeTimeoutMillis + " (expected: >= 0)");
        }
        this.handshakeTimeoutMillis = handshakeTimeoutMillis;
    }

    public long getCloseNotifyTimeoutMillis() {
        return this.closeNotifyTimeoutMillis;
    }

    public void setCloseNotifyTimeout(long closeNotifyTimeout, TimeUnit unit) {
        if (unit == null) {
            throw new NullPointerException("unit");
        }
        this.setCloseNotifyTimeoutMillis(unit.toMillis(closeNotifyTimeout));
    }

    public void setCloseNotifyTimeoutMillis(long closeNotifyTimeoutMillis) {
        if (closeNotifyTimeoutMillis < 0L) {
            throw new IllegalArgumentException("closeNotifyTimeoutMillis: " + closeNotifyTimeoutMillis + " (expected: >= 0)");
        }
        this.closeNotifyTimeoutMillis = closeNotifyTimeoutMillis;
    }

    public SSLEngine engine() {
        return this.engine;
    }

    public String applicationProtocol() {
        SSLSession sess = this.engine().getSession();
        if (!(sess instanceof ApplicationProtocolAccessor)) {
            return null;
        }
        return ((ApplicationProtocolAccessor)((Object)sess)).getApplicationProtocol();
    }

    public Future<Channel> handshakeFuture() {
        return this.handshakePromise;
    }

    public ChannelFuture close() {
        return this.close(this.ctx.newPromise());
    }

    public ChannelFuture close(final ChannelPromise future) {
        final ChannelHandlerContext ctx = this.ctx;
        ctx.executor().execute(new Runnable(){

            @Override
            public void run() {
                block2 : {
                    SslHandler.this.engine.closeOutbound();
                    try {
                        SslHandler.this.write(ctx, Unpooled.EMPTY_BUFFER, future);
                        SslHandler.this.flush(ctx);
                    }
                    catch (Exception e) {
                        if (future.tryFailure(e)) break block2;
                        logger.warn("{} flush() raised a masked exception.", (Object)ctx.channel(), (Object)e);
                    }
                }
            }
        });
        return future;
    }

    public Future<Channel> sslCloseFuture() {
        return this.sslCloseFuture;
    }

    @Override
    public void handlerRemoved0(ChannelHandlerContext ctx) throws Exception {
        if (!this.pendingUnencryptedWrites.isEmpty()) {
            this.pendingUnencryptedWrites.removeAndFailAll(new ChannelException("Pending write on removal of SslHandler"));
        }
    }

    @Override
    public void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) throws Exception {
        ctx.bind(localAddress, promise);
    }

    @Override
    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) throws Exception {
        ctx.connect(remoteAddress, localAddress, promise);
    }

    @Override
    public void deregister(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        ctx.deregister(promise);
    }

    @Override
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        this.closeOutboundAndChannel(ctx, promise, true);
    }

    @Override
    public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        this.closeOutboundAndChannel(ctx, promise, false);
    }

    @Override
    public void read(ChannelHandlerContext ctx) throws Exception {
        if (!this.handshakePromise.isDone()) {
            this.readDuringHandshake = true;
        }
        ctx.read();
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (!(msg instanceof ByteBuf)) {
            promise.setFailure(new UnsupportedMessageTypeException(msg, ByteBuf.class));
            return;
        }
        this.pendingUnencryptedWrites.add(msg, promise);
    }

    @Override
    public void flush(ChannelHandlerContext ctx) throws Exception {
        if (this.startTls && !this.sentFirstMessage) {
            this.sentFirstMessage = true;
            this.pendingUnencryptedWrites.removeAndWriteAll();
            ctx.flush();
            return;
        }
        if (this.pendingUnencryptedWrites.isEmpty()) {
            this.pendingUnencryptedWrites.add(Unpooled.EMPTY_BUFFER, ctx.newPromise());
        }
        if (!this.handshakePromise.isDone()) {
            this.flushedBeforeHandshake = true;
        }
        this.wrap(ctx, false);
        ctx.flush();
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private void wrap(ChannelHandlerContext ctx, boolean inUnwrap) throws SSLException {
        ByteBuf out = null;
        ChannelPromise promise = null;
        ByteBufAllocator alloc = ctx.alloc();
        try {
            SSLEngineResult result;
            block14 : do {
                Object msg;
                if ((msg = this.pendingUnencryptedWrites.current()) == null) {
                    return;
                }
                ByteBuf buf = (ByteBuf)msg;
                if (out == null) {
                    out = this.allocateOutNetBuf(ctx, buf.readableBytes());
                }
                result = this.wrap(alloc, this.engine, buf, out);
                promise = !buf.isReadable() ? this.pendingUnencryptedWrites.remove() : null;
                if (result.getStatus() == SSLEngineResult.Status.CLOSED) {
                    this.pendingUnencryptedWrites.removeAndFailAll(SSLENGINE_CLOSED);
                    return;
                }
                switch (result.getHandshakeStatus()) {
                    case NEED_TASK: {
                        this.runDelegatedTasks();
                        continue block14;
                    }
                    case FINISHED: {
                        this.setHandshakeSuccess();
                    }
                    case NOT_HANDSHAKING: {
                        this.setHandshakeSuccessIfStillHandshaking();
                    }
                    case NEED_WRAP: {
                        this.finishWrap(ctx, out, promise, inUnwrap);
                        promise = null;
                        out = null;
                        continue block14;
                    }
                    case NEED_UNWRAP: {
                        return;
                    }
                }
                break;
            } while (true);
            throw new IllegalStateException("Unknown handshake status: " + (Object)((Object)result.getHandshakeStatus()));
        }
        catch (SSLException e) {
            this.setHandshakeFailure(ctx, e);
            throw e;
        }
        finally {
            this.finishWrap(ctx, out, promise, inUnwrap);
        }
    }

    private void finishWrap(ChannelHandlerContext ctx, ByteBuf out, ChannelPromise promise, boolean inUnwrap) {
        if (out == null) {
            out = Unpooled.EMPTY_BUFFER;
        } else if (!out.isReadable()) {
            out.release();
            out = Unpooled.EMPTY_BUFFER;
        }
        if (promise != null) {
            ctx.write(out, promise);
        } else {
            ctx.write(out);
        }
        if (inUnwrap) {
            this.needsFlush = true;
        }
    }

    private void wrapNonAppData(ChannelHandlerContext ctx, boolean inUnwrap) throws SSLException {
        ReferenceCounted out = null;
        ByteBufAllocator alloc = ctx.alloc();
        try {
            SSLEngineResult result;
            block12 : do {
                if (out == null) {
                    out = this.allocateOutNetBuf(ctx, 0);
                }
                if ((result = this.wrap(alloc, this.engine, Unpooled.EMPTY_BUFFER, (ByteBuf)out)).bytesProduced() > 0) {
                    ctx.write(out);
                    if (inUnwrap) {
                        this.needsFlush = true;
                    }
                    out = null;
                }
                switch (result.getHandshakeStatus()) {
                    case FINISHED: {
                        this.setHandshakeSuccess();
                        break;
                    }
                    case NEED_TASK: {
                        this.runDelegatedTasks();
                        break;
                    }
                    case NEED_UNWRAP: {
                        if (inUnwrap) continue block12;
                        this.unwrapNonAppData(ctx);
                        break;
                    }
                    case NEED_WRAP: {
                        break;
                    }
                    case NOT_HANDSHAKING: {
                        this.setHandshakeSuccessIfStillHandshaking();
                        if (inUnwrap) continue block12;
                        this.unwrapNonAppData(ctx);
                        break;
                    }
                    default: {
                        throw new IllegalStateException("Unknown handshake status: " + (Object)((Object)result.getHandshakeStatus()));
                    }
                }
            } while (result.bytesProduced() != 0);
        }
        catch (SSLException e) {
            this.setHandshakeFailure(ctx, e);
            throw e;
        }
        finally {
            if (out != null) {
                out.release();
            }
        }
    }

    private SSLEngineResult wrap(ByteBufAllocator alloc, SSLEngine engine, ByteBuf in, ByteBuf out) throws SSLException {
        ReferenceCounted newDirectIn = null;
        try {
            ByteBuffer[] in0;
            int readerIndex = in.readerIndex();
            int readableBytes = in.readableBytes();
            if (in.isDirect() || !this.wantsDirectBuffer) {
                if (!(in instanceof CompositeByteBuf) && in.nioBufferCount() == 1) {
                    in0 = this.singleBuffer;
                    in0[0] = in.internalNioBuffer(readerIndex, readableBytes);
                } else {
                    in0 = in.nioBuffers();
                }
            } else {
                newDirectIn = alloc.directBuffer(readableBytes);
                newDirectIn.writeBytes(in, readerIndex, readableBytes);
                in0 = this.singleBuffer;
                in0[0] = newDirectIn.internalNioBuffer(0, readableBytes);
            }
            do {
                ByteBuffer out0 = out.nioBuffer(out.writerIndex(), out.writableBytes());
                SSLEngineResult result = engine.wrap(in0, out0);
                in.skipBytes(result.bytesConsumed());
                out.writerIndex(out.writerIndex() + result.bytesProduced());
                switch (result.getStatus()) {
                    case BUFFER_OVERFLOW: {
                        out.ensureWritable(this.maxPacketBufferSize);
                        break;
                    }
                    default: {
                        SSLEngineResult sSLEngineResult = result;
                        return sSLEngineResult;
                    }
                }
            } while (true);
        }
        finally {
            this.singleBuffer[0] = null;
            if (newDirectIn != null) {
                newDirectIn.release();
            }
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        this.setHandshakeFailure(ctx, CHANNEL_CLOSED);
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (this.ignoreException(cause)) {
            if (logger.isDebugEnabled()) {
                logger.debug("{} Swallowing a harmless 'connection reset by peer / broken pipe' error that occurred while writing close_notify in response to the peer's close_notify", (Object)ctx.channel(), (Object)cause);
            }
            if (ctx.channel().isActive()) {
                ctx.close();
            }
        } else {
            ctx.fireExceptionCaught(cause);
        }
    }

    private boolean ignoreException(Throwable t) {
        if (!(t instanceof SSLException) && t instanceof IOException && this.sslCloseFuture.isDone()) {
            StackTraceElement[] elements;
            String message = String.valueOf(t.getMessage()).toLowerCase();
            if (IGNORABLE_ERROR_MESSAGE.matcher(message).matches()) {
                return true;
            }
            for (StackTraceElement element : elements = t.getStackTrace()) {
                String classname = element.getClassName();
                String methodname = element.getMethodName();
                if (classname.startsWith("io.netty.") || !"read".equals(methodname)) continue;
                if (IGNORABLE_CLASS_IN_STACK.matcher(classname).matches()) {
                    return true;
                }
                try {
                    Class clazz = PlatformDependent.getClassLoader(this.getClass()).loadClass(classname);
                    if (SocketChannel.class.isAssignableFrom(clazz) || DatagramChannel.class.isAssignableFrom(clazz)) {
                        return true;
                    }
                    if (PlatformDependent.javaVersion() >= 7 && "com.sun.nio.sctp.SctpChannel".equals(clazz.getSuperclass().getName())) {
                        return true;
                    }
                }
                catch (ClassNotFoundException clazz) {
                    // empty catch block
                }
            }
        }
        return false;
    }

    public static boolean isEncrypted(ByteBuf buffer) {
        if (buffer.readableBytes() < 5) {
            throw new IllegalArgumentException("buffer must have at least 5 readable bytes");
        }
        return SslHandler.getEncryptedPacketLength(buffer, buffer.readerIndex()) != -1;
    }

    private static int getEncryptedPacketLength(ByteBuf buffer, int offset) {
        boolean tls;
        int packetLength = 0;
        switch (buffer.getUnsignedByte(offset)) {
            case 20: 
            case 21: 
            case 22: 
            case 23: {
                tls = true;
                break;
            }
            default: {
                tls = false;
            }
        }
        if (tls) {
            short majorVersion = buffer.getUnsignedByte(offset + 1);
            if (majorVersion == 3) {
                packetLength = buffer.getUnsignedShort(offset + 3) + 5;
                if (packetLength <= 5) {
                    tls = false;
                }
            } else {
                tls = false;
            }
        }
        if (!tls) {
            boolean sslv2 = true;
            int headerLength = (buffer.getUnsignedByte(offset) & 128) != 0 ? 2 : 3;
            short majorVersion = buffer.getUnsignedByte(offset + headerLength + 1);
            if (majorVersion == 2 || majorVersion == 3) {
                packetLength = headerLength == 2 ? (buffer.getShort(offset) & 32767) + 2 : (buffer.getShort(offset) & 16383) + 3;
                if (packetLength <= headerLength) {
                    sslv2 = false;
                }
            } else {
                sslv2 = false;
            }
            if (!sslv2) {
                return -1;
            }
        }
        return packetLength;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws SSLException {
        int readableBytes;
        int startOffset = in.readerIndex();
        int endOffset = in.writerIndex();
        int offset = startOffset;
        int totalLength = 0;
        if (this.packetLength > 0) {
            if (endOffset - startOffset < this.packetLength) {
                return;
            }
            offset += this.packetLength;
            totalLength = this.packetLength;
            this.packetLength = 0;
        }
        boolean nonSslRecord = false;
        while (totalLength < 18713 && (readableBytes = endOffset - offset) >= 5) {
            int packetLength = SslHandler.getEncryptedPacketLength(in, offset);
            if (packetLength == -1) {
                nonSslRecord = true;
                break;
            }
            assert (packetLength > 0);
            if (packetLength > readableBytes) {
                this.packetLength = packetLength;
                break;
            }
            int newTotalLength = totalLength + packetLength;
            if (newTotalLength > 18713) break;
            offset += packetLength;
            totalLength = newTotalLength;
        }
        if (totalLength > 0) {
            boolean decoded = false;
            in.skipBytes(totalLength);
            if (in.isDirect() && this.wantsInboundHeapBuffer) {
                ByteBuf copy = ctx.alloc().heapBuffer(totalLength);
                try {
                    copy.writeBytes(in, startOffset, totalLength);
                    decoded = this.unwrap(ctx, copy, 0, totalLength);
                }
                finally {
                    copy.release();
                }
            } else {
                decoded = this.unwrap(ctx, in, startOffset, totalLength);
            }
            if (!this.firedChannelRead) {
                this.firedChannelRead = decoded;
            }
        }
        if (nonSslRecord) {
            NotSslRecordException e = new NotSslRecordException("not an SSL/TLS record: " + ByteBufUtil.hexDump(in));
            in.skipBytes(in.readableBytes());
            ctx.fireExceptionCaught(e);
            this.setHandshakeFailure(ctx, e);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        this.discardSomeReadBytes();
        if (this.needsFlush) {
            this.needsFlush = false;
            ctx.flush();
        }
        if (!(ctx.channel().config().isAutoRead() || this.firedChannelRead && this.handshakePromise.isDone())) {
            ctx.read();
        }
        this.firedChannelRead = false;
        ctx.fireChannelReadComplete();
    }

    private void unwrapNonAppData(ChannelHandlerContext ctx) throws SSLException {
        this.unwrap(ctx, Unpooled.EMPTY_BUFFER, 0, 0);
    }

    private boolean unwrap(ChannelHandlerContext ctx, ByteBuf packet, int offset, int length) throws SSLException {
        boolean decoded;
        decoded = false;
        boolean wrapLater = false;
        boolean notifyClosure = false;
        ByteBuf decodeOut = this.allocate(ctx, length);
        try {
            block16 : do {
                SSLEngineResult result = this.unwrap(this.engine, packet, offset, length, decodeOut);
                SSLEngineResult.Status status = result.getStatus();
                SSLEngineResult.HandshakeStatus handshakeStatus = result.getHandshakeStatus();
                int produced = result.bytesProduced();
                int consumed = result.bytesConsumed();
                offset += consumed;
                length -= consumed;
                switch (status) {
                    case BUFFER_OVERFLOW: {
                        int readableBytes = decodeOut.readableBytes();
                        if (readableBytes > 0) {
                            decoded = true;
                            ctx.fireChannelRead(decodeOut);
                        } else {
                            decodeOut.release();
                        }
                        decodeOut = this.allocate(ctx, this.engine.getSession().getApplicationBufferSize() - readableBytes);
                        continue block16;
                    }
                    case CLOSED: {
                        notifyClosure = true;
                        break;
                    }
                }
                switch (handshakeStatus) {
                    case NEED_UNWRAP: {
                        break;
                    }
                    case NEED_WRAP: {
                        this.wrapNonAppData(ctx, true);
                        break;
                    }
                    case NEED_TASK: {
                        this.runDelegatedTasks();
                        break;
                    }
                    case FINISHED: {
                        this.setHandshakeSuccess();
                        wrapLater = true;
                        continue block16;
                    }
                    case NOT_HANDSHAKING: {
                        if (this.setHandshakeSuccessIfStillHandshaking()) {
                            wrapLater = true;
                            continue block16;
                        }
                        if (!this.flushedBeforeHandshake) break;
                        this.flushedBeforeHandshake = false;
                        wrapLater = true;
                        break;
                    }
                    default: {
                        throw new IllegalStateException("unknown handshake status: " + (Object)((Object)handshakeStatus));
                    }
                }
                if (status == SSLEngineResult.Status.BUFFER_UNDERFLOW || consumed == 0 && produced == 0) break;
            } while (true);
            if (wrapLater) {
                this.wrap(ctx, true);
            }
            if (notifyClosure) {
                this.sslCloseFuture.trySuccess(ctx.channel());
            }
        }
        catch (SSLException e) {
            this.setHandshakeFailure(ctx, e);
            throw e;
        }
        finally {
            if (decodeOut.isReadable()) {
                decoded = true;
                ctx.fireChannelRead(decodeOut);
            } else {
                decodeOut.release();
            }
        }
        return decoded;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private SSLEngineResult unwrap(SSLEngine engine, ByteBuf in, int readerIndex, int len, ByteBuf out) throws SSLException {
        SSLEngineResult result;
        int nioBufferCount = in.nioBufferCount();
        int writerIndex = out.writerIndex();
        if (engine instanceof OpenSslEngine && nioBufferCount > 1) {
            OpenSslEngine opensslEngine = (OpenSslEngine)engine;
            try {
                this.singleBuffer[0] = SslHandler.toByteBuffer(out, writerIndex, out.writableBytes());
                result = opensslEngine.unwrap(in.nioBuffers(readerIndex, len), this.singleBuffer);
                out.writerIndex(writerIndex + result.bytesProduced());
            }
            finally {
                this.singleBuffer[0] = null;
            }
        } else {
            result = engine.unwrap(SslHandler.toByteBuffer(in, readerIndex, len), SslHandler.toByteBuffer(out, writerIndex, out.writableBytes()));
        }
        out.writerIndex(writerIndex + result.bytesProduced());
        return result;
    }

    private static ByteBuffer toByteBuffer(ByteBuf out, int index, int len) {
        return out.nioBufferCount() == 1 ? out.internalNioBuffer(index, len) : out.nioBuffer(index, len);
    }

    private void runDelegatedTasks() {
        if (this.delegatedTaskExecutor == ImmediateExecutor.INSTANCE) {
            Runnable task;
            while ((task = this.engine.getDelegatedTask()) != null) {
                task.run();
            }
        } else {
            Runnable task;
            final ArrayList<Runnable> tasks = new ArrayList<Runnable>(2);
            while ((task = this.engine.getDelegatedTask()) != null) {
                tasks.add(task);
            }
            if (tasks.isEmpty()) {
                return;
            }
            final CountDownLatch latch = new CountDownLatch(1);
            this.delegatedTaskExecutor.execute(new Runnable(){

                /*
                 * WARNING - Removed try catching itself - possible behaviour change.
                 */
                @Override
                public void run() {
                    try {
                        for (Runnable task : tasks) {
                            task.run();
                        }
                    }
                    catch (Exception e) {
                        SslHandler.this.ctx.fireExceptionCaught(e);
                    }
                    finally {
                        latch.countDown();
                    }
                }
            });
            boolean interrupted = false;
            while (latch.getCount() != 0L) {
                try {
                    latch.await();
                }
                catch (InterruptedException e) {
                    interrupted = true;
                }
            }
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private boolean setHandshakeSuccessIfStillHandshaking() {
        if (!this.handshakePromise.isDone()) {
            this.setHandshakeSuccess();
            return true;
        }
        return false;
    }

    private void setHandshakeSuccess() {
        String cipherSuite = String.valueOf(this.engine.getSession().getCipherSuite());
        if (!this.wantsDirectBuffer && (cipherSuite.contains("_GCM_") || cipherSuite.contains("-GCM-"))) {
            this.wantsInboundHeapBuffer = true;
        }
        this.handshakePromise.trySuccess(this.ctx.channel());
        if (logger.isDebugEnabled()) {
            logger.debug("{} HANDSHAKEN: {}", (Object)this.ctx.channel(), (Object)this.engine.getSession().getCipherSuite());
        }
        this.ctx.fireUserEventTriggered(SslHandshakeCompletionEvent.SUCCESS);
        if (this.readDuringHandshake && !this.ctx.channel().config().isAutoRead()) {
            this.readDuringHandshake = false;
            this.ctx.read();
        }
    }

    private void setHandshakeFailure(ChannelHandlerContext ctx, Throwable cause) {
        block2 : {
            this.engine.closeOutbound();
            try {
                this.engine.closeInbound();
            }
            catch (SSLException e) {
                String msg = e.getMessage();
                if (msg != null && msg.contains("possible truncation attack")) break block2;
                logger.debug("{} SSLEngine.closeInbound() raised an exception.", (Object)ctx.channel(), (Object)e);
            }
        }
        this.notifyHandshakeFailure(cause);
        this.pendingUnencryptedWrites.removeAndFailAll(cause);
    }

    private void notifyHandshakeFailure(Throwable cause) {
        if (this.handshakePromise.tryFailure(cause)) {
            this.ctx.fireUserEventTriggered(new SslHandshakeCompletionEvent(cause));
            this.ctx.close();
        }
    }

    private void closeOutboundAndChannel(ChannelHandlerContext ctx, ChannelPromise promise, boolean disconnect) throws Exception {
        if (!ctx.channel().isActive()) {
            if (disconnect) {
                ctx.disconnect(promise);
            } else {
                ctx.close(promise);
            }
            return;
        }
        this.engine.closeOutbound();
        ChannelPromise closeNotifyFuture = ctx.newPromise();
        this.write(ctx, Unpooled.EMPTY_BUFFER, closeNotifyFuture);
        this.flush(ctx);
        this.safeClose(ctx, closeNotifyFuture, promise);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;
        this.pendingUnencryptedWrites = new PendingWriteQueue(ctx);
        if (ctx.channel().isActive() && this.engine.getUseClientMode()) {
            this.handshake(null);
        }
    }

    public Future<Channel> renegotiate() {
        ChannelHandlerContext ctx = this.ctx;
        if (ctx == null) {
            throw new IllegalStateException();
        }
        return this.renegotiate(ctx.executor().newPromise());
    }

    public Future<Channel> renegotiate(final Promise<Channel> promise) {
        if (promise == null) {
            throw new NullPointerException("promise");
        }
        ChannelHandlerContext ctx = this.ctx;
        if (ctx == null) {
            throw new IllegalStateException();
        }
        EventExecutor executor = ctx.executor();
        if (!executor.inEventLoop()) {
            executor.execute(new OneTimeTask(){

                @Override
                public void run() {
                    SslHandler.this.handshake(promise);
                }
            });
            return promise;
        }
        this.handshake(promise);
        return promise;
    }

    private void handshake(final Promise<Channel> newHandshakePromise) {
        Promise<Channel> p;
        if (newHandshakePromise != null) {
            Promise<Channel> oldHandshakePromise = this.handshakePromise;
            if (!oldHandshakePromise.isDone()) {
                oldHandshakePromise.addListener(new FutureListener<Channel>(){

                    @Override
                    public void operationComplete(Future<Channel> future) throws Exception {
                        if (future.isSuccess()) {
                            newHandshakePromise.setSuccess(future.getNow());
                        } else {
                            newHandshakePromise.setFailure(future.cause());
                        }
                    }
                });
                return;
            }
            this.handshakePromise = p = newHandshakePromise;
        } else {
            p = this.handshakePromise;
            assert (!p.isDone());
        }
        ChannelHandlerContext ctx = this.ctx;
        try {
            this.engine.beginHandshake();
            this.wrapNonAppData(ctx, false);
            ctx.flush();
        }
        catch (Exception e) {
            this.notifyHandshakeFailure(e);
        }
        long handshakeTimeoutMillis = this.handshakeTimeoutMillis;
        if (handshakeTimeoutMillis <= 0L || p.isDone()) {
            return;
        }
        final io.netty.util.concurrent.ScheduledFuture timeoutFuture = ctx.executor().schedule(new Runnable(){

            @Override
            public void run() {
                if (p.isDone()) {
                    return;
                }
                SslHandler.this.notifyHandshakeFailure(HANDSHAKE_TIMED_OUT);
            }
        }, handshakeTimeoutMillis, TimeUnit.MILLISECONDS);
        p.addListener(new FutureListener<Channel>(){

            @Override
            public void operationComplete(Future<Channel> f) throws Exception {
                timeoutFuture.cancel(false);
            }
        });
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        if (!this.startTls && this.engine.getUseClientMode()) {
            this.handshake(null);
        }
        ctx.fireChannelActive();
    }

    private void safeClose(final ChannelHandlerContext ctx, ChannelFuture flushFuture, final ChannelPromise promise) {
        if (!ctx.channel().isActive()) {
            ctx.close(promise);
            return;
        }
        final io.netty.util.concurrent.ScheduledFuture timeoutFuture = this.closeNotifyTimeoutMillis > 0L ? ctx.executor().schedule(new Runnable(){

            @Override
            public void run() {
                logger.warn("{} Last write attempt timed out; force-closing the connection.", (Object)ctx.channel());
                ctx.close(ctx.newPromise()).addListener(new ChannelPromiseNotifier(promise));
            }
        }, this.closeNotifyTimeoutMillis, TimeUnit.MILLISECONDS) : null;
        flushFuture.addListener(new ChannelFutureListener(){

            @Override
            public void operationComplete(ChannelFuture f) throws Exception {
                if (timeoutFuture != null) {
                    timeoutFuture.cancel(false);
                }
                ctx.close(ctx.newPromise()).addListener(new ChannelPromiseNotifier(promise));
            }
        });
    }

    private ByteBuf allocate(ChannelHandlerContext ctx, int capacity) {
        ByteBufAllocator alloc = ctx.alloc();
        if (this.wantsDirectBuffer) {
            return alloc.directBuffer(capacity);
        }
        return alloc.buffer(capacity);
    }

    private ByteBuf allocateOutNetBuf(ChannelHandlerContext ctx, int pendingBytes) {
        if (this.wantsLargeOutboundNetworkBuffer) {
            return this.allocate(ctx, this.maxPacketBufferSize);
        }
        return this.allocate(ctx, Math.min(pendingBytes + 2329, this.maxPacketBufferSize));
    }

    static {
        SSLENGINE_CLOSED.setStackTrace(EmptyArrays.EMPTY_STACK_TRACE);
        HANDSHAKE_TIMED_OUT.setStackTrace(EmptyArrays.EMPTY_STACK_TRACE);
        CHANNEL_CLOSED.setStackTrace(EmptyArrays.EMPTY_STACK_TRACE);
    }

    private final class LazyChannelPromise
    extends DefaultPromise<Channel> {
        private LazyChannelPromise() {
        }

        @Override
        protected EventExecutor executor() {
            if (SslHandler.this.ctx == null) {
                throw new IllegalStateException();
            }
            return SslHandler.this.ctx.executor();
        }

        @Override
        protected void checkDeadLock() {
            if (SslHandler.this.ctx == null) {
                return;
            }
            super.checkDeadLock();
        }
    }

}

